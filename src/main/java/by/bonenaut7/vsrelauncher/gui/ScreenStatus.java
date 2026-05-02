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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import by.bonenaut7.vsrelauncher.Application;
import by.bonenaut7.vsrelauncher.systems.QueueSystem;
import by.bonenaut7.vsrelauncher.util.ButtonsRow;
import by.bonenaut7.vsrelauncher.util.Utils;

public final class ScreenStatus extends ScreenTabs {
	public static final ScreenStatus INSTANCE = new ScreenStatus();
	private static final String KEY_GAMEDIR = "status::gamedir";
	private static final String KEY_DATADIR = "status::datadir";
	
	private JPanel infoPanel;
	private JLabel queuePositionText;
	private JLabel queueSpeedText;
	private JLabel queueETAText;
	private JLabel queueWaitText;
	private JLabel statsPlayTimeText;
	private JLabel statsQueueTimeText;
	private JPanel settingsPanel;
	private ButtonsRow tabFolderButtons;
	private JButton buttonGameFolder;
	private JButton buttonDataFolder;
	
	@Override
	protected void init(Window window, int width, int height) {
		super.init(window, width, height);
		tabButtonStatus.setEnabled(false);
		
		infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Queue monitor"));
		infoPanel.setBounds(5, TAB_OFFSET, width - 10, 110);
		infoPanel.setLayout(null);
		
		final int yOffsetStep = 18;
		int yOffset = 4;
		queuePositionText = new JLabel("Queue status");
		queuePositionText.setBounds(10, yOffset += yOffsetStep, width - 20, 20);
		infoPanel.add(queuePositionText);
		
		queueSpeedText = new JLabel("Queue speed");
		queueSpeedText.setBounds(10, yOffset += yOffsetStep, width - 20, 20);
		queueSpeedText.setToolTipText("Queue speed in queue positions per minute.");
		infoPanel.add(queueSpeedText);
		
		queueETAText = new JLabel("Queue ETA");
		queueETAText.setBounds(10, yOffset += yOffsetStep, width - 20, 20);
		queueETAText.setToolTipText("30 minutes estimate of how much time you need to wait before joining the server.");
		infoPanel.add(queueETAText);
		
		queueWaitText = new JLabel("Queue wait time");
		queueWaitText.setBounds(10, yOffset += yOffsetStep, width - 20, 20);
		queueWaitText.setToolTipText("Amount of time you've already waited in queue.");
		infoPanel.add(queueWaitText);
		
		settingsPanel = new JPanel();
		settingsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
		settingsPanel.setBounds(5, TAB_OFFSET + 110, width - 10, height - TAB_OFFSET - 110 - 30);
		settingsPanel.setLayout(null);
		
		yOffset = 4;
		statsPlayTimeText = new JLabel("Play time");
		statsPlayTimeText.setBounds(10, yOffset += yOffsetStep, width - 20, 20);
		statsPlayTimeText.setToolTipText("Amount of time you've played in total.");
		settingsPanel.add(statsPlayTimeText);
		
		statsQueueTimeText = new JLabel("Queue wait time");
		statsQueueTimeText.setBounds(10, yOffset += yOffsetStep, width - 20, 20);
		statsQueueTimeText.setToolTipText("Amount of time you've waited in queue in total.");
		settingsPanel.add(statsQueueTimeText);
		
		tabFolderButtons = new ButtonsRow();
		buttonGameFolder = tabFolderButtons.add("Open game folder", KEY_GAMEDIR, this);
		buttonGameFolder.setToolTipText("Opens game folder");
		buttonDataFolder = tabFolderButtons.add("Open data folder", KEY_DATADIR, this);
		buttonDataFolder.setToolTipText("Opens game data folder with logs, configs, etc.");
	}
	
	@Override
	public void apply(Window window, int width, int height) {
		super.apply(window, width, height);
		
		final var ctx = Application.context();
		final var system = ctx.getSystem(QueueSystem.class);
		
		if (system.getGameState() == QueueSystem.GAME_STATE_IN_QUEUE) {
			final int queuePosition = system.getQueuePosition();
			final float queueSpeed = system.getQueueSpeed();
			
			this.queuePositionText.setText(String.format("Your queue position is: %d", queuePosition));
			
			final String stuckMessage = system.isQueueStuck() ? " (Queue stuck?)" : "";
			if (queueSpeed < 0F) {
				this.queueSpeedText.setText("Speed: Estimating... (less than 0.05/min)" + stuckMessage);
				this.queueETAText.setText(String.format("ETA: Estimating... (more than %s)%s", Utils.formatTimeMinutes((int)(queuePosition / 0.05f)), stuckMessage));
			} else {
				this.queueSpeedText.setText(String.format("Speed: %s%s", String.format("%.2f/min", queueSpeed), stuckMessage));
				this.queueETAText.setText(String.format("ETA: %s%s", Utils.formatTimeMinutes((int)(queuePosition / queueSpeed)), stuckMessage));
			}
			
			this.queueWaitText.setText(String.format("Waiting for: %s", Utils.formatTimeMinutes(system.getQueueJoinInstant().until(Instant.now(), ChronoUnit.MINUTES))));
			
			queueSpeedText.setVisible(true);
			queueETAText.setVisible(true);
			queueWaitText.setVisible(true);
		} else {
			this.queuePositionText.setText(switch (system.getGameState()) {
				case QueueSystem.GAME_STATE_IN_GAME -> "You're in the game.";
				default -> "You are not in the queue.";
			});
			
			queueSpeedText.setVisible(false);
			queueETAText.setVisible(false);
			queueWaitText.setVisible(false);
		}
		
		statsPlayTimeText.setText(String.format("Total play time: %s", Utils.formatTime(ctx.config.stats_playTime)));
		statsQueueTimeText.setText(String.format("Total queue wait: %s", Utils.formatTime(ctx.config.stats_queueTime)));
		
		buttonGameFolder.setEnabled(ctx.config.gamePath != null);
		buttonDataFolder.setEnabled(ctx.config.gameDataPath != null);
		
		add(window.getFrame(), infoPanel, settingsPanel);
		tabFolderButtons.apply(window.getFrame(), 5, height - 25, width - 10, 20, 5);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		final var ctx = Application.context();
		
		switch (e.getActionCommand()) {
			case KEY_GAMEDIR:
				Utils.sneakyThrows(() -> Desktop.getDesktop().open(new File(ctx.config.gamePath)));
				break;
			
			case KEY_DATADIR:
				Utils.sneakyThrows(() -> Desktop.getDesktop().open(new File(ctx.config.gameDataPath)));
				break;
		}
	}
	
	@Override
	public Screen clone() {
		return new ScreenStatus();
	}
}