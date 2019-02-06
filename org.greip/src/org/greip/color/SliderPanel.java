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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.greip.common.Util;

class SliderPanel extends Composite {

	private static final int DEFAULT_WIDTH = 180;

	private final ColorSlider[] sliders;

	public SliderPanel(final AbstractColorChooser parent, final ColorResolution colorResolution, final String... titles) {
		super(parent, SWT.NO_FOCUS);

		final int width = (int) (Util.getZoom(getDisplay()) * DEFAULT_WIDTH);
		sliders = new ColorSlider[titles.length];

		GridLayoutFactory.fillDefaults().spacing(0, 5).applyTo(this);

		for (int i = 0; i < titles.length; i++) {
			final Label label = new Label(this, SWT.CENTER);
			label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, true));
			label.setText(titles[i]);
			Util.applyDerivedFont(label, -1, SWT.ITALIC);

			final ColorSlider slider = new ColorSlider(this, colorResolution);
			slider.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).hint(width, SWT.DEFAULT).create());
			slider.addListener(SWT.MouseDoubleClick, e -> {
				parent.setRGB(parent.getRGB());
				parent.notifyListeners(SWT.Selection, new Event());
			});

			sliders[i] = slider;
		}
	}

	public ColorSlider[] getSliders() {
		return sliders;
	}
}
