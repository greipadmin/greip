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

public enum ColorResolution {

	Minimal(
		4.65f,
		12),

	Low(
		6.45f,
		18),

	Medium(
		8.5f,
		24),

	High(
		9.9f,
		30),

	Maximal(
		80,
		-1);

	final float saturationSteps;
	final int hueSteps;

	ColorResolution(final float saturationSteps, final int hueSteps) {
		this.saturationSteps = saturationSteps;
		this.hueSteps = hueSteps;
	}
}