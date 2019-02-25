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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.greip.nls.Messages;

public final class ColorCircleChooser extends AbstractColorChooser {

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
			return new ColorCircleChooser(parent, colorResolution, showInfo, showHistory);
		}
	}

	private ColorCircle colorCircle;
	private IColorSliderConnector connector;

	public ColorCircleChooser(final Composite parent, final ColorResolution colorResolution, final boolean showInfo,
			final boolean showHistory) {
		super(parent, colorResolution, showInfo, showHistory);
		setRGB(getBackground().getRGB());
	}

	private void createColorCircle(final Composite parent) {
		colorCircle = new ColorCircle(parent, getColorResolution());
		colorCircle.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		colorCircle.addListener(SWT.Modify, e -> setNewRGB(determineNewRGB()));
		colorCircle.addListener(SWT.Selection, e -> notifyListeners(SWT.Selection, new Event()));
	}

	private RGB determineNewRGB() {
		final float hue = colorCircle.getRGB().getHSB()[0];
		final float[] hsb = connector.getRGB().getHSB();
		final float saturation = hsb[2];
		final float brightness = hsb[1] == 0.0f ? 1.0f : hsb[1];

		final RGB rgb = new RGB(hue, saturation, brightness);
		connector.setRGB(rgb);

		return rgb;
	}

	private void createSliders(final Composite parent) {
		final ColorResolution colorResolution = getColorResolution();
		final SliderPanel panel = new SliderPanel(parent, SWT.VERTICAL, colorResolution, Messages.Saturation, Messages.Brightness);

		GridDataFactory.fillDefaults().grab(false, true).align(SWT.LEFT, SWT.FILL).applyTo(panel);
		connector = new ColorSliderConnectorCircle(this, panel.getSliders());
	}

	@Override
	protected Composite createColorChooserPanel() {
		final Composite panel = new Composite(this, SWT.NO_FOCUS);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(10, 0).applyTo(panel);

		createColorCircle(panel);
		createSliders(panel);

		return panel;
	}

	@Override
	public void setRGB(final RGB rgb) {
		super.setRGB(rgb);
		colorCircle.setRGB(rgb);
		connector.setRGB(rgb);
	}
}
