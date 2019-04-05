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

	private static final String PREFERENCE_NODE = ColorHistoryList.class.getPackage().getName();
	private static final Preferences preferences = InstanceScope.INSTANCE.getNode(PREFERENCE_NODE);

	static ColorHistoryList INSTANCE = new ColorHistoryList();

	public void add(final RGB rgb) {
		final List<Integer> history = new ArrayList<>();
		final Integer rgbInt = Integer.valueOf(rgb.hashCode());

		for (int i = 0; i < size(); i++) {
			history.add(Integer.valueOf(preferences.getInt("color_" + i, SWT.DEFAULT)));
		}

		history.remove(rgbInt);
		history.add(0, rgbInt);

		for (int i = 0; i < size(); i++) {
			preferences.putInt("color_" + i, history.get(i).intValue());
		}
	}

	public RGB get(final int index) {
		final int rgbInt = preferences.getInt("color_" + index, SWT.DEFAULT);
		return rgbInt == SWT.DEFAULT ? null : new RGB(rgbInt & 0xFF, rgbInt >> 8 & 0xFF, rgbInt >> 16 & 0xFF);
	}

	public int size() {
		return 8;
	}
}
