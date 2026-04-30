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
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class ScreenAbout extends ScreenTabs {
	public static final ScreenAbout INSTANCE = new ScreenAbout();
	private static final String URL_GITHUB_PAGE = "https://github.com/bonenaut7";
	
	private JPanel panel;
	private JButton buttonGithub;

	@Override
	protected void init(Window window, int width, int height) {
		super.init(window, width, height);
		tabButtonAbout.setEnabled(false);
		
		panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("About"));
		panel.setBounds(5, TAB_OFFSET, width - 10, height - TAB_OFFSET - 30);
		
		final JLabel label = new JLabel(String.format("<html><div style=\"width:%dpx\">%s</div></html>",
			220,
			"""
			VS Relauncher is an application that allows you to get some QOL features without using any mods. <br><br>
			
			Currently implemented features:
			<ul>
				<li>Queue monitor and notifications</li>
				<li>Chat notifications (Ping Me)</li>
				<li>[Not yet] File backups</li>
				<li>[Not yet] Storm notifications</li>
				<li>[Not yet] Death notifications</li>
			</ul>
			"""));
		panel.add(label);
		
		buttonGithub = new JButton("GitHub");
		buttonGithub.setBounds(width - 85, height - 25, 80, 20);
		buttonGithub.addActionListener(this);
	}
	
	@Override
	public void apply(Window window, int width, int height) {
		super.apply(window, width, height);
		add(window.getFrame(), panel, buttonGithub);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		if (e.getSource() == this.buttonGithub) {
			try {
				Desktop.getDesktop().browse(new URI(URL_GITHUB_PAGE));
			} catch (Exception exception) {
				;
			}
		}
	}
	
	@Override
	public Screen clone() {
		return new ScreenAbout();
	}
}