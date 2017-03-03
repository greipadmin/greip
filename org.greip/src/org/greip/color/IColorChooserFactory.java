/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.color;

import org.eclipse.swt.widgets.Composite;

/**
 * A <code>IColorChooserFactory</code> represents a factory for creating color
 * choosers. This factories are used by {@link ColorButton}.
 *
 * @author Thomas Lorbeer
 */
@FunctionalInterface
public interface IColorChooserFactory {

	/**
	 * This method is called for creating a new color chooser instance.
	 *
	 * @param parent
	 *        a composite control which will be the parent of the new instance
	 *        (cannot be null)
	 *
	 * @return The new color chooser instance.
	 *
	 * @see ColorChooserHSB
	 * @see ColorChooserRGB
	 * @see ColorWheelChooser
	 * @see ColorPicker
	 */
	AbstractColorChooser create(Composite parent);
}
