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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.greip.nls.Messages;
import org.greip.separator.LineStyle;
import org.greip.separator.Separator;

public abstract class AbstractColorChooser extends Composite implements IColorChooser {

	private final ColorResolution colorResolution;
	private RGB newRGB;
	private RGB rgb;

	private Spinner spiRed;
	private Spinner spiGreen;
	private Spinner spiBlue;
	private Composite previewPanel;
	private final boolean showInfo;

	public AbstractColorChooser(final Composite parent, final ColorResolution colorResolution, final boolean showInfo) {
		super(parent, SWT.NO_FOCUS);

		this.colorResolution = colorResolution;
		this.showInfo = showInfo;
		this.rgb = new RGB(255, 255, 255);
		this.newRGB = rgb;

		setLayout(GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).create());
		setBackgroundMode(SWT.INHERIT_FORCE);

		createColorChooserPanel();

		if (showInfo) {
			createInfoPanel();
		}
	}

	protected abstract Composite createColorChooserPanel();

	private void createInfoPanel() {
		final Composite infoPanel = new Composite(this, SWT.NONE);
		infoPanel.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(0, 10, 10, 10).spacing(3, 5).create());
		infoPanel.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		final Separator line = new Separator(infoPanel, SWT.VERTICAL, LineStyle.Dot);
		line.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 4));
		line.setLineColor(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		line.setMarginWidth(0);

		spiRed = createRGBSpinner(infoPanel, Messages.Red);
		spiGreen = createRGBSpinner(infoPanel, Messages.Green);
		spiBlue = createRGBSpinner(infoPanel, Messages.Blue);

		previewPanel = new Composite(infoPanel, SWT.NO_FOCUS);
		previewPanel.setLayoutData(GridDataFactory.swtDefaults().hint(60, 30).span(2, 1).indent(7, 7).align(SWT.CENTER, SWT.BOTTOM).create());
		previewPanel.addListener(SWT.Paint, e -> {
			final Rectangle size = previewPanel.getClientArea();
			final Color oldColor = new Color(e.display, rgb);
			final Color newColor = new Color(e.display, getRGB());

			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
			e.gc.drawRectangle(0, 0, size.width - 1, size.height - 3);
			e.gc.setBackground(newColor);
			e.gc.fillRectangle(1, 1, size.width / 2, size.height - 4);
			e.gc.setBackground(oldColor);
			e.gc.fillRectangle(size.width / 2, 1, (size.width - 2) / 2, size.height - 4);

			oldColor.dispose();
			newColor.dispose();
		});
	}

	private static Spinner createRGBSpinner(final Composite parent, final String label) {
		final Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(label + ":"); //$NON-NLS-1$
		lbl.setLayoutData(GridDataFactory.swtDefaults().indent(8, 0).create());

		final Spinner spi = new Spinner(parent, SWT.RIGHT | SWT.BORDER);
		spi.setMaximum(255);
		spi.setEnabled(false);

		return spi;
	}

	protected final ColorResolution getColorResolution() {
		return colorResolution;
	}

	protected final void setNewRGB(final RGB rgb) {
		newRGB = rgb;

		if (showInfo) {
			spiRed.setSelection(rgb.red);
			spiGreen.setSelection(rgb.green);
			spiBlue.setSelection(rgb.blue);
			previewPanel.redraw();
		}
	}

	@Override
	public void setRGB(final RGB rgb) {
		this.rgb = rgb;
		setNewRGB(rgb);
	}

	@Override
	public final RGB getRGB() {
		return newRGB;
	}
}
