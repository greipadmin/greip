/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class DropDownButton extends Button {

	private boolean dropDownArrowVisible;
	private String text;

	public DropDownButton(final Composite parent, final int style) {
		super(parent, style | SWT.PUSH & ~SWT.DROP_DOWN);

		setDropDownArrowVisible((style & SWT.DROP_DOWN) != 0);

		addListener(SWT.Paint, e -> {
			if (isDropDownArrowVisible()) {
				final Point size = getSize();
				final int x = size.x - 10;
				final int y = size.y / 2;

				e.gc.setBackground(e.display.getSystemColor(isEnabled() ? SWT.COLOR_BLACK : SWT.COLOR_DARK_GRAY));
				e.gc.fillPolygon(new int[] { x, y + 3, x - 3, y - 1, x + 4, y - 1 });
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	protected Point getTextSize() {
		final Point size = Util.getTextSize(this, super.getText(), SWT.DRAW_MNEMONIC);

		if (size.x > 0) {
			size.x += 6;
		}

		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final Point size = super.computeSize(wHint, hHint, changed);
		return new Point(Math.max(wHint, getTextSize().x), size.y);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Button#setText(java.lang.String)
	 */
	@Override
	public void setText(final String text) {
		super.setText(text + (isDropDownArrowVisible() ? "    " : ""));
		this.text = text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Button#getText()
	 */
	@Override
	public String getText() {
		return Util.nvl(text, "");
	}

	public void setDropDownArrowVisible(final boolean visible) {
		dropDownArrowVisible = visible;
		setText(getText());
		redraw();
	}

	public boolean isDropDownArrowVisible() {
		return dropDownArrowVisible;
	}
}
