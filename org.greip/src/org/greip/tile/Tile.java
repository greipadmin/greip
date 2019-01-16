/**
 * Copyright (c) 2018 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;
import org.greip.common.Greip;
import org.greip.common.Util;
import org.greip.decorator.IDecorator;
import org.greip.markup.HtmlMarkupParser;
import org.greip.markup.MarkupText;

/**
 * Instances of this class represent a non-selectable user interface object that
 * displays a decorator and/or many of text sections.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 *
 * @author Thomas Lorbeer
 */
public class Tile extends Composite {

	private class SelectionHandler implements Listener {

		private void fireSelectionEvent(final int detail, final Object data) {
			final Event e = new Event();
			e.detail = detail;
			e.data = data;

			notifyListeners(SWT.Selection, e);
		}

		private Rectangle getLineBounds(final TextLayout layout, final int offset) {
			final int lineIndex = layout.getLineIndex(offset);
			final int[] lineOffsets = layout.getLineOffsets();

			return layout.getBounds(lineOffsets[lineIndex], lineOffsets[lineIndex + 1] - 1);
		}

		private String getLinkAt(final int x, final int y) {
			final Rectangle clientArea = getClientArea();
			final TextArea[] textAreas = createTextAreas(clientArea.width, clientArea.height);

			try {
				for (final TextArea textArea : textAreas) {
					final Rectangle bounds = textArea.getBounds();

					if (bounds.contains(x, y)) {
						final int offset = textArea.layout.getOffset(x - bounds.x, y - bounds.y, null);
						final Rectangle lineBounds = getLineBounds(textArea.layout, offset);

						if (lineBounds.contains(x - bounds.x, y - bounds.y)) {
							final TextStyle style = textArea.layout.getStyle(offset);

							if (style.data instanceof String) {
								return (String) style.data;
							}
						}
					}
				}
			} finally {
				disposeTextAreas(textAreas);
			}

			return null;
		}

		@Override
		public void handleEvent(final Event event) {
			if (event.type == SWT.MouseMove) {
				handleMouseMove(event);
			} else if (event.type == SWT.MouseDown) {
				handleMouseDown(event);
			}
		}

		private void handleMouseDown(final Event event) {
			final String linkId = getLinkAt(event.x, event.y);

			if (linkId != null) {
				fireSelectionEvent(Greip.LINK, linkId);
			} else if (getDecoratorBounds().contains(event.x, event.y)) {
				fireSelectionEvent(Greip.DECORATOR, null);
			} else {
				fireSelectionEvent(SWT.NONE, null);
			}
		}

		private void handleMouseMove(final Event event) {
			final String linkId = getLinkAt(event.x, event.y);
			final Cursor cursor;

			if (linkId != null) {
				cursor = getDisplay().getSystemCursor(SWT.CURSOR_HAND);
			} else if (getDecoratorBounds().contains(event.x, event.y)) {
				cursor = getDecoratorCursor();
			} else {
				cursor = getCursor();
			}

			showCursor(cursor);
		}
	}

	private static class TextArea {
		private final TextLayout layout;
		private int x;
		private int y;

		public TextArea(final TextLayout layout) {
			this.layout = layout;
		}

		public void setLocation(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public Rectangle getBounds() {
			final Rectangle bounds = layout.getBounds();
			return new Rectangle(x, y, bounds.width, layout.getText().isEmpty() ? 0 : bounds.height);
		}

		public void draw(final GC gc) {
			layout.draw(gc, x, y);
		}
	}

	private static class TextDescriptor {
		public String text;
		public int alignment;
		public Font font;
		public Color foreground;
		public boolean wrap;
	}

	private IDecorator decorator;
	private int decoratorAlignment = SWT.LEFT;

	private int marginHeight = 10;
	private int marginWidth = 10;
	private int decoratorSpacing = 10;
	private int textSpacing = 5;

	private int borderWidth;
	private Color borderColor;
	private int edgeRadius;

	private boolean selected;
	private boolean highlight;
	private final SelectionHandler linkHandler = new SelectionHandler();
	private final List<TextDescriptor> sections = new ArrayList<>();
	private final Color[] dimmedBackground = new Color[4];

	private Cursor decoratorCursor;
	private Cursor cursor;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 *
	 * @param parent
	 *        a composite control which will be the parent of the new instance
	 *        (cannot be null)
	 * @param style
	 *        the style of control to construct (reserved for future use, only
	 *        SWT.NONE allowed)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            <li>ERROR_INVALID_ARGUMENT - if style is not SWT.NONE</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the parent</li>
	 *            </ul>
	 */
	public Tile(final Composite parent, final int style) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_FOCUS);
		if (style != SWT.NONE) SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(final MouseEvent e) {
				selected = highlight;
				redraw();
			}

			@Override
			public void mouseExit(final MouseEvent e) {
				selected = false;
				redraw();
			}
		});

		addPaintListener(new PaintListener() {

			private void paintBackground(final GC gc) {
				final Rectangle size = getClientArea();

				if (edgeRadius > 0) {
					gc.setBackground(getParent().getBackground());
					gc.fillRectangle(0, 0, size.width, size.height);
				}

				final int innerRadius = Math.max(0, 2 * edgeRadius - borderWidth);

				gc.setBackground(selected ? dimmedBackground[0] : getBackground());
				gc.fillRoundRectangle(borderWidth, borderWidth, size.width - 2 * borderWidth, 2 * edgeRadius, innerRadius, innerRadius);
				gc.setForeground(selected ? dimmedBackground[0] : getBackground());
				gc.setBackground(selected ? dimmedBackground[1] : getBackground());
				gc.fillGradientRectangle(borderWidth, edgeRadius, size.width - 2 * borderWidth, size.height / 2 - edgeRadius, true);

				gc.setForeground(selected ? dimmedBackground[2] : getBackground());
				gc.setBackground(getBackground());
				gc.fillRoundRectangle(borderWidth, size.height - borderWidth - 2 * edgeRadius, size.width - 2 * borderWidth, 2 * edgeRadius,
						innerRadius, innerRadius);
				gc.fillGradientRectangle(borderWidth, size.height / 2, size.width - 2 * borderWidth, size.height / 2 - edgeRadius, true);

				if (borderWidth > 0) {
					gc.setClipping((Rectangle) null);
					gc.setForeground(getBorderColor());
					gc.setLineWidth(borderWidth);
					gc.drawRoundRectangle(borderWidth / 2, borderWidth / 2, size.width - borderWidth, size.height - borderWidth, edgeRadius * 2,
							edgeRadius * 2);
				}

				if (selected) {
					final int radius = Math.max(0, edgeRadius * 2 - 2);
					final int borderOffset = 2 * borderWidth - 1;

					gc.setForeground(dimmedBackground[3]);
					gc.setLineWidth(1);
					gc.drawRoundRectangle(borderWidth, borderWidth, size.width - borderOffset, size.height - borderOffset, radius, radius);
				}
			}

			@Override
			public void paintControl(final PaintEvent e) {
				e.gc.setAntialias(SWT.ON);

				paintBackground(e.gc);
				e.gc.setForeground(getForeground());

				final Rectangle clientArea = getClientArea();
				final TextArea[] textAreas = createTextAreas(clientArea.width, clientArea.height);
				for (final TextArea textArea : textAreas) {
					textArea.draw(e.gc);
				}
				disposeTextAreas(textAreas);

				if (hasDecorator()) {
					final Rectangle decoratorBounds = getDecoratorBounds();

					e.gc.setClipping(decoratorBounds);
					decorator.doPaint(e.gc, decoratorBounds.x, decoratorBounds.y);
				}
			}
		});

		addListener(SWT.MouseMove, linkHandler);
		addListener(SWT.MouseDown, linkHandler);
		addListener(SWT.Dispose, e -> disposeBackgroundColors());

		setBackground(getBackground());
		setMargins(10, 10);
		setDecoratorAlignment(SWT.RIGHT);
	}

	/**
	 * Adds a new text section to the list of sections. Sections are not
	 * removeable.
	 *
	 * @param text
	 *        the text content of the section (cannot be null)
	 * @param alignment
	 *        the text alignment (SWT.LEFT, SWT.CENTER, SWT.RIGHT and
	 *        SWT.JUSTIFY)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            </ul>
	 */
	public void addSection(final String text, final int alignment) {
		addSection(text, alignment, null, null, true);
	}

	/**
	 * Adds a new text section to the list of sections. You can remove sections
	 * by {@link #removeSection(int)}
	 *
	 * @param text
	 *        the text content of the section (cannot be null)
	 * @param alignment
	 *        the text alignment (SWT.LEFT, SWT.CENTER, SWT.RIGHT and
	 *        SWT.JUSTIFY)
	 * @param font
	 *        the font to use
	 * @param foreground
	 *        the text foreground color
	 * @param wrap
	 *        the line wrapping behaviour
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            <li>ERROR_IVALID_ARGUMENT - if the alignment value is
	 *            invalid</li>
	 *            </ul>
	 */
	public void addSection(final String text, final int alignment, final Font font, final Color foreground, final boolean wrap) {
		checkWidget();
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (!Util.in(alignment, SWT.LEFT, SWT.RIGHT, SWT.CENTER, Greip.JUSTIFY)) SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		final TextDescriptor descriptor = new TextDescriptor();
		descriptor.text = text;
		descriptor.alignment = alignment;
		descriptor.font = font;
		descriptor.foreground = foreground;
		descriptor.wrap = wrap;

		sections.add(descriptor);
		redraw();
	}

	/**
	 * Removes the spcified section from the list of sections.
	 *
	 * @param index
	 *        the sections index
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if the index out of range</li>
	 *            </ul>
	 */
	public void removeSection(final int index) {
		sections.remove(getSection(index));
		redraw();
	}

	/**
	 * Removes all sections from the list of sections.
	 */
	public void removeAllSections() {
		sections.clear();
	}

	/**
	 * Returns a value which describes the position of the text in the section.
	 * The value will be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>,
	 * <code>SWT.CENTER</code> or <code>Greip.JUSTIFY</code>.
	 *
	 * @param index
	 *        the sections index
	 *
	 * @return the alignment
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public int getAlignment(final int index) {
		return getSection(index).alignment;
	}

	/**
	 * Controls how text content in the sction will be displayed. The argument
	 * should be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>,
	 * <code>SWT.CENTER</code> or <code>Greip.JUSTIFY</code>.
	 *
	 * @param index
	 *        the sections index
	 * @param alignment
	 *        the new alignment
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_IVALID_ARGUMENT - if the alignment value is
	 *            invalid</li>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public void setAlignment(final int index, final int alignment) {
		if (!Util.in(alignment, SWT.LEFT, SWT.RIGHT, SWT.CENTER, Greip.JUSTIFY)) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		getSection(index).alignment = alignment;
		redraw();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.
	 * Color)
	 */
	@Override
	public void setBackground(final Color color) {
		super.setBackground(color);
		disposeBackgroundColors();

		final Display display = getDisplay();
		final RGB backgroundRGB = color.getRGB();

		dimmedBackground[0] = new Color(display, Util.getDimmedRGB(backgroundRGB, 0.07f));
		dimmedBackground[1] = new Color(display, Util.getDimmedRGB(backgroundRGB, -0.02f));
		dimmedBackground[2] = new Color(display, Util.getDimmedRGB(backgroundRGB, -0.07f));
		dimmedBackground[3] = new Color(display, Util.getDimmedRGB(backgroundRGB, 0.25f));
	}

	/**
	 * Returns the color of the border.
	 *
	 * @return the color
	 */
	public Color getBorderColor() {
		return borderColor != null ? borderColor : getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}

	/**
	 * Defines the color of the border. The border is drawn, if the width is set
	 * to any value greater than zero. The default value is
	 * <code>SWT.COLOR_WIDGET_BORDER</code>
	 *
	 * @param borderColor
	 *        the border color
	 *
	 * @see #setBorderWidth(int)
	 * @see #setEdgeRadius(int)
	 */
	public void setBorderColor(final Color borderColor) {
		if (borderColor != null && borderColor.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.borderColor = borderColor;
		redraw();
	}

	/**
	 * Returns the width of the border.
	 *
	 * @return the border width
	 */
	@Override
	public int getBorderWidth() {
		return borderWidth;
	}

	/**
	 * Defines the width of the border.
	 *
	 * @param borderWidth
	 *        the border width (cannot be less then zero)
	 *
	 * @exception InvalidArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the border width less then
	 *            zero</li>
	 *            </ul>
	 *
	 * @see #setBorderColor(Color)
	 * @see #setEdgeRadius(int)
	 */
	public void setBorderWidth(final int borderWith) {
		if (borderWidth < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.borderWidth = borderWith;
		redraw();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getCursor()
	 */
	@Override
	public Cursor getCursor() {
		return cursor;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
	 */
	@Override
	public void setCursor(final Cursor cursor) {
		this.cursor = cursor;
	}

	/**
	 * Returns the current decorator.
	 *
	 * @return the decorator
	 */
	@SuppressWarnings("unchecked")
	public <T extends IDecorator> T getDecorator() {
		return (T) decorator;
	}

	/**
	 * Sets the tiles decorator. A decorator is a graphical component (e.g.
	 * picture or visual counter).
	 *
	 * @param decorator
	 *        the decorator instance or <code>null</code> if no decorator should
	 *        be use
	 */
	public void setDecorator(final IDecorator decorator) {
		this.decorator = decorator;
		redraw();
	}

	/**
	 * Returns a value which describes the position of the decorator.
	 *
	 * @return the alignment
	 */
	public int getDecoratorAlignment() {
		return decoratorAlignment;
	}

	/**
	 * Controls how decorator amd text sections will be displayed in the control.
	 * The argument should be on of these styles or style combinations:
	 * <ul>
	 * <li>SWT.LEFT</li>
	 * <li>SWT.RIGHT</li>
	 * <li>SWT.TOP</li>
	 * <li>SWT.BOTTOM</li>
	 * <li>SWT.CENTER</li>
	 * <li>SWT.LEFT, SWT.TOP</li>
	 * <li>SWT.LEFT, SWT.BOTTOM</li>
	 * <li>SWT.RIGHT, SWT.TOP</li>
	 * <li>SWT.RIGHT, SWT.BOTTOM</li>
	 * </ul>
	 * <p>
	 * The default is SWT.LEFT.
	 *
	 * @param alignment
	 *        the alignment
	 *
	 * @exception InvalidArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the alignemnt style is
	 *            wrong</li>
	 *            </ul>
	 */
	public void setDecoratorAlignment(final int alignment) {
		switch (alignment) {
			case SWT.LEFT:
			case SWT.RIGHT:
			case SWT.TOP:
			case SWT.BOTTOM:
			case SWT.CENTER:
			case SWT.LEFT | SWT.TOP:
			case SWT.RIGHT | SWT.TOP:
			case SWT.LEFT | SWT.BOTTOM:
			case SWT.RIGHT | SWT.BOTTOM:
				this.decoratorAlignment = alignment;
				redraw();
				break;

			default:
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
	}

	/**
	 * Returns the decorators cursor or <code>null</code> if not specified.
	 *
	 * @return the cursor
	 */
	public Cursor getDecoratorCursor() {
		return decoratorCursor == null ? getCursor() : decoratorCursor;
	}

	/**
	 * Sets the decorators cursor to the cursor specified by the argument, or to
	 * the default cursor if the argument is null.
	 * <p>
	 * When the mouse pointer passes over the decorator its appearance is changed
	 * to the specified cursor.
	 *
	 * @param cursor
	 *        the new cursor (or null)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *            disposed</li>
	 *            </ul>
	 */
	public void setDecoratorCursor(final Cursor cursor) {
		if (cursor != null && cursor.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.decoratorCursor = cursor;
	}

	/**
	 * Returns the spacing between decorator and text sections.
	 *
	 * @return the spacing in pixels
	 */
	public int getDecoratorSpacing() {
		return decoratorSpacing;
	}

	/**
	 * Defines the spacing between decorator and text sections.
	 *
	 * @param decoratorSpacing
	 *        the spacing in pixels
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the spacing value less than
	 *            zero</li>
	 *            </ul>
	 */
	public void setDecoratorSpacing(final int decoratorSpacing) {
		if (decoratorSpacing < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.decoratorSpacing = decoratorSpacing;
		redraw();
	}

	/**
	 * Gets the radius of the rounded edges.
	 *
	 * @return the radius
	 */
	public int getEdgesRadius() {
		return edgeRadius;
	}

	/**
	 * Defines the radius of the rounded edges if the control shows a border
	 * line.
	 *
	 * @param edgeRadius
	 *        the radius of the rounded edges
	 *
	 * @exception InvalidArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the edge radius less then
	 *            zero</li>
	 *            </ul>
	 *
	 * @see #setBorderColor(Color)
	 * @see #setBorderWidth(int)
	 */
	public void setEdgeRadius(final int edgeRadius) {
		if (edgeRadius < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.edgeRadius = edgeRadius;
		redraw();
	}

	/**
	 * Return the font used to be paint the sections textual content.
	 *
	 * @param index
	 * @return
	 */
	public Font getFont(final int index) {
		final Font font = getSection(index).font;
		return font == null ? getFont() : font;
	}

	/**
	 * Sets the font for the text content of the section. The default font is
	 * {@link #getFont()}.
	 *
	 * @param index
	 *        the sections index
	 * @param font
	 *        the font
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public void setFont(final int index, final Font font) {
		if (font != null && font.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		getSection(index).font = font;
		redraw();
	}

	/**
	 * Returns the foreground color wich is used to paint textual content.
	 *
	 * @param index
	 *        the sections index
	 *
	 * @return the foreground color
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public Color getForeground(final int index) {
		final Color foreground = getSection(index).foreground;
		return foreground == null ? getForeground() : foreground;
	}

	/**
	 * Defines the foreground color wich is used to paint textual content.
	 *
	 * @param index
	 *        the sections index
	 * @param foreground
	 *        the foreground color
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public void setForeground(final int index, final Color foreground) {
		if (foreground != null && foreground.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		getSection(index).foreground = foreground;
		redraw();
	}

	/**
	 * Returns <code>true</code> if highlighting on mouse hover is enabled,
	 * otherwise <code>false</code>.
	 *
	 * @return the highlighting state
	 */
	public boolean isHighlight() {
		return highlight;
	}

	/**
	 * Enables or disables the mouse hover effect.
	 *
	 * @param highlight
	 *        <code>true</code> if highlighting on mouse hover is enabled,
	 *        otherwise <code>false</code>.
	 */
	public void setHighlight(final boolean highlight) {
		this.highlight = highlight;
		redraw();
	}

	/**
	 * Returns the controls margin width and height. The x coordinate of the
	 * result is the width and the y coordinate is the height.
	 *
	 * @return the margins
	 */
	public Point getMargins() {
		return new Point(marginWidth, marginHeight);
	}

	/**
	 * Sets the controls margin height and width. The margin is the distance
	 * between border and the controls content.
	 *
	 * @param marginWidth
	 *        the margin width
	 * @param marginHeight
	 *        the margin height
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the margin width or height less
	 *            than zero</li>
	 *            </ul>
	 */
	public void setMargins(final int marginWidth, final int marginHeight) {
		if (marginHeight < 0 || marginWidth < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.marginHeight = marginHeight;
		this.marginWidth = marginWidth;
		redraw();
	}

	/**
	 * Returns the sections textual content.
	 *
	 * @param index
	 *        the sections index
	 *
	 * @return the text content
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public String getText(final int index) {
		return getSection(index).text;
	}

	/**
	 * Sets the textual content for the specified text section.
	 *
	 * @param index
	 *        the sections index
	 * @param text
	 *        the new text content (null not allowed)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public void setText(final int index, final String text) {
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		getSection(index).text = text;
		redraw();
	}

	/**
	 * Returns then spacing between two text sections.
	 *
	 * @return the spacing in pixels
	 */
	public int getTextSpacing() {
		return textSpacing;
	}

	/**
	 * Defines the spacing between two text sections.
	 *
	 * @param textSpacing
	 *        the spacing in pixels
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the spacing value less than
	 *            zero</li>
	 *            </ul>
	 */
	public void setTextSpacing(final int textSpacing) {
		if (decoratorSpacing < 0) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.textSpacing = textSpacing;
		redraw();
	}

	/**
	 * Returns the current line wrap behaviour at the spcified text section.
	 *
	 * @param index
	 *        the sections index
	 *
	 * @return returns <code>true</code> if line wrap behaviour enabled,
	 *         otherwise <code>false</code>.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public boolean isWrap(final int index) {
		return getSection(index).wrap;
	}

	/**
	 * Defines the line wrap behaviour at the specified text section.
	 *
	 * @param index
	 *        the sections index
	 * @param wrap
	 *        <code>true</code> if line wrap enabled, <code>false</code>
	 *        otherwise.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if index is out of range</li>
	 *            </ul>
	 */
	public void setWrap(final int index, final boolean wrap) {
		getSection(index).wrap = wrap;
		redraw();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * a link is selected by the user, by sending it one of the messages defined
	 * in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when a link is selected by the user.
	 * The data member of the event contains the link object.
	 * <code>widgetDefaultSelected</code> is never called.
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
		addListener(SWT.Selection, new TypedListener(listener));
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
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final Point size = new Point(0, 0);

		final TextArea[] textAreas = createTextAreas(wHint, SWT.DEFAULT);
		final int height = getTotalTextHeight(textAreas);
		final int width = getMaxTextWidth(textAreas);

		size.x = 2 * (marginWidth + borderWidth);
		size.y = 2 * (marginHeight + borderWidth);

		final Point decoratorSize = getDecoratorSize();

		if ((decoratorAlignment & SWT.LEFT) > 0 || (decoratorAlignment & SWT.RIGHT) > 0) {
			size.x += decoratorSize.x + getEffectiveDecoratorSpacing() + width;
			size.y += Math.max(decoratorSize.y, height);

		} else if (decoratorAlignment == SWT.CENTER) {
			final int nonEmptyTextCount = getNonEmptyTextCount();

			size.x += Math.max(decoratorSize.x, width);
			size.y += decoratorSize.y + height + Math.min(nonEmptyTextCount, 2) * getEffectiveDecoratorSpacing()
					- (nonEmptyTextCount >= 2 ? textSpacing : 0);

		} else {
			size.x += Math.max(decoratorSize.x, width);
			size.y += decoratorSize.y + getEffectiveDecoratorSpacing() + height;
		}

		disposeTextAreas(textAreas);

		return size;
	}

	private void showCursor(final Cursor cursor) {
		super.setCursor(cursor);
	}

	private TextDescriptor getSection(final int index) {
		if (index < 0 || index >= sections.size()) SWT.error(SWT.ERROR_INVALID_RANGE);
		return sections.get(index);
	}

	private int computeMaxTextWidth(final int maxWidth) {
		int width = maxWidth - 2 * marginWidth;

		if ((decoratorAlignment & SWT.LEFT) > 0 || (decoratorAlignment & SWT.RIGHT) > 0) {
			width -= getDecoratorSize().x + getEffectiveDecoratorSpacing();
		}

		return Math.max(10, width - 2 * borderWidth);
	}

	private int computeTextIndent() {
		final Point decoratorSize = getDecoratorSize();
		int x = marginWidth + borderWidth;

		if ((decoratorAlignment & SWT.LEFT) > 0) {
			x += decoratorSize.x + getEffectiveDecoratorSpacing();
		}

		return x;
	}

	private TextArea createTextArea(final int index, final int wHint, final int hHint) {
		final TextLayout layout = createTextLayout(index, computeMaxTextWidth(wHint), hHint);
		final TextArea textArea = new TextArea(layout);

		textArea.setLocation(computeTextIndent(), 0);

		return textArea;
	}

	private TextArea[] createTextAreas(final int wHint, final int hHint) {
		final TextArea[] textAreas = IntStream.range(0, sections.size()).mapToObj(i -> createTextArea(i, wHint, SWT.DEFAULT))
				.toArray(TextArea[]::new);

		final Point decoratorSize = getDecoratorSize();
		int y = marginHeight + borderWidth;

		if (decoratorAlignment == SWT.TOP) {
			y += decoratorSize.y + decoratorSpacing;

		} else if (decoratorAlignment == SWT.CENTER && textAreas.length > 0) {
			int offset = Math.min(getNonEmptyTextCount(), 2) * decoratorSpacing;
			offset -= getNonEmptyTextCount() >= 2 ? textSpacing : 0;
			offset += decoratorSize.y;

			for (int i = 1; i < textAreas.length; i++) {
				final Rectangle bounds = textAreas[i].getBounds();
				textAreas[i].setLocation(bounds.x, bounds.y + offset);
			}

		} else if (decoratorAlignment != SWT.BOTTOM) {
			y += Math.max(0, (decoratorSize.y - getTotalTextHeight(textAreas)) / 2);
		}

		for (final TextArea textArea : textAreas) {
			final Rectangle bounds = textArea.getBounds();
			final int height = bounds.height;

			textArea.setLocation(bounds.x, bounds.y + y);
			y += height + (height == 0 ? 0 : textSpacing);
		}

		if (hHint != SWT.DEFAULT) {
			shortenPartiallyDisplayedTextArea(textAreas, wHint, hHint);
		}

		return textAreas;
	}

	private void shortenPartiallyDisplayedTextArea(final TextArea[] textAreas, final int wHint, final int hHint) {
		for (int i = textAreas.length - 1; i >= 0; i--) {
			final Rectangle bounds = textAreas[i].getBounds();
			final int height = bounds.y + bounds.height;

			if (height + marginHeight + borderWidth > hHint && bounds.y < hHint) {
				textAreas[i].layout.dispose();
				textAreas[i] = createTextArea(i, wHint, Math.max(0, hHint - bounds.y - marginHeight - borderWidth));
				textAreas[i].setLocation(bounds.x, bounds.y);
			}
		}
	}

	private static void disposeTextAreas(final TextArea[] textAreas) {
		for (final TextArea textArea : textAreas) {
			textArea.layout.dispose();
		}
	}

	private TextLayout createTextLayout(final int index, final int maxWidth, final int maxHeight) {
		final MarkupText markupText = new MarkupText(getDisplay(), new HtmlMarkupParser());

		markupText.setFont(getFont(index));
		markupText.setForeground(getForeground(index));
		markupText.setAlignment(getAlignment(index));
		markupText.setWrap(isWrap(index));
		markupText.layout(getText(index), maxWidth, maxHeight);

		return markupText.getTextLayout();
	}

	private Rectangle getDecoratorBounds() {
		final Point decoratorSize = getDecoratorSize();
		final Rectangle size = getClientArea();
		int x;
		int y;

		if (decoratorAlignment == SWT.BOTTOM) {
			x = (size.width - 2 * marginWidth - decoratorSize.x) / 2 + marginWidth;
			y = size.height - marginHeight - decoratorSize.y - borderWidth;
		} else if (decoratorAlignment == SWT.TOP) {
			x = (size.width - 2 * marginWidth - decoratorSize.x) / 2 + marginWidth;
			y = marginHeight + borderWidth;
		} else if (decoratorAlignment == SWT.CENTER) {
			x = (size.width - 2 * marginWidth - decoratorSize.x) / 2 + marginWidth;
			y = marginHeight + borderWidth;
			if (!sections.isEmpty()) {
				final int height = createTextArea(0, size.width, size.height).getBounds().height;
				y += height + (height == 0 ? 0 : decoratorSpacing);
			}
		} else {
			if ((decoratorAlignment & SWT.LEFT) > 0) {
				x = marginWidth + borderWidth;
			} else {
				x = size.width - decoratorSize.x - marginWidth - borderWidth;
			}
			if ((decoratorAlignment & SWT.TOP) > 0) {
				y = marginHeight + borderWidth;
			} else if ((decoratorAlignment & SWT.BOTTOM) > 0) {
				y = size.height - marginHeight - decoratorSize.y;
			} else {
				y = marginHeight + Math.max(0, (size.height - decoratorSize.y) / 2 - marginHeight);
			}
		}

		x = Math.max(x, borderWidth + marginWidth);
		y = Math.max(y, borderWidth + marginHeight);

		// Clipping auf sichtbaren Bereich, Rand wird nicht übermalt
		final int maxHeight = size.height - borderWidth - marginHeight - y;
		final int maxWidth = size.width - borderWidth - marginWidth - x;

		return new Rectangle(x, y, Math.min(decoratorSize.x, maxWidth), Math.min(decoratorSize.y, maxHeight));
	}

	private Point getDecoratorSize() {
		return hasDecorator() ? decorator.getSize() : new Point(0, 0);
	}

	private int getEffectiveDecoratorSpacing() {
		return hasDecorator() && hasAnyText() ? decoratorSpacing : 0;
	}

	private static int getMaxTextWidth(final TextArea[] textAreas) {
		int maxWidth = 0;

		for (final TextArea textArea : textAreas) {
			maxWidth = Math.max(maxWidth, textArea.getBounds().width);
		}

		return maxWidth;
	}

	private int getTotalTextHeight(final TextArea[] textAreas) {
		int totalHeight = 0;

		for (final TextArea textArea : textAreas) {
			totalHeight += textArea.getBounds().height;
		}

		return Math.max(totalHeight + (getNonEmptyTextCount() - 1) * textSpacing, 0);
	}

	private boolean hasDecorator() {
		return decorator != null;
	}

	private boolean hasAnyText() {
		for (final TextDescriptor descriptor : sections) {
			if (!descriptor.text.isEmpty()) {
				return true;
			}
		}
		return !sections.isEmpty();
	}

	private int getNonEmptyTextCount() {
		int count = sections.size();

		for (final TextDescriptor descriptor : sections) {
			if (descriptor.text == null || descriptor.text.isEmpty()) {
				count--;
			}
		}
		return count;
	}

	private void disposeBackgroundColors() {
		for (final Color color : dimmedBackground) {
			if (color != null) {
				color.dispose();
			}
		}
	}
}
