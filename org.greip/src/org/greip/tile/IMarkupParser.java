/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.tile;

import java.util.List;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.xml.sax.SAXException;

/**
 * 
 * @author Thomas Lorbeer
 */
public interface IMarkupParser {

	/**
	 * Parse the content and build the list of style ranges.
	 *
	 * @throws SAXException
	 */
	void parse(String html) throws SAXException;

	String getPlainText();

	List<StyleRange> getStyleRanges();

	Font getDefaultFont();

	void setDefaultFont(Font defaultFont);

}