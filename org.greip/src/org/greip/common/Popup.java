/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class Popup extends Shell {

	private final Control control;

	public Popup(final Control control) {
		super(control.getShell(), SWT.TOOL);
		this.control = control;

		setBackgroundMode(SWT.INHERIT_FORCE);
		addListener(SWT.Deactivate, e -> dispose());
		addListener(SWT.Traverse, e -> Util.when(e.detail == SWT.TRAVERSE_ESCAPE, this::close));
	}

	protected void block() {
		final Display display = getDisplay();
		while (!isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Override
	protected final void checkSubclass() {
		// nothing to do
	}

	private Point computeLocation(final Control control) {
		final Rectangle screenSize = control.getDisplay().getClientArea();
		final Point size = getSize();
		final Point controlSize = control.getSize();
		final Point controlLocation = control.toDisplay(0, 0);
		final int borderWidth = control instanceof Button ? 1 : control.getBorderWidth();

		controlLocation.x -= borderWidth;
		controlLocation.y -= borderWidth;

		if (control instanceof Button) {
			controlLocation.x += 2;
		}

		final List<Rectangle> positions = new ArrayList<>();
		// bottom
		positions.add(new Rectangle(controlLocation.x, controlLocation.y + controlSize.y, size.x, size.y));
		positions.add(new Rectangle(controlLocation.x + controlSize.x, controlLocation.y, size.x, size.y));
		positions.add(new Rectangle(controlLocation.x - size.x, controlLocation.y, size.x, size.y));
		// top
		positions.add(new Rectangle(controlLocation.x, controlLocation.y - size.y, size.x, size.y));
		positions.add(new Rectangle(controlLocation.x + controlSize.x, controlLocation.y + controlSize.y - size.y, size.x, size.y));
		positions.add(new Rectangle(controlLocation.x - size.x, controlLocation.y + controlSize.y - size.y, size.x, size.y));
		// center
		positions.add(new Rectangle((screenSize.width - size.x) / 2, controlLocation.y + controlSize.y, size.x, size.y));
		positions.add(new Rectangle(controlLocation.x + controlSize.x, (screenSize.height - size.y) / 2, size.x, size.y));
		positions.add(new Rectangle(controlLocation.x - size.x, (screenSize.height - size.y) / 2, size.x, size.y));
		positions.add(new Rectangle((screenSize.width - size.x) / 2, controlLocation.y - size.y, size.x, size.y));
		positions.add(new Rectangle((screenSize.width - size.x) / 2, (screenSize.height - size.y) / 2, size.x, size.y));

		for (final Rectangle r : positions) {
			if (r.x > screenSize.x && r.y > screenSize.y && r.height + r.y < screenSize.height && r.width + r.x < screenSize.width) {
				return new Point(r.x, r.y);
			}
		}

		return new Point(0, 0);
	}

	@Override
	public final void open() {
		layout(true, true);
		pack();
		setLocation(computeLocation(control));
		super.open();
		setFocus();
		block();
	}
}
