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
package by.bonenaut7.vsrelauncher.notification.twoslices;

import com.sshtools.twoslices.Toast;
import com.sshtools.twoslices.ToastBuilder;
import com.sshtools.twoslices.ToastType;

import by.bonenaut7.vsrelauncher.Application;
import by.bonenaut7.vsrelauncher.notification.NotificationType;
import by.bonenaut7.vsrelauncher.notification.Notifications;

public final class TwoSlicesNotifications implements Notifications {
	private String title;
	
	@Override
	public void init(String appTitle) {
		this.title = appTitle;
	}
	
	@Override
	public void show(NotificationType type, String text) {
		show(type, -1, text);
	}
	
	@Override
	public void show(NotificationType type, int expirationTimeMs, String text) {
		Application.debug(text);
		
		final ToastBuilder builder = Toast.builder();
		builder.title(title);
		builder.content(text);
		builder.type(mapType(type));
		
		if (expirationTimeMs > 0) {
			builder.timeout(expirationTimeMs);
		}
		
		builder.toast();
	}
	
	private static ToastType mapType(NotificationType type) {
		return switch (type) {
			case PLAIN -> ToastType.NONE;
			case INFO -> ToastType.INFO;
			case WARNING -> ToastType.WARNING;
			case ERROR -> ToastType.ERROR;
		};
	}

	
}
