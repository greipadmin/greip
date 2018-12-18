/**
 * Copyright (c) 2018 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **/
package org.greip.decorator;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
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
public final class CountDecorator extends AbstractDecorator {

	private final Map<Integer, Color> treshholds = new TreeMap<>((o1, o2) -> o2.intValue() - o1.intValue());

	private int value;
	private Font font;
	private Color circleColor;
	private Color foreground;
	private int outerDiameter = 50;
	private int innerDiameter = 30;
	private boolean animate = true;

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
		final int lineWidth = (outerDiameter - innerDiameter) / 2;
		final Color background = gc.getBackground();
		final int antialias = gc.getAntialias();

		gc.setAntialias(SWT.ON);
		gc.setBackground(getTreshholdColor());
		gc.fillOval(x + offset, y + offset, outerDiameter - offset * 2, outerDiameter - offset * 2);

		if (animate) {
			doAnimate();
		}

		if (offset == 0) {
			gc.setBackground(background);
			gc.fillOval(x + lineWidth, y + lineWidth, innerDiameter, innerDiameter);

			Util.whenNotNull(font, gc::setFont);

			final String text = Integer.toString(value);
			final Point p = gc.textExtent(text, SWT.NONE);

			gc.setForeground(getForeground());
			gc.drawText(text, x + (getSize().x - p.x + 1) / 2, y + (getSize().y - p.y + 1) / 2, true);
		}

		gc.setAntialias(antialias);
	}

	/**
	 * Returns the color to paint the circle. Default is
	 * {@link SWT#COLOR_DARK_GRAY}.
	 *
	 * @return the color
	 */
	public Color getCircleColor() {
		return Util.nvl(circleColor, getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
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
	 * Gets the current font.
	 *
	 * @return the font or <code>null</code> if no font is set.
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Sets the font that the decorator will use to paint text.
	 *
	 * @param font
	 *        the new font (or null, to sets the default font)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
	 *            </ul>
	 */
	public void setFont(final Font font) {
		this.font = Util.checkResource(font, true);
		redraw();
	}

	/**
	 * Gets the foregound color. The color is used for drawing text.
	 *
	 * @return the foreground color
	 */
	public Color getForeground() {
		return Util.nvl(foreground, getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	/**
	 * Sets the foreground color that the decorator will use to paint text.
	 *
	 * @param font
	 *        the new foreground color
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setForeground(final Color foreground) {
		this.foreground = Util.checkResource(foreground, true);
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
	 * Sets the inner diameter of the ring. When the inner diameter is zero a
	 * circle is drawn.
	 *
	 * @param innerDiameter
	 *        the inner diameter in pixels
	 */
	public void setInnerDiameter(final int innerDiameter) {
		if (innerDiameter < 0 || innerDiameter >= outerDiameter) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
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
	 */
	public void setOuterDiameter(final int outerDiameter) {
		if (outerDiameter <= innerDiameter) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.outerDiameter = outerDiameter;
		redraw();
	}

	/**
	 * Gets the current animation behaviour.
	 *
	 * @return the animation behaviour
	 */
	public boolean isShowAnimation() {
		return animate;
	}

	/**
	 * Enables or disables the animation on value change.
	 *
	 * @param animate
	 *        the new animation behaviour
	 */
	public void setShowAnimation(final boolean animate) {
		this.animate = animate;
	}

	@Override
	public Point getSize() {
		return new Point(outerDiameter, outerDiameter);
	}

	/**
	 * Returns the current unmodifiable treshhold map.
	 *
	 * @return the treshhold map
	 */
	public Map<Integer, Color> getTreshholdColors() {
		return Collections.unmodifiableMap(treshholds);
	}

	/**
	 * Sets a new treshhold map.
	 *
	 * @param treshholdMap
	 *        the new treshhold map or null to clear the map
	 */
	public void setTreshholdColors(final Map<Integer, Color> treshholdMap) {
		treshholds.clear();

		if (treshholdMap != null) {
			treshholdMap.forEach((v, c) -> treshholds.put(v, Util.checkResource(c, false)));
		}

		redraw();
	}

	/**
	 * Gets the current displayed value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets the new value.
	 *
	 * @param value
	 *        the value
	 */
	public void setValue(final int value) {
		this.value = value;
		this.offset = animate ? outerDiameter / 2 : 0;

		redraw();
	}

	private void doAnimate() {
		getDisplay().timerExec(40, () -> {
			if (offset > 0) {
				offset = Math.max(0, offset - 4);
				redraw();
			}
		});
	}

	private Color getTreshholdColor() {
		for (final Entry<Integer, Color> entry : treshholds.entrySet()) {
			if (value >= entry.getKey().intValue()) {
				return entry.getValue();
			}
		}

		return getCircleColor();
	}
}
