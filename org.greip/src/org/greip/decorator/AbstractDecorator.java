/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractDecorator implements IDecorator {

	private Cursor cursor;

	@Override
	public void dispose() {
	}

	@Override
	public final void doPaint(final GC gc, final Point pos) {
		doPaint(gc, pos.x, pos.y);
	}

	@Override
	public Cursor getCursor() {
		return cursor;
	}

	protected static Display getDisplay() {
		return Display.getCurrent();
	}

	@Override
	public void setCursor(final Cursor cursor) {
		this.cursor = cursor;
	}
}
