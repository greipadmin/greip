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
import org.eclipse.swt.widgets.Composite;
import org.greip.nls.Messages;

public final class ColorChooserHSB extends AbstractColorChooser {

	private IColorSliderConnector connector;

	public ColorChooserHSB(final Composite parent, final ColorResolution colorResolution, final boolean showInfo,
			final boolean showHistory) {
		super(parent, colorResolution, showInfo, showHistory);
		setRGB(getBackground().getRGB());
	}

	@Override
	protected Composite createColorChooserPanel() {
		final ColorResolution colorResolution = getColorResolution();
		final SliderPanel panel = new SliderPanel(this, colorResolution, Messages.Hue, Messages.Saturation, Messages.Brightness);

		connector = new ColorSliderConnectorHSB(this, panel.getSliders());

		return panel;
	}

	@Override
	public void setRGB(final RGB rgb) {
		super.setRGB(rgb);
		connector.setRGB(rgb);
	}
}
