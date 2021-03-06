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
public final class PercentageDecorator extends AbstractValueDecorator<Double> {

	private static final Double ZERO = Double.valueOf(0d);

	private Double maxValue = Double.valueOf(100.0d);
	private Double minValue = ZERO;
	private Color circleBackground;
	private Color circleForeground;
	private int outerDiameter = 50;
	private int innerDiameter = 40;
	private CircleType circleType = CircleType.Circle;

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
	public PercentageDecorator(final Control parent) {
		super(parent);

		setUnit("%");
		setValue(ZERO);
	}

	@Override
	protected AnimationContext getAnimationContext() {
		return ctx;
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
	public void setBackground(final Color color) {
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
	public void setForeground(final Color color) {
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
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * Sets the maximum value that the decorator will allow. If the new maximum
	 * is applied then the current value will be adjusted if necessary to fall
	 * within its new range. The default maximum is 100.
	 *
	 * @param maxValue
	 *        the new maximum value, which must be greater as minimum value.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if maximum value is null</li>
	 *            <li>ERROR_INVALID_RANGE - if maximum value is less or equal
	 *            minimum value</li>
	 *            </ul>
	 */
	public void setMaxValue(final Double maxValue) {
		if (maxValue == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (maxValue.compareTo(minValue) <= 0) SWT.error(SWT.ERROR_INVALID_RANGE);

		this.maxValue = maxValue;

		if (getValue().compareTo(maxValue) > 0) {
			setValue(maxValue);
		} else {
			redraw();
		}
	}

	/**
	 * Returns the minimum value which the decorator will allow. The default
	 * minimum is zero.
	 *
	 * @return the minimum
	 */
	public Double getMinValue() {
		return minValue;
	}

	/**
	 * Sets the minimum value that the decorator will allow. If the new minimum
	 * is applied then the current value will be adjusted if necessary to fall
	 * within its new range. The default minimum is zero.
	 *
	 * @param minValue
	 *        the new minimum value, must be less then maximum value.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if minimum value is null</li>
	 *            <li>ERROR_INVALID_RANGE - if minimum value is greater or equal
	 *            maximum value</li>
	 *            </ul>
	 */
	public void setMinValue(final Double minValue) {
		if (minValue == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (minValue.compareTo(maxValue) >= 0) SWT.error(SWT.ERROR_INVALID_RANGE);

		this.minValue = minValue;

		if (getValue().compareTo(minValue) < 0) {
			setValue(minValue);
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
	 *            <li>ERROR_NULL_ARGUMENT - if value is null</li>
	 *            <li>ERROR_INVALID_ARGUMENT - if outer diameter less than zero
	 *            or greater than {@link #getMaxValue()}</li>
	 *            </ul>
	 */
	@Override
	public void setValue(final Double value) {
		if (value == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (value.compareTo(ZERO) < 0 || value.compareTo(maxValue) > 0) SWT.error(SWT.ERROR_INVALID_RANGE);
		super.setValue(value);
	}

	@Override
	protected Double getValueToDisplay() {
		return Double.valueOf(getValue().doubleValue() * 100 / (getMaxValue().doubleValue() - getMinValue().doubleValue()));
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		gc.setAntialias(SWT.ON);

		paintCircle(gc, x, y);

		if (!ctx.isActive()) {
			final Point size = getSize();
			final Point textSize = getValueSize();
			final int offsetY;

			if (circleType == CircleType.Circle) {
				offsetY = (size.y - textSize.y) / 2;
			} else {
				offsetY = size.y - textSize.y;
			}

			paintValue(gc, x + (size.x - textSize.x) / 2, y + offsetY);

		} else {
			paintCircle(gc, x, y);
		}
	}

	@Override
	public Point getSize() {
		final Point textSize = getValueSize();

		if (circleType == CircleType.Circle) {
			return new Point(Math.max(outerDiameter, textSize.x), Math.max(outerDiameter, textSize.y));
		}

		final int height = (int) (outerDiameter / circleType.heightQuotient) + textSize.y / 2;
		return new Point(Math.max(outerDiameter, textSize.x), Math.max(height, textSize.y));
	}

	private void paintCircle(final GC gc, final int x, final int y) {
		final double range = maxValue.doubleValue() - minValue.doubleValue();
		final double increment = range / ctx.getStepCount();
		final double curValue = Math.min(range / ctx.getStepCount() * ctx.getStep() + increment, getValue().doubleValue());

		final int curAngle = (int) Math.round(curValue * circleType.angle / range);

		gc.setForeground(Util.nvl(getTreshholdColor(getCircleForeground()), getParent().getForeground()));
		paintArc(gc, x, y, outerDiameter, circleType.angle - curAngle + circleType.offset, curAngle);
		gc.setForeground(Util.nvl(getCircleBackground(), getDisplay().getSystemColor(SWT.COLOR_GRAY)));
		paintArc(gc, x, y, outerDiameter, circleType.offset, circleType.angle - curAngle);
	}

	private void paintArc(final GC gc, final int x, final int y, final int diameter, final int startAngle, final int arcAngle) {
		final int lineWidth = (outerDiameter - innerDiameter) / 2;

		gc.setLineWidth(2);
		for (int i = 1; i < lineWidth; i++) {
			gc.drawArc(x + i, y + i, diameter - i - i, diameter - i - i, startAngle, arcAngle);
		}
		gc.setLineWidth(1);
	}
}
