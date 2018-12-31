package org.greip.decorator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.greip.common.Util;

public final class PercentageDecorator extends AbstractDecorator {

	public PercentageDecorator(final Control parent) {
		super(parent);
	}

	private Map<BigDecimal, Color> treshholdMap;
	private BigDecimal value;
	private BigDecimal curValue;
	private BigDecimal maxValue;
	private BigDecimal increment;
	private Font font;
	private Color foreground;
	private Color background;
	private int outerDiameter = 55;
	private int innerDiameter = 45;
	private boolean animate;
	private String unit;
	private CircleType circleType = CircleType.Circle;
	private int unitAlignment;
	private Font unitFont;

	private void applyUnitFont(final GC gc) {
		if (unitFont != null) {
			gc.setFont(unitFont);
		} else {
			final FontData[] fontData = getFont().getFontData();
			fontData[0].setHeight(Math.min(10, Math.max(2, (int) (fontData[0].getHeight() * 0.5))));
			fontData[0].setStyle(SWT.NONE);
			gc.setFont(new Font(gc.getDevice(), fontData[0]));
		}
	}

	private void doAnimate(final GC gc) {
		Display.getCurrent().timerExec(10, () -> {
			if (curValue.compareTo(value) < 0) {
				if (curValue.add(increment).compareTo(value) > 0) {
					curValue = value;
				} else {
					curValue = curValue.add(increment);
				}
				redraw();
			}
		});
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		final Point center = paintCircle(gc, x, y);

		if (animate) {
			doAnimate(gc);
		}

		if (curValue.equals(value)) {
			final Font textFont = Util.nvl(font, gc.getFont());
			final Point size = getSize();

			gc.setFont(textFont);

			final String text = value.toString();
			final Point textSize = gc.textExtent(text);
			final Point textPos;

			if (circleType == CircleType.Circle) {
				textPos = new Point(center.x - textSize.x / 2, y + (size.y - textSize.y) / 2);
			} else {
				textPos = new Point(center.x - textSize.x / 2, y + size.y - textSize.y);
			}

			if (foreground != null) {
				gc.setForeground(foreground);
			}

			if (unit != null) {
				applyUnitFont(gc);

				final int unitOffsetY = getUnitOffsetY(textFont, gc.getFont());
				final Point unitSize = gc.textExtent(unit);

				if ((unitAlignment & SWT.RIGHT) > 0) {
					gc.drawText(unit, textPos.x + textSize.x - unitSize.x / 2, textPos.y + textSize.y - unitSize.y - unitOffsetY, true);
					textPos.x -= unitSize.x / 2;
				} else if ((unitAlignment & SWT.LEFT) > 0) {
					gc.drawText(unit, textPos.x - unitSize.x / 2 - 2, textPos.y + textSize.y - unitSize.y - unitOffsetY, true);
					textPos.x += unitSize.x / 2;
				} else if (unitAlignment == SWT.BOTTOM) {
					textPos.y -= unitSize.y / (circleType == CircleType.Circle ? 2 : 1) - 3;
					gc.drawText(unit, center.x - unitSize.x / 2, textPos.y + textSize.y - 3, true);
				} else {
					textPos.y += circleType == CircleType.Circle ? unitSize.y / 2 : 0;
					gc.drawText(unit, center.x - unitSize.x / 2, textPos.y - unitSize.y + 4, true);
				}

				if (unitFont == null) {
					gc.getFont().dispose();
				}

				gc.setFont(textFont);
			}

			gc.drawText(text, textPos.x, textPos.y, true);
		}
	}

	public Color getBackground() {
		return background != null ? background : Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	}

	public CircleType getCircleType() {
		return circleType;
	}

	private Color getColor() {
		if (treshholdMap != null) {
			for (final Entry<BigDecimal, Color> entry : treshholdMap.entrySet()) {
				if (value.compareTo(entry.getKey()) >= 0) {
					return entry.getValue();
				}
			}
		}

		return getForeground();
	}

	public Font getFont() {
		return font;
	}

	public Color getForeground() {
		return foreground;
	}

	public int getInnerDiameter() {
		return innerDiameter;
	}

	public double getMaxValue() {
		return maxValue.doubleValue();
	}

	public int getOuterDiameter() {
		return outerDiameter;
	}

	@Override
	public Point getSize() {
		final GC gc = new GC(getDisplay());

		try {
			gc.setAntialias(SWT.ON);
			gc.setFont(getFont());
			final int textHeight = gc.textExtent(value.toString()).y;
			int height;

			if (circleType == CircleType.Circle) {
				height = outerDiameter;
			} else {
				height = (int) (outerDiameter / circleType.heightQuotient) + textHeight / 2;

				if (unit != null && unitAlignment == SWT.BOTTOM) {
					applyUnitFont(gc);
					height += gc.textExtent(unit.toString()).y / 2;
					if (unitFont == null) {
						gc.getFont().dispose();
					}
				}
			}
			return new Point(outerDiameter, height);

		} finally {
			gc.dispose();
		}
	}

	public String getUnit() {
		return unit;
	}

	public int getUnitAlignment() {
		return unitAlignment;
	}

	public Font getUnitFont() {
		return unitFont;
	}

	private int getUnitOffsetY(final Font textFont, final Font unitFont) {
		final int textFontHeight = textFont.getFontData()[0].getHeight();
		final int unitFontHeight = unitFont.getFontData()[0].getHeight();

		if (unitAlignment == SWT.LEFT || unitAlignment == SWT.RIGHT) {
			return (int) ((textFontHeight - unitFontHeight) / 4.0 + 0.5);
		} else if ((unitAlignment & SWT.LEFT) > 0 || (unitAlignment & SWT.RIGHT) > 0) {
			return textFontHeight - unitFontHeight + textFontHeight / 3;
		} else if (unitAlignment == SWT.TOP) {
			return unitFontHeight - textFontHeight / 10;
		}
		return -unitFontHeight - 2;
	}

	public double getValue() {
		return value.doubleValue();
	}

	public boolean isShowAnimation() {
		return animate;
	}

	private Point paintCircle(final GC gc, final int x, final int y) {
		final Color color = getColor();
		final Color bgColor = gc.getBackground();
		final int curAngle = Math.round(curValue.floatValue() * circleType.angle / maxValue.floatValue());
		final int diameterDiff = (outerDiameter - innerDiameter) / 2;

		gc.setBackground(color == null ? gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY) : color);
		gc.fillArc(x, y, outerDiameter, outerDiameter, circleType.angle - curAngle + circleType.offset, curAngle);
		gc.setBackground(getBackground());
		gc.fillArc(x, y, outerDiameter, outerDiameter, circleType.offset, circleType.angle - curAngle);
		gc.setBackground(bgColor);
		gc.fillOval(x + diameterDiff, y + diameterDiff, innerDiameter, innerDiameter);

		return new Point(x + outerDiameter / 2, y + outerDiameter / 2);
	}

	public void setBackground(final Color background) {
		this.background = background;
		redraw();
	}

	public void setCircleType(final CircleType angleType) {
		this.circleType = angleType;
		redraw();
	}

	public void setFont(final Font font) {
		this.font = font;
		redraw();
	}

	public void setForeground(final Color color) {
		this.foreground = color;
		redraw();
	}

	public void setInnerDiameter(final int innerDiameter) {
		this.innerDiameter = innerDiameter;
		redraw();
	}

	public void setMaxValue(final double maxValue) {
		this.maxValue = new BigDecimal(maxValue);
		this.increment = new BigDecimal(maxValue / 30);
		redraw();
	}

	public void setOuterDiameter(final int outerDiameter) {
		this.outerDiameter = outerDiameter;
		redraw();
	}

	public void setTreshholdColors(final Map<BigDecimal, Color> treshholdMap) {
		this.treshholdMap = new TreeMap<>(new Comparator<BigDecimal>() {
			@Override
			public int compare(final BigDecimal o1, final BigDecimal o2) {
				return -o1.compareTo(o2);
			}
		});
		this.treshholdMap.putAll(treshholdMap);
		redraw();
	}

	public void setUnit(final String unit) {
		this.unit = unit;
		redraw();
	}

	public void setUnitAlignment(final int unitAlignment) {
		this.unitAlignment = unitAlignment;
		redraw();
	}

	public void setUnitFont(final Font unitFont) {
		this.unitFont = unitFont;
		redraw();
	}

	public void setValue(final double value) {

		if (value > getMaxValue()) {
			throw new IllegalArgumentException("legal value is greater maxValue");
		}

		this.value = new BigDecimal(value);
		curValue = animate ? new BigDecimal(0) : this.value;

		redraw();
	}

	public void showAnimation(final boolean animate) {
		this.animate = animate;
	}
}
