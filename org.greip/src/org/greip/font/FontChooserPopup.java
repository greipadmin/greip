/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.font;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.greip.color.IColorChooserFactory;
import org.greip.common.Popup;

class FontChooserPopup extends Popup {

	private final FontChooser fontChooser;

	public FontChooserPopup(final Control control, final IColorChooserFactory colorChooserFactory) {
		super(control);

		final Shell shell = (Shell) getParent();
		final Cursor cursor = shell.getCursor();
		shell.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		setLayout(new FillLayout());

		fontChooser = new FontChooser(this, colorChooserFactory);
		fontChooser.addListener(SWT.Selection, e -> close());

		shell.setCursor(cursor);
	}

	public FontData getFontData() {
		return fontChooser.getFontData();
	}

	public void setFontData(final FontData fontData) {
		fontChooser.setFontData(fontData);
	}

	public void setFontColor(final RGB fontColor) {
		fontChooser.setFontColor(fontColor);
	}

	public RGB getFontColor() {
		return fontChooser.getFontColor();
	}
}