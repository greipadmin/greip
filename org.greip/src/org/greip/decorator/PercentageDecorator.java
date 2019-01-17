package org.greip.decorator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.greip.common.Util;

/**
 * Instances of this class represents a decorator that paints an animated circle
 * and a percentual value.
 *
 * @author Thomas Lorbeer
 */
public final class PercentageDecorator extends AbstractNumberDecorator {

	private double curValue;
	private double maxValue;
	private double increment;
	private Color circleBackground;
	private Color circleForeground;
	private int outerDiameter = 50;
	private int innerDiameter = 40;
	private CircleType circleType = CircleType.Circle;

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
	public PercentageDecorator(final Control parent) {
		super(parent);

		setUnit("%");
		setMaxValue(100.0d);
	}

	/**
	 * Returns the current circle background color.
	 *
	 * @return the color or <code>null</code> if no color defined
	 */
	public Color getCircleBackground() {
		return circleBackground;
	}

	/**
	 * Sets the new circle background color.
	 *
	 * @param color
	 *        The new color or <code>null</code> to use default color.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setCircleBackground(final Color color) {
		this.circleBackground = Util.checkResource(color, true);
		redraw();
	}

	/**
	 * Returns the current circle foreground color.
	 *
	 * @return the color or <code>null</code> if no color defined
	 */
	public Color getCircleForeground() {
		return circleForeground;
	}

	/**
	 * Sets the new circle foreground color.
	 *
	 * @param color
	 *        The new color or <code>null</code> to use default color.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setCircleForeground(final Color color) {
		this.circleForeground = Util.checkResource(color, true);
		redraw();
	}

	/**
	 * Gets the current circle type.
	 *
	 * @return the circle type
	 */
	public CircleType getCircleType() {
		return circleType;
	}

	/**
	 * Sets the circle Type.
	 *
	 * @param circleType
	 *        the new circle type
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the circle type is
	 *            <code>null</code></li>
	 *            </ul>
	 */
	public void setCircleType(final CircleType circleType) {
		if (circleType == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.circleType = circleType;
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

	/**
	 * Returns the maximum value which the decorator will allow. The default
	 * maximum is 100.
	 *
	 * @return the maximum
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Sets the maximum value that the decorator will allow. If the new maximum
	 * is applied then the current value will be adjusted if necessary to fall
	 * within its new range. The default maximum is 100.
	 *
	 * @param maxValue
	 *        the new maximum, which must be greater or equal than zero.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if maximum value is less then
	 *            zero</li>
	 *            </ul>
	 */
	public void setMaxValue(final double maxValue) {
		if (maxValue < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		this.maxValue = maxValue;
		this.increment = maxValue / 30;

		if (getValue() > maxValue) {
			setValue(maxValue);
		} else {
			redraw();
		}
	}

	/**
	 * Sets the new value.
	 *
	 * @param value
	 *        the value
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if outer diameter less than zero
	 *            or greater than {@link #getMaxValue()}</li>
	 *            </ul>
	 */
	@Override
	public void setValue(final double value) {
		if (value < 0 || value > getMaxValue()) SWT.error(SWT.ERROR_INVALID_RANGE);
		super.setValue(value);
	}

	@Override
	protected double getValueToDisplay() {
		return getValue() * 100 / getMaxValue();
	}

	@Override
	protected void initAnimation() {
		curValue = isShowAnimation() ? 0d : getValue();
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		paintCircle(gc, x, y);

		if (curValue == getValue()) {
			final Point size = getSize();
			final Point textSize = getTextSize();
			final int offsetY;

			if (circleType == CircleType.Circle) {
				offsetY = (size.y - textSize.y) / 2;
			} else {
				offsetY = size.y - textSize.y;
			}

			paintValue(gc, x + (size.x - textSize.x) / 2, y + offsetY);

		} else {
			curValue = Math.min(curValue + increment, getValue());
			redrawAsync();
		}
	}

	@Override
	public Point getSize() {
		final Point textSize = getTextSize();

		if (circleType == CircleType.Circle) {
			return new Point(Math.max(outerDiameter, textSize.x), Math.max(outerDiameter, textSize.y));
		}

		final int height = (int) (outerDiameter / circleType.heightQuotient) + textSize.y / 2;
		return new Point(Math.max(outerDiameter, textSize.x), Math.max(height, textSize.y));
	}

	private void paintCircle(final GC gc, final int x, final int y) {
		final int curAngle = (int) Math.round(curValue * circleType.angle / maxValue);
		final int lineWidth = (outerDiameter - innerDiameter) / 2;

		gc.setForeground(Util.nvl(getTreshholdColor(getCircleForeground()), getParent().getForeground()));
		drawArc(gc, x, y, outerDiameter, lineWidth, circleType.angle - curAngle + circleType.offset, curAngle);
		gc.setForeground(Util.nvl(getCircleBackground(), getDisplay().getSystemColor(SWT.COLOR_GRAY)));
		drawArc(gc, x, y, outerDiameter, lineWidth, circleType.offset, circleType.angle - curAngle);
	}

	private static void drawArc(final GC gc, final int x, final int y, final int diameter, final int lineWidth, final int startAngle,
			final int arcAngle) {

		gc.setLineWidth(2);
		for (int i = 1; i < lineWidth; i++) {
			gc.drawArc(x + i, y + i, diameter - i - i, diameter - i - i, startAngle, arcAngle);
		}
		gc.setLineWidth(1);
	}
}
