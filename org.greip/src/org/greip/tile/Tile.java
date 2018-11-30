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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
			final TextArea[] textAreas = createTextAreas(getClientArea().width);

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

			if (linkId != null) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			} else if (getDecoratorBounds().contains(event.x, event.y)) {
				setCursor(decorator.getCursor());
			} else {
				setCursor(null);
			}
		}
	}

	private static class TextArea {
		private final TextLayout layout;
		private final Point pos;

		public TextArea(final TextLayout layout, final Point pos) {
			this.layout = layout;
			this.pos = pos;
		}

		public Rectangle getBounds() {
			final Rectangle bounds = layout.getBounds();
			return new Rectangle(pos.x, pos.y, bounds.width, layout.getText().isEmpty() ? 0 : bounds.height);
		}
	}

	private static class TextDescriptor {
		public String text;
		public Alignment alignment;
		public Font font;
		public Color foreground;
		public boolean wrap;
	}

	private int decoratorAlignment = SWT.LEFT;
	private int marginHeight = 0;
	private int marginWidth = 0;
	private IDecorator decorator;
	private int decoratorSpacing;
	private int textSpacing;
	private int borderWidth;
	private Color borderColor;
	private int edgeRadius;
	private boolean selected;
	private boolean showSelection;
	private final SelectionHandler linkHandler = new SelectionHandler();
	private final List<TextDescriptor> textDescriptors = new ArrayList<>();
	private final Color[] dimmedBackground = new Color[4];

	public Tile(final Composite parent) {
		super(parent, SWT.DOUBLE_BUFFERED);

		if ((getStyle() & SWT.V_SCROLL) > 0) {
			getVerticalBar().setVisible(false);
		}

		addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(final MouseEvent e) {
				selected = showSelection;
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

					gc.setForeground(dimmedBackground[3]);
					gc.setLineWidth(1);
					gc.drawRoundRectangle(borderWidth, borderWidth, size.width - 2 * borderWidth - 1, size.height - 2 * borderWidth - 1, radius,
							radius);
				}
			}

			@Override
			public void paintControl(final PaintEvent e) {
				e.gc.setAntialias(SWT.ON);

				final Point preferredSize = computeSize(getSize().x, SWT.DEFAULT);
				if ((getStyle() & SWT.V_SCROLL) > 0) {
					if (preferredSize.y > getSize().y) {
						getVerticalBar().setVisible(true);
					} else {
						getVerticalBar().setVisible(false);
					}
				}

				paintBackground(e.gc);
				e.gc.setForeground(getForeground());
				final TextArea[] textAreas = createTextAreas(getClientArea().width);

				for (final TextArea textArea : textAreas) {
					final Rectangle bounds = textArea.getBounds();
					textArea.layout.draw(e.gc, bounds.x, bounds.y);
				}

				if (hasDecorator()) {
					final Rectangle decoratorBounds = getDecoratorBounds();

					e.gc.setClipping(decoratorBounds);
					decorator.doPaint(e.gc, new Point(decoratorBounds.x, decoratorBounds.y));
				}
			}
		});

		addListener(SWT.MouseMove, linkHandler);
		addListener(SWT.MouseDown, linkHandler);
		addListener(SWT.Dispose, e -> disposeBackgroundColors());

		setBackground(getBackground());
		setMargins(10, 10, 5, 10);
		setDecoratorAlignment(SWT.RIGHT);
	}

	public int addText(final String text, final Alignment alignment) {
		return addText(text, alignment, null, null, true);
	}

	public int addText(final String text, final Alignment alignment, final Font font, final Color foreground, final boolean wrap) {
		checkWidget();
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		final TextDescriptor descriptor = new TextDescriptor();
		descriptor.text = text;
		descriptor.alignment = alignment;
		descriptor.font = font;
		descriptor.foreground = foreground;
		descriptor.wrap = wrap;

		textDescriptors.add(descriptor);
		redraw();

		return textDescriptors.size() - 1;
	}

	public void clear() {
		textDescriptors.clear();
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final Point size = new Point(0, 0);

		final TextArea[] textAreas = createTextAreas(wHint);
		final int height = getTotalTextHeight(textAreas);
		final int width = getMaxTextWidth(textAreas);

		size.x = 2 * (marginWidth + borderWidth);
		size.y = 2 * (marginHeight + borderWidth);

		final Point decoratorSize = getDecoratorSize();

		if ((decoratorAlignment & SWT.LEFT) > 0 || (decoratorAlignment & SWT.RIGHT) > 0) {
			size.x += decoratorSize.x + getDecoratorSpacing() + width;
			size.y += Math.max(decoratorSize.y, height);

		} else if (decoratorAlignment == SWT.CENTER) {
			final int nonEmptyTextCount = getNonEmptyTextCount();

			size.x += Math.max(decoratorSize.x, width);
			size.y += decoratorSize.y + height + Math.min(nonEmptyTextCount, 2) * getDecoratorSpacing()
					- (nonEmptyTextCount >= 2 ? textSpacing : 0);

		} else {
			size.x += Math.max(decoratorSize.x, width);
			size.y += decoratorSize.y + getDecoratorSpacing() + height;
		}

		return size;
	}

	public Alignment getAlignment(final int index) {
		return getTextDescriptor(index).alignment;
	}

	public void setAlignment(final int index, final Alignment alignment) {
		getTextDescriptor(index).alignment = alignment;
		redraw();
	}

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

	public Color getBorderColor() {
		return borderColor != null ? borderColor : getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}

	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
		redraw();
	}

	@Override
	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(final int borderWith) {
		this.borderWidth = borderWith;
		redraw();
	}

	public IDecorator getDecorator() {
		return decorator;
	}

	public void setDecorator(final IDecorator decorator) {
		this.decorator = decorator;
		redraw();
	}

	public int getDecoratorAlignment() {
		return decoratorAlignment;
	}

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

	public int getEdgesRadius() {
		return edgeRadius;
	}

	public void setEdgeRadius(final int edgeRadius) {
		this.edgeRadius = edgeRadius;
		redraw();
	}

	public Font getFont(final int index) {
		final Font font = getTextDescriptor(index).font;
		return font == null ? getFont() : font;
	}

	public void setFont(final int index, final Font font) {
		getTextDescriptor(index).font = font;
		redraw();
	}

	public Color getForeground(final int index) {
		final Color foreground = getTextDescriptor(index).foreground;
		return foreground == null ? getForeground() : foreground;
	}

	public void setForeground(final int index, final Color foreground) {
		getTextDescriptor(index).foreground = foreground;
		redraw();
	}

	public int[] getMargins() {
		return new int[] { marginHeight, marginWidth, textSpacing, decoratorSpacing };
	}

	public void setMargins(final int marginHeight, final int marginWidth, final int textSpacing, final int decoratorSpacing) {
		this.marginHeight = marginHeight;
		this.marginWidth = marginWidth;
		this.decoratorSpacing = decoratorSpacing;
		this.textSpacing = textSpacing;

		redraw();
	}

	public boolean isShowSelection() {
		return showSelection;
	}

	public void setShowSelection(final boolean showSelection) {
		this.showSelection = showSelection;
		redraw();
	}

	public String getText(final int index) {
		return getTextDescriptor(index).text;
	}

	public void setText(final int index, final String text) {
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		getTextDescriptor(index).text = text;
		redraw();
	}

	public boolean isWrap(final int index) {
		return getTextDescriptor(index).wrap;
	}

	public void setWrap(final int index, final boolean wrap) {
		getTextDescriptor(index).wrap = wrap;
		redraw();
	}

	public void addSelectionListener(final SelectionListener listener) {
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		addListener(SWT.Selection, new TypedListener(listener));
	}

	public void removeSelectionListener(final SelectionListener listener) {
		if (listener == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Selection, listener);
	}

	private TextDescriptor getTextDescriptor(final int index) {
		if (index < 0 || index >= textDescriptors.size()) SWT.error(SWT.ERROR_INVALID_RANGE);
		return textDescriptors.get(index);
	}

	private int computeMaxTextWidth(final int maxWidth) {
		int width = maxWidth - 2 * marginWidth;

		if ((decoratorAlignment & SWT.LEFT) > 0 || (decoratorAlignment & SWT.RIGHT) > 0) {
			width -= getDecoratorSize().x + getDecoratorSpacing();
		}

		return Math.max(10, width - 2 * borderWidth);
	}

	private Point computeTextLocation() {
		final Point decoratorSize = getDecoratorSize();
		final Point pos = new Point(0, 0);

		if ((decoratorAlignment & SWT.LEFT) > 0) {
			pos.x = marginWidth + decoratorSize.x + getDecoratorSpacing() + borderWidth;
		} else {
			pos.x = marginWidth + borderWidth;
		}

		if (decoratorAlignment == SWT.TOP) {
			pos.y = marginHeight + decoratorSize.y + getDecoratorSpacing() + borderWidth;
		} else {
			pos.y = marginHeight + borderWidth;
		}

		return pos;
	}

	private TextArea createTextArea(final int index, final int wHint) {
		final TextLayout layout = createTextLayout(index, computeMaxTextWidth(wHint), SWT.DEFAULT);
		final Point pos = computeTextLocation();

		return new TextArea(layout, pos);
	}

	private TextArea[] createTextAreas(final int wHint) {
		final TextArea[] textAreas = new TextArea[textDescriptors.size()];
		final Point decoratorSize = getDecoratorSize();

		for (int i = 0; i < textAreas.length; i++) {
			textAreas[i] = createTextArea(i, wHint);
			textAreas[i].pos.y = 0;
		}

		int y = marginHeight + borderWidth;

		if (decoratorAlignment == SWT.TOP) {
			y += decoratorSize.y + decoratorSpacing;

		} else if (decoratorAlignment == SWT.CENTER && textAreas.length > 0) {
			int offset = Math.min(getNonEmptyTextCount(), 2) * decoratorSpacing;
			offset -= getNonEmptyTextCount() >= 2 ? textSpacing : 0;
			offset += decoratorSize.y;

			for (int i = 1; i < textAreas.length; i++) {
				textAreas[i].pos.y += offset;
			}

		} else if (decoratorAlignment != SWT.BOTTOM) {
			y += Math.max(0, (decoratorSize.y - getTotalTextHeight(textAreas)) / 2);
		}

		for (int i = 0; i < textAreas.length; i++) {
			final int height = textAreas[i].getBounds().height;

			textAreas[i].pos.y += y;
			y += height + (height == 0 ? 0 : textSpacing);
		}

		return textAreas;
	}

	private TextLayout createTextLayout(final int index, final int maxWidth, final int maxHeight) {
		final MarkupText markupText = new MarkupText(getDisplay(), new HtmlMarkupParser());

		markupText.setFont(getFont(index));
		markupText.setForeground(getForeground(index));
		markupText.setAlignment(getAlignment(index));
		markupText.setWrap(isWrap(index));
		markupText.layout(getText(index), maxWidth, isWrap(index) ? SWT.DEFAULT : maxHeight);

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
			if (!textDescriptors.isEmpty()) {
				final int height = createTextArea(0, size.width).getBounds().height;
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

		return new Rectangle(x, y, decoratorSize.x, decoratorSize.y);
	}

	private Point getDecoratorSize() {
		return hasDecorator() ? decorator.getSize() : new Point(0, 0);
	}

	private int getDecoratorSpacing() {
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
		for (final TextDescriptor descriptor : textDescriptors) {
			if (!descriptor.text.isEmpty()) {
				return true;
			}
		}
		return !textDescriptors.isEmpty();
	}

	private int getNonEmptyTextCount() {
		int count = textDescriptors.size();

		for (final TextDescriptor descriptor : textDescriptors) {
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
