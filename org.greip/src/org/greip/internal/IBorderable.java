/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.internal;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public interface IBorderable {

	Color getBorderColor();

	int getCornerRadius();

	int getBorderWidth();

	Point getSize();
}
