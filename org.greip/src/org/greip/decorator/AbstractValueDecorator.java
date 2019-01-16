/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.greip.common.Util;

public abstract class AbstractValueDecorator extends AbstractDecorator {

	private static final int TEXT_SPACING = 2;

	private double value;
	private boolean animate = true;
	private NumberFormat numberFormat = new DecimalFormat("#0");
	private Font font;
	private final Map<Double, Color> treshholdMap = new TreeMap<>((o1, o2) -> -o1.compareTo(o2));
	private Color valueColor;
	private String unit;
	private int unitAlignment = SWT.RIGHT;
	private Font unitFont;

	protected AbstractValueDecorator(final Control parent) {
		super(parent);
	}

	/**
	 * Gets the current displayed value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the new value.
	 *
	 * @param value
	 *        the value
	 */
	public void setValue(final double value) {
		this.value = value;
		initAnimation();
		redraw();
	}

	protected double getValueToDisplay() {
		return value;
	}

	protected String getValueAsString() {
		return numberFormat.format(getValueToDisplay());
	}

	abstract protected void initAnimation();

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

	/**
	 * Sets the format for number formatting. The default format is "#0".
	 *
	 * @param format
	 *        The new format.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the format is null</li>
	 *            </ul>
	 */
	public void setNumberFormat(final NumberFormat numberFormat) {
		if (numberFormat == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.numberFormat = numberFormat;
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
	 * Gets the current color treshhold map.
	 *
	 * @return the current map
	 */
	public Map<Double, Color> getTreshholdColors() {
		return Collections.unmodifiableMap(treshholdMap);
	}

	/**
	 * Sets a new treshhold map.
	 *
	 * @param treshholdMap
	 *        the new treshhold map
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the map is null</li>
	 *            </ul>
	 */
	public void setTreshholdColors(final Map<Double, Color> treshholdMap) {
		if (treshholdMap == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.treshholdMap.clear();
		this.treshholdMap.putAll(treshholdMap);
		redraw();
	}

	protected Color getTreshholdColor(final Color defaultColor) {
		for (final Entry<Double, Color> entry : treshholdMap.entrySet()) {
			if (getValue() >= entry.getKey().doubleValue()) {
				return entry.getValue();
			}
		}

		return defaultColor;
	}

	/**
	 * Gets the value color. The color is used for drawing the value.
	 *
	 * @return The foreground color or <code>null</code> if no color defined.
	 */
	public Color getValueColor() {
		return valueColor;
	}

	/**
	 * Sets the foreground color that the decorator will use to paint value.
	 *
	 * @param color
	 *        the new value color
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setValueColor(final Color color) {
		this.valueColor = Util.checkResource(color, true);
		redraw();
	}

	/**
	 * Gets the unit text.
	 *
	 * @return the unit text
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Sets the unit text.
	 *
	 * @param unit
	 *        The new unit text or <code>null</code>.
	 */
	public void setUnit(final String unit) {
		this.unit = unit;
		redraw();
	}

	/**
	 * Returns the alignment of the unit.
	 *
	 * @return the alignment
	 */
	public int getUnitAlignment() {
		return unitAlignment;
	}

	/**
	 * Sets the alignment of the unit, which will be one of the constants
	 * <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>, <code>SWT.TOP</code> or
	 * <code>SWT.BOTTOM</code> and the bitwise OR'ing of <code>SWT.LEFT</code> or
	 * <code>SWT.RIGHT</code> and <code>SWT.TOP</code>.
	 *
	 * @param unitAlignment
	 *        the alignment
	 */
	public void setUnitAlignment(final int unitAlignment) {
		this.unitAlignment = unitAlignment;
		redraw();
	}

	/**
	 * Gets the current unit font or <code>null</code> if no font defined.
	 *
	 * @return the unit font
	 */
	public Font getUnitFont() {
		return unitFont;
	}

	/**
	 * Sets the font that will use to paint the unit text. If no font defined a
	 * font will be used with the half size of the font returned by
	 * {@link #getFont()}.
	 *
	 * @param unitFont
	 *        the unit font or <code>null</code> if no font defined.
	 */
	public void setUnitFont(final Font unitFont) {
		this.unitFont = Util.checkResource(unitFont, true);
		redraw();
	}

	protected Point getTextSize() {
		return Util.withResource(new GC(getDisplay()), gc -> {
			Util.whenNotNull(getFont(), gc::setFont);
			final Point size = gc.textExtent(getValueAsString());

			if (unit != null && !unit.isEmpty()) {
				final Point unitSize = Util.withFont(gc, createUnitFont(), font -> {
					gc.setFont(font);
					return gc.textExtent(unit);
				});

				if (Util.whenAnySet(unitAlignment, SWT.LEFT, SWT.RIGHT)) {
					size.x += unitSize.x + TEXT_SPACING;
				} else {
					size.y += unitSize.y;
				}
			}
			return size;
		});
	}

	private Font createUnitFont() {
		final FontData[] fontData;

		if (getUnitFont() != null) {
			fontData = getUnitFont().getFontData();
		} else {
			fontData = getFont().getFontData();
			fontData[0].setHeight(Math.min(10, Math.max(2, (int) (fontData[0].getHeight() * 0.5))));
			fontData[0].setStyle(SWT.NONE);
		}

		return new Font(getDisplay(), fontData[0]);
	}

	protected void paintValue(final GC gc, final int x, final int y) {
		final Point textSize = getTextSize();

		final FontMetrics valueMetrics = applyFont(gc, getFont());
		final Point valueSize = gc.textExtent(getValueAsString());

		gc.setForeground(Util.nvl(valueColor, getTreshholdColor(getParent().getForeground())));

		final Point textPos = new Point(x, y);

		if (Util.whenAnySet(unitAlignment, SWT.LEFT)) {
			textPos.x += textSize.x - valueSize.x;
		} else if (unitAlignment == SWT.TOP) {
			textPos.y += textSize.y - valueSize.y;
		}

		gc.drawText(getValueAsString(), textPos.x, textPos.y, true);

		if (unit != null && !unit.isEmpty()) {
			Util.withResource(createUnitFont(), font -> {
				final FontMetrics unitMetrics = applyFont(gc, font);
				final Point unitSize = gc.textExtent(unit);
				final Point unitPos = new Point(x, y);

				if (unitAlignment == SWT.BOTTOM) {
					unitPos.y += valueSize.y;
				}

				if (Util.whenAnySet(unitAlignment, SWT.LEFT, SWT.RIGHT)) {
					final int valueBaseline = valueMetrics.getAscent() + valueMetrics.getLeading();
					final int unitBaseline = unitMetrics.getAscent() + unitMetrics.getLeading();

					if (Util.whenAnySet(unitAlignment, SWT.TOP)) {
						unitPos.y += textSize.y - unitSize.y - (valueBaseline - unitBaseline);
					} else {
						unitPos.y += valueBaseline - unitBaseline;
					}
				} else if (Util.whenAnySet(unitAlignment, SWT.BOTTOM, SWT.TOP)) {
					unitPos.x += (textSize.x - unitSize.x) / 2;
				}

				if (Util.whenAnySet(unitAlignment, SWT.RIGHT)) {
					unitPos.x += textSize.x - unitSize.x;
				}

				gc.drawText(unit, unitPos.x, unitPos.y, true);
			});
		}
	}

	private static FontMetrics applyFont(final GC gc, final Font font) {
		Util.whenNotNull(font, gc::setFont);
		return gc.getFontMetrics();
	}

	protected void redrawAsync() {
		getDisplay().timerExec(10, this::redraw);
	}
}
