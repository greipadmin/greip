/**
 * Copyright (c) 2018 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **/
package org.greip.decorator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.greip.common.Util;

/**
 * Instances of this class represents a decorator that paints an animated circle
 * and a value.
 *
 * @author Thomas Lorbeer
 */
public final class CountDecorator extends AbstractValueDecorator {

	private Color circleColor;
	private int outerDiameter = 50;
	private int innerDiameter = 30;
	private int offset;

	/**
	 * Creates a new instance of the decorator.
	 *
	 * @param parent
	 *        the parent control, <code>null</code> not allowed.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            <li>ERROR_WIDGET_DISPOSED - if the parent has been
	 *            disposed</li>
	 *            </ul>
	 */
	public CountDecorator(final Control parent) {
		super(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.greip.decorator.IDecorator#doPaint(org.eclipse.swt.graphics.GC,
	 * int, int)
	 */
	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		final Color bgColor = gc.getBackground();

		gc.setBackground(getTreshholdColor(getCircleColor()));
		gc.fillOval(x + offset, y + offset, outerDiameter - offset * 2, outerDiameter - offset * 2);

		if (offset == 0) {
			final Point size = getSize();
			final Point textSize = getTextSize();

			gc.setBackground(bgColor);
			gc.fillOval(x + (outerDiameter - innerDiameter) / 2, y + (outerDiameter - innerDiameter) / 2, innerDiameter, innerDiameter);

			paintValue(gc, x + (size.x - textSize.x) / 2, y + (size.y - textSize.y) / 2);

		} else {
			offset = Math.max(0, offset - 4);
			redrawAsync();
		}
	}

	/**
	 * Returns the color to paint the circle. Default is
	 * {@link SWT#COLOR_DARK_GRAY}.
	 *
	 * @return the color
	 */
	public Color getCircleColor() {
		return Util.nvl(circleColor, getParent().getForeground());
	}

	/**
	 * Sets the color to paint the circle.
	 *
	 * @param color
	 *        the color, <code>null</code> clears the color an returns to default
	 *        {@link SWT#COLOR_DARK_GRAY}.
	 */
	public void setCircleColor(final Color color) {
		this.circleColor = Util.checkResource(color, true);
		redraw();
	}

	/**
	 * Returns the inner diameter of the ring.
	 *
	 * @return the inner diameter in pixels
	 */
	public int getInnerDiameter() {
		return innerDiameter;
	}

	/**
	 * Sets the inner diameter of the ring. When the inner diameter is zero and
	 * the outer diameter is greater than zero a circle is drawn.
	 *
	 * @param innerDiameter
	 *        the inner diameter in pixels
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if inner diameter less then
	 *            zero</li>
	 *            </ul>
	 */
	public void setInnerDiameter(final int innerDiameter) {
		if (innerDiameter < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.innerDiameter = innerDiameter;
		redraw();
	}

	/**
	 * Returns the outer diameter of the ring.
	 *
	 * @return the outer diameter in pixels
	 */
	public int getOuterDiameter() {
		return outerDiameter;
	}

	/**
	 * Sets the outer diameter of the ring.
	 *
	 * @param outerDiameter
	 *        the outer diameter in pixels
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if outer diameter less then
	 *            zero</li>
	 *            </ul>
	 */
	public void setOuterDiameter(final int outerDiameter) {
		if (outerDiameter < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.outerDiameter = outerDiameter;
		redraw();
	}

	@Override
	public Point getSize() {
		return new Point(outerDiameter, outerDiameter);
	}

	@Override
	protected void initAnimation() {
		this.offset = isShowAnimation() ? outerDiameter / 2 : 0;
	}
}
