package org.greip.markup;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.greip.common.Util;
import org.greip.tile.Alignment;

public class MarkupText {

	private final Device device;
	private Alignment alignment = Alignment.Left;
	private final Map<Point, String> links = new HashMap<>();
	private Font font;
	private Color foreground;
	private boolean wrap = true;
	private Optional<Consumer<ParseException>> exceptionHandler = Optional.empty();
	private final TextLayout textLayout;
	private final IMarkupParser parser;

	public MarkupText(final Device device, final IMarkupParser parser) {
		this.device = device;
		this.parser = parser;
		this.textLayout = new TextLayout(device);
	}

	public void layout(final String markupText, final int maxWidth, final int maxHeight) {
		textLayout.setWidth(maxWidth == SWT.DEFAULT ? SWT.DEFAULT : Math.max(maxWidth, 1));
		textLayout.setAlignment(getAlignment().style);
		textLayout.setJustify(getAlignment() == Alignment.Justify);
		links.clear();

		String plainText;
		try {
			parser.setDefaultFont(getFont());
			parser.parse(markupText);
			plainText = parser.getPlainText();

		} catch (final ParseException e) {
			exceptionHandler.ifPresent(c -> c.accept(e));
			plainText = markupText;
		}

		final List<StyleRange> styleRanges = Arrays.asList(parser.getStyleRanges());
		applyTextAndStyles(plainText, styleRanges, false);

		if (!wrap && textLayout.getLineCount() > 1) {
			final String text = plainText.substring(0, Math.min(plainText.length(), textLayout.getLineOffsets()[1] + 20));
			final StringBuilder buf = new StringBuilder(text).append("...");

			do {
				buf.deleteCharAt(buf.length() - 4);
				applyTextAndStyles(buf.toString(), styleRanges, true);
			} while (textLayout.getLineCount() > 1);
		}
	}

	private void applyTextAndStyles(final String text, final List<StyleRange> styleRanges, final boolean shorten) {
		links.clear();

		textLayout.setText(text);
		textLayout.setStyle(new TextStyle(getFont(), getForeground(), null), 0, text.length());

		for (final StyleRange range : styleRanges) {
			if (range.start < text.length() - (shorten ? 3 : 0)) {
				range.foreground = Util.nvl(range.foreground, getForeground());
				textLayout.setStyle(range, range.start, Math.min(range.start + range.length - 1, text.length() - (shorten ? 4 : 1)));

				if (range instanceof LinkRange) {
					links.put(new Point(range.start, range.length), ((LinkRange) range).getId());
					range.data = ((LinkRange) range).getId();
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

	public boolean isWrap() {
		return wrap;
	}

	public void setWrap(final boolean wrap) {
		this.wrap = wrap;
	}

	public int getOrientation() {
		return textLayout.getOrientation();
	}

	public void setOrientation(final int orientation) {
		textLayout.setOrientation(orientation);
	}

	public Map<Point, String> getLinks() {
		return links;
	}

	public LinkRange getLinkAtLocation(final int x, final int y) {
		final int offset = textLayout.getOffset(x, y, new int[1]);
		final TextStyle style = textLayout.getStyle(offset);

		if (style instanceof LinkRange) {
			final TextStyle[] styles = textLayout.getStyles();
			final int[] ranges = textLayout.getRanges();

			for (int i = 0; i < styles.length; i++) {
				if (styles[i] == style) {
					final Rectangle bounds = textLayout.getBounds(ranges[i * 2], ranges[i * 2 + 1]);
					return bounds.contains(x, y) ? (LinkRange) style : null;
				}
			}
		}

		return null;
	}

	public TextLayout getTextLayout() {
		return textLayout;
	}

	public Point getSize() {
		final Rectangle bounds = textLayout.getBounds();
		return new Point(bounds.width + 20, bounds.height);
	}

	public void setExceptionHandler(final Consumer<ParseException> exceptionHandler) {
		this.exceptionHandler = Optional.ofNullable(exceptionHandler);
	}
}
