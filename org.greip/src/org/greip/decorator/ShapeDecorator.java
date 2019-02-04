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
 * Instances of this class represents a decorator that paints an animated shape
 * and a formatted value.
 *
 * @param <T>
 *        the type of value
 *
 * @author Thomas Lorbeer
 */
public final class ShapeDecorator<T extends Comparable<T>> extends AbstractValueDecorator<T> {

	private Color foreground;
	private Color background;
	private Point shapeSize = new Point(50, 50);
	private int lineWidth = 10;
	private Point cornerArc = new Point(50, 50);

	private final AnimationContext ctx = new AnimationContext();

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
	public ShapeDecorator(final Control parent) {
		super(parent);
	}

	/**
	 * Returns the color with which the shape is filled.
	 *
	 * @return the color
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Ses the color with which the shape is filled. If no color is defined, the
	 * shapes background is transparent.
	 *
	 * @param color
	 *        the color
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setBackground(final Color color) {
		this.background = Util.checkResource(color, true);
		redraw();
	}

	/**
	 * Defines how the corners of the figure should be rounded off.
	 * <p>
	 * The resulting shape is a:
	 * <ul>
	 * <li><b>circle</b>, if both parameters and both parameters of
	 * {@link #setShapeSize(int, int)} have the same value.</li>
	 * <li><b>oval</b>, when <code>arcWidth</code> equals to the first parameter
	 * of {@link #setShapeSize(int, int)} and <code>arcHeight</code> equals to
	 * the second parameter.</li>
	 * <li><b>rectangle</b>, when <code>arcWidth</code> and
	 * <code>arcHeight</code> are equal to zero.</li>
	 * </ul>
	 * <p>
	 * All other values ​​give a rectangle with rounded corners.
	 * </p>
	 *
	 * @param arcWidth
	 *        the width of the arc
	 * @param arcHeight
	 *        the height of the arc
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the arcWidth or arcHeight less
	 *            then zero</li>
	 *            </ul>
	 */
	public void setCornerArc(final int arcWidth, final int arcHeight) {
		if (arcWidth < 0 || arcHeight < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.cornerArc = new Point(arcWidth, arcHeight);
		redraw();
	}

	/**
	 * Returns how the corners of the figure should be rounded off.
	 *
	 * @return the arc
	 */
	public Point getCornerArc() {
		return cornerArc;
	}

	/**
	 * Returns the color to paint the shape.
	 *
	 * @return The color or <code>null</code> if no color defined.
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Sets the color to paint the shape.
	 *
	 * @param color
	 *        the color, <code>null</code> clears the color
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setForeground(final Color color) {
		this.foreground = Util.checkResource(color, true);
		redraw();
	}

	/**
	 * Defines the line width with which the shape is drawn.
	 *
	 * @param lineWidth
	 *        the line width
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the line width less then
	 *            zero</li>
	 *            </ul>
	 */
	public void setLineWidth(final int lineWidth) {
		if (lineWidth < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.lineWidth = lineWidth;
		redraw();
	}

	/**
	 * Returns the line width with which the shape is drawn.
	 *
	 * @return the line width
	 */
	public int getLineWidth() {
		return lineWidth;
	}

	/**
	 * Defines the with and height of the shape.
	 *
	 * @param width
	 *        the width
	 * @param height
	 *        the height
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the width or height less then
	 *            one</li>
	 *            </ul>
	 */
	public void setShapeSize(final int width, final int height) {
		if (width < 1 || height < 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.shapeSize = new Point(width, height);
		redraw();
	}

	/**
	 * Returns the width of the shape.
	 *
	 * @return the size of the shape
	 */
	public Point getShapeSize() {
		return shapeSize;
	}

	@Override
	public Point getSize() {
		final Point valueSize = getValueSize();
		final Point shapeSize = getShapeSize();
		return new Point(Math.max(shapeSize.x, valueSize.x), Math.max(shapeSize.y, valueSize.y));
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		final Point size = getSize();
		final int posX = x + (size.x - shapeSize.x) / 2;
		final int posY = y + (size.y - shapeSize.y) / 2;

		final Color fg = Util.nvl(foreground, getParent().getForeground());

		gc.setAntialias(SWT.ON);

		if (ctx.isActive()) {
			final int steps = ctx.getStepCount();
			final int step = ctx.getStep();
			final int sizeX = Math.min(shapeSize.x, shapeSize.x / steps * step);
			final int sizeY = Math.min(shapeSize.y, shapeSize.y / steps * step);
			final int arcWidth = Math.min(cornerArc.x, cornerArc.x / steps * step);
			final int arcHeight = Math.min(cornerArc.y, cornerArc.y / steps * step);

			gc.setBackground(fg);
			gc.fillRoundRectangle(posX + (shapeSize.x - sizeX) / 2, posY + (shapeSize.y - sizeY) / 2, sizeX, sizeY, arcWidth, arcHeight);

		} else {
			if (background != null) {
				gc.setBackground(background);
				gc.fillRoundRectangle(posX + 1, posY + 1, shapeSize.x - 2, shapeSize.y - 2, Math.max(0, cornerArc.x), Math.max(0, cornerArc.y));
			}

			if (lineWidth > 0) {
				gc.setForeground(fg);

				for (int i = 0; i < lineWidth; i++) {
					gc.setLineWidth(i == 0 || i == lineWidth - 1 ? 1 : 2);
					gc.drawRoundRectangle(posX + i, posY + i, shapeSize.x - i * 2 - 1, shapeSize.y - i * 2 - 1,
							Math.max(0, (int) (cornerArc.x - i * 1.5f)), Math.max(0, (int) (cornerArc.y - i * 1.5f)));
				}
			}

			final Point valueSize = getValueSize();
			paintValue(gc, x + (size.x - valueSize.x) / 2, y + (size.y - valueSize.y) / 2);
		}
	}

	@Override
	protected AnimationContext getAnimationContext() {
		return ctx;
	}
}
