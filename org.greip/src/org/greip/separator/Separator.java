/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **/
package org.greip.separator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.greip.common.Greip;
import org.greip.common.Util;
import org.greip.decorator.ImageDecorator;

/**
 * Instances of this class represent a non-selectable user interface object that
 * combines a string, an image and a line.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of HORIZONTAL and VERTICAL may be specified.
 * </p>
 */
public class Separator extends Composite {

	private int lineWidth = 1;
	private LineStyle lineStyle = LineStyle.ShadowIn;
	private int lineCap = SWT.CAP_SQUARE;
	private int[] lineDashs;

	private int orientation;

	private String text;
	private final ImageDecorator imageDecorator = new ImageDecorator(this);

	private int marginHeight;
	private int marginWidth;
	private int indent = 5;
	private int spacing = 3;

	private Color lineColor;
	private Color background;

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
	 * @see SWT#HORIZONTAL
	 * @see SWT#VERTICAL
	 */
	public Separator(final Composite parent, final int style) {
		super(parent, SWT.DOUBLE_BUFFERED);

		this.orientation = (style & SWT.VERTICAL) != 0 ? SWT.VERTICAL : SWT.HORIZONTAL;

		addListener(SWT.Paint, this::onPaint);
		addListener(SWT.MouseUp, this::onMouseDown);
	}

	private void onMouseDown(final Event e) {
		final Rectangle bounds = getImageBounds();

		final Event event = new Event();
		event.detail = bounds.contains(e.x, e.y) ? Greip.IMAGE : SWT.NONE;
		notifyListeners(SWT.Selection, event);
	}

	private void onPaint(final Event e) {
		final int margin = isVertical() ? marginHeight : marginWidth;
		final Transform tr = new Transform(e.display);
		final Rectangle size = getClientArea();

		int width;
		int height;

		if (isVertical()) {
			width = size.height;
			height = size.width;

			tr.translate(height, 0);
			tr.rotate(90);
			e.gc.setTransform(tr);

		} else {
			width = size.width;
			height = size.height;
		}

		final int lineWidth = getLineWidth();
		final Point start = new Point(lineWidth / 2 - (lineWidth + 1) % 2 + margin, height / 2);
		final Point end = new Point(width - lineWidth / 2 - lineWidth % 2 - margin, height / 2);

		e.gc.setBackground(getBackground());
		e.gc.fillRectangle(0, 0, width, height);

		if (lineWidth > 0) {
			if (lineStyle == LineStyle.ShadowIn || lineStyle == LineStyle.ShadowOut) {
				final Color[] colors = getLineColors();

				e.gc.setForeground(colors[isVertical() ? 1 : 0]);
				e.gc.drawLine(start.x - 1, start.y - 1, end.x + 1, start.y - 1);
				e.gc.setForeground(colors[isVertical() ? 0 : 1]);
				e.gc.drawLine(start.x - 1, end.y, end.x + 1, end.y);

				colors[0].dispose();
				colors[1].dispose();

			} else {
				e.gc.setForeground(getLineColor());
				e.gc.setLineWidth(lineWidth);
				e.gc.setLineStyle(lineStyle.getValue());
				e.gc.setLineCap(lineCap);

				if (lineStyle == LineStyle.Custom) {
					e.gc.setLineDash(lineDashs);
				}

				e.gc.drawLine(start.x, start.y, end.x, end.y);
			}
		}

		final Rectangle imageBounds = getImageBounds();
		final Point textSize = e.gc.textExtent(getText(), SWT.DRAW_MNEMONIC);

		if (imageBounds.width != 0 || textSize.x != 0) {
			final int x = (imageBounds.width > 0 ? imageBounds.width + spacing : 0) + margin - 1;
			final int textWidth = Math.min(textSize.x, width - imageBounds.width - indent - 2 * margin - getSpacingCount() * spacing - 20);

			e.gc.fillRectangle(indent + margin, 0, x - margin + textWidth + spacing + (indent == 0 ? 0 : spacing), height);
			imageDecorator.doPaint(e.gc, imageBounds.x, imageBounds.y);

			if (textWidth > 0) {
				final String shortenText = Util.shortenText(e.gc, getText(), textWidth, SWT.DRAW_MNEMONIC);
				e.gc.setForeground(getForeground());
				e.gc.drawText(shortenText, x + indent + (indent == 0 ? 0 : spacing), (height - textSize.y) / 2, SWT.DRAW_MNEMONIC);
			}
		}

		tr.dispose();
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		final Point textSize = getTextSize();
		final int x = imageDecorator.getSize().x + textSize.x + indent + getSpacingCount() * spacing + 20;
		final int y = Math.max(imageDecorator.getSize().y, Math.max(textSize.y, getLineWidth()));
		final int marginX = 2 * (marginWidth + getBorderWidth());
		final int marginY = 2 * (marginHeight + getBorderWidth());

		return isVertical() ? new Point(y + marginX, x + marginY) : new Point(x + marginX, y + marginY);
	}

	@Override
	public Color getBackground() {
		checkWidget();
		return Util.nvl(background, getParent().getBackground());
	}

	@Override
	public void setBackground(final Color background) {
		checkWidget();
		this.background = background;
		redraw();
	}

	public int getIndent() {
		checkWidget();
		return indent;
	}

	public void setIndent(final int indent) {
		checkWidget();
		this.indent = indent;
		redraw();
	}

	/**
	 * Returns the receiver's line cap style, which will be one of the constants
	 * <code>SWT.CAP_FLAT</code>, <code>SWT.CAP_ROUND</code>, or
	 * <code>SWT.CAP_SQUARE</code>.
	 *
	 * @return the cap style used for drawing lines
	 */
	public int getLineCap() {
		checkWidget();
		return lineCap;
	}

	/**
	 * Sets the receiver's line cap style to the argument, which must be one of
	 * the constants <code>SWT.CAP_FLAT</code>, <code>SWT.CAP_ROUND</code>, or
	 * <code>SWT.CAP_SQUARE</code>.
	 *
	 * @param cap
	 *        the cap style to be used for drawing lines
	 */
	public void setLineCap(final int lineCap) {
		checkWidget();
		this.lineCap = lineCap;
		redraw();
	}

	/**
	 * Gets the current line color.
	 *
	 * @return the line color
	 */
	public Color getLineColor() {
		checkWidget();
		return Util.nvl(lineColor, getForeground());
	}

	/**
	 * Sets the line color. If the line style <code>LineStyle.ShadowIn</code> or
	 * <code>LineStyle.ShadowOut</code> the line color is set but not used.
	 *
	 * @param the
	 *        new line color
	 */
	public void setLineColor(final Color lineColor) {
		checkWidget();
		this.lineColor = lineColor;
		redraw();
	}

	private Color[] getLineColors() {
		final RGB backgroundRGB = getBackground().getRGB();
		final Color darkColor = new Color(getDisplay(), Util.getDimmedRGB(backgroundRGB, 0.15f));
		final Color lightColor = new Color(getDisplay(), Util.getDimmedRGB(backgroundRGB, -0.15f));

		return lineStyle == LineStyle.ShadowIn ? new Color[] { lightColor, darkColor } : new Color[] { darkColor, lightColor };
	}

	/**
	 * Returns the receiver's line dash style. The default value is
	 * <code>null</code>.
	 *
	 * @return the line dash style used for drawing lines
	 */
	public int[] getLineDashs() {
		checkWidget();
		return lineDashs;
	}

	/**
	 * Sets the receiver's line dash style to the argument. The default value is
	 * <code>null</code>. If the argument is not <code>null</code>, the
	 * receiver's line style is set to <code>SWT.LINE_CUSTOM</code>.
	 *
	 * @param dashes
	 *        the dash style to be used for drawing lines
	 */
	public void setLineDashs(final int[] lineDashs) {
		checkWidget();
		this.lineDashs = lineDashs;
		redraw();
	}

	/**
	 * Returns the receiver's line style.
	 *
	 * @return the line style
	 */
	public LineStyle getLineStyle() {
		checkWidget();
		return lineStyle;
	}

	/**
	 * Sets the receiver's line style to the argument, which must be one of the
	 * constants <code>SWT.LINE_SOLID</code>, <code>SWT.LINE_DASH</code>,
	 * <code>SWT.LINE_DOT</code>, <code>SWT.LINE_DASHDOT</code> or
	 * <code>SWT.LINE_DASHDOTDOT</code>.
	 *
	 * @param lineStyle
	 *        the style to be used for drawing the line
	 */
	public void setLineStyle(final LineStyle lineStyle) {
		checkWidget();
		this.lineStyle = lineStyle;
		redraw();
	}

	/**
	 * Gets the current line width. The line width for
	 * <code>LineStyle.ShadowIn</code> and <code>LineStyle.ShadowOut</code> is
	 * always 2. The default for all others is 1.
	 *
	 * @return the current line width
	 */
	public int getLineWidth() {
		checkWidget();
		return lineStyle == LineStyle.ShadowIn || lineStyle == LineStyle.ShadowOut ? 2 : lineWidth;
	}

	/**
	 * Sets the width that will be used when drawing the line.
	 * <p>
	 * Note that line width of zero is used as a hint to indicate that the
	 * fastest possible line drawing algorithms should be used. This means that
	 * the output may be different from line width one.
	 * </p>
	 *
	 * @param lineWidth
	 *        the width of a line
	 */
	public void setLineWidth(final int lineWidth) {
		checkWidget();
		this.lineWidth = lineWidth;
		redraw();
	}

	/**
	 * Returns the width of the receiver's top and bottom margin. The default
	 * margin is 0.
	 *
	 * @return the current margin height.
	 */
	public int getMarginHeight() {
		checkWidget();
		return marginHeight;
	}

	/**
	 * Sets the separator's new top and bottom margin.
	 *
	 * @param marginHeight
	 *        the new margin height
	 */
	public void setMarginHeight(final int marginHeight) {
		checkWidget();
		this.marginHeight = marginHeight;
		redraw();
	}

	/**
	 * Returns the width of the receiver's left and right margin. The default
	 * margin is 0.
	 *
	 * @return the current margin width.
	 */
	public int getMarginWidth() {
		checkWidget();
		return marginWidth;
	}

	/**
	 * Sets the separator's new left and reight margin.
	 *
	 * @param marginHeight
	 *        the new margin height
	 */
	public void setMarginWidth(final int marginWidth) {
		checkWidget();
		this.marginWidth = marginWidth;
		redraw();
	}

	/**
	 * Gets the separator's orientation. The default is
	 * <code>SWT.HORIZONTAL</code>.
	 *
	 * @return the current orientation.
	 */
	@Override
	public int getOrientation() {
		checkWidget();
		return orientation;
	}

	/**
	 * Sets the separator's orientation. The argument must be one of the
	 * constants <code>SWT.HORIZONTAL</code> and <code>SWT.VERTICAL</code>.
	 *
	 * @param the
	 *        new orientation.
	 */
	@Override
	public void setOrientation(final int orientation) {
		checkWidget();
		this.orientation = orientation;
		redraw();
	}

	private boolean isVertical() {
		return orientation == SWT.VERTICAL;
	}

	/**
	 * Gets the spacing between text and image in pixels. Default is 3 pixels.
	 *
	 * @return the spacing in pixels
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public int getSpacing() {
		checkWidget();
		return spacing;
	}

	/**
	 * Sets the spacing between text and image in pixels.
	 *
	 * @param spacing
	 *        the spacing in pixels
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setSpacing(final int spacing) {
		checkWidget();
		this.spacing = spacing;
		redraw();
	}

	private int getSpacingCount() {
		return (imageDecorator.getSize().x == 0 ? 0 : 1) + (getText().isEmpty() ? 0 : 1) + (indent == 0 ? 0 : 1);
	}

	/**
	 * Returns the separator's text, which will be an empty string if it has
	 * never been set.
	 *
	 * @return the separator's text
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public String getText() {
		checkWidget();
		return Util.nvl(text, ""); //$NON-NLS-1$
	}

	/**
	 * Sets the separator's text.
	 *
	 * @param string
	 *        the new text
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	public void setText(final String text) {
		checkWidget();
		this.text = text;
		redraw();
	}

	private Point getTextSize() {

		if (getText().isEmpty()) {
			return new Point(0, 0);
		}

		final GC gc = new GC(getDisplay());
		gc.setFont(getFont());
		final Transform tr = new Transform(getDisplay());

		if (isVertical()) {
			tr.rotate(90);
			gc.setTransform(tr);
		}

		final Point textSize = gc.textExtent(getText(), SWT.DRAW_MNEMONIC);
		gc.dispose();
		tr.dispose();

		return textSize;
	}

	/**
	 * Sets the separator's image to the argument, which may be null indicating
	 * that no image should be displayed.
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
		imageDecorator.setImage(image);
		redraw();
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
		imageDecorator.loadImage(stream);
		redraw();
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
		imageDecorator.loadImage(filename);
		redraw();
	}

	private Rectangle getImageBounds() {
		final Point imageSize = imageDecorator.getSize();
		final int margin = isVertical() ? marginHeight : marginWidth;
		final Rectangle size = getClientArea();
		final int height = isVertical() ? size.width : size.height;

		return new Rectangle(indent + (indent == 0 ? 0 : spacing) + margin - 1, (height - imageSize.y) / 2, imageSize.x, imageSize.y);
	}
}
