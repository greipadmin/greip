/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.greip.common.Util;

/**
 * This class paints a border around a widget.
 *
 * @author Thomas Lorbeer
 */
public class BorderPainter {

	private final IBorderable borderable;

	/**
	 * Creates a new instance of the painter.
	 *
	 * @param borderable
	 */
	public BorderPainter(final IBorderable borderable) {
		this.borderable = borderable;
	}

	/**
	 * The mothod paints the border to GC.
	 *
	 * @param gc
	 *        GC
	 * @param edgeBackground
	 *        The backgound color that is used for paintig edges.
	 */
	public void doPaint(final GC gc, final Color edgeBackground) {
		final int radius = borderable.getEdgeRadius();
		final int lineWidth = borderable.getBorderWidth();
		final Point size = borderable.getSize();

		gc.setAntialias(SWT.ON);
		gc.setLineWidth(1);
		gc.setLineStyle(SWT.LINE_SOLID);

		if (radius > 0) {
			gc.setForeground(edgeBackground);
			final ImageData imageData = createRoundedEdgeMask(gc);

			for (int x = 0; x < radius + lineWidth * 2; x++) {
				for (int y = 0; y < radius + lineWidth * 2; y++) {
					if (imageData.getPixel(x, y) >= 0) {
						gc.drawLine(x, -1, x, y);
						gc.drawLine(size.x - x - 1, -1, size.x - x - 1, y);
						gc.drawLine(x, size.y, x, size.y - y - 1);
						gc.drawLine(size.x - x - 1, size.y, size.x - x - 1, size.y - y - 1);
						break;
					}
				}
			}
		}

		if (lineWidth > 0) {
			gc.setForeground(borderable.getBorderColor());
			gc.setLineWidth(lineWidth);
			gc.drawRoundRectangle(lineWidth / 2, lineWidth / 2, size.x - lineWidth, size.y - lineWidth, radius * 2, radius * 2);
		}
	}

	private ImageData createRoundedEdgeMask(final GC gc) {
		final int radius = borderable.getEdgeRadius();
		final int lineWidth = borderable.getBorderWidth();
		final Point size = borderable.getSize();

		return Util.withResource(new Image(gc.getDevice(), size.x, size.y), image -> {
			Util.withResource(new GC(image), imageGC -> {
				if (lineWidth == 0) imageGC.setAntialias(SWT.ON);
				imageGC.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
				imageGC.setLineWidth(lineWidth);
				imageGC.drawRoundRectangle(lineWidth / 2, lineWidth / 2, size.x - lineWidth, size.y - lineWidth, radius * 2, radius * 2);
			});

			return image.getImageData();
		});
	}
}
