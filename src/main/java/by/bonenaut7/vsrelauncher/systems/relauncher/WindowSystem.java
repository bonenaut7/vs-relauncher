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

import java.util.function.Function;

import javax.swing.SwingUtilities;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.Application;
import by.bonenaut7.vsrelauncher.config.AppConfig;
import by.bonenaut7.vsrelauncher.gui.Screen;
import by.bonenaut7.vsrelauncher.gui.ScreenStatus;
import by.bonenaut7.vsrelauncher.gui.Window;
import by.bonenaut7.vsrelauncher.systems.AbstractSystem;
import by.bonenaut7.vsrelauncher.util.DebugKeyListener;

public class WindowSystem extends AbstractSystem {
	private Window window;
	
	public WindowSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		SwingUtilities.invokeLater(this::startWindow);
	}
	
	@Override
	public void shutdown() {
		window.destroy();
	}

	@Override
	public void onConfigUpdate(AppConfig config) {
		window.setTitle(config.getAppTitle());
		window.setVisibility(!config.nogui);
	}
	
	public void setScreen(Screen screen) {
		window.setScreen(screen);
	}
	
	public Window getWindow() {
		return window;
	}
	
	public void redrawIf(Function<Window, Boolean> function) {
		if (window == null) {
			return; // WTF?
		}
		
		if (function.apply(window)) {
			SwingUtilities.invokeLater(() -> window.resetScreen());
		}
	}
	
	public void redraw() {
		if (window == null) {
			return;
		}
		
		SwingUtilities.invokeLater(() -> window.resetScreen());
	}
	
	private void startWindow() {
		window = new Window();
		window.setScreen(ScreenStatus.INSTANCE);
		onConfigUpdate(ctx.config);
		
		if (Application.DEBUG) {
			final DebugKeyListener keyListener = new DebugKeyListener(window);
			window.getFrame().addKeyListener(keyListener);
			window.getFrame().getContentPane().addKeyListener(keyListener);
		}
	}
}
