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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.greip.common.Util;

class ColorHistory extends Composite {

	private final ColorHistoryList history = ColorHistoryList.INSTANCE;

	public ColorHistory(final AbstractColorChooser colorChooser) {
		super(colorChooser, SWT.NONE);

		setLayout(GridLayoutFactory.fillDefaults().spacing(0, 4).create());

		for (int i = 0; i < history.size(); i++) {
			final Label label = new Label(this, SWT.BORDER);

			label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).hint(9, 9).create());
			label.setBackground(getBackground());

			Util.whenNotNull(history.get(i), rgb -> {
				label.addListener(SWT.MouseDoubleClick, e -> {
					colorChooser.setRGB(rgb);
					colorChooser.notifyListeners(SWT.Selection, new Event());
				});

				Util.withResource(new Color(getDisplay(), rgb), label::setBackground);
			});
		}
	}
}
