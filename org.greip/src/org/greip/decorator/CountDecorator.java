package org.greip.decorator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.greip.common.Util;

public final class CountDecorator extends AbstractDecorator {

	public CountDecorator(final Control parent) {
		super(parent);
	}

	private Map<Integer, Color> treshholdMap;
	private int value;
	private Font font;
	private Color foreground;

	private int outerDiameter = 55;
	private int innerDiameter = 45;

	private boolean animate = true;
	private int offset;

	private void doAnimate() {
		Display.getCurrent().timerExec(40, () -> {
			if (offset > 0) {
				offset = Math.max(0, offset - 4);
				redraw();
			}
		});
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		final int lineWidth = (outerDiameter - innerDiameter) / 2;
		final Color color = getColor();
		final Color background = gc.getBackground();

		gc.setBackground(Util.nvl(color, gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY)));
		gc.fillOval(x + offset, y + offset, outerDiameter - offset * 2, outerDiameter - offset * 2);

		if (animate) {
			doAnimate();
		}

		if (offset == 0) {
			gc.setBackground(background);
			gc.fillOval(x + lineWidth, y + lineWidth, innerDiameter, innerDiameter);

			if (font != null) {
				gc.setFont(font);
			}

			String text = Integer.toString(value);
			Point p = gc.textExtent(text, SWT.NONE);

			if (p.x + 6 > getInnerDiameter()) {
				text = " " + text + " ";
				p = gc.textExtent(text, SWT.NONE);
			}

			gc.setForeground(Util.nvl(color, gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY)));
			gc.drawText(text, x + (getSize().x - p.x) / 2, y + (getSize().y - p.y) / 2, false);
		}
	}

	private Color getColor() {
		if (treshholdMap != null) {
			for (final Entry<Integer, Color> entry : treshholdMap.entrySet()) {
				if (value >= entry.getKey().intValue()) {
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

	public int getOuterDiameter() {
		return outerDiameter;
	}

	@Override
	public Point getSize() {
		return new Point(outerDiameter, outerDiameter);
	}

	public int getValue() {
		return value;
	}

	public boolean isShowAnimation() {
		return animate;
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

	public void setOuterDiameter(final int outerDiameter) {
		this.outerDiameter = outerDiameter;
		redraw();
	}

	public void setTreshholdColors(final Map<Integer, Color> treshholdMap) {
		this.treshholdMap = new TreeMap<>((o1, o2) -> o2.intValue() - o1.intValue());
		this.treshholdMap.putAll(treshholdMap);

		redraw();
	}

	public void setValue(final int value) {
		this.value = value;
		this.offset = animate ? getInnerDiameter() / 2 : 0;

		redraw();
	}

	public void showAnimation(final boolean animate) {
		this.animate = animate;
	}
}
