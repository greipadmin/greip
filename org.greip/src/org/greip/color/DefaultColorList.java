/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

class DefaultColorList extends ArrayList<RGB> {
	private static final long serialVersionUID = 1L;

	public DefaultColorList() {
		add(new RGB(255, 255, 255)); // white
		add(new RGB(235, 235, 235)); // light gray
		add(new RGB(192, 192, 192)); // gray
		add(new RGB(128, 128, 128)); // dark gray
		add(new RGB(64, 64, 64)); // darkest gray
		add(new RGB(0, 0, 0)); // black
		addAll(getGraduationFor(new RGB(255, 0, 0))); // red
		addAll(getGraduationFor(new RGB(255, 100, 0))); // Orange
		addAll(getGraduationFor(new RGB(255, 180, 0)));
		addAll(getGraduationFor(new RGB(255, 255, 0)));
		addAll(getGraduationFor(new RGB(0, 255, 0))); // green
		addAll(getGraduationFor(new RGB(50, 255, 127)));
		addAll(getGraduationFor(new RGB(0, 255, 255))); // cyan
		addAll(getGraduationFor(new RGB(0, 0, 255))); // blue
		addAll(getGraduationFor(new RGB(255, 0, 255))); // magenta
		addAll(getGraduationFor(new RGB(255, 10, 120)));
	}

	private static List<RGB> getGraduationFor(final RGB rgb) {
		final RGB lightRGB = lightenColor(rgb);
		final RGB darkRGB = shadeColor(rgb);
		final RGB darkRGB2 = shadeColor(darkRGB);

		return Arrays.asList(lightenColor(lightRGB), lightRGB, rgb, darkRGB, darkRGB2, shadeColor(darkRGB2));
	}

	private static RGB lightenColor(final RGB rgb) {
		final float[] hsb = rgb.getHSB();
		return new RGB(hsb[0], hsb[1] / 2.2f, hsb[2]);
	}

	private static RGB shadeColor(final RGB rgb) {
		final float[] hsb = rgb.getHSB();
		return new RGB(hsb[0], hsb[1], hsb[2] / 1.5f);
	}
}
