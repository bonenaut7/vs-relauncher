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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.util.SystemFileChooser;
import com.formdev.flatlaf.util.SystemFileChooser.FileNameExtensionFilter;

import by.bonenaut7.vsrelauncher.config.AppConfig;
import by.bonenaut7.vsrelauncher.util.Platform;
import by.bonenaut7.vsrelauncher.util.Utils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public final class Relauncher {
	
	public static void main(String[] args) throws Exception {
		final OptionParser parser = createParser();
		final OptionSet set = parser.parse(args);
		
		// Parse arguments
		final boolean nogui = set.has("nogui");
		final String configPath = (String)set.valueOf("cfg");
		final Long gameProcessID = (Long)set.valueOf("gamepid");

		// Init crap
		final AppContext ctx = new AppContext();
		ctx.gameProcessID = gameProcessID != null ? gameProcessID : 0;
		ctx.config = configPath != null ? new AppConfig(new File(configPath)) : new AppConfig();
		
		// Init config
		final AppConfig config = ctx.config;
		config.nogui = nogui;
		config.load();
		
		// Check executables
		if (config.relauncherExecutable == null || config.gameExecutableFile == null || config.gameDataPath == null) {
			SwingUtilities.invokeAndWait(() -> acquireGameExecAndDataFolder(config));
			config.save();
		}
		
		// Check if there's a process ID injected into the arguments as the last argument, or if game should be started at all in the first place
		if (!config.runGame || ctx.gameProcessID != 0) {
			Application.start(ctx);
		} else {
			startGame(args, ctx);
		}
	}

	private static void startGame(String[] args, AppContext ctx) throws Exception {
		final AppConfig config = ctx.config;
		
		// Start the game
		final ProcessBuilder builder = new ProcessBuilder(config.gameExecutableFile, "--dataPath", config.gameDataPath);
		builder.directory(new File(config.gameExecutableFile).toPath().getParent().toFile()); // "/Path/Vintagestory.exe" -> "/Path/"
		final long pid = builder.start().pid();
		
		// Start the relauncher again, cause it's impossible to start the game correctly otherwise
		final List<String> parameters = new ArrayList<>();
		parameters.addAll(Arrays.asList("java", "-jar", "\"" + config.relauncherExecutable + "\"")); // Add parameters to start relauncher again
		parameters.addAll(Arrays.asList(args)); // Add previous parameters
		
		// Add game process id
		parameters.add("--gamepid");
		parameters.add(String.valueOf(pid));
		
		final ProcessBuilder relauncherBuilder = new ProcessBuilder(parameters.toArray(new String[0]));
		relauncherBuilder.start();
	}
	
	private static void acquireGameExecAndDataFolder(AppConfig config) {
		if (config.relauncherExecutable == null) {
			final var relauncherFc = createRelauncherFileChooser();
			checkApprovedOrHalt(relauncherFc.showOpenDialog(null));
			config.relauncherExecutable = relauncherFc.getSelectedFile().getAbsolutePath();
		}
		
		if (config.gameExecutableFile == null) {
			while (true) {
				final var gameBinaryFc = createGameBinaryFileChooser();
				int code = gameBinaryFc.showOpenDialog(null);
				if (code == SystemFileChooser.APPROVE_OPTION) {
					config.gameExecutableFile = gameBinaryFc.getSelectedFile().getAbsolutePath();
					break;
				} else {
					final int postChooserCode = JOptionPane.showConfirmDialog(null, "Do you want to even start the game after all?");
					if (postChooserCode == JOptionPane.OK_OPTION) {
						continue;
					}
					
					config.runGame = false;
					break;
				}
			}
		}
		
		if (config.gameDataPath == null) {
			final var gameDataFolderFc = createGameDataFolderFileChooser();
			checkApprovedOrHalt(gameDataFolderFc.showOpenDialog(null));
			config.gameDataPath = gameDataFolderFc.getSelectedFile().getAbsolutePath();
		}
	}
	
	private static int checkApprovedOrHalt(int fileChooserCode) {
		if (fileChooserCode != SystemFileChooser.APPROVE_OPTION) {
			System.exit(0);
		}
		
		return fileChooserCode;
	}
	
	private static SystemFileChooser createRelauncherFileChooser() {
		final SystemFileChooser chooser = new SystemFileChooser();
		chooser.setDialogTitle("Choose relauncher file (The one you've opened right now. Yes, I know it's kinda stupid...)");
		chooser.setFileFilter(new FileNameExtensionFilter("Relauncher.jar", "jar", "exe"));
		chooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		Utils.sneakyThrows(() -> {
			chooser.setCurrentDirectory(Path.of(Relauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().toFile());
		}, e -> {
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		});
		
		return chooser;
	}
	
	private static SystemFileChooser createGameBinaryFileChooser() {
		final SystemFileChooser chooser = new SystemFileChooser();
		chooser.setDialogTitle("Choose game executable file");
		chooser.setFileFilter(new FileNameExtensionFilter("Vintagestory.exe", "exe"));
		chooser.setFileSelectionMode(SystemFileChooser.FILES_ONLY);
		
		return chooser;
	}
	
	private static SystemFileChooser createGameDataFolderFileChooser() {
		final SystemFileChooser chooser = new SystemFileChooser();
		chooser.setDialogTitle("Choose game data folder (%APPDATA%/VintagestoryData)");
		chooser.setFileSelectionMode(SystemFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(switch (Platform.get()) {
			case WINDOWS -> System.getenv("AppData") + "/VintagestoryData/";
			case FREEBSD, LINUX -> System.getProperty("user.home");
			case MACOSX -> System.getProperty("user.home") + "/Library/Application Support/";
			default -> "/";
		}));
		
		return chooser;
	}
	
	private static OptionParser createParser() {
		final OptionParser parser = new OptionParser();
		parser.acceptsAll(of("h", "help"), "Show help message.").forHelp();
		parser.acceptsAll(of("c", "cfg", "config"), "Specify relauncher config.").withRequiredArg().ofType(String.class);
		parser.accepts("nogui", "Don't show GUI in process of the game.");
		parser.accepts("gamepid", "You can specify game process ID to attach relauncher lifecycle to the process lifecycle.").withRequiredArg().ofType(Long.class);
		return parser;
	}
	
	private static List<String> of(String... args) {
		return Arrays.asList(args);
	}
}
