package org.greip.picture;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.greip.decorator.ImageDecorator;

public class Picture extends Composite {

	private final ImageDecorator decorator;

	public Picture(final Composite parent) {
		super(parent, SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED);

		decorator = new ImageDecorator(this);

		addListener(SWT.Paint, e -> decorator.doPaint(e.gc, new Point(0, 0)));
		addListener(SWT.Dispose, e -> decorator.dispose());
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		return decorator.getSize();
	}

	public int getMinDelay() {
		return decorator.getMinDelay();
	}

	public void loadImage(final InputStream stream) {
		decorator.loadImage(stream);
	}

	public void loadImage(final String filename) {
		decorator.loadImage(filename);
	}

	public void setImage(final Image image) {
		decorator.setImage(image);
	}

	public void setMinDelay(final int minDelay) {
		decorator.setMinDelay(minDelay);
	}
}
