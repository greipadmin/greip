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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.greip.nls.Messages;

public final class ColorChooserRGB extends AbstractColorChooser {

	public static class Factory implements IColorChooserFactory {

		private final ColorResolution colorResolution;
		private final boolean showInfo;
		private final boolean showHistory;

		public Factory(final ColorResolution colorResolution) {
			this(colorResolution, false, false);
		}

		public Factory(final ColorResolution colorResolution, final boolean showInfo, final boolean showHistory) {
			this.colorResolution = colorResolution;
			this.showInfo = showInfo;
			this.showHistory = showHistory;
		}

		@Override
		public AbstractColorChooser create(final Composite parent) {
			return new ColorChooserRGB(parent, colorResolution, showInfo, showHistory);
		}
	}

	private IColorSliderConnector connector;

	public ColorChooserRGB(final Composite parent, final ColorResolution colorResolution, final boolean showInfo,
			final boolean showHistory) {
		super(parent, colorResolution, showInfo, showHistory);
		setRGB(getBackground().getRGB());
	}

	@Override
	protected Composite createColorChooserPanel() {
		final ColorResolution colorResolution = getColorResolution();
		final SliderPanel panel = new SliderPanel(this, SWT.HORIZONTAL, colorResolution, Messages.Red, Messages.Green, Messages.Blue);

		connector = new ColorSliderConnectorRGB(this, panel.getSliders());

		return panel;
	}

	@Override
	public void setRGB(final RGB rgb) {
		super.setRGB(rgb);
		connector.setRGB(rgb);
	}
}
