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
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class MarkupParser extends DefaultHandler {

	private enum Tag {
		BODY,
		U,
		B,
		I,
		S,
		STYLE,
		BR,
		LINK
	}

	static class Link {
		String id;
		String url;

		Link(final String id) {
			this.id = id;
		}
	}

	static class MarkupStyler extends Styler {

		private final Font font;
		private final Color background;
		private final Color foreground;
		private final boolean underline;
		private final boolean strikeout;
		private final Object data;

		public MarkupStyler(final Font font, final Color background, final Color foreground, final boolean underline, final boolean strikeout,
				final Object data) {
			this.font = font;
			this.background = background;
			this.foreground = foreground;
			this.underline = underline;
			this.strikeout = strikeout;
			this.data = data;
		}

		@Override
		public void applyStyles(final TextStyle style) {
			style.underline = underline;
			style.strikeout = strikeout;
			style.font = font;
			style.foreground = foreground;
			style.background = background;
			style.data = data;
		}
	}

	private final List<Tag> tagStack = new ArrayList<>();
	private Font defaultFont;

	private int fontStyle;
	private int fontHeight;

	private boolean underline;
	private boolean strikeout;
	private RGB foreground;
	private RGB background;
	private Link link;

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
		this.link = null;

		styledString = new StyledString();

		final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), this);

		return styledString;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		final Tag t = Tag.valueOf(qName.toUpperCase());

		if (tagStack.contains(t)) {
			throw new SAXException("Close tag <" + t + "> before open a new one.");
		}

		if (t == Tag.BR) {
			styledString.append('\n');
			return;
		}

		if (t == Tag.B) {
			fontStyle |= SWT.BOLD;
		} else if (t == Tag.I) {
			fontStyle |= SWT.ITALIC;
		} else if (t == Tag.U) {
			underline = true;
		} else if (t == Tag.S) {
			strikeout = true;
		} else if (t == Tag.STYLE) {
			foreground = getRGB(attributes.getValue("fg"));
			background = getRGB(attributes.getValue("bg"));
			fontHeight = toInt(attributes.getValue("size"));
		}

		if (t == Tag.LINK) {
			lastUnderline = underline;
			lastForeground = foreground;
			lastStrikeout = strikeout;
			underline = true;
			foreground = defaultFont.getDevice().getSystemColor(SWT.COLOR_LINK_FOREGROUND).getRGB();
			link = new Link(attributes.getValue("id"));
		} else {
			link = null;
		}

		tagStack.add(t);
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		final String text = new String(ch, start, length);

		if (link != null) {
			link.url = text;
		}

		final MarkupStyler styler = new MarkupStyler(getFont(), getColor(background), getColor(foreground), underline, strikeout, link);

		styledString.append(text);
		styledString.setStyle(styledString.length() - length, length, styler);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		final Tag t = Tag.valueOf(qName.toUpperCase());

		if (t == Tag.BR) {
			return;
		}

		if (t == Tag.B) {
			fontStyle &= ~SWT.BOLD;
		} else if (t == Tag.I) {
			fontStyle &= ~SWT.ITALIC;
		} else if (t == Tag.U) {
			underline = false;
		} else if (t == Tag.S) {
			strikeout = false;
		} else if (t == Tag.STYLE) {
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
		if (color == null) {
			return null;
		}
		if (!color.matches("#[0-9A-Fa-f]{6}")) {
			throw new SAXException("invalid color definition \"" + color + '"');
		}
		return new RGB(Integer.parseInt(color.substring(1, 3), 16), Integer.parseInt(color.substring(3, 5), 16),
				Integer.parseInt(color.substring(5, 7), 16));
	}

	private static int toInt(final String value) {
		return value == null ? 0 : Integer.parseInt(value);
	}

	private Color getColor(final RGB rgb) {
		final String name = String.valueOf(rgb);

		if (rgb != null && !colorRegistry.hasValueFor(name)) {
			colorRegistry.put(name, rgb);
		}

		return colorRegistry.get(name);
	}

	private Font getFont() {
		final FontData fontData = defaultFont.getFontData()[0];
		fontData.setStyle(fontStyle);
		fontData.setHeight(fontHeight == 0 ? fontData.getHeight() : fontHeight);

		final String name = fontData.toString();

		if (!fontRegistry.hasValueFor(name)) {
			fontRegistry.put(name, new FontData[] { fontData });
		}

		return fontRegistry.get(name);
	}
}