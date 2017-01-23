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
import org.eclipse.swt.widgets.Display;
import org.greip.common.Util;

public final class ImageDecorator extends AbstractDecorator {

	private Image[] images;
	private int idx = 0;
	private final ImageLoader imageLoader = new ImageLoader();
	private int minDelay = 100;
	private boolean animated;
	private boolean disposed;
	private Color background;

	private synchronized Image[] createImages(final ImageData... imageData) {
		final Display display = getDisplay();
		final Point imageSize;

		disposeImages();

		if (imageData.length == 1) {
			imageSize = new Point(imageData[0].width, imageData[0].height);
		} else {
			imageSize = new Point(imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);
		}

		final Image offScreenImage = new Image(display, imageSize.x, imageSize.y);
		final GC imageGC = new GC(offScreenImage);
		final Image[] images = new Image[imageData.length];

		Color bgColor = null;
		for (int i = 0; i < imageData.length; i++) {
			if (i == 0 && imageLoader.backgroundPixel != -1) {
				bgColor = new Color(display, imageData[i].palette.getRGB(imageLoader.backgroundPixel));
				imageGC.setBackground(bgColor);
			}
			if (imageData[i].disposalMethod == SWT.DM_FILL_BACKGROUND) {
				final Color defaultBackground = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
				imageGC.setBackground(Util.nvl(background, defaultBackground));
				imageGC.fillRectangle(0, 0, imageSize.x, imageSize.y);
			}
			imageGC.drawImage(new Image(display, imageData[i]), imageData[i].x, imageData[i].y);
			imageGC.dispose();
			images[i] = new Image(display, offScreenImage.getImageData());

			if (bgColor != null) {
				bgColor.dispose();
				bgColor = null;
			}
		}

		return images;
	}

	@Override
	public void dispose() {
		super.dispose();
		disposeImages();
		disposed = true;
	}

	private void disposeImages() {
		if (images != null) {
			for (final Image image : images) {
				image.dispose();
			}
			idx = 0;
		}
	}

	private synchronized void doAnimate() {
		final int delayTime = images[idx].getImageData().delayTime * 10;
		final int usedDelay = Math.max(getMinDelay(), delayTime);

		animated = true;

		getDisplay().timerExec(usedDelay, new Runnable() {
			@Override
			public void run() {
				if (disposed || images == null) {
					animated = false;
				} else if (images.length == 1) {
					animated = false;
					fireSettingsChangedEvent();
				} else {
					idx = (idx + 1) % (images.length - 1);
					fireSettingsChangedEvent();
					doAnimate();
				}
			}
		});
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
		if (images != null) {
			gc.fillRectangle(x, y, getSize().x - 1, getSize().y - 1);
			gc.drawImage(images[idx], x, y);
		}
	}

	public int getMinDelay() {
		return minDelay;
	}

	@Override
	public Point getSize() {
		return images == null ? new Point(0, 0) : new Point(images[0].getBounds().width, images[0].getBounds().height);
	}

	public void loadImage(final InputStream stream) {
		setImages(imageLoader.load(stream));
	}

	public void loadImage(final String filename) {
		setImages(imageLoader.load(filename));
	}

	public void setBackground(final Color background) {
		this.background = background;
	}

	public void setImage(final Image image) {
		setImages(image.getImageData());
	}

	private void setImages(final ImageData... imageDatas) {
		this.images = createImages(imageDatas);
		if (!animated) {
			doAnimate();
		}
		fireSettingsChangedEvent();
	}

	public void setMinDelay(final int minDelay) {
		this.minDelay = minDelay;
	}
}