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

import org.eclipse.swt.graphics.RGB;

final class HSB {

	private final float[] hsb;

	public HSB(final float hue, final float saturation, final float brightness) {
		hsb = new float[] { hue, saturation, brightness };
	}

	public HSB(final RGB rgb) {
		hsb = rgb.getHSB();
	}

	public RGB getRGB() {
		return new RGB(hsb[0], hsb[1], hsb[2]);
	}

	public float getHue() {
		return hsb[0];
	}

	public float getSaturation() {
		return hsb[1];
	}

	public float getBrightness() {
		return hsb[2];
	}

	@Override
	public String toString() {
		return "HSB {" + hsb[0] + ", " + hsb[1] + ", " + hsb[2] + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
