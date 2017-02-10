/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.separator;

import java.io.InputStream;

import org.eclipse.swt.SWT;
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

public class Separator extends Composite {

	private int lineWidth = 1;
	private LineStyle lineStyle;
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

	public Separator(final Composite parent, final int style) {
		this(parent, style, LineStyle.ShadowIn);
	}

	public Separator(final Composite parent, final int style, final LineStyle lineStyle) {
		super(parent, SWT.DOUBLE_BUFFERED);

		this.orientation = (style & SWT.VERTICAL) != 0 ? SWT.VERTICAL : SWT.HORIZONTAL;
		this.lineStyle = lineStyle;

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
		final Point textSize = getTextSize();
		final int x = imageDecorator.getSize().x + textSize.x + indent + getSpacingCount() * spacing + 20;
		final int y = Math.max(imageDecorator.getSize().y, Math.max(textSize.y, getLineWidth()));
		final int marginX = 2 * (marginWidth + getBorderWidth());
		final int marginY = 2 * (marginHeight + getBorderWidth());

		return isVertical() ? new Point(y + marginX, x + marginY) : new Point(x + marginX, y + marginY);
	}

	@Override
	public Color getBackground() {
		return Util.nvl(background, getParent().getBackground());
	}

	private Rectangle getImageBounds() {
		final Point imageSize = imageDecorator.getSize();
		final int margin = isVertical() ? marginHeight : marginWidth;
		final Rectangle size = getClientArea();
		final int height = isVertical() ? size.width : size.height;

		return new Rectangle(indent + (indent == 0 ? 0 : spacing) + margin - 1, (height - imageSize.y) / 2, imageSize.x, imageSize.y);
	}

	public int getIndent() {
		return indent;
	}

	public int getLineCap() {
		return lineCap;
	}

	public Color getLineColor() {
		return Util.nvl(lineColor, getForeground());
	}

	private Color[] getLineColors() {
		final RGB backgroundRGB = getBackground().getRGB();
		final Color darkColor = new Color(getDisplay(), Util.getDimmedRGB(backgroundRGB, 0.15f));
		final Color lightColor = new Color(getDisplay(), Util.getDimmedRGB(backgroundRGB, -0.15f));

		return lineStyle == LineStyle.ShadowIn ? new Color[] { lightColor, darkColor } : new Color[] { darkColor, lightColor };
	}

	public int[] getLineDashs() {
		return lineDashs;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public int getLineWidth() {
		return lineStyle == LineStyle.ShadowIn || lineStyle == LineStyle.ShadowOut ? 2 : lineWidth;
	}

	public int getMarginHeight() {
		return marginHeight;
	}

	public int getMarginWidth() {
		return marginWidth;
	}

	@Override
	public int getOrientation() {
		return orientation;
	}

	public int getSpacing() {
		return spacing;
	}

	private int getSpacingCount() {
		return (imageDecorator.getSize().x == 0 ? 0 : 1) + (getText().isEmpty() ? 0 : 1) + (indent == 0 ? 0 : 1);
	}

	public String getText() {
		return Util.nvl(text, ""); //$NON-NLS-1$
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

	private boolean isVertical() {
		return orientation == SWT.VERTICAL;
	}

	@Override
	public void setBackground(final Color background) {
		this.background = background;
		redraw();
	}

	public void setImage(final Image image) {
		imageDecorator.setImage(image);
		redraw();
	}

	public void loadImage(final InputStream stream) {
		imageDecorator.loadImage(stream);
		redraw();
	}

	public void loadImage(final String filename) {
		imageDecorator.loadImage(filename);
		redraw();
	}

	public void setIndent(final int indent) {
		this.indent = indent;
		redraw();
	}

	public void setLineCap(final int lineCap) {
		this.lineCap = lineCap;
		redraw();
	}

	public void setLineColor(final Color lineColor) {
		this.lineColor = lineColor;
		redraw();
	}

	public void setLineDashs(final int[] lineDashs) {
		this.lineDashs = lineDashs;
		redraw();
	}

	public void setLineStyle(final LineStyle lineStyle) {
		this.lineStyle = lineStyle;
		redraw();
	}

	public void setLineWidth(final int lineWidth) {
		this.lineWidth = lineWidth;
		redraw();
	}

	public void setMarginHeight(final int marginHeight) {
		this.marginHeight = marginHeight;
		redraw();
	}

	public void setMarginWidth(final int marginWidth) {
		this.marginWidth = marginWidth;
		redraw();
	}

	@Override
	public void setOrientation(final int orientation) {
		this.orientation = orientation;
		redraw();
	}

	public void setSpacing(final int spacing) {
		this.spacing = spacing;
		redraw();
	}

	public void setText(final String text) {
		this.text = text;
		redraw();
	}
}
