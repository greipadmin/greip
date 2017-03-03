/**
 * Copyright (c) 2017 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

class ColorHistory extends Composite {

	private final ColorHistoryList history = ColorHistoryList.INSTANCE;
	private final Control[] historyItems = new Control[9];

	public ColorHistory(final AbstractColorChooser parent) {
		super(parent, SWT.NONE);

		setLayout(GridLayoutFactory.fillDefaults().margins(0, 10).spacing(0, 4).create());

		for (int i = 0; i < historyItems.length; i++) {
			final RGB rgb = history.get(i);

			historyItems[i] = new Label(this, SWT.BORDER);
			historyItems[i].setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(9, 9).indent(10, 0).create());

			historyItems[i].addListener(SWT.MouseDoubleClick, e -> {
				parent.setRGB(((Control) e.widget).getBackground().getRGB());
				parent.notifyListeners(SWT.Selection, new Event());
			});

			if (rgb == null) {
				historyItems[i].setBackground(getBackground());
				historyItems[i].setEnabled(false);
			} else {
				final Color color = new Color(getDisplay(), rgb);
				historyItems[i].setBackground(color);
				historyItems[i].setEnabled(true);
				color.dispose();
			}
		}
	}
}
