/**
 * Copyright (c) 2016 by Thomas Lorbeer
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.greip.nls.messages"; //$NON-NLS-1$

	// Color choosers
	public static String Blue;
	public static String Green;
	public static String Red;
	public static String Hue;
	public static String Saturation;
	public static String Brightness;

	// Calculator
	public static String Error;
	public static String Overflow;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
