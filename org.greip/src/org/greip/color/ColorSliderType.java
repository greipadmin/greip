/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.swt.graphics.RGB;

public enum ColorSliderType {

	Red(
		(h, v) -> new RGB(0.0f, v.floatValue(), 1.0f),
		(h) -> new RGB(255, 255 - h.getRGB().red, 255 - h.getRGB().red)),

	Green(
		(h, v) -> new RGB(120.0f, v.floatValue(), 1.0f),
		(h) -> new RGB(255 - h.getRGB().green, 255, 255 - h.getRGB().green)),

	Blue(
		(h, v) -> new RGB(240.0f, v.floatValue(), 1.0f),
		(h) -> new RGB(255 - h.getRGB().blue, 255 - h.getRGB().blue, 255)),

	Hue(
		(h, v) -> new RGB(v.floatValue() * 360.0f, 1.0f, 1.0f),
		(h) -> new RGB(h.getHue(), 1.0f, 1.0f)),

	Saturation(
		(h, v) -> new RGB(h.getHue(), v.floatValue(), 1.0f),
		(h) -> new RGB(h.getHue(), h.getSaturation(), 1.0f)),

	Brightness(
		(h, v) -> new RGB(0.0f, 0.0f, v.floatValue()),
		(h) -> new RGB(0.0f, 0.0f, h.getBrightness())),

	Lightness(
		(h, v) -> new RGB(h.getHue(), h.getSaturation(), 1 - v.floatValue()),
		(h) -> new RGB(h.getHue(), h.getSaturation(), h.getBrightness()));

	private BiFunction<HSB, Float, RGB> fSegmentRGB;
	private Function<HSB, RGB> fInitialRGB;

	private ColorSliderType(final BiFunction<HSB, Float, RGB> fSegmentRGB, final Function<HSB, RGB> fInitialRGB) {
		this.fSegmentRGB = fSegmentRGB;
		this.fInitialRGB = fInitialRGB;
	}

	RGB createSegmentRGB(final HSB originalHSB, final float value) {
		return fSegmentRGB.apply(originalHSB, Float.valueOf(value));
	}

	RGB createInitialRGB(final HSB hsb) {
		return fInitialRGB.apply(hsb);
	}
}
