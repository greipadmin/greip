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

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

abstract class AbstractColorSliderConnector implements IColorSliderConnector {

	private final List<ColorSlider> sliders;

	public AbstractColorSliderConnector(final AbstractColorChooser colorChooser, final ColorSlider... sliders) {
		this.sliders = Arrays.asList(sliders);
		this.sliders.forEach(s -> s.addListener(SWT.Selection, e -> colorChooser.setNewRGB(getRGB())));
	}

	@Override
	public abstract RGB getRGB();

	@Override
	public void setRGB(final RGB rgb) {
		sliders.forEach(s -> s.setHSB(new HSB(rgb)));
	}

	protected final float[] getValues() {
		final float[] values = new float[sliders.size()];

		for (int i = 0; i < sliders.size(); i++) {
			values[i] = sliders.get(i).getValue();
		}

		return values;
	}
}
