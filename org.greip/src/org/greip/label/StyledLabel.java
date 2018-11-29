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

import java.text.ParseException;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Greip;
import org.greip.common.Util;
import org.greip.markup.Anchor;
import org.greip.markup.HtmlMarkupParser;
import org.greip.markup.MarkupText;
import org.greip.tile.Alignment;

/**
 * Instances of this class represent a non-selectable user interface object that
 * displays a formatted string and/or image.
 * <p>
 * Shadow styles are hints and may not be honored by the platform.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dd>SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dd>CENTER, LEFT, RIGHT</dd>
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

	private final MarkupText markupText = new MarkupText(getDisplay(), new HtmlMarkupParser());
	private String text = "";
	private final Image tmpImage;

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
	 * @see SWT#SHADOW_IN
	 * @see SWT#SHADOW_OUT
	 * @see SWT#SHADOW_NONE
	 * @see SWT#CENTER
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see Greip#JUSTIFY
	 */
	public StyledLabel(final Composite parent, final int style) {
		super(parent, style & ~SWT.RIGHT & ~SWT.CENTER & ~Greip.JUSTIFY);

		markupText.setAlignment(Alignment.valueOf(style));
		markupText.setFont(super.getFont());
		markupText.setForeground(super.getForeground());
		markupText.setOrientation(getOrientation());
		markupText.setWrap((style & SWT.WRAP) != 0);

		tmpImage = createTemporaryImage();
		setImage(tmpImage);

		addListener(SWT.Dispose, e -> tmpImage.dispose());

		addListener(SWT.Paint, e -> {
			final Point size = getSize();
			final Point offset = getOffset();

			markupText.layout(getText(), size.x - offset.x, size.y - offset.y);
			final int y = (size.y - markupText.getSize().y) / 2;
			markupText.getTextLayout().draw(e.gc, offset.x - getBorderWidth(), y);
		});

		addListener(SWT.MouseMove, e -> {
			final Anchor link = markupText.getLinkAtLocation(e.x, e.y);
			setCursor(link == null ? null : e.display.getSystemCursor(SWT.CURSOR_HAND));
		});

		addListener(SWT.MouseDown, e -> {
			final Anchor link = markupText.getLinkAtLocation(e.x, e.y);

			Util.whenNotNull(link, () -> {
				final Event event = new Event();
				event.data = link;
				notifyListeners(SWT.Selection, event);
			});
		});
	}

	private Image createTemporaryImage() {
		final ImageData data = new ImageData(1, 1, 24, new PaletteData(0x0000FF, 0x00FF00, 0xFF0000));
		data.setAlpha(0, 0, 0);

		return new Image(getDisplay(), data);
	}

	private int getTextIndent() {
		return getImage() == null ? 0 : getImage().getBounds().width + 5;
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final MarkupText markupText = new MarkupText(getDisplay(), new HtmlMarkupParser());
		final Point offset = getOffset();

		markupText.setFont(getFont());
		markupText.setWrap(isWrap());

		markupText.layout(getText(), wHint == SWT.DEFAULT ? SWT.DEFAULT : wHint - offset.x,
				hHint == SWT.DEFAULT ? SWT.DEFAULT : hHint - offset.y);

		final Point textSize = markupText.getSize();

		return new Point(textSize.x + offset.x, Math.max(textSize.y + offset.y, getMinHeight()));
	}

	private int getMinHeight() {
		final int borderWidth = 2 * getBorderWidth();
		final Image image = getImage();

		return (image == null ? 0 : image.getBounds().height) + borderWidth;
	}

	private Point getOffset() {
		final int borderWidth = 2 * getBorderWidth();
		final int textIndent = getTextIndent();

		return new Point(borderWidth + textIndent + 1, borderWidth + 2);
	}

	/**
	 * Returns a value which describes the position of the text in the receiver.
	 * The value will be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>,
	 * <code>SWT.CENTER</code> or <code>Greip.JUSTIFY</code>.
	 *
	 * @return the alignment
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	@Override
	public int getAlignment() {
		checkWidget();
		return markupText.getAlignment().style;
	}

	/**
	 * Controls how text will be displayed in the receiver. The argument should
	 * be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>,
	 * <code>SWT.CENTER</code> or <code>Greip.JUSTIFY</code>.
	 *
	 * @param alignment
	 *        the new alignment
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	@Override
	public void setAlignment(final int alignment) {
		markupText.setAlignment(Alignment.valueOf(alignment));
		redraw();
	}

	@Override
	public Font getFont() {
		checkWidget();
		return markupText.getFont();
	}

	@Override
	public void setFont(final Font font) {
		markupText.setFont(font);
		redraw();
	}

	@Override
	public Color getForeground() {
		checkWidget();
		return markupText.getForeground();
	}

	@Override
	public void setForeground(final Color color) {
		markupText.setForeground(color);
		redraw();
	}

	@Override
	public Image getImage() {
		final Image image = super.getImage();
		return image == tmpImage ? null : image;
	}

	@Override
	public void setImage(final Image image) {
		super.setImage(image == null ? tmpImage : image);
	}

	@Override
	public void setOrientation(final int orientation) {
		super.setOrientation(orientation);
		markupText.setOrientation(getOrientation());
	}

	/**
	 * Returns the receiver's text, which will be an empty string if it has never
	 * been set.
	 *
	 * @return the receiver's text
	 *
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	@Override
	public String getText() {
		checkWidget();
		return text;
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * The text can contains some pseudo-HTML tags for formatting:
	 * <ul>
	 * <li><b>&ltbr/&gt</b> for adding a line break
	 * <li><b>&lti&gt</b> to render text in italic
	 * <li><b>&ltu&gt</b> to render text in underline
	 * <li><b>&ltb&gt</b> to render text in bold
	 * <li><b>&lts&gt</b> to render strikeout text
	 * <li><b>&ltsub&gt</b> to render subscript text
	 * <li><b>&ltsup&gt</b> to render superscript text
	 * <li><b>&ltstyle fg="{color}" bg="{color}" size="{size}"
	 * font="{name}"&gt</b> to define text foreground and background color as
	 * HTML color code (e.g. #FFAABB) and font size in pixels. All attributes are
	 * optional.
	 * <li><b>&ltlink id="{id}" url="{url}"&gt</b> to define a link. The
	 * attributes id and url are accessible from selection listeners.
	 * </ul>
	 *
	 * @param text
	 *        the new text
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *            disposed</li>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the receiver</li>
	 *            </ul>
	 */
	@Override
	public void setText(final String text) {
		checkWidget();
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		this.text = text;
		redraw();
	}

	/**
	 * Set the exception handler for handling parse exceptions.
	 *
	 * @param exceptionHandler
	 *        the exception handler
	 */
	public void setExceptionHandler(final Consumer<ParseException> exceptionHandler) {
		markupText.setExceptionHandler(exceptionHandler);
	}

	/**
	 * Returns the current line wrap behaviour.
	 *
	 * @return returns <code>true</code> if line wrap behaviou enabled, otherwise
	 *         <code>false</code>.
	 */
	public boolean isWrap() {
		return markupText.isWrap();
	}

	/**
	 * Enables or disables the automatic line wrap behavior.
	 *
	 * @param wrap
	 *        the new line wrap behaviour
	 */
	public void setWrap(final boolean wrap) {
		markupText.setWrap(wrap);
		redraw();
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
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}
}
