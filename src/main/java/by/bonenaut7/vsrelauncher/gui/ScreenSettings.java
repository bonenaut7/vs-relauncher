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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.Application;
import by.bonenaut7.vsrelauncher.systems.relauncher.WindowSystem;
import by.bonenaut7.vsrelauncher.util.ButtonsRow;
import by.bonenaut7.vsrelauncher.util.Utils;

public final class ScreenSettings extends ScreenTabs {
	public static final ScreenSettings INSTANCE = new ScreenSettings();
	private static final String KEY_EDITCFG = "settings::editcfg";
	private static final String KEY_LOADCFG = "settings::loadcfg";
	private static final String KEY_SAVECFG = "settings::savecfg";
	private static final String CHECKBOX_ENABLE_ARCHIVING = "chk::enable_archive";
	private static final String CHECKBOX_ENABLE_PING = "chk::enable_ping";
	private static final String CHECKBOX_ENABLE_JOINLEAVEGAME = "chk::enable_stategame";
	private static final String CHECKBOX_ENABLE_JOINLEAVEQUEUE = "chk::enable_statequeue";
	
	private JPanel infoPanel;
	private JLabel unsavedChangesText;
	private ButtonsRow configButtons;
	
	private JCheckBox checkboxEnableArchiving;
	private JCheckBox checkboxEnablePing;
	private JCheckBox checkboxEnableJoinLeaveGame;
	private JCheckBox checkboxEnableJoinLeaveQueue;
	
	@Override
	protected void init(Window window, int width, int height) {
		super.init(window, width, height);
		this.tabButtonSettings.setEnabled(false);
		
		infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
		infoPanel.setBounds(5, TAB_OFFSET, width - 10, height - TAB_OFFSET - 30);
		infoPanel.setLayout(null);
		
		unsavedChangesText = new JLabel("You have unsaved changes");
		unsavedChangesText.setHorizontalAlignment(SwingConstants.CENTER);
		unsavedChangesText.setBounds(0, height - 100, width - 25, 20);
		infoPanel.add(unsavedChangesText);
		
		final int contentWidth = width - 20;
		final int yOffsetStep = 25;
		int yOffset = -5;
		checkboxEnableArchiving = addCheckbox(infoPanel, "Enable archiving", CHECKBOX_ENABLE_ARCHIVING, this, 10, yOffset += yOffsetStep, contentWidth, 20);
		checkboxEnableArchiving.setToolTipText("Enables log file backups for every game session");
		checkboxEnablePing = addCheckbox(infoPanel, "Enable \"Ping Me\"", CHECKBOX_ENABLE_PING, this, 10, yOffset += yOffsetStep, contentWidth, 20);
		checkboxEnablePing.setToolTipText("Enables \"Ping Me\", notifications about in-game chat mentions.");
		checkboxEnableJoinLeaveGame = addCheckbox(infoPanel, "Notify on game join/leave", CHECKBOX_ENABLE_JOINLEAVEGAME, this, 10, yOffset += yOffsetStep, contentWidth, 20);
		checkboxEnableJoinLeaveGame.setToolTipText("Enables notifications when you join and leave the game(world or server).");
		checkboxEnableJoinLeaveQueue = addCheckbox(infoPanel, "Notify on queue join/leave", CHECKBOX_ENABLE_JOINLEAVEQUEUE, this, 10, yOffset += yOffsetStep, contentWidth, 20);
		checkboxEnableJoinLeaveQueue.setToolTipText("Enables notifications when you join and leave the queue.");
		
		configButtons = new ButtonsRow();
		configButtons.add("Edit config", KEY_EDITCFG, this).setToolTipText("Edit configuration file");
		configButtons.add("Load config", KEY_LOADCFG, this).setToolTipText("Loads configuration file");
		configButtons.add("Save config", KEY_SAVECFG, this).setToolTipText("Updates and saves configuration file");
	}
	
	@Override
	public void apply(Window window, int width, int height) {
		super.apply(window, width, height);
		
		var ctx = Application.context();
		var cfg = ctx.config;
		
		checkboxEnableArchiving.setSelected(cfg.archive_enabled);
		checkboxEnablePing.setSelected(cfg.ping_enabled);
		checkboxEnableJoinLeaveGame.setSelected(cfg.notify_joinLeaveGame);
		checkboxEnableJoinLeaveQueue.setSelected(cfg.notify_joinLeaveQueue);
		unsavedChangesText.setVisible(ctx.configUnsavedChanges);
		
		add(window.getFrame(), infoPanel);
		configButtons.apply(window.getFrame(), 5, height - 25, width - 10, 20, 5);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		var ctx = Application.context();
		switch (e.getActionCommand()) {
			case KEY_EDITCFG:
				final File configFile = ctx.config.getConfigFile();
				
				Utils.sneakyThrows(
					() -> Desktop.getDesktop().edit(configFile),
					ex1 -> Utils.sneakyThrows(
						() -> Desktop.getDesktop().open(configFile),
						ex2 -> Utils.sneakyThrows(() -> Desktop.getDesktop().open(configFile.toPath().getParent().toFile()))
					)
				);
				break;
		
			case KEY_LOADCFG:
				ctx.config.load();
				ctx.onConfigUpdate();
				onChangesSaved(ctx);
				ctx.getSystem(WindowSystem.class).redraw();
				break;
			
			case KEY_SAVECFG:
				ctx.config.save();
				onChangesSaved(ctx);
				break;
				
			case CHECKBOX_ENABLE_ARCHIVING:
				ctx.config.archive_enabled = checkboxEnableArchiving.isSelected();
				onConfigUpdate(ctx);
				break;
				
			case CHECKBOX_ENABLE_PING:
				ctx.config.ping_enabled = checkboxEnablePing.isSelected();
				onConfigUpdate(ctx);
				break;
				
			case CHECKBOX_ENABLE_JOINLEAVEGAME:
				ctx.config.notify_joinLeaveGame = checkboxEnableJoinLeaveGame.isSelected();
				onConfigUpdate(ctx);
				break;
				
			case CHECKBOX_ENABLE_JOINLEAVEQUEUE:
				ctx.config.notify_joinLeaveQueue = checkboxEnableJoinLeaveQueue.isSelected();
				onConfigUpdate(ctx);
				break;
		}
	}
	
	@Override
	public Screen clone() {
		return new ScreenSettings();
	}
	
	private void onConfigUpdate(AppContext ctx) {
		ctx.onConfigUpdate();
		ctx.configUnsavedChanges = true;
		unsavedChangesText.setVisible(true);
	}
	
	private void onChangesSaved(AppContext ctx) {
		ctx.configUnsavedChanges = false;
		unsavedChangesText.setVisible(false);
	}
}