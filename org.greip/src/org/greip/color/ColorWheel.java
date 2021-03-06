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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Util;

class ColorWheel extends Composite {

	private static final int DIAMETER = 128;
	private static final int RADIUS = DIAMETER / 2;

	private static class ColorData {
		private final RGB[] rgbs;
		private final Point[] points;

		public ColorData(final List<RGB> rgbs, final List<Point> points) {
			this.rgbs = rgbs.toArray(new RGB[rgbs.size()]);
			this.points = points.toArray(new Point[points.size()]);
		}
	}

	private ColorResolution colorResolution;
	private Image image;
	private RGB rgb = new RGB(255, 255, 255);
	private Color bgColor;
	private ColorData colorData;

	private final float zoom;
	private final int scaledDiameter;
	private final int scaledRadius;

	public ColorWheel(final Composite parent, final ColorResolution colorResolution) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_FOCUS);

		zoom = Util.getZoom(getDisplay());
		scaledRadius = zoom(DIAMETER) / 2;
		scaledDiameter = scaledRadius * 2 + 1;

		setColorResolution(colorResolution);
		setBackgroundMode(SWT.INHERIT_FORCE);

		final Cursor cursor = createCursor();

		addListener(SWT.Paint, e -> {
			if (bgColor != getBackground()) {
				recreateColorWheelImage();
			}

			e.gc.drawImage(image, 0, 0);
			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));

			final int rgbIndex = Util.getSimilarColor(colorData.rgbs, rgb);
			final Point p = colorData.points[rgbIndex];

			e.gc.setAntialias(SWT.OFF);
			e.gc.setLineWidth(1);
			e.gc.setLineStyle(SWT.LINE_SOLID);

			final int radius = zoom(5) / 2;
			final int diameter = radius * 2;
			e.gc.drawOval(p.x - radius, p.y - radius, diameter, diameter);
		});

		addListener(SWT.MouseMove, e -> {
			if (circleContains(e.x, e.y)) {
				setCursor(cursor);
				if (e.stateMask == SWT.BUTTON1) {
					setRGB(getColorFromImage(e.x, e.y));
					notifyListeners(SWT.Modify, e);
				}
			} else {
				setCursor(null);
			}
		});

		addListener(SWT.MouseDown, e -> {
			if (circleContains(e.x, e.y)) {
				setRGB(getColorFromImage(e.x, e.y));
				notifyListeners(SWT.Modify, e);
			}
		});

		addListener(SWT.MouseDoubleClick, e -> {
			if (circleContains(e.x, e.y)) {
				notifyListeners(SWT.Selection, new Event());
			}
		});

		addListener(SWT.Dispose, e -> {
			cursor.dispose();
			disposeColorWheelImage();
		});
	}

	private void recreateColorWheelImage() {
		disposeColorWheelImage();
		createColorWheelImage();
		bgColor = getBackground();
	}

	private void disposeColorWheelImage() {
		Util.whenNotNull(image, Image::dispose);
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

	private boolean circleContains(final int x, final int y) {
		return distanceToCenter(x, y) < scaledRadius - 1;
	}

	private double distanceToCenter(final int x, final int y) {
		return distanceBetween(x, y, scaledRadius, scaledRadius);
	}

	private static double distanceBetween(final int x1, final int y1, final int x2, final double y2) {
		final int diffX = x1 - x2;
		final double diffY = y1 - y2;

		return Math.sqrt(diffX * diffX + diffY * diffY);
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
		return new Point(scaledDiameter, scaledDiameter);
	}

	private int zoom(final int pixels) {
		return (int) (pixels * zoom);
	}

	private void createColorWheelImage() {
		final int eyeSize;

		if (colorResolution == ColorResolution.Maximal) {
			createFullResolutionImage();
			eyeSize = 2;
		} else {
			createLowResolutionImage();
			eyeSize = 8;
		}

		Util.withResource(new GC(image), gc -> {
			gc.setAntialias(SWT.ON);
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

			final int radius = zoom(eyeSize) / 2;
			final int diameter = radius * 2 + 1;
			gc.fillOval(scaledRadius - radius, scaledRadius - radius, diameter, diameter);
		});
	}

	private void createLowResolutionImage() {
		final Display display = getDisplay();

		image = new Image(display, scaledDiameter, scaledDiameter);
		final GC gc = new GC(image);
		final Transform transform = new Transform(gc.getDevice());
		transform.scale(zoom, zoom);

		gc.setTransform(transform);
		gc.setBackground(getBackground());
		gc.fillRectangle(0, 0, scaledDiameter, scaledDiameter);
		gc.setAntialias(SWT.ON);

		final List<RGB> rgbList = new ArrayList<>();
		final List<Point> pointList = new ArrayList<>();

		rgbList.add(new RGB(255, 255, 255));
		pointList.add(new Point(scaledRadius, scaledRadius));

		final int saturationSteps = colorResolution.saturationSteps;
		final float decrement = 1.0f / saturationSteps;
		final int scale = (DIAMETER - 8) / saturationSteps;

		for (int j = 0; j < saturationSteps; j++) {
			final int pos = j * scale / 2;
			final int diameter = DIAMETER - j * scale;
			final double radius = (diameter - scale / 2) / 2.0d;

			final int hueSteps = getHueSteps(diameter);
			final int arcAngle = Math.round(360.0f / hueSteps) + 1;

			for (int i = 0; i < hueSteps; i++) {
				final float startAngle = 360.f / hueSteps * i;
				final RGB rgb = new RGB(startAngle, 1 - decrement * j, 1.0f);

				Util.withResource(new Color(display, rgb), c -> {
					gc.setBackground(c);
					gc.fillArc(pos, pos, diameter, diameter, Math.round(startAngle + 90.0f - arcAngle / 2), arcAngle);
				});

				final double arc = (startAngle + 90.0f) * Math.PI / 180;
				final int centerX = (int) Math.round((radius * Math.cos(arc) + RADIUS) * zoom);
				final int centerY = (int) Math.round((radius * Math.sin(-arc) + RADIUS) * zoom);

				rgbList.add(rgb);
				pointList.add(new Point(centerX, centerY));
			}
		}

		transform.dispose();
		gc.dispose();

		colorData = new ColorData(rgbList, pointList);
	}

	private void createFullResolutionImage() {
		final PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
		final ImageData imageData = new ImageData(scaledDiameter, scaledDiameter, 24, palette);
		final int background = getBackground().getRGB().hashCode();

		final List<RGB> rgbList = new ArrayList<>();
		final List<Point> pointList = new ArrayList<>();

		for (int x = 0; x < scaledDiameter; x++) {
			for (int y = 0; y < scaledDiameter; y++) {
				final double a = distanceToCenter(x, y);

				if (a > scaledRadius) {
					imageData.setPixel(x, y, background);

				} else {
					final double c = distanceBetween(x, y, scaledRadius, scaledRadius - a);
					final float gamma = (float) (Math.asin(c / (2 * a)) / Math.PI * 360);

					final float hue = x < scaledRadius ? gamma : 360.0f - gamma;
					final float saturation = (float) (a / scaledRadius);

					final RGB rgb = new RGB(hue, saturation, 1.0f);
					imageData.setPixel(x, y, rgb.hashCode());

					rgbList.add(rgb);
					pointList.add(new Point(x, y));
				}
			}
		}

		image = new Image(getDisplay(), imageData);

		Util.withResource(new GC(image), gc -> {
			gc.setAntialias(SWT.ON);
			gc.setForeground(getBackground());
			gc.setLineWidth(1);
			gc.drawOval(0, 0, scaledDiameter - 1, scaledDiameter - 1);
		});

		colorData = new ColorData(rgbList, pointList);
	}

	private int getHueSteps(final int width) {
		return colorResolution == ColorResolution.Maximal ? (int) (Math.PI * width / 4) : colorResolution.hueSteps;
	}

	private RGB getColorFromImage(final int x, final int y) {
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
