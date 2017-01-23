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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public final class ColorSliderConnectorHSB extends AbstractColorSliderConnector {

	public ColorSliderConnectorHSB(final ColorChooserHSB colorChooser, final ColorSlider... sliders) {
		super(colorChooser, sliders);

		sliders[0].setType(ColorSliderType.Hue);
		sliders[1].setType(ColorSliderType.Saturation);
		sliders[2].setType(ColorSliderType.Brightness);

		sliders[0].addListener(SWT.Selection, e -> sliders[1].setHSB(getHSB()));
	}

	private HSB getHSB() {
		final float[] values = getValues();
		return new HSB(values[0] * 360, values[1], values[2]);
	}

	@Override
	public RGB getRGB() {
		return getHSB().getRGB();
	}
}
