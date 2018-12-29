/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.font;

import java.awt.GraphicsEnvironment;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.greip.common.Util;

class FontTable extends Table {

	private static final int FONT_ITEM_HEIGHT = 20;
	private static final int DEFAULT_COLUMN_WIDTH = 150;
	private final String[] availableFonts;

	public FontTable(final Composite parent) {
		super(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		new TableColumn(this, SWT.LEFT).setWidth(DEFAULT_COLUMN_WIDTH);

		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		availableFonts = ge.getAvailableFontFamilyNames();

		addListener(SWT.MeasureItem, e -> {
			e.width = getColumn(0).getWidth();
			e.height = getItemHeight();
		});

		addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
		addListener(SWT.SetData, e -> ((TableItem) e.item).setText(availableFonts[e.index]));

		addListener(SWT.PaintItem, e -> {
			final String fontName = ((TableItem) e.item).getText();
			Util.withResource(applyFont(fontName, e.gc), font -> {
				final Point p = e.gc.textExtent(fontName, SWT.DRAW_TRANSPARENT);
				e.gc.drawText(fontName, e.x, e.y + (getItemHeight() - p.y) / 2, true);
			});
		});

		setItemCount(availableFonts.length);
		setToolTipText(""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Table#getItemHeight()
	 */
	@Override
	public int getItemHeight() {
		return FONT_ITEM_HEIGHT;
	}

	public void setColumnWidth(final int width) {
		getColumn(0).setWidth(width);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		return super.computeSize(getColumn(0).getWidth(), hHint, changed);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Table#checkSubclass()
	 */
	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	private static Font applyFont(final String fontName, final GC gc) {
		int fontHeight = 9;
		FontMetrics fm = null;
		Font font = null;

		do {
			Util.whenNotNull(font, Font::dispose);
			font = new Font(gc.getDevice(), fontName, fontHeight++, SWT.NONE);
			gc.setFont(font);
			fm = gc.getFontMetrics();
		} while (fm.getAscent() + fm.getDescent() < 12 && fontHeight < 20);

		return font;
	}
}
