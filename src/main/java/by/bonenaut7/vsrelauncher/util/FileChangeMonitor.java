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

import java.io.File;
import java.util.function.Consumer;

public final class FileChangeMonitor extends AbstractScheduledTask {
	private final Consumer<File> callback;
	
	private volatile File file;
	private volatile long lastTimeUpdate = -1;
	
	public FileChangeMonitor(String name, long delayMs, Consumer<File> callback) {
		super(name, delayMs);
		this.task = this::run;
		
		this.callback = callback;
	}
	
	public boolean start(File target) {
		if (canStart()) {
			file = target;
			lastTimeUpdate = file.lastModified();
			
			return start();
		}
		
		return false;
	}
	
	public boolean stop() {
		if (super.stop()) {
			file = null;
			lastTimeUpdate = 0L;
			
			return true;
		}
		
		return false;
	}
	
	public void restart(File target) {
		if (stop()) {
			start(target);
		}
	}
	
	// ASYNC!
	private void run() {
		while (running) {
			if (file.lastModified() != lastTimeUpdate) {
				lastTimeUpdate = file.lastModified();
				callback.accept(file);
			}
			
			Utils.sleep(delayMs);
		}
	}
}
