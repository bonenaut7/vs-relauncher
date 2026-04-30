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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.Application;

public final class ScreenPingMe extends ScreenTabs {
	public static final ScreenPingMe INSTANCE = new ScreenPingMe();
	private static final String CHECKBOX_ENABLE_PING = "chk::enable_ping";
	private static final String CHECKBOX_CASE_SENSETIVE = "chk::case_sensetive";
	
	private JPanel infoPanel;
	private JScrollPane pingMePane;
	private DefaultListModel<String> pingMeList;
	private JButton entryAddButton;
	private JTextField entryField;
	
	private JCheckBox checkboxEnablePing;
	private JCheckBox checkboxCaseSensetive;
	
	@Override
	protected void init(Window window, int width, int height) {
		super.init(window, width, height);
		tabButtonPingme.setEnabled(false);
		
		infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Ping Me"));
		infoPanel.setBounds(5, TAB_OFFSET, width - 10, height - TAB_OFFSET - 5);
		infoPanel.setLayout(null);
		
		final JLabel text = new JLabel("Keywords");
		text.setBounds(width - 170 - 20, 11, 170, 20);
		text.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(text);
		
		pingMeList = new DefaultListModel<>();
		pingMePane = new JScrollPane(createListWithPopupRemoval(pingMeList, this::onKeywordDeleted));
		pingMePane.setBounds(width - 170 - 20, 35, 170, 200);
		pingMePane.setFocusable(false);
		infoPanel.add(pingMePane);
		
		entryField = new JTextField();
		entryField.setBounds(width - 170 - 20, 240, 145, 20);
		entryField.addActionListener(this::onActionKeywordAdd);
		infoPanel.add(entryField);
		
		entryAddButton = new JButton("+");
		entryAddButton.setBounds(width - 40, 240, 20, 20);
		entryAddButton.addActionListener(this::onActionKeywordAdd);
		infoPanel.add(entryAddButton);
		
		checkboxEnablePing = addCheckbox(infoPanel, "Enable \"Ping Me\"", CHECKBOX_ENABLE_PING, this, 10, 30, width / 2 - 20, 20);
		checkboxEnablePing.setToolTipText("Enables \"Ping Me\", notifications about in-game chat mentions.");
		checkboxCaseSensetive = addCheckbox(infoPanel, "Case sensetive", CHECKBOX_CASE_SENSETIVE, this, 10, 55, width / 2 - 20, 20);
		checkboxCaseSensetive.setToolTipText("Enables case sensetive checks for the mention keywords.");
	}
	
	@Override
	public void apply(Window window, int width, int height) {
		super.apply(window, width, height);
		
		var cfg = Application.context().config;
		
		pingMeList.clear();
		cfg.ping_keywords.forEach(pingMeList::addElement);
		entryField.setText("");
		checkboxEnablePing.setSelected(cfg.ping_enabled);
		checkboxCaseSensetive.setSelected(!cfg.ping_ignoreCase);
		
		add(window.getFrame(), infoPanel);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		var ctx = Application.context();
		var cfg = ctx.config;
		
		switch (e.getActionCommand()) {
			case CHECKBOX_ENABLE_PING:
				cfg.ping_enabled = checkboxEnablePing.isSelected();
				onConfigUpdated(ctx);
				break;
				
			case CHECKBOX_CASE_SENSETIVE:
				ctx.config.ping_ignoreCase = !checkboxCaseSensetive.isSelected();
				onConfigUpdated(ctx);
				break;
		}
	}
	
	@Override
	public Screen clone() {
		return new ScreenPingMe();
	}

	private void onConfigUpdated(AppContext ctx) {
		ctx.config.save();
		ctx.onConfigUpdate();
		ctx.configUnsavedChanges = false;
	}
	
	private void onActionKeywordAdd(ActionEvent event) {
		final String keyword = entryField.getText().trim();
		if (keyword.length() != 0 && !Application.context().config.ping_keywords.contains(keyword)) {
			onKeywordAdded(keyword);
			pingMeList.addElement(keyword);
			entryField.setText("");
		}
	}
	
	private void onKeywordDeleted(String keyword) {
		var ctx = Application.context();
		var cfg = ctx.config;
		
		cfg.ping_keywords.remove(keyword);
		cfg.save();
		ctx.onConfigUpdate();
		ctx.configUnsavedChanges = false;
	}
	
	private void onKeywordAdded(String keyword) {
		var ctx = Application.context();
		var cfg = ctx.config;
		
		cfg.ping_keywords.add(keyword);
		cfg.save();
		ctx.onConfigUpdate();
		ctx.configUnsavedChanges = false;
	}
	
	private <T> JList<T> createListWithPopupRemoval(DefaultListModel<T> model, Consumer<T> callback) {
		// Create list with mouse RMB selection
		final JList<T> list = new JList<>(model);
		list.setFocusable(false);
		list.setFixedCellHeight(20);
		list.addMouseListener(new MouseAdapter() {
			@Override
		    public void mousePressed(MouseEvent e) {
		        if (SwingUtilities.isRightMouseButton(e)) {
		            int row = list.locationToIndex(e.getPoint());
		            list.setSelectedIndex(row);
		        }
		    }
		});
		
		// Popup selection menu
		final JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener(e -> {
			final int index = list.getSelectedIndex();
			if (index != -1) {
				callback.accept(model.elementAt(index));
				model.remove(index);
			}
		});
		
		final JPopupMenu popup = new JPopupMenu();
		popup.add(deleteItem);
		list.setComponentPopupMenu(popup);
		
		return list;
	}
}