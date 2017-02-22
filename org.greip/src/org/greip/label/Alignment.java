package org.greip.label;

import org.eclipse.swt.SWT;

enum Alignment {
	Left(
		SWT.LEFT,
		false),
	Right(
		SWT.RIGHT,
		false),
	Center(
		SWT.CENTER,
		false),
	Justify(
		SWT.LEFT,
		true);

	public final int style;
	public final boolean justify;

	private Alignment(final int style, final boolean justify) {
		this.style = style;
		this.justify = justify;
	}
}
