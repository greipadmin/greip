/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.samples;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractSample {

	protected final Display display;
	protected final Shell shell;

	protected AbstractSample() {
		display = new Display();
		shell = new Shell(display);
	}

	protected void show(final String windowTitle) {

		layout();

		if (!shell.isDisposed()) {
			shell.setLayout(new GridLayout());
			shell.setText(windowTitle);
			shell.pack();
			shell.open();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();
		}
	}

	protected abstract void layout();
}
