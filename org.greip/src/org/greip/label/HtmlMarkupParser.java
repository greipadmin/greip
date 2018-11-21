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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class HtmlMarkupParser extends DefaultHandler {

	private enum Tag {
		BODY,
		U,
		B,
		I,
		S,
		STYLE,
		BR,
		LINK,
		UL,
		LI,
		SUB,
		SUP
	}

	private final List<Tag> tagStack = new ArrayList<>();
	private Font defaultFont;

	private int fontStyle;
	private int fontHeight;
	private boolean subscript;
	private boolean superscript;

	private boolean underline;
	private boolean strikeout;
	private RGB foreground;
	private RGB background;
	private LinkDescriptor link;

	private boolean lastUnderline;
	private RGB lastForeground;
	private boolean lastStrikeout;

	private ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
	private FontRegistry fontRegistry = JFaceResources.getFontRegistry();
	private StyledString styledString;

	/**
	 * Parse the content, build the list of style ranges and apply them to the
	 * styled text widget
	 *
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public StyledString parse(final String html, final Font defaultFont) throws IOException, SAXException, ParserConfigurationException {
		this.defaultFont = defaultFont;
		this.fontStyle = defaultFont == null ? SWT.NONE : defaultFont.getFontData()[0].getStyle();
		this.fontHeight = 0;
		this.underline = false;
		this.strikeout = false;
		this.subscript = false;
		this.superscript = false;
		this.link = null;

		styledString = new StyledString();

		final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), this);

		return styledString;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		final Tag t = Tag.valueOf(qName.toUpperCase());

		if (tagStack.contains(t)) throw new SAXException("Close tag <" + t + "> before open a new one.");

		if (t == Tag.BR) {
			styledString.append('\n');
			return;
		}

		if (t == Tag.B)
			fontStyle |= SWT.BOLD;
		else if (t == Tag.I)
			fontStyle |= SWT.ITALIC;
		else if (t == Tag.U)
			underline = true;
		else if (t == Tag.S)
			strikeout = true;
		else if (t == Tag.SUB) {
			if (superscript) throw new SAXException("Close tag <SUP> before open SUB>.");
			subscript = true;
		} else if (t == Tag.SUP) {
			if (subscript) throw new SAXException("Close tag <SUB> before open SUP>.");
			superscript = true;
		} else if (t == Tag.STYLE) {
			foreground = getRGB(attributes.getValue("fg"));
			background = getRGB(attributes.getValue("bg"));
			fontHeight = toInt(attributes.getValue("size"), 10);
		}

		if (t == Tag.LINK) {
			lastUnderline = underline;
			lastForeground = foreground;
			lastStrikeout = strikeout;
			underline = true;
			foreground = defaultFont.getDevice().getSystemColor(SWT.COLOR_LINK_FOREGROUND).getRGB();
			link = new LinkDescriptor(attributes.getValue("id"));
		} else
			link = null;

		tagStack.add(t);
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		final String text = new String(ch, start, length);

		if (link != null) link.setUrl(text);

		final TextStyler styler = new TextStyler(getFont(), getColor(background), getColor(foreground), underline, strikeout, link,
				getRise());

		styledString.append(text);
		styledString.setStyle(styledString.length() - length, length, styler);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		final Tag t = Tag.valueOf(qName.toUpperCase());

		if (t == Tag.BR) return;

		if (t == Tag.B)
			fontStyle &= ~SWT.BOLD;
		else if (t == Tag.I)
			fontStyle &= ~SWT.ITALIC;
		else if (t == Tag.U)
			underline = false;
		else if (t == Tag.S)
			strikeout = false;
		else if (t == Tag.SUB)
			subscript = false;
		else if (t == Tag.SUP)
			superscript = false;
		else if (t == Tag.STYLE) {
			foreground = null;
			background = null;
			fontHeight = 0;
		} else if (t == Tag.LINK) {
			foreground = lastForeground;
			underline = lastUnderline;
			strikeout = lastStrikeout;
			link = null;
		}

		tagStack.remove(tagStack.size() - 1);
	}

	public final void setColorRegistry(final ColorRegistry colorRegistry) {
		this.colorRegistry = colorRegistry;
	}

	public final void setFontRegistry(final FontRegistry fontRegistry) {
		this.fontRegistry = fontRegistry;
	}

	private static RGB getRGB(final String color) throws SAXException {
		if (color == null) return null;
		if (!color.matches("#[0-9A-Fa-f]{6}")) throw new SAXException("invalid color definition \"" + color + '"');

		return new RGB(toInt(color.substring(1, 3), 16), toInt(color.substring(3, 5), 16), toInt(color.substring(5, 7), 16));
	}

	private static int toInt(final String value, final int radix) {
		return value == null ? 0 : Integer.parseInt(value, radix);
	}

	private Color getColor(final RGB rgb) {
		final String name = String.valueOf(rgb);

		if (rgb != null && !colorRegistry.hasValueFor(name)) colorRegistry.put(name, rgb);

		return colorRegistry.get(name);
	}

	private Font getFont() {
		final FontData fontData = defaultFont.getFontData()[0];
		fontData.setStyle(fontStyle);
		fontData.setHeight(getFontHeight());

		final String name = fontData.toString();

		if (!fontRegistry.hasValueFor(name)) fontRegistry.put(name, new FontData[] { fontData });

		return fontRegistry.get(name);
	}

	private int getFontHeight() {
		final FontData fontData = defaultFont.getFontData()[0];
		final int height = fontHeight == 0 ? fontData.getHeight() : fontHeight;

		return subscript || superscript ? (int) (height * 0.7f) : height;
	}

	private int getRise() {
		return superscript ? getFontHeight() - 1 : subscript ? -3 : 0;
	}
}