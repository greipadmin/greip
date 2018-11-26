package org.greip.tile;

import org.eclipse.swt.SWT;
import org.greip.common.Greip;

public enum Alignment {
	Left(
		SWT.LEFT),
	Right(
		SWT.RIGHT),
	Center(
		SWT.CENTER),
	Justify(
		SWT.NONE);

	public final int style;

	private Alignment(final int style) {
		this.style = style;
	}

	public static Alignment valueOf(final int style) {
		switch (style) {
			case SWT.RIGHT:
				return Right;
			case SWT.CENTER:
				return Center;
			case Greip.JUSTIFY:
				return Justify;
			default:
				return Left;
		}
	}
}
