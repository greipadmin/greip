/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.font;

import java.awt.GraphicsEnvironment;

class FontList {

	private static String[] fontNames;
	private static boolean touched;

	private FontList() {
		// nothing to do
	}

	public static synchronized String[] getFontNames() {
		if (fontNames == null) {
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			fontNames = ge.getAvailableFontFamilyNames();
		}
		return fontNames;
	}

	public static synchronized void touch() {
		if (!touched) {
			new Thread(FontList::getFontNames).start();
			touched = true;
		}
	}
}
