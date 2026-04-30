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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import by.bonenaut7.vsrelauncher.util.ButtonsRow;

public abstract class ScreenTabs extends Screen implements ActionListener {
	private static final String KEY_STATUS = "tabs::status";
	private static final String KEY_PINGME = "tabs::pingme";
	private static final String KEY_SETTINGS = "tabs::settings";
	private static final String KEY_ABOUT = "tabs::about";
	protected static final int TAB_OFFSET = 30;

	protected ButtonsRow tabButtons;
	protected JButton tabButtonStatus;
	protected JButton tabButtonPingme;
	protected JButton tabButtonSettings;
	protected JButton tabButtonAbout;
	
	@Override
	protected void init(Window window, int width, int height) {
		tabButtons = new ButtonsRow();
		tabButtonStatus = tabButtons.add("Status", KEY_STATUS, this);
		tabButtonPingme = tabButtons.add("Ping Me", KEY_PINGME, this);
		tabButtonSettings = tabButtons.add("Settings", KEY_SETTINGS, this);
		tabButtonAbout = tabButtons.add("About", KEY_ABOUT, this);
	}
	
	@Override
	public void apply(Window window, int width, int height) {
		tabButtons.apply(window.getFrame(), 5, 5, width - 10, 20, 5);
	
		window.getFrame().getContentPane().addMouseListener(new MouseAdapter() {
		    @Override
		    public void mousePressed(MouseEvent e) {
		        window.getFrame().getContentPane().requestFocusInWindow();
		    }
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		openScreen(switch (e.getActionCommand()) {
			case KEY_STATUS -> ScreenStatus.INSTANCE;
			case KEY_PINGME -> ScreenPingMe.INSTANCE;
			case KEY_SETTINGS -> ScreenSettings.INSTANCE;
			case KEY_ABOUT -> ScreenAbout.INSTANCE;
			default -> null;
		});
	}
}