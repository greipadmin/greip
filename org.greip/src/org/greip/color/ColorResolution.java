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

public enum ColorResolution {

	Minimal(
		4,
		12),

	Low(
		6,
		18),

	Medium(
		10,
		24),

	High(
		12,
		30),

	Maximal(
		80,
		-1);

	final int saturationSteps;
	final int hueSteps;

	ColorResolution(final int saturationSteps, final int hueSteps) {
		this.saturationSteps = saturationSteps;
		this.hueSteps = hueSteps;
	}
}