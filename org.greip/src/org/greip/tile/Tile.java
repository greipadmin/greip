package org.greip.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
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
import org.greip.common.Greip;
import org.greip.common.Util;
import org.greip.decorator.IDecorator;

public class Tile extends Composite {

	private class SelectionHandler implements Listener {

		private void fireSelectionEvent(final Event event, final int detail, final Object data) {
			final SelectionEvent e = new SelectionEvent(event);
			e.detail = detail;
			e.data = data;

			for (final SelectionListener selectionListener : selectionListeners) {
				selectionListener.widgetSelected(e);
			}
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
				fireSelectionEvent(event, Greip.LINK, linkId);
			} else if (getDecoratorBounds().contains(event.x, event.y)) {
				fireSelectionEvent(event, Greip.DECORATOR, null);
			} else {
				fireSelectionEvent(event, SWT.NONE, null);
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

		Rectangle getBounds() {
			final Rectangle bounds = layout.getBounds();
			return new Rectangle(pos.x, pos.y, bounds.width, bounds.height);
		}
	}

	private static class TextDescriptor {
		public String text;
		public Alignment alignment;
		public Font font;
		public Color foreground;
		public boolean wrap;
	}

	private final Set<SelectionListener> selectionListeners = new HashSet<>();

	private int decoratorAlignment = SWT.LEFT;
	private int marginHeight = 0;
	private int marginWidth = 0;
	private IDecorator decorator;
	private int imageSpacing;
	private int titleSpacing;
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

//		if (parent instanceof TileBar) {
//			((TileBar) parent).addItem(this);
//		}

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

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				disposeBackgroundColors();
			}
		});

		setBackground(getBackground());
		setMargins(10, 10, 5, 10);
		setDecoratorAlignment(SWT.RIGHT);
	}

//	public Tile(final TileBar parent, final int style) {
//		this(parent);
//		parent.addItem(this);
//	}

	public void addSelectionListener(final SelectionListener selectionListener) {
		selectionListeners.add(selectionListener);
	}

	public void addText(final String text, final Alignment alignment) {
		addText(text, alignment, null, null, true);
	}

	public void addText(final String text, final Alignment alignment, final Font font, final Color foreground, final boolean wrap) {
		final TextDescriptor descriptor = new TextDescriptor();
		descriptor.text = text;
		descriptor.alignment = alignment;
		descriptor.font = font;
		descriptor.foreground = foreground;
		descriptor.wrap = wrap;

		textDescriptors.add(descriptor);

		redraw();
	}

	public void clear() {
		textDescriptors.clear();
	}

	private int computeMaxTextWidth(final int maxWidth) {
		int width = 0;

		if (decoratorAlignment == SWT.TOP || decoratorAlignment == SWT.BOTTOM) {
			width = maxWidth - 2 * marginWidth;
		} else {
			width = maxWidth - 2 * marginWidth - getDecoratorSize().x - getImageSpacing();
		}

		return Math.max(10, width - 2 * borderWidth);
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
			size.x += decoratorSize.x + getImageSpacing() + width;
			size.y += Math.max(decoratorSize.y, height);
		} else {
			size.x += Math.max(decoratorSize.x, width);
			size.y += decoratorSize.y + getImageSpacing() + height;
		}

		return size;
	}

	private Point computeTextLocation() {
		final Point decoratorSize = getDecoratorSize();
		final Point pos = new Point(0, 0);

		if ((decoratorAlignment & SWT.LEFT) > 0) {
			pos.x = marginWidth + decoratorSize.x + getImageSpacing() + borderWidth;
		} else {
			pos.x = marginWidth + borderWidth;
		}

		if (decoratorAlignment == SWT.TOP) {
			pos.y = marginHeight + decoratorSize.y + getImageSpacing() + borderWidth;
		} else {
			pos.y = marginHeight + borderWidth;
		}

		return pos;
	}

	private TextArea createTextArea(final int index, final int wHint) {
		final TextLayout layout = createTextLayout(index, computeMaxTextWidth(wHint), Integer.MAX_VALUE);
		final Point pos = computeTextLocation();

		return new TextArea(layout, pos);
	}

	private TextArea[] createTextAreas(final int wHint) {
		final TextArea[] textAreas = new TextArea[textDescriptors.size()];

		for (int i = 0; i < textAreas.length; i++) {
			textAreas[i] = createTextArea(i, wHint);
		}

		int y = marginHeight + borderWidth;
		if (decoratorAlignment == SWT.TOP) {
			y += getDecoratorSize().y + imageSpacing;
		} else if (decoratorAlignment != SWT.BOTTOM) {
			y += Math.max(0, (getDecoratorSize().y - getTotalTextHeight(textAreas)) / 2);
		}

		for (int i = 0; i < textAreas.length; i++) {
			textAreas[i].pos.y = y;
			y += textAreas[i].getBounds().height + titleSpacing;
		}

		return textAreas;
	}

	private TextLayout createTextLayout(final int index, final int maxWidth, final int maxHeight) {
		final MarkupText markupText = new MarkupText(getDisplay());

		markupText.setFont(getFont(index));
		markupText.setForeground(getForeground(index));
		markupText.setAlignment(getAlignment(index));
		markupText.layout(getText(index), maxWidth, isWrap(index) ? -1 : maxHeight);

		return markupText.getTextLayout();
	}

	private void disposeBackgroundColors() {
		for (final Color color : dimmedBackground) {
			if (color != null) {
				color.dispose();
			}
		}
	}

	public Alignment getAlignment(final int index) {
		return textDescriptors.get(index).alignment;
	}

	public Color getBorderColor() {
		return borderColor != null ? borderColor : getDisplay().getSystemColor(SWT.COLOR_WIDGET_BORDER);
	}

	@Override
	public int getBorderWidth() {
		return borderWidth;
	}

	public IDecorator getDecorator() {
		return decorator;
	}

	public int getDecoratorAlignment() {
		return decoratorAlignment;
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

	public int getEdgesRadius() {
		return edgeRadius;
	}

	public Font getFont(final int index) {
		final Font font = textDescriptors.get(index).font;
		return font == null ? getFont() : font;
	}

	public Color getForeground(final int index) {
		final Color foreground = textDescriptors.get(index).foreground;
		return foreground == null ? getForeground() : foreground;
	}

	private int getImageSpacing() {
		return hasDecorator() && !isEmpty() ? imageSpacing : 0;
	}

	public int[] getMargins() {
		return new int[] { marginHeight, marginWidth, titleSpacing, imageSpacing };
	}

	private static int getMaxTextWidth(final TextArea[] textAreas) {
		int maxWidth = 0;

		for (final TextArea textArea : textAreas) {
			maxWidth = Math.max(maxWidth, textArea.getBounds().width);
		}

		return maxWidth;
	}

	public String getText(final int index) {
		return textDescriptors.get(index).text;
	}

	private int getTotalTextHeight(final TextArea[] textAreas) {
		int totalHeight = 0;

		for (int i = 0; i < textAreas.length; i++) {
			if (!getText(i).isEmpty()) {
				totalHeight += textAreas[i].getBounds().height;
				if (i > 0) {
					totalHeight += titleSpacing;
				}
			}
		}

		return totalHeight;
	}

	private boolean hasDecorator() {
		return decorator != null;
	}

	private boolean isEmpty() {
		for (final TextDescriptor descriptor : textDescriptors) {
			if (descriptor.text != null && !descriptor.text.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isShowSelection() {
		return showSelection;
	}

	public boolean isWrap(final int index) {
		return textDescriptors.get(index).wrap;
	}

	public void removeSelectionListener(final SelectionListener selectionListener) {
		selectionListeners.remove(selectionListener);
	}

	public void setAlignment(final int index, final Alignment alignment) {
		textDescriptors.get(index).alignment = alignment;
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

	public void setBorderColor(final Color borderColor) {
		this.borderColor = borderColor;
		redraw();
	}

	public void setBorderWidth(final int borderWith) {
		this.borderWidth = borderWith;
		redraw();
	}

	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		super.setBounds(x, y, width, height);
		// maxTextWidth = computeMaxTextWidth(width);
	}

	public void setDecorator(final IDecorator decorator) {
		this.decorator = decorator;
	}

	public void setDecoratorAlignment(final int alignment) {
		this.decoratorAlignment = alignment;
		redraw();
	}

	public void setEdgeRadius(final int edgeRadius) {
		this.edgeRadius = edgeRadius;
		redraw();
	}

	public void setFont(final int index, final Font font) {
		textDescriptors.get(index).font = font;
		redraw();
	}

	public void setForeground(final int index, final Color foreground) {
		textDescriptors.get(index).foreground = foreground;
		redraw();
	}

	public void setMargins(final int marginHeight, final int marginWidth, final int titleSpacing, final int imageSpacing) {
		this.marginHeight = marginHeight;
		this.marginWidth = marginWidth;
		this.imageSpacing = imageSpacing;
		this.titleSpacing = titleSpacing;

		redraw();
	}

	public void setShowSelection(final boolean showSelection) {
		this.showSelection = showSelection;
		redraw();
	}

	public void setText(final int index, final String text) {
		textDescriptors.get(index).text = text;
		redraw();
	}

	public void setWrap(final int index, final boolean wrap) {
		textDescriptors.get(index).wrap = wrap;
		redraw();
	}
}
