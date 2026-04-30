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
package by.bonenaut7.vsrelauncher.systems.relauncher;

import java.nio.file.Path;
import java.util.List;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.events.EventChatLogMessage;
import by.bonenaut7.vsrelauncher.events.EventMainLogMessage;
import by.bonenaut7.vsrelauncher.events.EventMainLogMessagesBulk;
import by.bonenaut7.vsrelauncher.systems.AbstractSystem;
import by.bonenaut7.vsrelauncher.util.FileMonitorReader;

public final class LogWatchSystem extends AbstractSystem {
	private static final String CHAT_TAG = "[Chat] ";
	private final FileMonitorReader mainReader = new FileMonitorReader("Thread-FileMonitorReader-MainLog", 500, this::onMainLogInit, this::onMainLogRead, 4096);
	private final FileMonitorReader chatReader = new FileMonitorReader("Thread-FileMonitorReader-ChatLog", 500, null, this::onChatLogRead, 4096);

	public LogWatchSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		start();
	}

	@Override
	public void shutdown() {
		stop();
	}
	
	private void start() {
		mainReader.start(Path.of(ctx.config.gameDataPath).resolve("Logs/client-main.log"));
		chatReader.start(Path.of(ctx.config.gameDataPath).resolve("Logs/client-chat.log"));
	}
	
	private void stop() {
		mainReader.stop();
		chatReader.stop();
	}
	
	private void onMainLogInit(List<String> messages) {
		bus.post(new EventMainLogMessagesBulk(messages, true));
	}

	private void onMainLogRead(String line) {
		bus.post(new EventMainLogMessage(line));
	}

	// FIXME Fix invalid symbols
	//  ̡ ̛ ̵ ̧ ̨ ̵ ̸ ͜ ̵ ҉ ̴ ̡ ͟ ͢ ̨ ͢ ̢ ̨ ͜ ̢ ̴ ̶ ̷ ͢ ̸ ͟ ͏ ͠ ́ ͠ ̨ ̕ ̢ ͘ ͏ ͞ ͝ ̢ ̡ ͢ ͜ ̀ ̴ ̨ ̨ ̛ ͟ ̢ ̷ ́ ͜ ͘ ͜ ͏ ͞ ́ ̸ ͟ ͡ ͠ ͘ ̸ ̸ ́ ͏ ́ ̀ ̶ ̶ ̛ ͢ ̵ ̨ ͜ ̢ ̢ ͡ ̡ ̢ ҉ ͠ ̸ ͢ ̧ ̡ ͠ ̡ ́ ̨ ̢ ͞ ͝ ͘ ͝ ҉ ̴ ҉ ҉ ̷ ̶ ͡ ͡ ͢ ̢ ͏ ̨ ҉ ͟ ̸ ̢ ́ ̴ ͜ ͘ ͢ ͡ ̵ ͘ ̸ ́ ̴ ͠ ͡ ̵ ̶ ͠ ͏ ҉ ́ ̡ ̢ ̕ ̵ ̵ ̨ ̵ ́ ̶ ͡ ̴ ̴ ̵ ̴ ͠ ̵ ̧ ͝ ̀ ̨ ͡ ͝ ̕ ͡ ̢ ͠ ͞ ͜ ͏ ̨ ̶ ́ ͟ ́ ͟ ͢ ̷ ͝ ͞ ̢ ͏ ͞ ̴ ͝ ͠ ͘ ̸ ̢ ̸ ̸ ̷ ̡ ̛ ́ ͡ ̡ ͜ ͝ ̷ ̶ ̀ ̛ ͘ ̸ ̕ ҉ ̡ ̀ ͘ ̡ ͡ ̕ ͡ ͠ ̧ ͟ ̵ ҉ ҉ ̵ ̢ ̡ ̵ ̛ ̀ ͢ ̕ ͠ ͠ ͠ ͢ ̕ ̀ ̛ ̧ ̨ ̴ ̧ ̵ ͡ ͟ ̡ ̵ ̢ ͘ ̷ ́ ͢ ҉ ̵ ͢ ́ ͟ ͠ ͞ ͠ ͜ ̸ ̴ ̢ ̸ ̧ ̸ ̧ ͞ ̢ ̷ ̨ ̴ ͠ ͝ ͞ ͏ ̛ ͠ ̴ ͢ ́ ̢ ̡ ̢ ̡ ͢ ҉ ̷ ̛ ͏ ̴ ̴ ̸ ̧ ̶ ̶ ̸ ͠ ̢ ̢ ́ ̡ ̧ ̕ ̛ ̕ ͠ ͘ ͟ ̢ ̵ ̨ ͘ ͏ ̢ ̸ ͠ ̢ ̀ ́ ͘ ̴ ͜ ͜ ͡ ̷ ̛ ҉ ͢ ̀ ͘ ̷ ͝ ̸ ̀ ͜ ́ ̶ ͏ ̵ ̀ ̧ ̵ ͝ ̀ ́ ̕ ͞ ̵ ̷ ͜ ͘ ͢ ̴ ̵ ҉ ̷ ̕ ̸ ͞ ͘ ̡ ̧ ͞ ̛ ͟ ́ ̴ ̵ ̛ ̨ ̨ ̷ ̛ ̛ ̢ ̧ ͜ ̵ ̨ ҉ ͝ ͝ ͟ ̴ ̧ ͜ ͏ ͟ ͠ ̀
	private void onChatLogRead(String line) {
		final int chatMarkIndex = line.indexOf(CHAT_TAG);
		if (chatMarkIndex == -1) {
			return;
		}
		
		final int groupIndex = line.lastIndexOf('@');
		if (groupIndex == -1) {
			return;
		}
		
		// strip timestamp and html tags
		final String strippedText = line.substring(chatMarkIndex + CHAT_TAG.length(), groupIndex - 1).replaceAll("\\<[^>]*>", "");
		bus.post(new EventChatLogMessage(line, strippedText));
	}
}
