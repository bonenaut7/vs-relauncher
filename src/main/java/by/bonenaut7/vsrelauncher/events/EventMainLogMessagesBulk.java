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
package by.bonenaut7.vsrelauncher.events;

import java.util.List;

import by.bonenaut7.uebus.Event;

/**
 * @param messages - Main log read lines
 * @param logInit - Log file initialization flag, if it's true, then it's bulk of all previously written to the log file messages.
 */
public final class EventMainLogMessagesBulk extends Event {
	private final List<String> messages;
	private final boolean logInit;
	
	public EventMainLogMessagesBulk(List<String> messages, boolean logInit) {
		this.messages = messages;
		this.logInit = logInit;
	}
	
	public Iterable<String> getMessages() {
		return messages;
	}
	
	public boolean isLogInit() {
		return logInit;
	}
}
