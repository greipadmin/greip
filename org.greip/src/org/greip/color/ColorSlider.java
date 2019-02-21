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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Util;

class ColorSlider extends Composite {

	private final ColorResolution resolution;
	private ColorSliderType type = ColorSliderType.Hue;
	private boolean vertical;
	private int barHeight;
	private Color borderColor;
	private Color markerColor;
	private String text;

	private int colorSteps;
	private float stepSize;

	private RGB[] rgbs;
	private int rgbIndex;
	private RGB currentRGB;
	private HSB originalHSB;

	private final float zoom;
	private int scaledBarHeight;

	public ColorSlider(final Composite parent, final ColorResolution resolution) {
		super(parent, SWT.DOUBLE_BUFFERED);

		this.resolution = resolution;
		this.zoom = Util.getZoom(getDisplay());

		addListener(SWT.Paint, this::handlePaint);
		addListener(SWT.MouseMove, this::handleMouseMove);
		addListener(SWT.MouseDown, this::handleMouseMove);
		addListener(SWT.MouseUp, this::handleMouseUp);
		addListener(SWT.Resize, e -> initColors());
		addListener(SWT.KeyDown, this::handleKeyDown);
		addListener(SWT.FocusIn, e -> redraw());
		addListener(SWT.FocusOut, e -> redraw());

		setBarHeight(4);
		setHSB(new HSB(0, 0, 0));
	}

	private void handleKeyDown(final Event e) {
		if (e.keyCode == SWT.TAB) {
			traverse(e.stateMask == SWT.SHIFT ? SWT.TRAVERSE_TAB_PREVIOUS : SWT.TRAVERSE_TAB_NEXT, e);
		} else if (e.keyCode == SWT.ARROW_RIGHT || e.keyCode == (vertical ? SWT.ARROW_DOWN : SWT.ARROW_UP)) {
			setSelectedRGB(Math.min(rgbIndex + 1, rgbs.length - 1));
		} else if (e.keyCode == SWT.ARROW_LEFT || e.keyCode == (vertical ? SWT.ARROW_UP : SWT.ARROW_DOWN)) {
			setSelectedRGB(Math.max(rgbIndex - 1, 0));
		}
	}

	private void setSelectedRGB(final int index) {
		rgbIndex = index;
		notifyListeners(SWT.Selection, new Event());
		redraw();
	}

	public void setType(final ColorSliderType type) {
		this.type = type;
		initColors();
	}

	public ColorSliderType getType() {
		return type;
	}

	private void handleMouseUp(final Event e) {
		if (e.y >= getBarBounds().y) {
			currentRGB = rgbs[rgbIndex];
			notifyListeners(SWT.DefaultSelection, new Event());
		}
	}

	private void initColors() {
		final int size = vertical ? getBarBounds().height : getBarBounds().width;

		final int hueSteps = resolution.hueSteps == -1 ? size : resolution.hueSteps;
		colorSteps = type == ColorSliderType.Hue ? hueSteps - 1 : hueSteps;
		rgbs = new RGB[colorSteps];

		for (int i = 0; i < colorSteps; i++) {
			rgbs[i] = type.createSegmentRGB(originalHSB, 1.0f / (hueSteps - 1) * i);
		}

		stepSize = size / (float) colorSteps;
		rgbIndex = Util.getSimilarColor(rgbs, currentRGB);

		redraw();
	}

	private void handleMouseMove(final Event e) {
		if ((e.stateMask == SWT.BUTTON1 || e.button == 1) && e.y >= getBarBounds().y) {
			setSelectedRGB(calculateRgbIndex(vertical ? e.y : e.x));
		}
	}

	private int calculateRgbIndex(final int x) {
		return Math.max(0, Math.min(colorSteps - 1, (int) ((x - 3) / stepSize)));
	}

	private void handlePaint(final Event e) {
		paintText(e.gc);
		paintBar(e.gc);
		paintMarker(e.gc);
		paintBorder(e.gc);
		paintFocusRect(e.gc);
	}

	private void paintFocusRect(final GC gc) {
		if (isFocusControl()) {
			final Rectangle barBounds = getBarBounds();

			if (vertical) {
				gc.drawFocus(barBounds.x - 2, barBounds.y - 3, barBounds.width + 4 + zoom(8), barBounds.height + 8);
			} else {
				gc.drawFocus(barBounds.x - 3, barBounds.y - 2, barBounds.width + 8, barBounds.height + 4 + zoom(8));
			}
		}
	}

	private void paintText(final GC gc) {
		if (text != null) {
			final Point textSize = getTextSize();
			final Point size = getSize();

			if (vertical) {
				Util.withResource(new Transform(gc.getDevice()), transform -> {
					transform.rotate(-90f);
					transform.translate(0, 0);

					gc.setTransform(transform);
					gc.drawText(text, -size.y + (size.y - textSize.x) / 2, 0, true);
					gc.setTransform(null);
				});
			} else {
				gc.drawText(text, (size.x - textSize.x) / 2, 0, true);
			}
		}
	}

	private void paintMarker(final GC gc) {
		final int markerPos = (int) ((rgbIndex + 0.5f) * stepSize);
		final Rectangle barBounds = getBarBounds();

		gc.setAntialias(SWT.ON);
		gc.setBackground(getMarkerColor());

		final int four = zoom(4);
		final int seven = zoom(7);
		final int markerHeight = zoom(8);

		if (vertical) {
			final int x = barBounds.x + barBounds.width;
			gc.fillPolygon(new int[] { x + 1, markerPos + four, x + markerHeight, markerPos + 1, x + markerHeight, markerPos + seven });
		} else {
			final int y = barBounds.y + barBounds.height;
			gc.fillPolygon(new int[] { markerPos + four, y + 1, markerPos + seven, y + markerHeight, markerPos + 1, y + markerHeight });
		}
	}

	private void paintBar(final GC gc) {
		final Rectangle barBounds = getBarBounds();

		for (int i = 0; i < colorSteps; i++) {
			final Rectangle bounds;

			if (vertical) {
				final float increment = (float) barBounds.height / colorSteps;
				bounds = new Rectangle(barBounds.x + 1, (int) (increment * i) + 4, scaledBarHeight, (int) increment + 1);
			} else {
				final float increment = (float) barBounds.width / colorSteps;
				bounds = new Rectangle((int) (increment * i) + 4, barBounds.y + 1, (int) increment + 1, scaledBarHeight);
			}

			Util.withResource(new Color(gc.getDevice(), rgbs[i]), c -> {
				gc.setBackground(c);
				gc.fillRectangle(bounds);
			});
		}
	}

	private void paintBorder(final GC gc) {
		final Rectangle bounds = getBarBounds();

		gc.setForeground(getBorderColor());
		if (vertical) {
			gc.drawRectangle(bounds.x, bounds.y, barHeight + 1, bounds.height + 1);
		} else {
			gc.drawRectangle(bounds.x, bounds.y, bounds.width + 1, barHeight + 1);
		}
	}

	protected final HSB getOriginalHSB() {
		return originalHSB;
	}

	public float getValue() {
		return 1.0f / (colorSteps - 1) * rgbIndex;
	}

	public void setHSB(final HSB hsb) {
		this.originalHSB = hsb;
		this.currentRGB = type.createInitialRGB(hsb);
		initColors();
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final Point textSize = getTextSize();

		return vertical ? new Point(scaledBarHeight + zoom(12) + textSize.y, Math.max(100, hHint))
				: new Point(Math.max(100, wHint), scaledBarHeight + zoom(12) + textSize.y);
	}

	private Point getTextSize() {
		if (getText() == null) {
			return new Point(0, 0);
		}

		final Point size = Util.withResource(new GC(getDisplay()), gc -> {
			gc.setFont(getFont());
			return gc.textExtent(getText());
		});
		size.y += zoom(2);

		return size;
	}

	public Color getBorderColor() {
		return Util.nvl(borderColor, getDisplay().getSystemColor(SWT.COLOR_GRAY));
	}

	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
		redraw();
	}

	public Color getMarkerColor() {
		return Util.nvl(markerColor, getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	public void setMarkerColor(final Color markerColor) {
		this.markerColor = markerColor;
		redraw();
	}

	public int getBarHeight() {
		return barHeight;
	}

	public void setBarHeight(final int barHeight) {
		this.barHeight = Math.max(1, barHeight);
		this.scaledBarHeight = zoom(barHeight);
		redraw();
	}

	private Rectangle getBarBounds() {
		final Point size = getSize();
		final int textHeight = getTextSize().y;

		return vertical ? new Rectangle(2 + textHeight, 3, scaledBarHeight, Math.max(1, size.y - 8))
				: new Rectangle(3, 2 + textHeight, Math.max(1, size.x - 8), scaledBarHeight);
	}

	public void addSelectionListener(final SelectionListener listener) {
		final TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	@Override
	public void setOrientation(final int style) {
		this.vertical = (style & SWT.VERTICAL) != 0;
		initColors();
	}

	@Override
	public int getOrientation() {
		return vertical ? SWT.VERTICAL : SWT.HORIZONTAL;
	}

	private int zoom(final int pixels) {
		return (int) (pixels * zoom);
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}
}
