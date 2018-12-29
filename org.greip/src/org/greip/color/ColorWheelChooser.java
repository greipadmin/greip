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

public final class ColorWheelChooser extends AbstractColorChooser {

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
			return new ColorWheelChooser(parent, colorResolution, showInfo, showHistory);
		}
	}

	private ColorWheel colorWheel;
	private ColorSlider brightnessSlider;

	public ColorWheelChooser(final Composite parent, final ColorResolution colorResolution, final boolean showInfo,
			final boolean showHistory) {
		super(parent, colorResolution, showInfo, showHistory);
		setRGB(getBackground().getRGB());
	}

	private void createColorWheel(final Composite parent) {
		colorWheel = new ColorWheel(parent, getColorResolution());
		colorWheel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 5));
		colorWheel.addListener(SWT.Modify, e -> setNewRGB(determineNewRGB()));
		colorWheel.addListener(SWT.Selection, e -> notifyListeners(SWT.Selection, new Event()));
	}

	private RGB determineNewRGB() {
		final float[] wheelHSB = colorWheel.getRGB().getHSB();
		final float brightness = brightnessSlider.getValue();

		final HSB hsb = new HSB(wheelHSB[0], wheelHSB[1], 1 - brightness);
		brightnessSlider.setHSB(hsb);

		return hsb.getRGB();
	}

	private void createBrightnessSlider(final Composite parent) {
		brightnessSlider = new ColorSlider(parent, getColorResolution());
		brightnessSlider.setLayoutData(GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 127).create());
		brightnessSlider.setType(ColorSliderType.Lightness);
		brightnessSlider.setOrientation(SWT.VERTICAL);
		brightnessSlider.addListener(SWT.Selection, e -> setNewRGB(determineNewRGB()));
		brightnessSlider.addListener(SWT.MouseDoubleClick, e -> notifyListeners(SWT.Selection, e));
	}

	@Override
	protected Composite createColorChooserPanel() {
		final Composite panel = new Composite(this, SWT.NO_FOCUS);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(10, 0).applyTo(panel);

		createColorWheel(panel);
		createBrightnessSlider(panel);

		return panel;
	}

	@Override
	public void setRGB(final RGB rgb) {
		super.setRGB(rgb);
		colorWheel.setRGB(rgb);
		brightnessSlider.setHSB(new HSB(rgb));
	}
}
