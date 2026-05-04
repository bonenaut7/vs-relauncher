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
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

import by.bonenaut7.uebus.EventRegistration;
import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.config.AppConfig;
import by.bonenaut7.vsrelauncher.events.EventMainLogMessage;
import by.bonenaut7.vsrelauncher.events.EventMainLogMessagesBulk;
import by.bonenaut7.vsrelauncher.gui.ScreenStatus;
import by.bonenaut7.vsrelauncher.notification.NotificationType;
import by.bonenaut7.vsrelauncher.systems.relauncher.WindowSystem;
import by.bonenaut7.vsrelauncher.util.ScheduledTask;

public final class QueueSystem extends AbstractSystem {
	public static final int GAME_STATE_IN_MENU = 0;
	public static final int GAME_STATE_IN_QUEUE = 1;
	public static final int GAME_STATE_IN_GAME = 2;
	public static final int NOTIFICATION_JOINED_QUEUE = 0;
	public static final int NOTIFICATION_LEFT_QUEUE = 1;
	public static final int NOTIFICATION_JOINED_GAME = 2;
	public static final int NOTIFICATION_LEFT_GAME = 3;
	private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss");
	
	private final ScheduledTask task = new ScheduledTask("Thread-QueueMonitorTicker", 60000, this::onTick);
	private WindowSystem windowSystem;
	private EventRegistration<?> mainLogSub;
	private EventRegistration<?> mainLogBulkSub;
	
	private volatile int gameState = GAME_STATE_IN_MENU;
	private volatile int queuePosition = 0;
	
	// Shitton of instants cause they're read from logs and I'm pretty lazy
	private volatile Instant queueJoinInfoTs = null;
	private volatile Instant queueJoinStatsTs = null;
	private volatile Instant playingTimeSinceJoin = null;
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
		
		gameState = GAME_STATE_IN_MENU;
		queueJoinInfoTs = null;
		queueJoinStatsTs = null;
		playingTimeSinceJoin = null;
		
		task.start();
	}
	
	public void shutdown() {
		bus.unregister(mainLogSub);
		bus.unregister(mainLogBulkSub);
		
		task.stop();
		
		// Finalize statistics counters
		final Instant instant = Instant.now();
		addStatisticQueueTime(queueJoinStatsTs, instant);
		addStatisticPlayTime(playingTimeSinceJoin, instant);
	}
	
	public void onConfigUpdate(AppConfig config) {
		task.restart();
	}
	
	public int getGameState() {
		return gameState;
	}
	
	public int getQueuePosition() {
		return queuePosition;
	}
	
	public float getQueueSpeed() {
		return queueSpeed;
	}
	
	public Instant getQueueJoinInstant() {
		return queueJoinInfoTs;
	}
	
	public boolean isQueueStuck() {
		return hasPassed(lastQueueMessage, 10, ChronoUnit.MINUTES);
	}
	
	private void onTick() {
		if (hasPassed(lastQueuePositionUpdate, 30, ChronoUnit.MINUTES)) {
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
			
			if (gameState != GAME_STATE_IN_QUEUE) {
				queueJoinInfoTs = instant;
				queueJoinStatsTs = instant;
				setQueueState(GAME_STATE_IN_QUEUE);
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
			if (ctx.config.notify_joinLeaveGame) {
				ctx.notifications.show(NotificationType.PLAIN, 30_000, "You've joined the game!");
			}
			
			setQueueState(GAME_STATE_IN_GAME);
			return;
		}
		
		// "6.4.2026 06:43:57 [Notification] Destroying game session, waiting up to 200ms for client threads to exit"
		// "6.4.2026 05:05:41 [Notification] Exiting current game to main menu, reason: leave world button pressed" OR "6.4.2026 06:43:58 [Notification] Exiting current game"
		if (line.contains("Destroying game session")) {
			switch (gameState) {
				case GAME_STATE_IN_QUEUE:
					if (ctx.config.notify_joinLeaveQueue) {
						// Only when leaving queue manually or being kicked out
						ctx.notifications.show(NotificationType.PLAIN, 15_000, "You've left the queue");
					}
					break;
			
				case GAME_STATE_IN_GAME:
					if (ctx.config.notify_joinLeaveGame) {
						ctx.notifications.show(NotificationType.PLAIN, 30_000, "You've left the game!");
					}
					
					break;
			}
			
			setQueueState(GAME_STATE_IN_MENU);
			return;
		}
	}
	
	private void onLogBulkEvent(EventMainLogMessagesBulk event) {
		final ParsePosition parsePosition = new ParsePosition(0);
		
		for (String line : event.getMessages()) {
			if (line.contains("Client is in connect queue")) {
				Instant instant = null;
				
				if (gameState != GAME_STATE_IN_QUEUE) {
					instant = parseInstant(line, parsePosition);
					queueJoinInfoTs = instant;
					gameState = GAME_STATE_IN_QUEUE;
				}
				
				final int position = Integer.valueOf(line.substring(line.lastIndexOf(' ') + 1, line.length()));
				if (queuePosition != position) {
					if (instant == null) {
						instant = parseInstant(line, parsePosition);
					}

					lastQueuePositionUpdate = instant;
					updateQueueSpeed(instant, true, true);
				}
				
				if (instant == null) {
					instant = parseInstant(line, parsePosition);
				}
				
				lastQueueMessage = instant;
				queuePosition = position;
			} else if (line.contains("Processed server identification")) {
				queueJoinInfoTs = null;
				gameState = GAME_STATE_IN_GAME;
			} else if (line.contains("Destroying game session")) {
				queueJoinInfoTs = null;
				gameState = GAME_STATE_IN_MENU;
			}
		}
		
		// Reset queue join timestamp to not fuck-up the statistics (it's intentionally less in some specific cases)
		if (gameState == GAME_STATE_IN_QUEUE) {
			queueJoinStatsTs = Instant.now();
		}
		
		// Reset message counter to now show "QUEUE STUCK!??!?!" message on startup
		lastQueueMessage = Instant.now();
		
		updateQueueSpeed(Instant.now(), false, false);
		markScreenDirty();
	}
	
	private void setQueueState(int state) {
		if (gameState != state) {
			if (state == GAME_STATE_IN_GAME) {
				playingTimeSinceJoin = Instant.now();
			} else {
				addStatisticPlayTime(playingTimeSinceJoin, Instant.now());
				playingTimeSinceJoin = null;
			}
			
			if (state == GAME_STATE_IN_QUEUE) {
				queueJoinInfoTs = queueJoinStatsTs = Instant.now();
			} else {
				addStatisticQueueTime(queueJoinStatsTs, Instant.now());
				queueJoinInfoTs = null;
				queueJoinStatsTs = null;
			}
		}
		
		gameState = state;
		markScreenDirty();
	}
	
	private void setQueuePosition(int position) {
		if (gameState == GAME_STATE_IN_QUEUE) {
			// check for notifications
			// FIXME find out what it's needed for, I forgot :skull_emoji:
		} else { // 
			position = 0;
		}
		
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
		
		long minutes = durationBetween(this.pastQueuePositions.get(0), instant, ChronoUnit.MINUTES);
		this.queueSpeed = this.pastQueuePositions.size() / (float)minutes;
	}
	
	private long durationBetween(Instant first, Instant second, TemporalUnit unit) {
		return first.isBefore(second) ? first.until(second, unit) : second.until(first, unit);
	}
	
	private boolean hasPassed(Instant instant, long value, TemporalUnit unit) {
		return instant != null && instant.until(Instant.now(), unit) >= value;
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
	
	private void addStatisticPlayTime(Instant from, Instant to) {
		if (from == null || to == null) {
			return;
		}
		
		ctx.config.stats_playTime += durationBetween(from, to, ChronoUnit.SECONDS);
		ctx.config.save();
	}
	
	private void addStatisticQueueTime(Instant from, Instant to) {
		if (from == null || to == null) {
			return;
		}
		
		ctx.config.stats_queueTime += durationBetween(from, to, ChronoUnit.SECONDS);
		ctx.config.save();
	}
}
