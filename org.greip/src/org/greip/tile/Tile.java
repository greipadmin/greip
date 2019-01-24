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
import java.util.Arrays;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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
import org.greip.internal.BorderPainter;
import org.greip.internal.IBorderable;
import org.greip.markup.HtmlMarkupParser;
import org.greip.markup.MarkupText;
import org.greip.tile.TextSection.TextSectionModifyListener;

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
public class Tile extends Composite implements IBorderable {

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
			final Point size = getSize();
			final TextArea[] textAreas = createTextAreas(size.x, size.y);

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
		private int[] margins;

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

		public int[] getMargins() {
			return margins;
		}

		public void setMargins(final int[] margins) {
			this.margins = margins;
		}
	}

	private IDecorator decorator;
	private int decoratorAlignment = SWT.LEFT;

	private int marginHeight = 10;
	private int marginWidth = 10;
	private int decoratorSpacing = 10;
	private int textSpacing = 5;

	private final BorderPainter border = new BorderPainter(this);
	private int borderWidth;
	private Color borderColor;
	private int edgeRadius;

	private boolean selected;
	private boolean highlight;
	private final SelectionHandler linkHandler = new SelectionHandler();
	private final List<TextSection> textSections = new ArrayList<>();
	private final Color[] dimmedBackground = new Color[5];

	private final TextSectionModifyListener sectionModifyListener = s -> redraw();

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
				final Point size = getSize();

				if (getBackgroundImage() == null) {
					final int innerRadius = Math.max(0, 2 * edgeRadius - borderWidth);

					gc.setBackground(selected ? dimmedBackground[0] : getBackground());
					gc.fillRoundRectangle(borderWidth, borderWidth, size.x - 2 * borderWidth, 2 * edgeRadius, innerRadius, innerRadius);
					gc.setForeground(selected ? dimmedBackground[0] : getBackground());
					gc.setBackground(selected ? dimmedBackground[1] : getBackground());
					gc.fillGradientRectangle(borderWidth, edgeRadius, size.x - 2 * borderWidth, size.y / 2 - edgeRadius, true);

					gc.setForeground(selected ? dimmedBackground[2] : getBackground());
					gc.setBackground(selected ? dimmedBackground[4] : getBackground());
					gc.fillRoundRectangle(borderWidth, size.y - borderWidth - 2 * edgeRadius, size.x - 2 * borderWidth, 2 * edgeRadius,
							innerRadius, innerRadius);
					gc.fillGradientRectangle(borderWidth, size.y / 2, size.x - 2 * borderWidth, size.y / 2 - edgeRadius, true);

				} else if (selected) {
					gc.setBackground(getBackground());
					gc.fillRectangle(0, 0, size.x, size.y);

					final ImageData imageData = createTransparentBackgroundImage(gc);
					Util.withResource(new Image(gc.getDevice(), imageData), img -> {
						gc.drawImage(img, 0, 0);
					});
				}
			}

			private ImageData createTransparentBackgroundImage(final GC gc) {
				final Point size = getSize();

				return Util.withResource(new Image(gc.getDevice(), size.x, size.y), image -> {
					Util.withResource(new GC(image), imageGC -> {
						imageGC.drawImage(getBackgroundImage(), 0, 0);
					});

					final ImageData data = image.getImageData();
					final byte[] alphas = new byte[size.x * size.y];

					Arrays.fill(alphas, (byte) 230);
					data.setAlphas(0, 0, alphas.length, alphas, 0);

					return data;
				});
			}

			@Override
			public void paintControl(final PaintEvent e) {
				e.gc.setAntialias(SWT.ON);

				paintBackground(e.gc);
				e.gc.setForeground(getForeground());

				final Point size = getSize();
				final TextArea[] textAreas = createTextAreas(size.x, size.y);
				for (final TextArea textArea : textAreas) {
					textArea.draw(e.gc);
				}
				disposeTextAreas(textAreas);

				if (hasDecorator()) {
					final Rectangle decoratorBounds = getDecoratorBounds();

					e.gc.setClipping(decoratorBounds);
					decorator.doPaint(e.gc, decoratorBounds.x, decoratorBounds.y);
					e.gc.setClipping((Rectangle) null);
				}

				border.doPaint(e.gc, getParent().getBackground());
			}
		});

		addListener(SWT.MouseMove, linkHandler);
		addListener(SWT.MouseDown, linkHandler);
		addListener(SWT.Dispose, e -> disposeBackgroundColors());

		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		setMargins(10, 10);
		setDecoratorAlignment(SWT.RIGHT);
	}

	/**
	 * Adds a new text section to the list of sections. You can remove sections
	 * by {@link #removeTextSections(TextSection...)}.
	 *
	 * @param section
	 *        the new text section
	 */
	public void addTextSection(final TextSection section) {
		textSections.add(section);
		section.addModifyListener(sectionModifyListener);
	}

	/**
	 * Removes the spcified sections from the list of sections.
	 *
	 * @param indexes
	 *        the sections index
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if the index out of range</li>
	 *            </ul>
	 */
	public void removeTextSections(final TextSection... sections) {
		if (sections == null || sections.length == 0) {
			removeSections(textSections.stream().toArray(TextSection[]::new));
		} else {
			removeSections(sections);
		}
		redraw();
	}

	/**
	 * Returns the n'th text section. The first text section has index 0.
	 *
	 * @param index
	 *        the index
	 *
	 * @return the text section
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_RANGE - if the index less than zero or
	 *            greater or equal to the count of text sections</li>
	 *            </ul>
	 */
	public TextSection getTextSection(final int index) {
		if (index < 0 || index >= textSections.size()) SWT.error(SWT.ERROR_INVALID_RANGE);
		return textSections.get(index);
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
		RGB backgroundRGB = color.getRGB();
		final float brightness = backgroundRGB.getHSB()[2];

		if (brightness > 0.7f) {
			backgroundRGB = Util.getDimmedRGB(backgroundRGB, -(0.3f - brightness) / 10);
		} else if (brightness < 0.3f) {
			backgroundRGB = Util.getDimmedRGB(backgroundRGB, 0.3f - brightness);
		}

		dimmedBackground[0] = new Color(display, Util.getDimmedRGB(backgroundRGB, 0.07f));
		dimmedBackground[1] = new Color(display, Util.getDimmedRGB(backgroundRGB, -0.02f));
		dimmedBackground[2] = new Color(display, Util.getDimmedRGB(backgroundRGB, -0.07f));
		dimmedBackground[3] = new Color(display, Util.getDimmedRGB(backgroundRGB, 0.25f));
		dimmedBackground[4] = new Color(display, backgroundRGB);
	}

	/**
	 * Returns the color of the border.
	 *
	 * @return the color
	 */
	@Override
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
	@Override
	public int getEdgeRadius() {
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

	private void removeSections(final TextSection... sections) {
		for (final TextSection section : sections) {
			section.removeModifyListener(sectionModifyListener);
			textSections.remove(section);
		}
	}

	private void showCursor(final Cursor cursor) {
		super.setCursor(cursor);
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
		final int[] margins = getTextSection(index).getMargins();

		final TextLayout layout = createTextLayout(index, computeMaxTextWidth(wHint - margins[0] - margins[1]), hHint);
		final TextArea textArea = new TextArea(layout);

		textArea.setLocation(computeTextIndent(), 0);
		textArea.setMargins(margins);

		return textArea;
	}

	private TextArea[] createTextAreas(final int wHint, final int hHint) {
		final TextArea[] textAreas = IntStream.range(0, textSections.size()).mapToObj(i -> createTextArea(i, wHint, SWT.DEFAULT))
				.toArray(TextArea[]::new);

		final Point decoratorSize = getDecoratorSize();
		int y = marginHeight + borderWidth;

		if (decoratorAlignment == SWT.TOP) {
			y += decoratorSize.y + decoratorSpacing;

		} else if (decoratorAlignment == SWT.CENTER && textAreas.length > 0) {
			final int nonEmptyTextCount = getNonEmptyTextCount();

			int offset = Math.min(nonEmptyTextCount, 2) * decoratorSpacing;
			offset -= nonEmptyTextCount >= 2 ? textSpacing : 0;
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
			final int[] margins = textArea.getMargins();
			final int height = bounds.height;

			textArea.setLocation(bounds.x + margins[0], bounds.y + y + margins[2]);
			y += height + (height == 0 ? 0 : textSpacing + margins[2] + margins[3]);
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
		final TextSection section = getTextSection(index);

		markupText.setFont(section.getFont());
		markupText.setForeground(section.getForeground());
		markupText.setAlignment(section.getAlignment());
		markupText.setWrap(section.isWrap());
		markupText.layout(section.getText(), maxWidth, maxHeight);

		return markupText.getTextLayout();
	}

	private Rectangle getDecoratorBounds() {
		final Point decoratorSize = getDecoratorSize();
		final Point size = getSize();
		int x;
		int y;

		if (decoratorAlignment == SWT.BOTTOM) {
			x = (size.x - 2 * marginWidth - decoratorSize.x) / 2 + marginWidth;
			y = size.y - marginHeight - decoratorSize.y - borderWidth;
		} else if (decoratorAlignment == SWT.TOP) {
			x = (size.x - 2 * marginWidth - decoratorSize.x) / 2 + marginWidth;
			y = marginHeight + borderWidth;
		} else if (decoratorAlignment == SWT.CENTER) {
			x = (size.x - 2 * marginWidth - decoratorSize.x) / 2 + marginWidth;
			y = marginHeight + borderWidth;
			if (!textSections.isEmpty()) {
				final int height = createTextArea(0, size.x, size.y).getBounds().height;
				y += height + (height == 0 ? 0 : decoratorSpacing);
			}
		} else {
			if ((decoratorAlignment & SWT.LEFT) > 0) {
				x = marginWidth + borderWidth;
			} else {
				x = size.x - decoratorSize.x - marginWidth - borderWidth;
			}
			if ((decoratorAlignment & SWT.TOP) > 0) {
				y = marginHeight + borderWidth;
			} else if ((decoratorAlignment & SWT.BOTTOM) > 0) {
				y = size.y - marginHeight - decoratorSize.y - borderWidth;
			} else {
				y = marginHeight + Math.max(0, (size.y - decoratorSize.y) / 2 - marginHeight);
			}
		}

		x = Math.max(x, borderWidth + marginWidth);
		y = Math.max(y, borderWidth + marginHeight);

		// Clipping auf sichtbaren Bereich, Rand wird nicht übermalt
		final int maxHeight = size.y - borderWidth - marginHeight - y;
		final int maxWidth = size.x - borderWidth - marginWidth - x;

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
			final int[] margins = textArea.margins;
			maxWidth = Math.max(maxWidth, textArea.getBounds().width + margins[0] + margins[1]);
		}

		return maxWidth;
	}

	private int getTotalTextHeight(final TextArea[] textAreas) {
		int totalHeight = 0;

		for (final TextArea textArea : textAreas) {
			final int[] margins = textArea.margins;
			totalHeight += textArea.getBounds().height + margins[2] + margins[3];
		}

		return Math.max(totalHeight + (getNonEmptyTextCount() - 1) * textSpacing, 0);
	}

	private boolean hasDecorator() {
		return decorator != null;
	}

	private boolean hasAnyText() {
		for (final TextSection descriptor : textSections) {
			if (!descriptor.getText().isEmpty()) {
				return true;
			}
		}
		return !textSections.isEmpty();
	}

	private int getNonEmptyTextCount() {
		int count = textSections.size();

		for (final TextSection section : textSections) {
			if (section.getText().isEmpty()) {
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

	@Override
	public Rectangle getClientArea() {
		final Point size = getSize();
		return new Rectangle(0, 0, size.x, size.y);
	}
}
