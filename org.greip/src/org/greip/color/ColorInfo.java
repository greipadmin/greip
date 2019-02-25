/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ColorInfo extends Label {

	public ColorInfo(final Composite parent) {
		super(parent, SWT.CENTER);
		setOrientation(SWT.LEFT_TO_RIGHT);
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	public void setRGB(final RGB rgb) {
		final String msg = "#%s   R:\u2006%d\u2000G:\u2006%d\u2000B:\u2006%d";
		setText(String.format(msg, toHex(rgb), Integer.valueOf(rgb.red), Integer.valueOf(rgb.green), Integer.valueOf(rgb.blue)));
	}

	private static String toHex(final RGB rgb) {
		return String.format("%1$-6s", Integer.toHexString(rgb.hashCode())).toUpperCase().replace(' ', '0');
	}
}
