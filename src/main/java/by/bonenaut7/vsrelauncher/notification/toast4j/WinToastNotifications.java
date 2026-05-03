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
package by.bonenaut7.vsrelauncher.notification.toast4j;

import by.bonenaut7.vsrelauncher.Application;
import by.bonenaut7.vsrelauncher.notification.NotificationType;
import by.bonenaut7.vsrelauncher.notification.Notifications;
import de.mobanisto.toast4j.ToastBuilder;
import de.mobanisto.toast4j.Toaster;
import de.mobanisto.wintoast.WinToastTemplate.WinToastTemplateType;

public final class WinToastNotifications implements Notifications {
	private Toaster toaster;
	
	@Override
	public void init(String appTitle) {
		toaster = Toaster.forAppName(appTitle);
		toaster.initialize();
	}
	
	@Override
	public void show(NotificationType type, String text) {
		show(type, -1, text);
	}
	
	@Override
	public void show(NotificationType type, int expirationTimeMs, String text) {
		Application.debug(text);
		
		final ToastBuilder builder = new ToastBuilder(WinToastTemplateType.ToastText01);
		builder.setLine1(text);
		
		if (expirationTimeMs > 0) {
			builder.setExpiration(expirationTimeMs);
		}
		
		toaster.showToast(builder.build());
	}
}
