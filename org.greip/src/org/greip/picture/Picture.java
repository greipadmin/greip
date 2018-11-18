/**
 * Copyright (c) 2016 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.picture;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.greip.decorator.ImageDecorator;

/**
 * This class represents a non-selectable user interface object that displays an
 * image. Supported picture formats are PNG, BMP, JPEG, GIF (including animated
 * GIF), ICO and TIFF.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @author Thomas Lorbeer
 */
public class Picture extends Composite {

	private final ImageDecorator decorator;
	private Point scaleTo;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
	 * constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * <p>
	 * The size of the widget is the scaled size of the image. Is no image
	 * loaded, the size is Point(0, 0).
	 * </p>
	 *
	 * @param parent
	 *        a composite control which will be the parent of the new instance
	 *        (cannot be null)
	 * @param style
	 *        the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the parent</li>
	 *            </ul>
	 *
	 * @see SWT#BORDER
	 */
	public Picture(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);

		decorator = new ImageDecorator(this);
		addListener(SWT.Paint, e -> {
			if (scaleTo == null && e.height > 0 && e.width > 0) {
				decorator.scaleTo(new Point(e.width, e.height));
			}
			decorator.doPaint(e.gc, new Point(0, 0));
		});

		scaleTo(new Point(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		return decorator.getSize();
	}

	/**
	 * Loads an image from the specified input stream. Throws an error if either
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
		checkWidget();
		decorator.loadImage(stream);
		setSize(decorator.getSize());
	}

	/**
	 * Loads an image from the file with the specified name. Throws an error if
	 * either an error occurs while loading the image, or if the image are not of
	 * a supported type.
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
		checkWidget();
		decorator.loadImage(filename);
		setSize(decorator.getSize());
	}

	/**
	 * Sets the image to the argument, which may be null indicating that no image
	 * should be displayed.
	 *
	 * @param image
	 *        the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *            disposed</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setImage(final Image image) {
		checkWidget();
		decorator.setImage(image);
		setSize(decorator.getSize());
	}

	/**
	 * Scale the image to specified size. Default is <code>Point(SWT.DEFAULT,
	 * SWT.DEFAULT)</code>, that means the original image size.
	 * <code>Point(100, SWT.DEFAULT)</code> means scale to width 100px and
	 * calculate the new height.
	 *
	 * @param scaleTo
	 *        the new image size.
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            <li>ERROR_NULL_ARGUMENT - if the new size is null</li>
	 *            </ul>
	 */
	public void scaleTo(final Point scaleTo) {
		checkWidget();
		this.scaleTo = scaleTo;
		decorator.scaleTo(scaleTo == null ? new Point(SWT.DEFAULT, SWT.DEFAULT) : scaleTo);
		setSize(decorator.getSize());
	}
}
