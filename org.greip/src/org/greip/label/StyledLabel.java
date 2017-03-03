/**
 * Copyright (c) 2017 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.label;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Util;

/**
 * Instances of this class represent a non-selectable user interface object that
 * displays a formatted string and/or image. When SEPARATOR is specified,
 * displays a single vertical or horizontal line.
 * <p>
 * Shadow styles are hints and may not be honored by the platform. To create a
 * separator label with the default shadow style for the platform, do not
 * specify a shadow style.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SEPARATOR, HORIZONTAL, VERTICAL</dd>
 * <dd>SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dd>CENTER, LEFT, RIGHT, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of SHADOW_IN, SHADOW_OUT and SHADOW_NONE may be specified.
 * SHADOW_NONE is a HINT. Only one of HORIZONTAL and VERTICAL may be specified.
 * Only one of CENTER, LEFT and RIGHT may be specified. In difference to SWT
 * Label widget images are allways left aligned.
 * </p>
 *
 * @author Thomas Lorbeer
 */
public class StyledLabel extends Label {

	private final FormattedText formattedText = new FormattedText(getDisplay());

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
	 * @see SWT#SEPARATOR
	 * @see SWT#HORIZONTAL
	 * @see SWT#VERTICAL
	 * @see SWT#SHADOW_IN
	 * @see SWT#SHADOW_OUT
	 * @see SWT#SHADOW_NONE
	 * @see SWT#CENTER
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#WRAP
	 */
	public StyledLabel(final Composite parent, final int style) {
		super(parent, style & ~SWT.RIGHT & ~SWT.CENTER | SWT.DOUBLE_BUFFERED);

		formattedText.setAlignment(getAlignment(style));
		formattedText.setFont(super.getFont());
		formattedText.setForeground(super.getForeground());
		formattedText.setOrientation(super.getOrientation());

		addListener(SWT.Paint, e -> {
			final Point size = getSize();
			final Point imageSize = getImageSize();
			final int maxWidth = size.x - imageSize.x - 2 * getBorderWidth() - 2;
			final int maxHeight = size.y - 2 * getBorderWidth();

			formattedText.layout(maxWidth, maxHeight).draw(e.gc, 2 + imageSize.x, 0);
		});

		addListener(SWT.MouseMove, e -> {
			final LinkDescriptor link = formattedText.getLinkAtLocation(e.x, e.y);
			setCursor(link != null ? e.display.getSystemCursor(SWT.CURSOR_HAND) : null);
		});

		addListener(SWT.MouseDown, e -> {
			final LinkDescriptor link = formattedText.getLinkAtLocation(e.x, e.y);

			Util.whenNotNull(link, () -> {
				final Event event = new Event();
				event.data = link;
				notifyListeners(SWT.Selection, event);
			});
		});

		addListener(SWT.MouseExit, e -> setCursor(null));
		addListener(SWT.Dispose, e -> formattedText.dispose());
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	@Override
	public String getText() {
		checkWidget();
		return formattedText.getText();
	}

	@Override
	public void setText(final String text) {
		formattedText.setText(text);
		redraw();
	}

	@Override
	public Font getFont() {
		checkWidget();
		return formattedText.getFont();
	}

	@Override
	public void setFont(final Font font) {
		formattedText.setFont(font);
		redraw();
	}

	@Override
	public Color getForeground() {
		checkWidget();
		return formattedText.getForeground();
	}

	@Override
	public void setForeground(final Color color) {
		formattedText.setForeground(color);
		redraw();
	}

	@Override
	public void setOrientation(final int orientation) {
		super.setOrientation(orientation);
		formattedText.setOrientation(orientation);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * a link is selected by the user, by sending it one of the messages defined
	 * in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when a link is selected by the user.
	 * The data member of the event contains the link object.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 *
	 * @param listener
	 *        the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		checkWidget();
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		final TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when a link is selected by the user.
	 *
	 * @param listener
	 *        the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		checkWidget();
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();

		final Point imageSize = getImageSize();
		final int width = wHint == SWT.DEFAULT ? wHint : wHint - 2 * getBorderWidth() - 2;
		final int height = hHint == SWT.DEFAULT ? hHint : hHint - 2 * getBorderWidth();
		final Rectangle bounds = formattedText.layout(width - imageSize.x, height).getBounds();

		return new Point(bounds.width + imageSize.x + 2 * getBorderWidth(), Math.max(bounds.height, imageSize.y) + 2 * getBorderWidth());
	}

	private static int getAlignment(final int style) {
		if ((style & SWT.RIGHT) != 0)
			return SWT.RIGHT;
		else if ((style & SWT.CENTER) != 0) return SWT.CENTER;
		return SWT.LEFT;
	}

	private Point getImageSize() {
		final Point imageSize;
		if (getImage() != null) {
			final Rectangle bounds = getImage().getBounds();
			imageSize = new Point(bounds.width + 5, bounds.height);
		} else {
			imageSize = new Point(0, 0);
		}
		return imageSize;
	}
}
