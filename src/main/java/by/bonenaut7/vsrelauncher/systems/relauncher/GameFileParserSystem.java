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

import java.io.File;
import java.io.FileReader;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.config.AppConfig;
import by.bonenaut7.vsrelauncher.systems.AbstractSystem;
import by.bonenaut7.vsrelauncher.util.FileChangeMonitor;
import by.bonenaut7.vsrelauncher.util.Utils;

public final class GameFileParserSystem extends AbstractSystem {
	private final Gson gson = new GsonBuilder().setLenient().create();
	private final FileChangeMonitor settingsMonitor = new FileChangeMonitor("Thread-FileWatcher-ClientSettings", 30000, this::onWatchUpdateSettings);
	
	private volatile File clientSettingsFile = null;
	
	public GameFileParserSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		onConfigUpdate(ctx.config);
	}

	@Override
	public void shutdown() {
		settingsMonitor.stop();
	}

	@Override
	public void onConfigUpdate(AppConfig config) {
		clientSettingsFile = new File(config.gameDataPath, "clientsettings.json");
		settingsMonitor.restart(clientSettingsFile);
		updateUsername(clientSettingsFile);
	}
	
	// ASYNC! WatchService sucks...
	private void onWatchUpdateSettings(File file) {
		updateUsername(file);
	}
	
	private void updateUsername(File clientSettingsFile) {
		ctx.username = ObjectUtils.firstNonNull(Utils.sneakyCallable(() -> {
			final JsonReader reader = new JsonReader(new FileReader(clientSettingsFile));
			final JsonObject clientSettings = gson.fromJson(reader, JsonObject.class);
			reader.close();
			
			return clientSettings.getAsJsonObject("stringSettings").get("playername").getAsString();
		}), "undefined");
	}
}
