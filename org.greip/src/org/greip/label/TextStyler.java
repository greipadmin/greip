/**
 * Copyright (c) 2017 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.label;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;

class TextStyler extends Styler {

	private final Font font;
	private final Color background;
	private final Color foreground;
	private final boolean underline;
	private final boolean strikeout;
	private final Object data;

	public TextStyler(final Font font, final Color background, final Color foreground, final boolean underline, final boolean strikeout,
			final Object data) {
		this.font = font;
		this.background = background;
		this.foreground = foreground;
		this.underline = underline;
		this.strikeout = strikeout;
		this.data = data;
	}

	@Override
	public void applyStyles(final TextStyle style) {
		style.underline = underline;
		style.strikeout = strikeout;
		style.font = font;
		style.foreground = foreground;
		style.background = background;
		style.data = data;
	}
}