/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.greip.common.Util;

public final class ImageDecorator extends AbstractDecorator {

	private final ImageLoader imageLoader = new ImageLoader();

	private ImageData[] images;
	private int idx;
	private boolean animated;
	private Point scaleTo = new Point(SWT.DEFAULT, SWT.DEFAULT);
	private Point imageSize = new Point(0, 0);

	public ImageDecorator(final Control parent) {
		super(parent);
	}

	private void createImages(final ImageData... imageData) {
		final Display display = getDisplay();

		images = new ImageData[imageData.length];

		if (imageData.length == 1) {
			imageSize = new Point(imageData[0].width, imageData[0].height);
			images[0] = imageData[0];

			return;
		}

		imageSize = new Point(imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);

		Util.withResource(new Image(display, imageSize.x, imageSize.y), drawingArea -> {
			Util.withResource(new GC(drawingArea), gc -> {
				Color bgColor = null;

				if (imageLoader.backgroundPixel != -1) {
					bgColor = new Color(display, imageData[0].palette.getRGB(imageLoader.backgroundPixel));
					gc.setBackground(bgColor);
				}

				for (int i = 0; i < imageData.length; i++) {
					if (imageData[0].disposalMethod == SWT.DM_FILL_BACKGROUND) {
						gc.fillRectangle(0, 0, imageSize.x, imageSize.y);
					}
					images[i] = createFrame(drawingArea, gc, imageData[i]);
				}

				Util.whenNotNull(bgColor, bgColor::dispose);
			});
		});
	}

	private ImageData createFrame(final Image drawingArea, final GC gc, final ImageData imageData) {
		return Util.withResource(new Image(getDisplay(), imageData, imageData.getTransparencyMask()), img -> {
//			if (imageData.disposalMethod != SWT.DM_FILL_NONE) {
//				gc.setBackground(getParent().getBackground());
//				gc.fillRectangle(0, 0, imageSize.x, imageSize.y);
//			}
			gc.drawImage(img, imageData.x, imageData.y);

			final ImageData frameData = drawingArea.getImageData();
			frameData.delayTime = imageData.delayTime;

			return frameData;
		});
	}

	private void doAnimate() {
		animated = true;

		getDisplay().timerExec(Math.max(5, images[idx].delayTime) * 10, () -> {
			if (getParent().isDisposed() || images == null) {
				animated = false;
			} else if (images.length == 1) {
				animated = false;
				getParent().redraw();
			} else {
				idx = ++idx % images.length;
				getParent().redraw();
				doAnimate();
			}
		});
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		if (images != null) {
			Util.withResource(new Image(getDisplay(), images[idx]), img -> {
				Util.withResource(new Image(getDisplay(), getSize().x, getSize().y), tmpImg -> {
					Util.withResource(new GC(tmpImg), tmpGC -> {
						tmpGC.setBackground(getParent().getBackground());
						tmpGC.fillRectangle(0, 0, getSize().x, getSize().y);
						tmpGC.setInterpolation(SWT.HIGH);
						tmpGC.drawImage(img, 0, 0, imageSize.x, imageSize.y, 0, 0, getSize().x, getSize().y);
					});
					gc.drawImage(tmpImg, x, y);
				});
			});
		}
	}

	@Override
	public Point getSize() {
		if (scaleTo.x == SWT.DEFAULT && scaleTo.y == SWT.DEFAULT) {
			return imageSize;
		} else if (scaleTo.x == SWT.DEFAULT) {
			return new Point(imageSize.x * scaleTo.y / imageSize.y, scaleTo.y);
		} else if (scaleTo.y == SWT.DEFAULT) {
			return new Point(scaleTo.x, scaleTo.x * imageSize.y / imageSize.x);
		}
		return scaleTo;
	}

	public void loadImage(final InputStream stream) {
		setImages(imageLoader.load(stream));
	}

	public void loadImage(final String filename) {
		setImages(imageLoader.load(filename));
	}

	public void setImage(final Image image) {
		setImages(image.getImageData());
	}

	private void setImages(final ImageData... imageDatas) {
		createImages(imageDatas);
		idx = 0;

		if (!animated) {
			doAnimate();
		}
		getParent().redraw();
	}

	public void scaleTo(final Point scaleTo) {
		this.scaleTo = scaleTo;
		doAnimate();
	}
}