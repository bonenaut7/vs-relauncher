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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import by.bonenaut7.vsrelauncher.gui.Window;

public final class DebugKeyListener implements KeyListener {
	private final Window window;
	
	public DebugKeyListener(Window window) {
		this.window = window;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_NUMPAD5) {
			window.setScreen(window.getScreen().clone());
			window.resetScreen();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
