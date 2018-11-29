package org.greip.markup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HtmlMarkupParser implements IMarkupParser {

	private enum Tag {
		BODY,
		U,
		B,
		BR,
		I,
		A,
		S,
		SUB,
		SUP,
		STYLE
	}

	private static class MarkupHandler extends DefaultHandler {

		private Font font;
		private int fontStyle;
		private int fontHeight;
		private String fontName;
		private boolean subscript;
		private boolean superscript;
		private boolean underline;
		private boolean strikeout;

		private RGB foreground;
		private RGB background;
		private String link;

		private boolean lastUnderline;
		private RGB lastForeground;
		private boolean lastStrikeout;

		private final List<Tag> tagStack = new ArrayList<>();
		private StyleRange currentStyleRange;
		private final List<StyleRange> styleRanges = new ArrayList<>();
		private final StringBuilder plainText = new StringBuilder();
		private final Font defaultFont;

		public MarkupHandler(final Font defaultFont) {
			this.defaultFont = defaultFont;
		}

		public List<StyleRange> getStyleRanges() {
			return styleRanges;
		}

		public String getPlaintText() {
			return plainText.toString();
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
				throws SAXException {
			final Tag t = getTag(qName);

			if (tagStack.contains(t)) {
				throw new SAXException("Close tag <" + t + "> before open a new one.");
			}

			if (t == Tag.BODY) {
				font = defaultFont;
				fontStyle = font == null ? SWT.NONE : font.getFontData()[0].getStyle();
				fontHeight = 0;
				fontName = null;
				underline = false;
				strikeout = false;
				subscript = false;
				superscript = false;
				link = null;
			}

			if (t == Tag.BR) {
				styleRanges.add(new StyleRange(plainText.length(), 1, null, null));
				plainText.append('\n');
				return;
			}

			if (t == Tag.B) {
				fontStyle |= SWT.BOLD;
			} else if (t == Tag.I) {
				fontStyle |= SWT.ITALIC;
			} else if (t == Tag.U) {
				underline = true;
			} else if (t == Tag.S)
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
				fontHeight = getSize(attributes.getValue("size"));
				fontName = attributes.getValue("font");
			}

			if (t == Tag.A) {
				lastUnderline = underline;
				lastForeground = foreground;
				lastStrikeout = strikeout;
				underline = true;
				foreground = Display.getCurrent().getSystemColor(SWT.COLOR_LINK_FOREGROUND).getRGB();
				link = Objects.toString(attributes.getValue("href"), "");
			} else {
				link = null;
			}

			tagStack.add(t);
		}

		private static Tag getTag(final String qName) throws SAXException {
			try {
				return Tag.valueOf(qName.toUpperCase());
			} catch (final IllegalArgumentException e) {
				throw new SAXException("Unknown tag <" + qName.toUpperCase() + ">");
			}
		}

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			currentStyleRange = link == null ? new StyleRange() : new LinkRange(link.length() == 0 ? new String(ch, start, length) : link);
			currentStyleRange.start = plainText.length();
			currentStyleRange.underline = underline;
			currentStyleRange.strikeout = strikeout;
			currentStyleRange.font = getFont();
			currentStyleRange.foreground = getColor(foreground);
			currentStyleRange.background = getColor(background);
			currentStyleRange.length = length;
			currentStyleRange.rise = getRise();
			styleRanges.add(currentStyleRange);

			plainText.append(ch, start, length);
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			final Tag t = getTag(qName);

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
			} else if (t == Tag.SUB)
				subscript = false;
			else if (t == Tag.SUP) {
				superscript = false;
			} else if (t == Tag.STYLE) {
				foreground = null;
				background = null;
				fontHeight = 0;
				fontName = null;
			} else if (t == Tag.A) {
				foreground = lastForeground;
				underline = lastUnderline;
				strikeout = lastStrikeout;
				link = null;
			}

			tagStack.remove(tagStack.size() - 1);
		}

		private static RGB getRGB(final String color) {
			return color == null ? null
					: new RGB(Integer.parseInt(color.substring(1, 3), 16), Integer.parseInt(color.substring(3, 5), 16),
							Integer.parseInt(color.substring(5, 7), 16));
		}

		private static Color getColor(final RGB rgb) {
			final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
			final String name = String.valueOf(rgb);

			if (rgb != null && !colorRegistry.hasValueFor(name)) {
				colorRegistry.put(name, rgb);
			}

			return colorRegistry.get(name);
		}

		private Font getFont() {
			final FontData fontData;

			if (fontName != null) {
				fontData = new FontData(fontName, getFontHeight(), fontStyle);
			} else {
				fontData = getDefaultFontData();
				fontData.setStyle(fontStyle);
				fontData.setHeight(getFontHeight());
			}

			final String name = fontData.toString();
			final FontRegistry fontRegistry = JFaceResources.getFontRegistry();

			if (!fontRegistry.hasValueFor(name)) {
				fontRegistry.put(name, new FontData[] { fontData });
			}

			return fontRegistry.get(name);
		}

		private int getFontHeight() {
			final FontData fontData = getDefaultFontData();
			final int height = fontHeight == 0 ? fontData.getHeight() : fontHeight;

			return subscript || superscript ? (int) (height * 0.6f) : height;
		}

		private FontData getDefaultFontData() {
			final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
			return Optional.ofNullable(font).orElse(fontRegistry.defaultFont()).getFontData()[0];
		}

		private int getRise() {
			return superscript ? getFontHeight() - 1 : subscript ? -1 : 0;
		}

		private static int getSize(final String size) throws SAXException {
			try {
				return size == null ? 0 : Integer.valueOf(size).intValue();
			} catch (final NumberFormatException e) {
				throw new SAXException(e);
			}
		}
	}

	private Font defaultFont;

	private List<StyleRange> styleRanges = new ArrayList<>();
	private String plainText;

	/*
	 * (non-Javadoc)
	 * @see org.greip.tile.IMarkupParser#parse(java.lang.String)
	 */
	@Override
	public void parse(final String markup) throws ParseException {
		final MarkupHandler handler = new MarkupHandler(getDefaultFont());

		styleRanges = Collections.emptyList();
		plainText = markup;

		try {
			final String html = "<body>" + markup + "</body>";
			final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), handler);

			styleRanges = handler.getStyleRanges();
			plainText = handler.getPlaintText();

		} catch (IOException | ParserConfigurationException e) {
			throw new IllegalStateException(e);

		} catch (final SAXException e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.greip.tile.IMarkupParser#getPlainText()
	 */
	@Override
	public String getPlainText() {
		return plainText;
	}

	/*
	 * (non-Javadoc)
	 * @see org.greip.tile.IMarkupParser#getStyleRanges()
	 */
	@Override
	public StyleRange[] getStyleRanges() {
		return styleRanges.toArray(new StyleRange[styleRanges.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.greip.tile.IMarkupParser#getDefaultFont()
	 */
	@Override
	public Font getDefaultFont() {
		return defaultFont;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.greip.tile.IMarkupParser#setDefaultFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setDefaultFont(final Font defaultFont) {
		this.defaultFont = defaultFont;
	}
}