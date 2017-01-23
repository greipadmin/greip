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

import org.eclipse.swt.graphics.RGB;

public final class ColorSliderConnectorRGB extends AbstractColorSliderConnector {

	public ColorSliderConnectorRGB(final ColorChooserRGB colorChooser, final ColorSlider... sliders) {
		super(colorChooser, sliders);

		sliders[0].setType(ColorSliderType.Red);
		sliders[1].setType(ColorSliderType.Green);
		sliders[2].setType(ColorSliderType.Blue);
	}

	@Override
	public RGB getRGB() {
		final float[] values = getValues();
		return new RGB((int) (values[0] * 255), (int) (values[1] * 255), (int) (values[2] * 255));
	}
}
