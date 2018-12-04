/**
 * Copyright (c) 2017 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Instances of this class represents a graphical painted on a graphic context.
 * The base implementation is {@link AbstractDecorator}.
 *
 * @see AbstractDecorator
 *
 * @author Thomas Lorbeer
 */
public interface IDecorator {

	/**
	 * Paint the decorator to the specified GC.
	 *
	 * @param gc
	 *        the GC
	 * @param x
	 *        the x coordinate to draw
	 * @param y
	 *        the y coordinate to draw
	 */
	void doPaint(GC gc, int x, int y);

	/**
	 * Returns the size of the decorator.
	 *
	 * @return the size
	 */
	Point getSize();
}
