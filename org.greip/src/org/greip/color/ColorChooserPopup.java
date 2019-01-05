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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.greip.common.Popup;
import org.greip.common.Util;

class ColorChooserPopup extends Popup {

	private AbstractColorChooser colorChooser;
	private RGB rgb;

	public ColorChooserPopup(final Control control) {
		super(control);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}

	public final void createContent(final IColorChooserFactory factory) {
		colorChooser = factory.create(this);
		colorChooser.addListener(SWT.Selection, e -> propagateNewRGB());
		colorChooser.addListener(SWT.Traverse, e -> Util.when(e.detail == SWT.TRAVERSE_RETURN, this::propagateNewRGB));

		final FillLayout layout = getLayout();
		layout.marginHeight = colorChooser.getMargins().y;
		layout.marginWidth = colorChooser.getMargins().x;
	}

	public RGB getRGB() {
		return rgb;
	}

	public void setRGB(final RGB rgb) {
		colorChooser.setRGB(rgb);
	}

	private void propagateNewRGB() {
		rgb = colorChooser.getRGB();
		close();
	}
}
