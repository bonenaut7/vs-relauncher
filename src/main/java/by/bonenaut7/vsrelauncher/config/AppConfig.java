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
package by.bonenaut7.vsrelauncher.config;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.yaml.snakeyaml.Yaml;

import by.bonenaut7.configfacade.AbstractConfigWrapped;
import by.bonenaut7.configfacade.adapter.yaml.YamlConfigurationAdapter;
import by.bonenaut7.configfacade.facade.elements.ConfigArray;
import by.bonenaut7.configfacade.facade.elements.ConfigCompound;
import by.bonenaut7.configfacade.facade.elements.ConfigElement;

public final class AppConfig extends AbstractConfigWrapped {
	private static final Yaml YAML_INSTANCE = new Yaml();
	
	public boolean nogui = false;
	
	// Basics
	public String configAlias = "Vintage Story";
	public String relauncherExecutable = null;
	public String gameExecutableFile = null;
	public String gamePath = null;
	public String gameDataPath = null;
	public boolean runGame = true;
	
	// Archive config
	public boolean archive_enabled = true;
	public boolean archive_backupOnDisconnects = true;
	public boolean archive_flattenHierarchyOnBackup = true;
	public List<String> archive_fileNamesForBackup = new ArrayList<>();
	
	// Ping config
	public boolean ping_enabled = true;
	public boolean ping_ignoreCase = true;
	public boolean ping_blacklistIgnoreCase = true;
	public List<String> ping_keywords = new ArrayList<>();
	public List<String> ping_blacklistUsernames = new ArrayList<>();
	
	// Notifications config
	public boolean notify_joinLeaveGame = true;
	public boolean notify_joinLeaveQueue = false;
	public int[] notify_queuePositions = new int[] { 1, 10 };
	
	// Experimental
	public boolean experimental_stormNotifications;
	public boolean experimental_deathNotifications;
	
	public long stats_queueTime = 0;
	public long stats_playTime = 0;
	
	public AppConfig() {
		this(new File("relauncher.yml"));
	}
	
	public AppConfig(File configFile) {
		super(configFile);
		setReadWriter(new YamlConfigurationAdapter(YAML_INSTANCE));
		
		archive_fileNamesForBackup.add("Logs/client-chat.log");
		ping_keywords.add("SampleText");
	}

	@Override
	public void deserialize(ConfigCompound compound) {
		// Clear state
		archive_enabled = false;
		ping_enabled = false;
		archive_fileNamesForBackup.clear();
		ping_keywords.clear();
		ping_blacklistUsernames.clear();
		
		// Load
		// main
		if (compound.has("main")) {
			final ConfigCompound mainTag = compound.getCompound("main");
			
			configAlias = mainTag.getString("alias", configAlias);
			
			relauncherExecutable = mainTag.getString("relauncherExecutable");
			if (Objects.equals(relauncherExecutable, "null")) {
				relauncherExecutable = null;
			}
			
			gameExecutableFile = mainTag.getString("gameExecutableFile");
			if (Objects.equals(gameExecutableFile, "null")) {
				gameExecutableFile = null;
			}
			
			gameDataPath = mainTag.getString("gameDataPath");
			if (Objects.equals(gameDataPath, "null")) {
				gameDataPath = null;
			}
			
			runGame = mainTag.getBoolean("runGame");
		}
		
		// Log archives
		if (compound.has("archive")) {
			final ConfigCompound archiveTag = compound.getCompound("archive");
			archive_enabled = archiveTag.getBoolean("enabled", archive_enabled);
			archive_backupOnDisconnects = archiveTag.getBoolean("backupOnDisconnects", archive_backupOnDisconnects);
			archive_flattenHierarchyOnBackup = archiveTag.getBoolean("flattenHierarchyOnBackup", archive_flattenHierarchyOnBackup);
			
			readStringList(archiveTag.getArray("filesForBackup"), archive_fileNamesForBackup);
		}
		
		// Ping me
		if (compound.has("ping")) {
			final ConfigCompound pingTag = compound.getCompound("ping");
			ping_enabled = pingTag.getBoolean("enabled", ping_enabled);
			ping_ignoreCase = pingTag.getBoolean("ignoreCase", ping_ignoreCase);
			ping_blacklistIgnoreCase = pingTag.getBoolean("blacklistIgnoreCase", ping_blacklistIgnoreCase);
			
			readStringList(pingTag.getArray("keywords"), ping_keywords);
			readStringList(pingTag.getArray("blacklist"), ping_blacklistUsernames);
		}
		
		// Notifications
		if (compound.has("notifications")) {
			final ConfigCompound notifyTag = compound.getCompound("notifications");
			notify_joinLeaveGame = notifyTag.getBoolean("joinLeaveGame", notify_joinLeaveGame);
			notify_joinLeaveQueue = notifyTag.getBoolean("joinLeaveQueue", notify_joinLeaveQueue);
			notify_queuePositions = readIntArray(notifyTag.getArray("notifyQueuePositions"));
		}
		
		if (compound.has("experimental")) {
			final ConfigCompound experimentalTag = compound.getCompound("experimental");
			experimental_stormNotifications = experimentalTag.getBoolean("stormNotifications", experimental_stormNotifications);
			experimental_deathNotifications = experimentalTag.getBoolean("deathNotifications", experimental_deathNotifications);
		}
		
		if (compound.has("statistics")) {
			final ConfigCompound statsTag = compound.getCompound("statistics");
			stats_queueTime = statsTag.getLong("queueTime", stats_queueTime);
			stats_playTime = statsTag.getLong("playTime", stats_playTime);
		}
		
		// Post-load
		gamePath = gameExecutableFile != null ? Path.of(gameExecutableFile).getParent().toFile().getAbsolutePath() : null;
	}

	@Override
	public ConfigCompound serialize() {
		final ConfigCompound compound = new ConfigCompound();
		
		final ConfigCompound mainTag = new ConfigCompound();
		mainTag.append("alias", configAlias, "Config alias for the application title.");
		mainTag.append("relauncherExecutable", relauncherExecutable == null ? "null" : relauncherExecutable, "Hey, I'm too lazy to get the file I'm running. Specify it once, please.");
		mainTag.append("gameExecutableFile", gameExecutableFile == null ? "null" : gameExecutableFile, "Game executable file. Usually called \'Vintagestory.exe\'");
		mainTag.append("gameDataPath", gameDataPath == null ? "null" : gameDataPath, "Game data path with the user data and settings.");
		mainTag.append("runGame", runGame, "If true, relauncher starts the game by itself, otherwise it runs in standalone mode.");
		compound.append("main", mainTag);
		
		final ConfigCompound archiveTag = new ConfigCompound();
		archiveTag.append("enabled", archive_enabled);
		archiveTag.append("backupOnDisconnects", archive_backupOnDisconnects, "Backups files every server disconnect (tracked by logs).");
		archiveTag.append("flattenHierarchyOnBackup", archive_flattenHierarchyOnBackup, "Flattens file hierarchy in the logs output folder.");
		archiveTag.append("filesForBackup", writeStringList(archive_fileNamesForBackup), "Files to be backed up, root path is game's data path.");
		compound.append("archive", archiveTag, "File archiving after each game start. Made to archive log files.");
		
		final ConfigCompound pingTag = new ConfigCompound();
		pingTag.append("enabled", ping_enabled);
		pingTag.append("ignoreCase", ping_ignoreCase, "Ignore case of ping keywords");
		pingTag.append("blacklistIgnoreCase", ping_blacklistIgnoreCase, "Ignore case of blacklisted usernames");
		pingTag.append("keywords", writeStringList(ping_keywords), "Ping me keywords for notifications");
		pingTag.append("blacklist", writeStringList(ping_blacklistUsernames), "Blacklisted players from which you won't get notifications");
		compound.append("ping", pingTag, "Ping me notifications");
		
		final ConfigCompound notifyTag = new ConfigCompound();
		notifyTag.append("joinLeaveGame", notify_joinLeaveGame, "Notifies about joining/leaving game");
		notifyTag.append("joinLeaveQueue", notify_joinLeaveQueue, "Notifies about joining/leaving queue");
		notifyTag.append("notifyQueuePositions", writeIntArray(notify_queuePositions), "Queue positions that will trigger a notification.");
		compound.append("notifications", notifyTag, "Any other notifications");
		
		final ConfigCompound experimentalTag = new ConfigCompound();
		experimentalTag.append("stormNotifications", experimental_stormNotifications, "Notifies about temporal storms");
		experimentalTag.append("deathNotifications", experimental_deathNotifications, "Notifies about your deaths");
		compound.append("experimental", experimentalTag, "Experimental functions configuration");
		
		final ConfigCompound statsTag = new ConfigCompound();
		experimentalTag.append("queueTime", stats_queueTime, "Your queue wait time in seconds");
		experimentalTag.append("playTime", stats_playTime, "Your playtime in seconds");
		compound.append("statistics", statsTag, "VS Relauncher statistics");
		
		return compound;
	}
	
	public String getAppTitle() {
		return configAlias != null ? ("VS Relauncher - " + configAlias) : "VS Relauncher";
	}

	private void readStringList(ConfigArray arrayTag, List<String> list) {
		if (arrayTag == null) {
			return;
		}
		
		for (final ConfigElement element : arrayTag) {
			if (element.asString() == null) {
				continue;
			}
			
			list.add(element.asString().getString());
		}
	}
	
	private ConfigArray writeStringList(List<String> list) {
		final ConfigArray arrayTag = new ConfigArray();
		list.forEach(arrayTag::append);
		return arrayTag;
	}
	
	private int[] readIntArray(ConfigArray arrayTag) {
		if (arrayTag == null) {
			return new int[0];
		}
		
		final int[] array = new int[arrayTag.size()];
		for (int idx = 0; idx < arrayTag.size(); idx++) {
			final ConfigElement element = arrayTag.getAt(idx);
			
			if (element.asNumber() == null) {
				continue;
			}
			
			array[idx] = element.asNumber().getInteger();
		}
		
		return array;
	}
	
	private ConfigArray writeIntArray(int[] array) {
		final ConfigArray arrayTag = new ConfigArray();
		
		for (int idx = 0; idx < array.length; idx++) {
			arrayTag.append(array[idx]);
		}
		
		return arrayTag;
	}
}
