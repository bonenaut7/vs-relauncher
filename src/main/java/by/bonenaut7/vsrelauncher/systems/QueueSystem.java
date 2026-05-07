/**
 *  Copyright 2026 Matvey "bonenaut7" Zholudz
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package by.bonenaut7.vsrelauncher.systems;

import java.text.ParsePosition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import by.bonenaut7.uebus.EventRegistration;
import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.GameState;
import by.bonenaut7.vsrelauncher.config.AppConfig;
import by.bonenaut7.vsrelauncher.events.EventMainLogMessage;
import by.bonenaut7.vsrelauncher.events.EventMainLogMessagesBulk;
import by.bonenaut7.vsrelauncher.gui.ScreenStatus;
import by.bonenaut7.vsrelauncher.notification.NotificationType;
import by.bonenaut7.vsrelauncher.systems.relauncher.WindowSystem;
import by.bonenaut7.vsrelauncher.util.ScheduledTask;
import by.bonenaut7.vsrelauncher.util.Utils;

public final class QueueSystem extends AbstractSystem {
	private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss");
	
	private final ScheduledTask task = new ScheduledTask("Thread-QueueMonitorTicker", 60000, this::onTick);
	private WindowSystem windowSystem;
	private EventRegistration<?> mainLogSub;
	private EventRegistration<?> mainLogBulkSub;
	private volatile int queuePosition = 0;
	
	// Shitton of instants cause they're read from logs and I'm pretty lazy
	private volatile Instant timestampQueueJoin = null;
	private volatile Instant lastQueuePositionUpdate = null;
	private volatile Instant lastQueueMessage = null;
	
	// Past queue positions time stampts for past 30 minutes
	private volatile List<Instant> pastQueuePositions = new ArrayList<>();
	private volatile float queueSpeed = -1F;
	
	public QueueSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		windowSystem = ctx.getSystem(WindowSystem.class);
		
		mainLogSub = bus.register(EventMainLogMessage.class, this::onLogEvent);
		mainLogBulkSub = bus.register(EventMainLogMessagesBulk.class, this::onLogBulkEvent);
		
		task.start();
	}
	
	public void shutdown() {
		bus.unregister(mainLogSub);
		bus.unregister(mainLogBulkSub);
		
		task.stop();
	}
	
	public void onConfigUpdate(AppConfig config) {
		task.restart();
	}
	
	public int getQueuePosition() {
		return queuePosition;
	}
	
	public float getQueueSpeed() {
		return queueSpeed;
	}
	
	public Instant getQueueJoinInstant() {
		return timestampQueueJoin;
	}
	
	public boolean isQueueStuck() {
		return Utils.hasPassed(lastQueueMessage, 10, ChronoUnit.MINUTES);
	}
	
	private void onTick() {
		if (Utils.hasPassed(lastQueuePositionUpdate, 30, ChronoUnit.MINUTES)) {
			ctx.notifications.show(NotificationType.ERROR, 1_800_000, "Queue is probably dead. Staying in the same place for more than 30 minutes...");
		} else if (isQueueStuck()) {
			ctx.notifications.show(NotificationType.WARNING, 300_000, "Queue may be dead. Client didn't received any queue updates for more than 10 minutes.");
		}
		
		markScreenDirty();
	}
	
	private void onLogEvent(EventMainLogMessage event) {
		final String line = event.getRawText();
		
		if (line.contains("Client is in connect queue")) {
			final Instant instant = Instant.now();
			
			if (ctx.getState() != GameState.IN_QUEUE) {
				timestampQueueJoin = instant;
				ctx.setState(GameState.IN_QUEUE);
			}
			
			final int newPosition = Integer.valueOf(line.substring(line.lastIndexOf(' ') + 1, line.length()));
			if (newPosition != queuePosition) {
				lastQueuePositionUpdate = instant;
				setQueuePosition(newPosition);
			}
			
			lastQueueMessage = instant;
			return;
		}

		// "5.4.2026 23:06:23 [Notification] Connected to server, downloading data..."
		// "13.4.2026 20:13:31 [Notification] Processed server identification"
		if (line.contains("Processed server identification")) {
			ctx.setState(GameState.IN_GAME);
			return;
		}
		
		// "6.4.2026 06:43:57 [Notification] Destroying game session, waiting up to 200ms for client threads to exit"
		// "6.4.2026 05:05:41 [Notification] Exiting current game to main menu, reason: leave world button pressed" OR "6.4.2026 06:43:58 [Notification] Exiting current game"
		if (line.contains("Destroying game session")) {
			ctx.setState(GameState.IN_MENU);
			return;
		}
	}
	
	private void onLogBulkEvent(EventMainLogMessagesBulk event) {
		final ParsePosition parsePosition = new ParsePosition(0);
		GameState state = ctx.getState();
		
		for (String line : event.getMessages()) {
			if (line.contains("Client is in connect queue")) {
				final Instant instant = parseInstant(line, parsePosition);
				
				if (state != GameState.IN_QUEUE) {
					timestampQueueJoin = instant;
					state = GameState.IN_QUEUE;
				}
				
				final int position = Integer.valueOf(line.substring(line.lastIndexOf(' ') + 1, line.length()));
				if (queuePosition != position) {
					lastQueuePositionUpdate = instant;
					updateQueueSpeed(instant, true, true);
				}

				lastQueueMessage = instant;
				queuePosition = position;
			} else if (line.contains("Processed server identification")) {
				timestampQueueJoin = null;
				state = GameState.IN_GAME;
			} else if (line.contains("Destroying game session")) {
				timestampQueueJoin = null;
				state = GameState.IN_MENU;
			}
		}
		
		// Reset message counter to now show "QUEUE STUCK!??!?!" message on startup
		lastQueueMessage = Instant.now();
		
		updateQueueSpeed(Instant.now(), false, false);
		markScreenDirty();
	}
	
	private void setQueuePosition(int position) {
		final int[] notifyPositions = ctx.config.notify_queuePositions;
		if (notifyPositions.length > 0) {
			for (int idx = 0; idx < notifyPositions.length; idx++) {
				if (notifyPositions[idx] == position) {
					ctx.notifications.show(NotificationType.INFO, 30_000, "Your position in the queue: " + position);
				}
			}
		}
		
		queuePosition = position;
		updateQueueSpeed(Instant.now(), true, false);
		markScreenDirty();
	}
	
	private void updateQueueSpeed(Instant instant, boolean addOne, boolean skipCalculation) {
		if (addOne) {
			this.pastQueuePositions.add(instant);
		}
		
		final Instant pastInstantForRemovals = instant.minus(Duration.ofMinutes(30));
		while (this.pastQueuePositions.size() > 0) {
			final Instant oldestInstant = this.pastQueuePositions.get(0);
			
			if (oldestInstant.isBefore(pastInstantForRemovals)) {
				this.pastQueuePositions.remove(0);
				continue;
			}
			
			break;
		}
		
		if (skipCalculation) {
			return;
		}
		
		if (this.pastQueuePositions.size() <= 2) {
			this.queueSpeed = -1F;
			return;
		}
		
		long minutes = Utils.durationBetween(this.pastQueuePositions.get(0), instant, ChronoUnit.MINUTES);
		this.queueSpeed = this.pastQueuePositions.size() / (float)minutes;
	}
	
	private void markScreenDirty() {
		windowSystem.redrawIf(w -> w.getScreen() instanceof ScreenStatus);
	}
	
	private Instant parseInstant(String line, ParsePosition parsePosition) {
		final TemporalAccessor accessor = TS_FORMATTER.parse(line, parsePosition);
		final LocalDateTime ldt = LocalDateTime.from(accessor); // Cause parsed data is missing timezone information
		final Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
		
		parsePosition.setIndex(0);
		parsePosition.setErrorIndex(-1);
		return instant;
	}
}
