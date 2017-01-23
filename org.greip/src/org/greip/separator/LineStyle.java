/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.separator;

public enum LineStyle {
	Solid(
		1),
	Dash(
		2),
	Dot(
		3),
	DashDot(
		4),
	DashDotDot(
		5),
	Custom(
		6),
	ShadowIn(
		7),
	ShadowOut(
		8);

	private final int value;

	private LineStyle(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}