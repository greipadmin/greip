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
import org.greip.common.Util;

class ColorHistory extends Composite {

	private final ColorHistoryList history = ColorHistoryList.INSTANCE;

	public ColorHistory(final AbstractColorChooser parent) {
		super(parent, SWT.NONE);

		setLayout(GridLayoutFactory.fillDefaults().margins(0, 10).spacing(0, 4).create());

		for (int i = 0; i < 9; i++) {
			final RGB rgb = history.get(i);

			final Label label = new Label(this, SWT.BORDER);
			label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(9, 9).indent(10, 0).create());

			label.addListener(SWT.MouseDoubleClick, e -> {
				parent.setRGB(((Control) e.widget).getBackground().getRGB());
				parent.notifyListeners(SWT.Selection, new Event());
			});

			if (rgb == null) {
				label.setBackground(getBackground());
				label.setEnabled(false);
			} else {
				Util.withResource(new Color(getDisplay(), rgb), color -> {
					label.setBackground(color);
					label.setEnabled(true);
				});
			}
		}
	}
}
