/**
 * Copyright (c) 2016 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.separator;

/**
 * This enum contains line style constants.
 *
 * @see Separator#setLineStyle(LineStyle)
 *
 * @author Thomas Lorbeer
 */
public enum LineStyle {
	/**
	 * Defines a solid line style.
	 */
	Solid(
		1),
	/**
	 * Defines a dashed line style.
	 */
	Dash(
		2),
	/**
	 * Defines a dotted line style.
	 */
	Dot(
		3),
	/**
	 * Defines a dash-dotted line style.
	 */
	DashDot(
		4),
	/**
	 * Defines a dash-dot-dot line style.
	 */
	DashDotDot(
		5),
	/**
	 * Defines a custom defined line style.
	 */
	Custom(
		6),
	/**
	 * Defines a line with shadow in behavior.
	 */
	ShadowIn(
		7),
	/**
	 * Defines a line with shadow out behavior.
	 */
	ShadowOut(
		8);

	private final int value;

	private LineStyle(final int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}
}