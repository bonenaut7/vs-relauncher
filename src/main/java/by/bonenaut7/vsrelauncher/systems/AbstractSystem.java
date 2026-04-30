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

import by.bonenaut7.uebus.AbstractEventBus;
import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.config.AppConfig;

public abstract class AbstractSystem {
	private final Class<? extends AbstractSystem> type = getClass();
	protected final AppContext ctx;
	protected final AbstractEventBus bus;
	private boolean enabled = false;
	
	public AbstractSystem(AppContext ctx) {
		this.ctx = ctx;
		this.bus = ctx.getBus();
	}
	
	public Class<? extends AbstractSystem> getType() {
		return type;
	}
	
	public abstract void init();
	
	public void shutdown() {
	}
	
	public void onConfigUpdate(AppConfig config) {
	}
	
	@Deprecated
	public boolean isEnabled() {
		return enabled;
	}
	
	@Deprecated
	public void enable() {
		if (enabled) {
			throw new IllegalStateException("Already enabled.");
		}
		
		this.enabled = true;
	}
	
	@Deprecated
	public void disable() {
		if (!enabled) {
			throw new IllegalStateException("Already disabled.");
		}
		
		this.enabled = false;
	}
	
	@Deprecated
	public void toggleEnableDisable() {
		if (enabled) {
			disable();
		} else {
			enable();
		}
	}
}
