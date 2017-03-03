/**
 * Copyright (c) 2017 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.osgi.service.prefs.Preferences;

class ColorHistoryList {

	private static final String PREFERENCE_NODE = ColorHistory.class.getPackage().getName();
	private static final Preferences preferences = InstanceScope.INSTANCE.getNode(PREFERENCE_NODE);

	static ColorHistoryList INSTANCE = new ColorHistoryList();

	private final List<RGB> history = new ArrayList<>();

	private ColorHistoryList() {

		for (int i = 0;; i++) {
			final int rgbInt = preferences.getInt("color_" + i, SWT.DEFAULT);

			if (rgbInt == SWT.DEFAULT) {
				break;
			}

			history.add(i, new RGB(255, 255, 255));
		}
	}

	public boolean add(final RGB rgb) {
		history.remove(rgb);
		history.add(0, rgb);
		if (history.size() == 10) history.remove(9);

		updatePreferences();

		return true;
	}

	public RGB get(final int index) {
		return history.size() > index ? history.get(index) : null;
	}

	private void updatePreferences() {
		for (int i = 0; i < history.size(); i++) {
			preferences.putInt("color_", history.get(i).hashCode());
		}
	}
}
