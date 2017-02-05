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
import java.util.Arrays;

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

	private Image[] images;
	private int[] delays;
	private int idx;
	private final ImageLoader imageLoader = new ImageLoader();
	private boolean animated;
	private boolean disposed;
	private final Point scaleTo;
	private Point imageSize;

	public ImageDecorator(final Control parent) {
		this(parent, new Point(SWT.DEFAULT, SWT.DEFAULT));
	}

	public ImageDecorator(final Control parent, final Point scaleTo) {
		super(parent);
		this.scaleTo = scaleTo;
	}

	private void createImages(final ImageData... imageData) {
		final Display display = getDisplay();

		disposeImages();

		if (imageData.length == 1) {
			imageSize = new Point(imageData[0].width, imageData[0].height);
		} else {
			imageSize = new Point(imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);
		}

		final Image offScreenImage = new Image(display, imageSize.x, imageSize.y);

		images = new Image[imageData.length];
		delays = new int[imageData.length];

		Util.withResource(new GC(offScreenImage), imageGC -> {
			Color bgColor = null;

			if (imageLoader.backgroundPixel != -1) {
				bgColor = new Color(display, imageData[0].palette.getRGB(imageLoader.backgroundPixel));
				imageGC.setBackground(bgColor);
			}

			for (int i = 0; i < imageData.length; i++) {
				if (imageData[0].disposalMethod == SWT.DM_FILL_BACKGROUND) {
					imageGC.fillRectangle(0, 0, imageSize.x, imageSize.y);
				}

				imageGC.drawImage(new Image(display, imageData[i]), imageData[i].x, imageData[i].y);

				images[i] = new Image(display, offScreenImage.getImageData().scaledTo(getSize().x, getSize().y));
				delays[i] = imageData[i].delayTime;
			}

			Util.whenNotNull(bgColor, bgColor::dispose);
		});
	}

	private Point getImageSize() {
		return imageSize == null ? new Point(0, 0) : imageSize;
	}

	@Override
	protected void dispose() {
		disposeImages();
		disposed = true;
	}

	private void disposeImages() {
		Util.whenNotNull(images, () -> Arrays.stream(images).forEach(Image::dispose));
	}

	private synchronized void doAnimate() {
		animated = true;

		getDisplay().timerExec(Math.max(5, delays[idx]) * 10, () -> {
			if (disposed || images == null) {
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
		Util.whenNotNull(images, () -> gc.drawImage(images[idx], x, y));
	}

	@Override
	public Point getSize() {
		final Point imageSize = getImageSize();
		return new Point(scaleTo.x == SWT.DEFAULT ? imageSize.x : scaleTo.x, scaleTo.y == SWT.DEFAULT ? imageSize.y : scaleTo.y);
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

	private synchronized void setImages(final ImageData... imageDatas) {
		createImages(imageDatas);
		idx = 0;

		if (!animated) {
			doAnimate();
		}
		getParent().redraw();
	}
}