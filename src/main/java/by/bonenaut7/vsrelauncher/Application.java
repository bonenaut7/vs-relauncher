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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.formdev.flatlaf.FlatDarkLaf;

import by.bonenaut7.vsrelauncher.events.EventGameShutdown;
import by.bonenaut7.vsrelauncher.systems.ArchiveSystem;
import by.bonenaut7.vsrelauncher.systems.NotificationSystem;
import by.bonenaut7.vsrelauncher.systems.PingMeSystem;
import by.bonenaut7.vsrelauncher.systems.QueueSystem;
import by.bonenaut7.vsrelauncher.systems.relauncher.GameFileParserSystem;
import by.bonenaut7.vsrelauncher.systems.relauncher.LogWatchSystem;
import by.bonenaut7.vsrelauncher.systems.relauncher.WindowSystem;
import by.bonenaut7.vsrelauncher.util.Utils;

// TODO File archiving
// TODO Boot commands
// TODO PingMe phrase blacklist
// TODO Storm & death warning notifications
// TODO Add log-file watchers in case if file gets recreated by game in non-attached use case
// Add file backups after emergency restarts (BSODs and stuff)?
public class Application {
	public static final boolean DEBUG = true;
	private static AppContext context;
	
	public static AppContext context() {
		return context;
	}
	
	public static void start(AppContext ctx) throws InterruptedException, ExecutionException {
		FlatDarkLaf.setup();
	
		// Prepare systems
		context = ctx;
		ctx.addSystem(new GameFileParserSystem(ctx));
		ctx.addSystem(new WindowSystem(ctx));
		ctx.addSystem(new LogWatchSystem(ctx));
		
		ctx.addSystem(new ArchiveSystem(ctx));
		ctx.addSystem(new QueueSystem(ctx));
		ctx.addSystem(new PingMeSystem(ctx));
		ctx.addSystem(new NotificationSystem(ctx));
		
		// Start the app
		ctx.init();
		
		// Wait for game process to appear completely
		Utils.sleep(2500);
		
		final List<ProcessHandle> list = ProcessHandle.allProcesses().filter(process -> process.pid() == ctx.gameProcessID).collect(Collectors.toList());
		if (list.size() > 0) {
			final ProcessHandle handle = list.get(0);
			if (handle.isAlive()) {
				ctx.gameProcess = handle;
				handle.onExit().get();
			}
			
			ctx.getBus().post(new EventGameShutdown());
			ctx.shutdown();
			System.exit(0); // Just in case, lol
		}
		
		// NO-OP in case if process is not found.
	}
	
	public static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}
}
