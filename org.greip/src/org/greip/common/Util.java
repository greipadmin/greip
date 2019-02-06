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

import java.util.Arrays;
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
import org.eclipse.swt.widgets.Widget;

public final class Util {

	private static final String ELLIPSES = "..."; //$NON-NLS-1$

	public static Cursor createCursor(final Device device, final Point size, final Point hotspot, final Consumer<GC> painter) {
		final PaletteData palette = new PaletteData(new RGB[] { new RGB(0, 0, 0), new RGB(255, 255, 255) });

		return withResource(new Image(device, new ImageData(size.x, size.y, 1, palette)), img -> {
			return withResource(new GC(img), gc -> {
				gc.fillRectangle(0, 0, size.x, size.y);
				painter.accept(gc);

				final ImageData source = img.getImageData();
				final ImageData mask = new ImageData(size.x, size.y, 1, palette);

				return new Cursor(device, source, mask, hotspot.x, hotspot.y);
			});
		});
	}

	public static RGB getDimmedRGB(final RGB rgb, final float brightnessOffset) {
		final float[] hsb = rgb.getHSB();
		return new RGB(hsb[0], hsb[1], Math.max(0, Math.min(1.0f, hsb[2] + brightnessOffset)));
	}

	@SafeVarargs
	public static <T> T nvl(final T... o) {
		for (final T t : o) {
			if (t != null) return t;
		}
		return null;
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

	public static int getSimilarColor(final RGB[] rgbs, final RGB rgb) {
		final float[] hsb = rgb.getHSB();
		float minDiff = Float.MAX_VALUE;
		int idx = 0;

		for (int i = 0; i < rgbs.length && minDiff > 0.0f; i++) {
			final float[] hsb2 = rgbs[i].getHSB();
			final float deltaHue = (hsb2[0] - hsb[0]) / 360;
			final float deltaSaturation = hsb2[1] - hsb[1];
			final float deltaBrightness = hsb2[2] - hsb[2];
			final float diff = deltaHue * deltaHue + deltaSaturation * deltaSaturation + deltaBrightness * deltaBrightness;

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

	public static void withFont(final GC gc, final Font font, final Consumer<Font> consumer) {
		withFont(gc, font, f -> {
			consumer.accept(f);
			return null;
		});
	}

	public static <O> O withFont(final GC gc, final Font font, final Function<Font, O> consumer) {
		final Font currentFont = gc.getFont();

		try {
			gc.setFont(font);
			return consumer.apply(font);
		} finally {
			font.dispose();
			gc.setFont(currentFont);
		}
	}

	public static <O> void whenNotNull(final O object, final Consumer<O> c) {
		whenNotNull(object, () -> c.accept(object));
	}

	public static <O> void whenNotNull(final O object, final Runnable r) {
		when(object != null, r);
	}

	public static void when(final boolean condition, final Runnable r) {
		if (condition) {
			r.run();
		}
	}

	public static boolean in(final int value, final int... values) {
		for (final int v : values) {
			if (v == value) return true;
		}
		return false;
	}

	@SafeVarargs
	public static <T> boolean in(final T value, final T... values) {
		return Arrays.asList(values).contains(value);
	}

	public static <R extends Resource> R checkResource(final R resource, final boolean nullable) {
		if (!nullable && resource == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (resource != null && resource.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return resource;
	}

	public static <W extends Widget> W checkWidget(final W widget, final boolean nullable) {
		if (!nullable && widget == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (widget != null && widget.isDisposed()) SWT.error(SWT.ERROR_WIDGET_DISPOSED);
		return widget;
	}

	public static boolean whenAnySet(final int style, final int... bits) {
		for (final int bit : bits) {
			if ((style & bit) > 0) {
				return true;
			}
		}
		return false;
	}

	public static RGB hexToRGB(final String hex) {
		if (!hex.matches("#[0-9A-F]{0,6}")) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		final int color = Integer.parseInt(hex.concat("000000").substring(1, 7), 16);

		final int blue = color & 0xff;
		final int green = color >> 8 & 0xff;
		final int red = color >> 16 & 0xff;

		return new RGB(red, green, blue);
	}

	public static float getZoom(final Device device) {
		return device.getDPI().x / 96.0f;
	}
}
