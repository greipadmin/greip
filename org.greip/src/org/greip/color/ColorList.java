/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

class ColorList extends ArrayList<RGB> {
	private static final long serialVersionUID = 1L;

	public ColorList(final Display display) {
		add(display.getSystemColor(SWT.COLOR_WHITE).getRGB());
		add(new RGB(235, 235, 235)); // light gray
		add(display.getSystemColor(SWT.COLOR_GRAY).getRGB());
		add(display.getSystemColor(SWT.COLOR_DARK_GRAY).getRGB());
		add(new RGB(70, 70, 70));
		add(display.getSystemColor(SWT.COLOR_BLACK).getRGB());
		addAll(getRGBsFor(display.getSystemColor(SWT.COLOR_RED).getRGB()));
		addAll(getRGBsFor(new RGB(255, 100, 0))); // Orange
		addAll(getRGBsFor(new RGB(255, 180, 0)));
		addAll(getRGBsFor(new RGB(255, 255, 0)));
		addAll(getRGBsFor(display.getSystemColor(SWT.COLOR_GREEN).getRGB()));
		addAll(getRGBsFor(new RGB(50, 255, 127)));
		addAll(getRGBsFor(display.getSystemColor(SWT.COLOR_CYAN).getRGB()));
		addAll(getRGBsFor(display.getSystemColor(SWT.COLOR_BLUE).getRGB()));
		addAll(getRGBsFor(display.getSystemColor(SWT.COLOR_MAGENTA).getRGB()));
		addAll(getRGBsFor(new RGB(255, 10, 120)));
	}

	private static List<RGB> getRGBsFor(final RGB rgb) {
		final RGB lightRGB = lightenColor(rgb);
		final RGB darkRGB = darkenColor(rgb);
		final RGB darkRGB2 = darkenColor(darkRGB);

		return Arrays.asList(lightenColor(lightRGB), lightRGB, rgb, darkRGB, darkRGB2, darkenColor(darkRGB2));
	}

	private static RGB lightenColor(final RGB rgb) {
		final float[] hsb = rgb.getHSB();
		return new RGB(hsb[0], hsb[1] / 2.2f, hsb[2]);
	}

	private static RGB darkenColor(final RGB rgb) {
		final float[] hsb = rgb.getHSB();
		return new RGB(hsb[0], hsb[1], hsb[2] / 1.5f);
	}
}
