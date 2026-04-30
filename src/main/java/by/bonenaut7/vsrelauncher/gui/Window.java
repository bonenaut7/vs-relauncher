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
package by.bonenaut7.vsrelauncher.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

import javax.swing.JFrame;

public final class Window implements ClipboardOwner {
	private static final int WINDOW_WIDTH = 350;
	private static final int WINDOW_HEIGHT = 350;
	
	private final JFrame frame;
	private Screen currentScreen = null;
	private int width = 0;
	private int height = 0;
	
	public Window() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		frame.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		frame.setResizable(false);
		frame.setTitle("VS Relauncher");

		frame.setVisible(false);
		frame.setLayout(null);
		frame.setFocusable(true);
		frame.setLocationRelativeTo(null);
		//frame.setLocationByPlatform(true);
	}
	
	public void setTitle(String title) {
		frame.setTitle(title);
	}
	
	public void setVisibility(boolean visible) {
		// Ignore random updates
		if (frame.isVisible() == visible) {
			return;
		}
		
		frame.setVisible(visible);
		
		if (visible) {
			width = frame.getContentPane().getWidth();
			height = frame.getContentPane().getHeight();
			
			resetScreen();
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public Screen getScreen() {
		return currentScreen;
	}
	
	public void setScreen(Screen screen) {
		if (currentScreen == screen) {
			return;
		}
		
		currentScreen = screen;
		
		// Avoid init with invalid height/width values
		if (frame.isVisible()) {
			resetScreen();
		}
	}
	
	public void resetScreen() {
		frame.getContentPane().removeAll();
		currentScreen.reset(this, width, height);
		frame.revalidate();
		frame.repaint();
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
}
