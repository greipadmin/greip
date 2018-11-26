package org.greip.decorator;

public enum CircleType {
	Circle(360, 90, 1.0), CircleSegment225(225, -22, 1.42), SemiCircle(180, 0, 2.0);

	final int angle;
	final int offset;
	final double heightQuotient;

	CircleType(final int angle, final int offset, final double heightQuotient) {
		this.angle = angle;
		this.offset = offset;
		this.heightQuotient = heightQuotient;
	}
}