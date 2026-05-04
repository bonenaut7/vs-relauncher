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

import org.apache.commons.lang3.Strings;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.GameState;
import by.bonenaut7.vsrelauncher.events.EventChatLogMessage;
import by.bonenaut7.vsrelauncher.events.EventGameStateChange;
import by.bonenaut7.vsrelauncher.notification.NotificationType;

public final class NotificationSystem extends AbstractSystem {
	public NotificationSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		bus.register(EventChatLogMessage.class, this::onChatMessage);
		bus.register(EventGameStateChange.class, this::onStateChange);
	}

	private void onChatMessage(EventChatLogMessage event) {
		// Send storm notification if possible
		if (ctx.config.experimental_stormNotifications) {
			sendStormNotification(event.getMessage());
		}
		
		// Send death notification if possible
		if (ctx.config.experimental_deathNotifications) {
			sendDeathNotification(event.getMessage());
		}
	}
	
	private void onStateChange(EventGameStateChange event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getPreviousState() == event.getNewState()) {
			return;
		}
		
		switch (event.getNewState()) {
			case IN_MENU:
				if (event.getPreviousState() == GameState.IN_QUEUE && ctx.config.notify_joinLeaveQueue) {
					ctx.notifications.show(NotificationType.PLAIN, 15_000, "You've left the queue");
				}
				
				if (event.getPreviousState() == GameState.IN_GAME && ctx.config.notify_joinLeaveGame) {
					ctx.notifications.show(NotificationType.PLAIN, 30_000, "You've left the game!");
				}
				break;
				
			case IN_QUEUE:
				if (ctx.config.notify_joinLeaveGame) {
					ctx.notifications.show(NotificationType.PLAIN, 15_000, "You've joined the queue!");
				}
				break;
				
			case IN_GAME:
				if (ctx.config.notify_joinLeaveGame) {
					ctx.notifications.show(NotificationType.PLAIN, 30_000, "You've joined the game!");
				}
				break;
		}
	}
	
	// TODO Add storm notifications
	private void sendStormNotification(String message) {
//		 * Storm approach "5.4.2026 00:02:08 [Chat] A heavy temporal storm is approaching @ -3"
//		 * Storm imminent "5.4.2026 00:18:07 [Chat] A heavy temporal storm is imminent @ -3"
//		 * Storm ending "5.4.2026 00:27:49 [Chat] The temporal storm seems to be waning @ -3"
	}
	
	// TODO Add death notifications
	private void sendDeathNotification(String message) {
		if (!Strings.CS.equals(message, ctx.username)) {
			return;
		}
		
		// load translations
	}
}