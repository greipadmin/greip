/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.decorator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
	private ImageData[] scaledImages;
	private int idx;
	private boolean animated;
	private Point scaleTo = new Point(SWT.DEFAULT, SWT.DEFAULT);
	private Point imageSize = new Point(0, 0);

	/**
	 * Creates a new instance of the decorator.
	 *
	 * @param parent
	 *        the parent control, <code>null</code> not allowed.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            </ul>
	 */
	public ImageDecorator(final Control parent) {
		super(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.greip.decorator.IDecorator#doPaint(org.eclipse.swt.graphics.GC,
	 * int, int)
	 */
	@Override
	public synchronized void doPaint(final GC gc, final int x, final int y) {
		if (images != null) {
			Util.withResource(new Image(getDisplay(), getScaledImage(idx)), (final Image img) -> gc.drawImage(img, x, y));
		}
	}

	/**
	 * Returns the size of the decorator. The size is calculated by native image
	 * size or the size defined by {@link #scaleTo(Point)}. If no image is set
	 * then width and height 0 returned.
	 *
	 * @return the size
	 */
	@Override
	public Point getSize() {
		if (images == null) {
			return new Point(0, 0);
		} else if (scaleTo.x == SWT.DEFAULT && scaleTo.y == SWT.DEFAULT) {
			return imageSize;
		} else if (scaleTo.x == SWT.DEFAULT) {
			return new Point(imageSize.x * scaleTo.y / imageSize.y, scaleTo.y);
		} else if (scaleTo.y == SWT.DEFAULT) {
			return new Point(scaleTo.x, scaleTo.x * imageSize.y / imageSize.x);
		}
		return scaleTo;
	}

	/**
	 * Loads an Image from the specified input stream. Throws an error if either
	 * an error occurs while loading the image, or if the image are not of a
	 * supported type.
	 *
	 * @param stream
	 *        the input stream to load the images from
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the stream is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_IO - if an IO error occurs while reading from the
	 *            stream</li>
	 *            <li>ERROR_INVALID_IMAGE - if the image stream contains invalid
	 *            data</li>
	 *            <li>ERROR_UNSUPPORTED_FORMAT - if the image stream contains an
	 *            unrecognized format</li>
	 *            </ul>
	 */
	public void loadImage(final InputStream stream) {
		setImages(imageLoader.load(stream));
	}

	/**
	 * Loads an Image from the file with the specified name. Throws an error if
	 * either an error occurs while loading the images, or if the images are not
	 * of a supported type.
	 *
	 * @param filename
	 *        the name of the file to load the images from
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the file name is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_IO - if an IO error occurs while reading from the
	 *            file</li>
	 *            <li>ERROR_INVALID_IMAGE - if the image file contains invalid
	 *            data</li>
	 *            <li>ERROR_UNSUPPORTED_FORMAT - if the image file contains an
	 *            unrecognized format</li>
	 *            </ul>
	 */
	public void loadImage(final String filename) {
		setImages(imageLoader.load(filename));
	}

	/**
	 * Sets the decorators image or removes the current image from decorator if
	 * image set to <code>null</code>.
	 *
	 * @param image
	 *        the new image
	 */
	public synchronized void setImage(final Image image) {
		if (image == null) {
			images = null;
		} else {
			setImages(image.getImageData());
		}
	}

	/**
	 * Defines the image size.
	 *
	 * @param scaleTo
	 *        the new image size. Use SWT.DEFAULT for native image size. The
	 *        minimum height and width are 1 pixel.
	 */
	public void scaleTo(final Point scaleTo) {
		if (scaleTo == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (scaleTo.x != -1 && scaleTo.x <= 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		if (scaleTo.y != -1 && scaleTo.y <= 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		this.scaleTo = scaleTo;
	}

	private synchronized void setImages(final ImageData... imageDatas) {
		createImages(imageDatas);
		idx = 0;

		if (!animated) {
			doAnimate();
		}
		getParent().redraw();
	}

	private void createImages(final ImageData... imageData) {
		final Display display = getDisplay();

		images = new ImageData[imageData.length];
		scaledImages = new ImageData[imageData.length];

		if (imageData.length == 1) {
			imageSize = new Point(imageData[0].width, imageData[0].height);
			images[0] = imageData[0];
			scaledImages[0] = images[0];

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
					scaledImages[i] = images[i];
				}

				Util.whenNotNull(bgColor, color -> color.dispose());
			});
		});
	}

	private ImageData createFrame(final Image drawingArea, final GC gc, final ImageData imageData) {
		return Util.withResource(new Image(getDisplay(), imageData, imageData.getTransparencyMask()), img -> {
			gc.drawImage(img, imageData.x, imageData.y);

			final ImageData frameData = drawingArea.getImageData();
			frameData.delayTime = imageData.delayTime;

			return frameData;
		});
	}

	private ImageData getScaledImage(final int idx) {
		final Point size = getSize();
		final Point oldScaledSize = new Point(scaledImages[idx].width, scaledImages[idx].height);

		if (!oldScaledSize.equals(size)) {
			Util.withResource(new Image(getDisplay(), size.x, size.y), tmpImg -> {
				Util.withResource(new GC(tmpImg), tmpGC -> {
					Util.withResource(new Image(getDisplay(), images[idx]), img -> {
						tmpGC.setBackground(getParent().getBackground());
						tmpGC.fillRectangle(0, 0, size.x, size.y);
						if (size.x < images[0].width && size.y < images[0].height) {
							tmpGC.setInterpolation(SWT.LOW);
						} else {
							tmpGC.setInterpolation(SWT.HIGH);
						}
						tmpGC.drawImage(img, 0, 0, imageSize.x, imageSize.y, 0, 0, size.x, size.y);
					});

					scaledImages[idx] = tmpImg.getImageData();
				});
			});
		}

		return scaledImages[idx];
	}

	private synchronized void doAnimate() {
		animated = true;

		getDisplay().timerExec(Math.max(5, images[idx].delayTime) * 10, () -> {
			if (getParent().isDisposed() || images == null) {
				animated = false;
			} else if (images.length == 1) {
				animated = false;
				getParent().redraw();
			} else {
				idx = ++idx % images.length;
				doAnimate();
				getParent().redraw();
			}
		});
	}
}