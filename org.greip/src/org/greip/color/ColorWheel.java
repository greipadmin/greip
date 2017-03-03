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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Util;

class ColorWheel extends Composite {

	private static final int DIAMETER = 127;

	private ColorResolution colorResolution;
	private Image image;
	private RGB rgb = new RGB(255, 255, 255);
	private RGB[] rgbs;
	private Point[] points;
	private Color bgColor;

	public ColorWheel(final Composite parent, final ColorResolution colorResolution) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_FOCUS);
		setColorResolution(colorResolution);
		setBackgroundMode(SWT.INHERIT_FORCE);

		final Cursor cursor = createCursor();

		addListener(SWT.Paint, e -> {
			if (bgColor != getBackground()) {
				recreateColorWheelImage();
			}

			e.gc.drawImage(image, 0, 0);
			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));

			final int rgbIndex = Util.getNearestColor(Arrays.asList(rgbs), rgb);
			final Point p = points[rgbIndex];

			e.gc.setAntialias(SWT.OFF);
			e.gc.setLineWidth(1);
			e.gc.setLineStyle(SWT.LINE_SOLID);
			e.gc.drawOval(p.x - 2, p.y - 2, 4, 4);
		});

		addListener(SWT.MouseMove, e -> {
			if (circleContains(e.x, e.y)) {
				if (e.stateMask == SWT.BUTTON1) {
					setRGB(getColorAt(e.x, e.y));
					notifyListeners(SWT.Selection, e);
				}
				setCursor(cursor);
			} else {
				setCursor(null);
			}
		});

		addListener(SWT.MouseDown, e -> {
			if (circleContains(e.x, e.y)) {
				setRGB(getColorAt(e.x, e.y));
				notifyListeners(SWT.Selection, e);
			}
		});

		addListener(SWT.Dispose, e -> {
			cursor.dispose();
			disposeColorWheelImage();
		});
	}

	private void recreateColorWheelImage() {
		disposeColorWheelImage();
		image = createColorWheelImage();
		bgColor = getBackground();
	}

	private void disposeColorWheelImage() {
		Util.whenNotNull(image, image::dispose);
	}

	private Cursor createCursor() {
		return Util.createCursor(getDisplay(), new Point(19, 19), new Point(9, 9), gc -> {
			gc.drawOval(7, 7, 4, 4);
			gc.setLineWidth(1);
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawLine(12, 9, 18, 9);
			gc.drawLine(0, 9, 6, 9);
			gc.drawLine(9, 0, 9, 6);
			gc.drawLine(9, 12, 9, 18);
		});
	}

	private static boolean circleContains(final int x, final int y) {
		final Point center = new Point(DIAMETER / 2, DIAMETER / 2);
		final double radius = Math.pow((DIAMETER - 2) / 2, 2);
		final double xy2 = Math.pow(y - center.y, 2) + Math.pow(x - center.x, 2);

		return radius - xy2 > 0;
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
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		return new Point(DIAMETER, DIAMETER);
	}

	private Image createColorWheelImage() {
		final Display display = getDisplay();
		final Image image = new Image(display, DIAMETER, DIAMETER);
		final GC gc = new GC(image);

		if (colorResolution != ColorResolution.Maximal) {
			gc.setAntialias(SWT.ON);
		}

		gc.setBackground(getBackground());
		gc.fillRectangle(0, 0, DIAMETER, DIAMETER);

		final Map<Point, RGB> rgbMap = new HashMap<>();
		rgbMap.put(new Point(DIAMETER / 2, DIAMETER / 2), new RGB(255, 255, 255));

		final float saturationSteps = colorResolution.saturationSteps;
		final float decrement = 1.0f / saturationSteps;
		final float scale = DIAMETER / (saturationSteps + 0.6f);

		for (int j = 0; j < saturationSteps; j++) {
			final int p = Math.round(j * (scale / 2));
			final int diameter = Math.round(DIAMETER - j * scale);
			final int radius = Math.round((DIAMETER - (j + 0.5f) * scale) / 2);

			final int hueSteps = getHueSteps(diameter);
			final int arcAngle = Math.round(360.0f / hueSteps) + 1;

			for (int i = 0; i < hueSteps; i++) {
				final float startAngle = 360.f / hueSteps * i;
				final RGB rgb = new RGB(startAngle, 1 - decrement * j, 1.0f);
				final Color color = new Color(display, rgb);

				gc.setBackground(color);
				gc.fillArc(p, p, diameter, diameter, Math.round(startAngle), arcAngle);
				color.dispose();

				if (colorResolution != ColorResolution.Maximal) {
					final double arc = (startAngle + arcAngle / 2) * Math.PI / 180;
					final int centerX = (int) (Math.round(radius * Math.cos(arc) + DIAMETER / 2));
					final int centerY = (int) (Math.round(radius * Math.sin(-arc) + DIAMETER / 2));
					final Point center = new Point(centerX, centerY);

					rgbMap.put(center, rgb);
				}
			}
		}

		gc.setAntialias(SWT.ON);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillOval(DIAMETER / 2 - 2, DIAMETER / 2 - 2, 5, 5);

		if (colorResolution == ColorResolution.Maximal) {
			final ImageData data = image.getImageData();

			for (int x = 0; x < DIAMETER; x++) {
				for (int y = 0; y < DIAMETER; y++) {
					if (circleContains(x, y)) {
						final int pixel = data.getPixel(x, y);
						rgbMap.put(new Point(x, y), data.palette.getRGB(pixel));
					}
				}
			}
		}

		gc.setForeground(getBackground());
		gc.setLineWidth(4);
		gc.drawOval(-1, -1, DIAMETER + 2, DIAMETER + 2);
		gc.dispose();

		this.rgbs = new RGB[rgbMap.size()];
		this.points = new Point[rgbMap.size()];

		final Iterator<Entry<Point, RGB>> it = rgbMap.entrySet().iterator();
		for (int i = 0; i < rgbMap.size(); i++) {
			final Entry<Point, RGB> entry = it.next();
			rgbs[i] = entry.getValue();
			points[i] = entry.getKey();
		}

		return image;
	}

	private int getHueSteps(final int width) {
		return colorResolution == ColorResolution.Maximal ? (int) (Math.PI * width) : colorResolution.hueSteps;
	}

	private RGB getColorAt(final int x, final int y) {
		final ImageData data = image.getImageData();
		final int pixel = data.getPixel(x, y);

		return data.palette.getRGB(pixel);
	}

	public ColorResolution getColorResolution() {
		return colorResolution;
	}

	public void setColorResolution(final ColorResolution colorResolution) {
		this.colorResolution = colorResolution;
		this.bgColor = null;

		redraw();
	}

	public RGB getRGB() {
		return rgb;
	}

	public void setRGB(final RGB rgb) {
		this.rgb = rgb;
		redraw();
	}
}
