/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.calculator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.greip.tile.TextSection;
import org.greip.tile.Tile;

public class FnButton extends Composite {

	private final Tile btn;

	public FnButton(final Composite parent, final String caption, final Color background) {
		super(parent, SWT.DOUBLE_BUFFERED);

		final FillLayout lf = new FillLayout();
		lf.marginHeight = 2;
		lf.marginWidth = 2;

		setLayout(lf);

		btn = new Tile(this, SWT.NONE);
		btn.addTextSection(new TextSection(caption, SWT.CENTER, null, null, false));
		btn.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		btn.setBackground(background);
		btn.setBorderColor(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		btn.setBorderWidth(1);
		btn.setCornerRadius(1);
		btn.setMargins(0, 0);
		btn.setHighlight(true);

		super.addListener(SWT.Paint, e -> {
			final Point size = getSize();

			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
			e.gc.drawRoundRectangle(1, 1, size.x - 3, size.y - 3, 4, 4);
		});
	}

	@Override
	public void addListener(final int eventType, final Listener listener) {
		btn.addListener(eventType, listener);
	}

	@Override
	public void removeListener(final int eventType, final Listener listener) {
		btn.removeListener(eventType, listener);
	}

	@Override
	public void setFont(final Font font) {
		btn.setFont(font);
	}

	@Override
	public Font getFont() {
		return btn.getFont();
	}
}
