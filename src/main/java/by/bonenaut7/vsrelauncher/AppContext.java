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
package by.bonenaut7.vsrelauncher;

import java.util.ArrayList;
import java.util.List;

import by.bonenaut7.uebus.AbstractEventBus;
import by.bonenaut7.uebus.SimpleEventBus;
import by.bonenaut7.vsrelauncher.config.AppConfig;
import by.bonenaut7.vsrelauncher.notification.Notifications;
import by.bonenaut7.vsrelauncher.notification.twoslices.TwoSlicesNotifications;
import by.bonenaut7.vsrelauncher.systems.AbstractSystem;
import by.bonenaut7.vsrelauncher.util.Platform;

public final class AppContext {
	private final List<AbstractSystem> activeSystems = new ArrayList<>();
	private final AbstractEventBus bus = new SimpleEventBus();
	
	public AppConfig config;
	public boolean configUnsavedChanges = false;
	public Notifications notifications;
	public long gameProcessID;
	public ProcessHandle gameProcess;
	public String username = "undefined";
	public String language = "en"; // FIXME read that from the config
	
	public void init() {
		notifications = switch (Platform.get()) {
//			case WINDOWS -> new WinToastNotifications();
			default -> new TwoSlicesNotifications();
		};
		
		notifications.init(config.getAppTitle());
		activeSystems.forEach(s -> s.init());
	}
	
	public void shutdown() {
		activeSystems.forEach(s -> s.shutdown());
	}
	
	public void addSystem(AbstractSystem system) {
		activeSystems.add(system);
	}
	
	public void onConfigUpdate() {
		activeSystems.forEach(s -> s.onConfigUpdate(config));
	}
	
	public void updateConfig() {
		onConfigUpdate();
		config.save();
	}
	
	public <T extends AbstractSystem> T getSystem( Class<T> type) {
		for (int idx = 0; idx < activeSystems.size(); idx++) {
			final AbstractSystem system = activeSystems.get(idx);
			if (system.getType() == type) {
				return (T)system;
			}
		}
		
		return null;
	}

	public AbstractEventBus getBus() {
		return bus;
	}
}
