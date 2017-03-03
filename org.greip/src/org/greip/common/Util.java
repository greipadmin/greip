/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Control;

public final class Util {

	private static final String ELLIPSES = "..."; //$NON-NLS-1$

	public static Cursor createCursor(final Device device, final Point size, final Point hotspot, final Consumer<GC> painter) {

		final PaletteData palette = new PaletteData(new RGB[] { new RGB(0, 0, 0), new RGB(255, 255, 255) });
		final Image img = new Image(device, new ImageData(size.x, size.y, 1, palette));
		final GC gc = new GC(img);

		gc.fillRectangle(0, 0, size.x, size.y);
		painter.accept(gc);

		final ImageData source = img.getImageData();
		final ImageData mask = new ImageData(size.x, size.y, 1, palette);

		gc.dispose();
		img.dispose();

		return new Cursor(device, source, mask, hotspot.x, hotspot.y);
	}

	public static RGB getDimmedRGB(final RGB rgb, final float brightnessOffset) {
		final float[] hsb = rgb.getHSB();
		return new RGB(hsb[0], hsb[1], Math.max(0, Math.min(1.0f, hsb[2] + brightnessOffset)));
	}

	public static <T> T nvl(final T o1, final T o2) {
		return o1 == null ? o2 : o1;
	}

	public static String shortenText(final GC gc, final String text, final int maxWidth, final int flags) {
		String result = text;
		int width = gc.textExtent(result, flags).x;

		if (width > maxWidth) {
			result += ELLIPSES;
			while (width > maxWidth && result.length() > 3) {
				result = result.substring(0, result.length() - 4) + ELLIPSES;
				width = gc.textExtent(result, flags).x;
			}
		}
		return result;
	}

	public static int getNearestColor(final List<RGB> rgbs, final RGB rgb) {
		final float[] h1 = rgb.getHSB();

		int idx = 0;
		float minDiff = Float.MAX_VALUE;

		for (int i = 0; i < rgbs.size(); i++) {
			final float[] h2 = rgbs.get(i).getHSB();
			final float diff = Math.abs(h1[0] - h2[0]) + Math.abs((h1[1] - h2[1]) * 45) + Math.abs((h1[2] - h2[2]) * 45);

			if (diff < minDiff) {
				minDiff = diff;
				idx = i;
			}
		}

		return idx;
	}

	public static void applyDerivedFont(final Control control, final int heightOffset, final int style) {
		final FontData fd = control.getFont().getFontData()[0];
		final Font newFont = new Font(control.getDisplay(), fd.getName(), fd.getHeight() + heightOffset, style);

		control.setFont(newFont);
		control.addListener(SWT.Dispose, e -> newFont.dispose());
	}

	public static Point getTextSize(final Control control, final String text, final int flags) {
		final GC gc = new GC(control);
		try {
			return gc.textExtent(text, flags);
		} finally {
			gc.dispose();
		}
	}

	public static <R extends Resource> void withResource(final R resource, final Consumer<R> consumer) {
		try {
			consumer.accept(resource);
		} finally {
			resource.dispose();
		}
	}

	public static <R extends Resource, O> O withResource(final R resource, final Function<R, O> consumer) {
		try {
			return consumer.apply(resource);
		} finally {
			resource.dispose();
		}
	}

	public static <O> void whenNotNull(final O object, final Consumer<O> c) {
		if (object != null) {
			c.accept(object);
		}
	}

	public static <O> void whenNotNull(final O object, final Runnable r) {
		if (object != null) {
			r.run();
		}
	}

	public static void when(final boolean condition, final Runnable r) {
		if (condition) {
			r.run();
		}
	}
}
