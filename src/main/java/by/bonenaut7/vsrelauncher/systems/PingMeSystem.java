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

import java.util.List;

import org.apache.commons.lang3.Strings;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.events.EventChatLogMessage;
import by.bonenaut7.vsrelauncher.notification.NotificationType;

public final class PingMeSystem extends AbstractSystem {
	public PingMeSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		bus.register(EventChatLogMessage.class, this::onChatMessage);
	}

	private void onChatMessage(EventChatLogMessage event) {
		// Send ping if possible
		if (ctx.config.ping_enabled) {
			sendPingNotification(event.getMessage());
		}
	}
	
	private void sendPingNotification(String message) {
		if (!matchString(message, ctx.config.ping_keywords)) {
			return;
		}
		
		final int colonIndex = message.indexOf(':');
		if (colonIndex == -1) {
			return;
		}
		
		final String username = message.substring(0, colonIndex);
		if (ctx.username != null ? (ctx.username.equals(username)) : matchString(username, ctx.config.ping_keywords)) {
			return;
		}

		ctx.notifications.show(NotificationType.INFO, 5000, message);
	}
	
	private boolean matchString(String str, List<String> keywords) {
		final Strings strings = ctx.config.ping_ignoreCase ? Strings.CI : Strings.CS;
		
		for (final String keyword : keywords) {
			if (strings.contains(str, keyword)) {
				final String[] arr = str.split("\\s+");
				if (arr.length == 0) {
					System.out.println("matchString() array is 0 len!");
					continue; // But how?
				}
				
				// 1. No separate words
				for (int idx = 0; idx < arr.length; idx++) {
					if (Strings.CI.equals(arr[idx], keyword)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
