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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import by.bonenaut7.vsrelauncher.Application;
import by.bonenaut7.vsrelauncher.events.EventAppShutdown;

public final class Window implements WindowListener, ClipboardOwner {
	private static final int WINDOW_WIDTH = 350;
	private static final int WINDOW_HEIGHT = 350;
	
	private final JFrame frame;
	private Screen currentScreen = null;
	private int width = 0;
	private int height = 0;
	
	public Window() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
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
	
	public void destroy() {
		frame.dispose();
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// NO-OP
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Application.context().getBus().post(new EventAppShutdown());
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// NO-OP
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// NO-OP
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// NO-OP
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// NO-OP
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// NO-OP
	}
}
