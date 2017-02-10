package org.greip.decorator;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class ProgressBarDecorator extends AbstractDecorator {

	protected ProgressBarDecorator(final Control parent) {
		super(parent);
	}

	@Override
	public void doPaint(final GC gc, final int x, final int y) {
	}

	@Override
	public Point getSize() {
		return null;
	}
}
