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
package by.bonenaut7.vsrelauncher.util;

public abstract class AbstractScheduledTask {
	protected final String name;
	protected final long delayMs;
	protected Runnable task;
	
	protected volatile boolean running = false;
	protected Thread thread;
	
	public AbstractScheduledTask(String name, long delayMs) {
		this.name = name;
		this.delayMs = delayMs;
	}
	
	public AbstractScheduledTask(String name, long delayMs, Runnable task) {
		this.name = name;
		this.delayMs = delayMs;
		this.task = task;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	protected boolean canStart() {
		return !running;
	}
	
	protected boolean start() {
		if (!canStart()) {
			return false;
		}
		
		running = true;
		
		thread = new Thread(task);
		thread.setDaemon(true);
		thread.setName(name);
		thread.start();
		
		return true;
	}
	
	protected boolean stop() {
		if (!running) {
			return false;
		}
		
		running = false;
		thread.interrupt();
		Utils.sneakyThrows(() -> thread.join());
		thread = null;
		
		return true;
	}
}
