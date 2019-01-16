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
import org.greip.common.Util;

/**
 * AbstractDecorator is the base implementation of {@link IDecorator}.
 *
 * @see IDecorator
 *
 * @author Thomas Lorbeer
 */
public abstract class AbstractDecorator implements IDecorator {

	private final Control parent;

	/**
	 * Constructs a new instance.
	 *
	 * @param parent
	 *        the parent control to paint the decorator, can not be null.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            <li>ERROR_WIDGET_DISPOSED - if the parent is disposed</li>
	 *            </ul>
	 */
	protected AbstractDecorator(final Control parent) {
		this.parent = Util.checkWidget(parent, false);
		this.parent.addListener(SWT.Dispose, e -> dispose());
	}

	/**
	 * This methode is called when the parent control is disposed.
	 */
	protected void dispose() {
		// nothing to do
	}

	/**
	 * This method returns the display of the parent control.
	 *
	 * @return the display
	 */
	protected Display getDisplay() {
		return parent.getDisplay();
	}

	/**
	 * Returns the parent control.
	 *
	 * @return the parent control
	 */
	protected Control getParent() {
		return parent;
	}

	/**
	 * Paint the decorator by force a redraw on parent control.
	 */
	protected void redraw() {
		parent.redraw();
	}
}
