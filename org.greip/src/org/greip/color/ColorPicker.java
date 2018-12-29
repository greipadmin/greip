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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.greip.common.Util;

public final class ColorPicker extends AbstractColorChooser {

	private static final int ITEM_HEIGHT = 20;
	private static final int ITEM_WIDTH = 80;
	private static final int MAX_ITEMS = 10;

	public static class Factory implements IColorChooserFactory {

		private final RGB[] colors;

		public Factory(final RGB... colors) {
			this.colors = colors;
		}

		@Override
		public AbstractColorChooser create(final Composite parent) {
			return new ColorPicker(parent, colors);
		}
	}

	private RGB[] rgbs;
	private Table table;

	public ColorPicker(final Composite parent, final RGB... colors) {
		super(parent, null, false, false);

		if (colors != null && colors.length > 0) {
			this.rgbs = colors;
		} else {
			this.rgbs = new DefaultColorList().toArray(new RGB[0]);
		}

		createTableItems(table);
		table.getParent().setLayoutData(new GridData(ITEM_WIDTH + getScrollBarWidth(), Math.min(MAX_ITEMS, rgbs.length) * ITEM_HEIGHT));

		setRGB(rgbs[0]);
	}

	@Override
	public Point getMargins() {
		return new Point(0, 0);
	}

	@Override
	protected Composite createColorChooserPanel() {
		final Composite panel = new Composite(this, getShell() instanceof ColorChooserPopup ? SWT.NONE : SWT.BORDER);
		panel.setLayout(new FillLayout());

		table = new Table(panel, SWT.FULL_SELECTION | SWT.H_SCROLL);
		new TableColumn(table, SWT.NONE).setWidth(ITEM_WIDTH);

		table.addListener(SWT.Activate, e -> table.showSelection());

		table.addListener(SWT.MeasureItem, e -> {
			e.width = ITEM_WIDTH;
			e.height = ITEM_HEIGHT;
		});

		table.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);

		table.addListener(SWT.PaintItem, e -> {
			final RGB rgb = (RGB) e.item.getData();

			Util.withResource(new Color(e.display, rgb), color -> {
				final Rectangle rect = new Rectangle(10, e.y + 5, ITEM_WIDTH - 21, e.height - 11);

				e.gc.setBackground(color);
				e.gc.fillRectangle(rect);
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				e.gc.drawRectangle(rect);
			});
		});

		table.addListener(SWT.DefaultSelection, e -> {
			setRGB((RGB) e.item.getData());
			notifyListeners(SWT.Selection, new Event());
		});

		table.addListener(SWT.Selection, e -> setNewRGB((RGB) e.item.getData()));
		table.setToolTipText(""); //$NON-NLS-1$

		return panel;
	}

	private void createTableItems(final Table table) {
		for (final RGB rgb : rgbs) {
			new TableItem(table, SWT.NONE).setData(rgb);
		}
	}

	private int getScrollBarWidth() {
		final ScrollBar vBar = table.getVerticalBar();
		return rgbs.length > MAX_ITEMS ? vBar.getSize().x : 0;
	}

	@Override
	public void setRGB(final RGB rgb) {
		super.setRGB(rgb);
		table.setSelection(Util.getSimilarColor(rgbs, rgb));
	}
}
