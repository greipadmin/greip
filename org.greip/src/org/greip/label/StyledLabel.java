package org.greip.label;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.greip.common.Util;
import org.greip.label.MarkupParser.Link;

public class StyledLabel extends Label {

	private final MarkupText markupText = new MarkupText(getDisplay());

	public StyledLabel(final Composite parent, final int style) {
		super(parent, style | SWT.WRAP | SWT.DOUBLE_BUFFERED);

		markupText.setAlignment(getAlignment(style));
		markupText.setFont(super.getFont());
		markupText.setForeground(super.getForeground());

		addListener(SWT.Paint, e -> {
			final Point size = getSize();
			final int maxWidth = size.x - 2 * getBorderWidth() - 2;
			final int maxHeight = size.y - 2 * getBorderWidth();

			markupText.layout(maxWidth, maxHeight).draw(e.gc, 2, 0);
		});

		addListener(SWT.MouseMove, e -> {
			final Link link = markupText.getLinkAtLocation(e.x, e.y);
			setCursor(link != null ? e.display.getSystemCursor(SWT.CURSOR_HAND) : null);
		});

		addListener(SWT.MouseDown, e -> {
			final Link link = markupText.getLinkAtLocation(e.x, e.y);

			Util.whenNotNull(link, () -> {
				final Event event = new Event();
				event.data = link;
				notifyListeners(SWT.Selection, event);
			});
		});

		addListener(SWT.MouseExit, e -> setCursor(null));
	}

	private static Alignment getAlignment(final int style) {
		if ((style & SWT.RIGHT) != 0) {
			return Alignment.Right;
		} else if ((style & SWT.CENTER) != 0) {
			return Alignment.Center;
		}
		return Alignment.Left;
	}

	@Override
	public void setText(final String text) {
		checkWidget();
		markupText.setText(text);
		redraw();
	}

	@Override
	public String getText() {
		checkWidget();
		return markupText.getText();
	}

	@Override
	public void setFont(final Font font) {
		checkWidget();
		markupText.setFont(font);
		redraw();
	}

	@Override
	public Font getFont() {
		checkWidget();
		return markupText.getFont();
	}

	@Override
	public void setForeground(final Color color) {
		checkWidget();
		markupText.setForeground(color);
		redraw();
	}

	@Override
	public Color getForeground() {
		checkWidget();
		return markupText.getForeground();
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();

		final int width = wHint == SWT.DEFAULT ? wHint : wHint - 2 * getBorderWidth() - 2;
		final int height = hHint == SWT.DEFAULT ? hHint : hHint - 2 * getBorderWidth();
		final Rectangle bounds = markupText.layout(width, height).getBounds();

		return new Point(bounds.width + 2 * getBorderWidth(), bounds.height + 2 * getBorderWidth());
	}

	@Override
	protected void checkSubclass() {
		// allow subclassing
	}
}
