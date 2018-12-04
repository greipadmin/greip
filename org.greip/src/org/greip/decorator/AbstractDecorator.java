/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractDecorator implements IDecorator {

	private final Control parent;

	protected AbstractDecorator(final Control parent) {
		this.parent = parent;
		parent.addListener(SWT.Dispose, e -> dispose());
	}

	protected void dispose() {
		// nothing to do
	}

	protected Display getDisplay() {
		return parent.getDisplay();
	}

	protected Control getParent() {
		return parent;
	}

	protected void redraw() {
		parent.redraw();
	}
}
