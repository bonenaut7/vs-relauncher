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

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;

import by.bonenaut7.vsrelauncher.Application;

public abstract class Screen {
	private boolean initialized = false;
	private Window initWindow = null;
	
	public final void reset(Window window, int width, int height) {
		if (!initialized || Application.DEBUG) {
			initWindow = window;
			init(window, width, height);
			
			initialized = true;
		}
		
		apply(window, width, height);
	}
	
	protected abstract void init(Window window, int width, int height);
	
	protected abstract void apply(Window window, int width, int height);
	
	public abstract Screen clone();
	
	protected boolean isInitizalized() {
		return initialized;
	}
	
	protected void openScreen(Screen screen) {
		if (screen == null || initWindow == null) {
			return;
		}
		
		initWindow.setScreen(screen);
	}
	
	protected void add(JFrame frame, JComponent... components) {
		for (int idx = 0; idx != components.length; idx++) {
			frame.add(components[idx]);
		}
	}
	
	protected void placeAsRow(int x, int y, int width, int height, int offset, JComponent... components) {
		int objWidth = (width + offset) / components.length - offset;
		int objOffset = 0;
		for (int idx = 0; idx != components.length; idx++) {
			final JComponent component = components[idx];
			component.setBounds(x + objOffset, y, objWidth, height);
			objOffset += objWidth + offset;
		}
		
		/* We can't calculate size of object for some weird reason
		int componentsWidth = 0;
		for (int idx = 0; idx != components.length; idx++) { 
			componentsWidth += components[idx].getWidth() + minObjOffset;
		}
		
		// Adding another minObjOffset in the braces to remove single excessive offset from the cycle above
		final int objOffset = (width - componentsWidth + minObjOffset) / components.length + minObjOffset; // - minObjOffset?
		int offset = 0;
		for (int idx = 0; idx != components.length; idx++) {
			final JComponent component = components[idx];
			component.setBounds(x + objOffset, y, component.getwid, height);
			objOffset += objWidth + minObjOffset;
		}
		*/
	}
	
	protected void placeAsRow(int x, int y, int width, int height, int offset, List<? extends JComponent> components) {
		int objWidth = (width + offset) / components.size() - offset;
		int objOffset = 0;
		for (int idx = 0; idx != components.size(); idx++) {
			final JComponent component = components.get(idx);
			component.setBounds(x + objOffset, y, objWidth, height);
			objOffset += objWidth + offset;
		}
	}
	
	protected JCheckBox addCheckbox(String name, int x, int y, int width, int height) {
		return addCheckbox(null, name, null, null, x, y, width, height);
	}
	
	protected JCheckBox addCheckbox(String name, String actionCommand, ActionListener listener, int x, int y, int width, int height) {
		return addCheckbox(null, name, actionCommand, listener, x, y, width, height);
	}
	
	protected JCheckBox addCheckbox(JComponent parent, String name, String actionCommand, ActionListener listener, int x, int y, int width, int height) {
		return addCheckbox(parent, name, actionCommand, listener, false, x, y, width, height);
	}
	
	protected JCheckBox addCheckbox(JComponent parent, String name, String actionCommand, ActionListener listener, boolean focusable, int x, int y, int width, int height) {
		final JCheckBox checkbox = new JCheckBox(name);
		checkbox.setActionCommand(actionCommand);
		checkbox.setFocusable(focusable);
		checkbox.setBounds(x, y, width, height);
		
		if (listener != null) {
			checkbox.addActionListener(listener);
		}
		
		if (parent != null) {
			parent.add(checkbox);
		}
		
		return checkbox;
	}
}
