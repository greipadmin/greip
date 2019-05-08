package org.greip.markup;

import java.text.ParseException;
import java.util.HashMap;
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
import org.eclipse.swt.widgets.Control;
import org.greip.common.Greip;
import org.greip.common.Util;

public class MarkupText {

	private final Device device;
	private int alignment = SWT.LEFT;
	private final Map<Point, String> links = new HashMap<>();
	private Font font;
	private Color foreground;
	private boolean wrap = true;
	private Optional<Consumer<ParseException>> exceptionHandler = Optional.empty();
	private final TextLayout textLayout;
	private final IMarkupParser parser;

	public MarkupText(final Control control, final IMarkupParser parser) {
		this.device = control.getDisplay();
		this.parser = parser;
		this.textLayout = new TextLayout(device);

		control.addListener(SWT.Dispose, e -> textLayout.dispose());
	}

	public void layout(final String markupText, final int maxWidth, final int maxHeight) {
		textLayout.setWidth(maxWidth == SWT.DEFAULT ? SWT.DEFAULT : Math.max(maxWidth, 1));
		textLayout.setAlignment(getAlignment());
		textLayout.setJustify(getAlignment() == Greip.JUSTIFY);
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

		final StyleRange[] styleRanges = parser.getStyleRanges();
		applyTextAndStyles(plainText, styleRanges, false);

		final int lineCount = textLayout.getLineCount();

		if (!wrap && lineCount > 1) {
			shortenToVisibleLines(styleRanges, 1);

		} else if (maxHeight != SWT.DEFAULT) {
			final Rectangle bounds = textLayout.getLineBounds(lineCount - 1);

			if (bounds.height + bounds.y > maxHeight) {
				final int lines = getVisibleLines(maxHeight);
				shortenToVisibleLines(styleRanges, lines);
			}
		}
	}

	private void shortenToVisibleLines(final StyleRange[] styleRanges, final int lines) {
		final String plainText = textLayout.getText();
		final String text = plainText.substring(0, Math.min(plainText.length(), textLayout.getLineOffsets()[lines] + 20));
		final StringBuilder buf = new StringBuilder(text).append("...");

		do {
			final int index = buf.length() - Math.min(4, buf.length());
			buf.deleteCharAt(index);
			applyTextAndStyles(buf.toString(), styleRanges, true);
		} while (textLayout.getLineCount() > lines && buf.length() > 0);
	}

	private int getVisibleLines(final int maxHeight) {
		final int[] lineOffsets = textLayout.getLineOffsets();

		for (int i = lineOffsets.length - 2; i >= 1; i--) {
			final Rectangle lineBounds = textLayout.getLineBounds(i - 1);
			if (lineBounds.y + lineBounds.height <= maxHeight) {
				return i;
			}
		}

		return 0;
	}

	private void applyTextAndStyles(final String text, final StyleRange[] styleRanges, final boolean shorten) {
		links.clear();

		textLayout.setText(text);
		textLayout.setStyle(new TextStyle(getFont(), getForeground(), null), 0, text.length());

		for (final StyleRange range : styleRanges) {
			if (range.start < text.length() - (shorten ? 3 : 0)) {
				range.foreground = Util.nvl(range.foreground, getForeground());
				textLayout.setStyle(range, range.start, Math.min(range.start + range.length - 1, text.length() - (shorten ? 4 : 1)));

				if (range instanceof Anchor) {
					final Anchor anchor = (Anchor) range;

					links.put(new Point(range.start, range.length), anchor.href);
					range.data = anchor.href;
				}
			}
		}
	}

	public int getAlignment() {
		return alignment;
	}

	public void setAlignment(final int alignment) {
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

	public Anchor getLinkAtLocation(final int x, final int y) {
		final TextStyle[] styles = textLayout.getStyles();
		final int[] ranges = textLayout.getRanges();

		for (int i = 0; i < styles.length; i++) {
			if (styles[i] instanceof Anchor) {
				final Rectangle bounds = textLayout.getBounds(ranges[i * 2], ranges[i * 2 + 1]);
				if (bounds.contains(x, y)) {
					return (Anchor) styles[i];
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
