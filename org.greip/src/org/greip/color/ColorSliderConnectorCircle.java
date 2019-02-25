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

public final class ColorSliderConnectorCircle extends AbstractColorSliderConnector {

	private final ColorCircleChooser colorChooser;

	public ColorSliderConnectorCircle(final ColorCircleChooser colorChooser, final ColorSlider... sliders) {
		super(colorChooser, sliders);

		this.colorChooser = colorChooser;

		sliders[0].setType(ColorSliderType.Saturation);
		sliders[1].setType(ColorSliderType.Brightness);
	}

	private HSB getHSB() {
		final double[] values = getValues();
		final float hue = colorChooser.getRGB().getHSB()[0];
		return new HSB(hue, (float) values[0], (float) values[1]);
	}

	@Override
	public RGB getRGB() {
		return getHSB().getRGB();
	}
}
