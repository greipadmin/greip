package org.greip.label;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.greip.common.Util;
import org.greip.label.MarkupParser.Link;

class MarkupText {

	private final Device device;
	private Alignment alignment;
	private final Map<Point, Link> links = new HashMap<>();
	private Font font;
	private Color foreground;
	private String text;
	private TextLayout textLayout;

	public MarkupText(final Device device) {
		this.device = device;
	}

	public TextLayout layout(final int maxWidth, final int maxHeight) {
		textLayout = new TextLayout(device);
		textLayout.setWidth(maxWidth);
		textLayout.setAlignment(getAlignment().style);
		textLayout.setJustify(getAlignment().justify);
		textLayout.setFont(getFont());

		links.clear();

		StyledString styledString;
		try {
			final MarkupParser parser = new MarkupParser();
			styledString = parser.parse("<body>" + text + "</body>", getFont());

		} catch (final Exception e) {
			System.err.println(e);
			styledString = new StyledString(text);
		}

		applyTextAndStyles(styledString, textLayout, false);

		// if (maxHeight != -1 && textLayout.getLineCount() > 1) {
		// final String text = plainText.substring(0, Math.min(plainText.length(),
		// textLayout.getLineOffsets()[1] + 20));
		// final StringBuilder buf = new StringBuilder(text).append("...");
		// do {
		// buf.deleteCharAt(buf.length() - 4);
		// applyTextAndStyles(buf.toString(), textLayout, parser.getStyleRanges(),
		// true);
		// } while (textLayout.getLineCount() > 1);
		// }

		return textLayout;
	}

	private void applyTextAndStyles(final StyledString styledString, final TextLayout textLayout, final boolean shorten) {
		links.clear();

		final String text = styledString.getString();
		textLayout.setText(text);
		try {
			textLayout.setStyle(new TextStyle(getFont(), getForeground(), null), 0, text.length() - 1);
		} catch (final Exception e) {
			System.out.println();
		}

		for (final StyleRange range : styledString.getStyleRanges()) {
			if (range.start < text.length() - (shorten ? 3 : 0)) {
				range.foreground = Util.nvl(range.foreground, getForeground());
				textLayout.setStyle(range, range.start, Math.min(range.start + range.length - 1, text.length() - (shorten ? 4 : 1)));

				if (range.data instanceof Link) {
					links.put(new Point(range.start, range.length), (Link) range.data);
				}
			}
		}
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(final Alignment alignment) {
		this.alignment = alignment;
	}

	public Font getFont() {
		return font == null ? device.getSystemFont() : font;
	}

	public void setFont(final Font font) {
		this.font = font;
	}

	public Color getForeground() {
		return foreground;
	}

	public void setForeground(final Color foreground) {
		this.foreground = foreground;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public Link getLinkAtLocation(final int x, final int y) {
		final int offset = textLayout.getOffset(x, y, new int[1]);
		final TextStyle textStyle = textLayout.getStyle(offset);

		return (textStyle != null && textStyle.data instanceof Link) ? (Link) textStyle.data : null;
	}
}
