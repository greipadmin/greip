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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public abstract class Popup extends Shell {

	private final Control control;
	private boolean canceled;

	public Popup(final Control control) {
		super(control.getShell(), SWT.TOOL);
		this.control = control;

		setRedraw(false);
		setLayoutDeferred(true);
		setBackgroundMode(SWT.INHERIT_FORCE);
		setLayout(new FillLayout());

		addListener(SWT.Deactivate, e -> {
			dispose();
			canceled = true;
		});

		final Listener traverseListener = e -> {
			if (e.detail == SWT.TRAVERSE_ESCAPE) {
				close();
				canceled = true;
			}
		};

		getDisplay().addFilter(SWT.Traverse, traverseListener);
		addListener(SWT.Dispose, e -> e.display.removeFilter(SWT.Traverse, traverseListener));
	}

	@Override
	protected final void checkSubclass() {
		// allow subclassing
	}

	@Override
	public FillLayout getLayout() {
		return (FillLayout) super.getLayout();
	}

	protected void block() {
		final Display display = getDisplay();
		while (!isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private Point computeLocation(final Control control) {
		final Rectangle screenSize = control.getDisplay().getClientArea();
		final Point size = getSize();
		final Point controlSize = control.getSize();
		final Point controlLocation = control.toDisplay(0, 0);
		final int borderWidth = control instanceof Button ? 0 : control.getBorderWidth();
		final int buttonOffset = control instanceof Button ? 1 : 0;

		controlLocation.x -= borderWidth;
		controlLocation.y -= borderWidth;

		final boolean rtl = getOrientation() == SWT.RIGHT_TO_LEFT;
		final List<Point> positions = new ArrayList<>();

		if (!rtl) {
			// bottom right/left
			positions.add(new Point(controlLocation.x + buttonOffset, controlLocation.y + controlSize.y));
			positions.add(new Point(controlLocation.x - size.x + controlSize.x - buttonOffset, controlLocation.y + controlSize.y));
			// top right/left
			positions.add(new Point(controlLocation.x + buttonOffset, controlLocation.y - size.y));
			positions.add(new Point(controlLocation.x - size.x + controlSize.x - buttonOffset, controlLocation.y - size.y));
			// center right/left
			positions.add(new Point(controlLocation.x + controlSize.x, (screenSize.height - size.y) / 2));
			positions.add(new Point(controlLocation.x - size.x, (screenSize.height - size.y) / 2));
		} else {
			// bottom left/right
			positions.add(new Point(controlLocation.x - size.x - buttonOffset, controlLocation.y + controlSize.y));
			positions.add(new Point(controlLocation.x - controlSize.x + buttonOffset, controlLocation.y + controlSize.y));
			// top left/right
			positions.add(new Point(controlLocation.x - size.x - buttonOffset, controlLocation.y - size.y));
			positions.add(new Point(controlLocation.x - controlSize.x + buttonOffset, controlLocation.y - size.y));
			// center left/right
			positions.add(new Point(controlLocation.x - size.x - controlSize.x, (screenSize.height - size.y) / 2));
			positions.add(new Point(controlLocation.x, (screenSize.height - size.y) / 2));
		}

		// center top/bottom/screen
		positions.add(new Point((screenSize.width - size.x) / 2, controlLocation.y + controlSize.y));
		positions.add(new Point((screenSize.width - size.x) / 2, controlLocation.y - size.y));
		positions.add(new Point((screenSize.width - size.x) / 2, (screenSize.height - size.y) / 2));

		for (final Point p : positions) {
			if (p.x > screenSize.x && p.y > screenSize.y && size.y + p.y < screenSize.height && size.x + p.x < screenSize.width) {
				return p;
			}
		}

		return new Point(0, 0);
	}

	public final boolean open(final Runnable closeHandler) {
		open();
		Util.when(!canceled, closeHandler);
		return !canceled;
	}

	@Override
	public final void open() {
		canceled = false;
		pack();
		setLayoutDeferred(false);
		setLocation(computeLocation(control));
		super.open();
		setRedraw(true);
		setFocus();
		block();
	}

	public boolean isCanceled() {
		return canceled;
	}
}
