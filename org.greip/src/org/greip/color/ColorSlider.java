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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Util;

class ColorSlider extends Composite {

	private final ColorResolution resolution;
	private ColorSliderType type = ColorSliderType.Hue;
	private boolean vertical;
	private int barHeight = 4;
	private Color borderColor;
	private Color markerColor;

	private int colorSteps;
	private float stepSize;

	private List<RGB> rgbs;
	private int rgbIndex;
	private RGB currentRGB;
	private HSB originalHSB;

	public ColorSlider(final Composite parent, final ColorResolution resolution) {
		super(parent, SWT.DOUBLE_BUFFERED);

		this.resolution = resolution;

		addListener(SWT.Paint, this::handlePaint);
		addListener(SWT.MouseMove, this::handleMouseMove);
		addListener(SWT.MouseDown, this::handleMouseMove);
		addListener(SWT.MouseUp, this::handleMouseUp);
		addListener(SWT.Resize, e -> initColors());
		addListener(SWT.KeyDown, this::handleKeyDown);
		addListener(SWT.FocusIn, e -> redraw());
		addListener(SWT.FocusOut, e -> redraw());

		setHSB(new HSB(0, 0, 0));
	}

	private void handleKeyDown(final Event e) {
		if (e.keyCode == SWT.TAB) {
			traverse(e.stateMask == SWT.SHIFT ? SWT.TRAVERSE_TAB_PREVIOUS : SWT.TRAVERSE_TAB_NEXT, e);
		} else if (e.keyCode == SWT.ARROW_RIGHT || e.keyCode == (vertical ? SWT.ARROW_DOWN : SWT.ARROW_UP)) {
			setSelectedRGB(Math.min(rgbIndex + 1, rgbs.size() - 1));
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
		currentRGB = rgbs.get(rgbIndex);
		notifyListeners(SWT.DefaultSelection, new Event());
	}

	private void initColors() {
		final int size = vertical ? getBarBounds().height : getBarBounds().width;

		colorSteps = resolution.hueSteps == -1 ? size : resolution.hueSteps;
		rgbs = new ArrayList<>();

		for (int i = 0; i < colorSteps; i++) {
			rgbs.add(type.createSegmentRGB(originalHSB, 1.0f / (colorSteps - 1) * i));
		}

		if (type == ColorSliderType.Hue) {
			colorSteps--;
			rgbs.remove(colorSteps);
		}

		stepSize = size / (float) colorSteps;
		rgbIndex = Util.getNearestColor(rgbs, currentRGB);

		redraw();
	}

	private void handleMouseMove(final Event e) {
		if (e.stateMask == SWT.BUTTON1 || e.button == 1) {
			setSelectedRGB(calculateRgbIndex(vertical ? e.y : e.x));
		}
	}

	private int calculateRgbIndex(final int x) {
		return Math.max(0, Math.min(colorSteps - 1, (int) ((x - 3) / stepSize)));
	}

	private void handlePaint(final Event e) {
		paintBar(e.gc);
		paintMarker(e.gc);
		paintBorder(e.gc);

		if (isFocusControl()) {
			e.gc.drawFocus(0, 0, getSize().x, getSize().y);
		}
	}

	private void paintMarker(final GC gc) {
		final int markerPos = (int) ((rgbIndex + 0.5f) * stepSize);
		final int height = vertical ? getBarBounds().width : getBarBounds().height;

		gc.setAntialias(SWT.ON);
		gc.setBackground(getMarkerColor());

		if (vertical) {
			gc.fillPolygon(new int[] { height + 4, markerPos + 4, height + 10, markerPos + 1, height + 10, markerPos + 7 });
		} else {
			gc.fillPolygon(new int[] { markerPos + 4, height + 3, markerPos + 7, height + 10, markerPos + 1, height + 10 });
		}
	}

	private void paintBar(final GC gc) {
		final Rectangle barBounds = getBarBounds();

		for (int i = 0; i < colorSteps; i++) {
			final Rectangle bounds;

			if (vertical) {
				final float increment = (float) barBounds.height / colorSteps;
				bounds = new Rectangle(barBounds.x + 1, (int) (increment * i) + 4, getBarHeight(), (int) increment + 1);
			} else {
				final float increment = (float) barBounds.width / colorSteps;
				bounds = new Rectangle((int) (increment * i) + 4, barBounds.y + 1, (int) increment + 1, getBarHeight());
			}

			Util.withResource(new Color(gc.getDevice(), rgbs.get(i)), c -> {
				gc.setBackground(c);
				gc.fillRectangle(bounds);
			});
		}
	}

	private void paintBorder(final GC gc) {
		final Rectangle bounds = getBarBounds();

		gc.setForeground(getBorderColor());
		gc.drawRectangle(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);
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
		return vertical ? new Point(getBarHeight() + 12, Math.max(100, hHint)) : new Point(Math.max(100, wHint), getBarHeight() + 12);
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
		redraw();
	}

	private Rectangle getBarBounds() {
		final Point size = getSize();
		return vertical ? new Rectangle(2, 3, barHeight, Math.max(1, size.y - 8)) : new Rectangle(3, 2, Math.max(1, size.x - 8), barHeight);
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
}
