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

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

public final class ButtonsRow extends ArrayList<JButton> {
	private static final long serialVersionUID = 232864182791333664L;
	private int cacheX = -1;
	private int cacheY = -1;
	private int cacheWidth = -1;
	private int cacheHeight = -1;
	private int cacheOffset = -1;
	
	public JButton add(String title, String actionCommand, ActionListener actionListener) {
		final JButton button = new JButton(title);
		button.setActionCommand(actionCommand);
		button.addActionListener(actionListener);
		this.add(button);
		return button;
	}
	
	public void apply(JFrame frame, int x, int y, int width, int height, int offset) {
		final int size = size();
		if (this.cacheX != x || this.cacheY != y || this.cacheWidth != width || this.cacheHeight != height || this.cacheOffset != offset) {
			this.invalidate(x, y, width, height, offset, size);
		}
		
		for (int idx = 0; idx != size; idx++) {
			frame.add(get(idx));
		}
	}
	
	private void invalidate(int x, int y, int width, int height, int offset, int size) {
		this.cacheX = x;
		this.cacheY = y;
		this.cacheWidth = width;
		this.cacheHeight = height;
		this.cacheOffset = offset;
		
		int buttonWidth = (width - offset * (size - 1)) / size;
		int buttonOffset = 0;
		for (int idx = 0; idx != size; idx++) {
			final JButton button = get(idx);
			button.setBounds(x + buttonOffset, y, buttonWidth, height);
			buttonOffset += buttonWidth + offset;
		}
	}
}
