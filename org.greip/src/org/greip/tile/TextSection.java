/**
 * Copyright (c) 2019 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.tile;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.greip.common.Greip;
import org.greip.common.Util;

public final class TextSection {

	interface TextSectionModifyListener {
		void sectionModified(TextSection section);
	}

	private String text;
	private int alignment;
	private Font font;
	private Color foreground;
	private boolean wrap;
	private int[] margins = new int[4];

	private final Set<TextSectionModifyListener> listeners = new HashSet<>(1);

	/**
	 * Creates a new text section.
	 *
	 * @param text
	 *        the text content of the section (cannot be null)
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            </ul>
	 */
	public TextSection(final String text) {
		this(text, SWT.LEFT);
	}

	/**
	 * Creates a new text section.
	 *
	 * @param text
	 *        the text content of the section (cannot be null)
	 * @param alignment
	 *        the text alignment, one of <code>SWT.LEFT</code>,
	 *        <code>SWT.RIGHT</code>, <code>SWT.CENTER</code> or
	 *        <code>Greip.JUSTIFY</code>
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            <li>ERROR_IVALID_ARGUMENT - if the alignment value is
	 *            invalid</li>
	 *            </ul>
	 */
	public TextSection(final String text, final int alignment) {
		this(text, alignment, null, null, true);
	}

	/**
	 * Creates a new text section.
	 *
	 * @param text
	 *        the text content of the section (cannot be null)
	 * @param alignment
	 *        the text alignment, one of <code>SWT.LEFT</code>,
	 *        <code>SWT.RIGHT</code>, <code>SWT.CENTER</code> or
	 *        <code>Greip.JUSTIFY</code>
	 * @param font
	 *        the font to use
	 * @param foreground
	 *        the text foreground color
	 * @param wrap
	 *        the line wrapping behaviour
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            <li>ERROR_IVALID_ARGUMENT - if the alignment value is invalid
	 *            or font and/or foreground is disposed</li>
	 *            </ul>
	 */
	public TextSection(final String text, final int alignment, final Font font, final Color foreground, final boolean wrap) {
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (!Util.in(alignment, SWT.LEFT, SWT.RIGHT, SWT.CENTER, Greip.JUSTIFY)) SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		setText(text);
		setAlignment(alignment);
		setFont(font);
		setForeground(foreground);
		setWrap(wrap);
	}

	/**
	 * Returns the sections textual content.
	 *
	 * @return the text content
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the textual content for the specified text section.
	 *
	 * @param text
	 *        the new text content (null not allowed)
	 *
	 * @return this
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *            </ul>
	 */
	public TextSection setText(final String text) {
		if (text == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.text = text;
		return fireEvent();
	}

	/**
	 * Returns a value which describes the position of the text in the section.
	 * The value will be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>,
	 * <code>SWT.CENTER</code> or <code>Greip.JUSTIFY</code>.
	 *
	 * @return the alignment
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * Controls how text content in the sction will be displayed. The argument
	 * should be one of <code>SWT.LEFT</code>, <code>SWT.RIGHT</code>,
	 * <code>SWT.CENTER</code> or <code>Greip.JUSTIFY</code>.
	 *
	 * @param alignment
	 *        the new alignment
	 *
	 * @return this
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_IVALID_ARGUMENT - if the alignment value is
	 *            invalid</li>
	 */
	public TextSection setAlignment(final int alignment) {
		if (!Util.in(alignment, SWT.LEFT, SWT.RIGHT, SWT.CENTER, Greip.JUSTIFY)) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.alignment = alignment;
		return fireEvent();
	}

	/**
	 * Return the font used to be paint the sections textual content.
	 *
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Sets the font for the text content of the section.
	 *
	 * @param font
	 *        the font
	 *
	 * @return this
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
	 *            </ul>
	 */
	public TextSection setFont(final Font font) {
		if (font != null && font.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.font = font;
		return fireEvent();
	}

	/**
	 * Returns the foreground color wich is used to paint textual content.
	 *
	 * @return the foreground color
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Defines the foreground color wich is used to paint textual content.
	 *
	 * @param foreground
	 *        the foreground color
	 *
	 * @return this
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *            disposed</li>
	 *            </ul>
	 */
	public TextSection setForeground(final Color foreground) {
		if (foreground != null && foreground.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.foreground = foreground;
		return fireEvent();
	}

	/**
	 * Returns the current line wrap behaviour at the spcified text section.
	 *
	 * @return returns <code>true</code> if line wrap behaviour enabled,
	 *         otherwise <code>false</code>.
	 */
	public boolean isWrap() {
		return wrap;
	}

	/**
	 * Defines the line wrap behaviour at the specified text section.
	 *
	 * @param wrap
	 *        <code>true</code> if line wrap enabled, <code>false</code>
	 *        otherwise.
	 *
	 * @return this
	 */
	public TextSection setWrap(final boolean wrap) {
		this.wrap = wrap;
		return fireEvent();
	}

	/**
	 * Returns the margins around the text section.
	 *
	 * @return the margins
	 */
	public int[] getMargins() {
		return margins;
	}

	/**
	 * Sets the margins around the text section. The margins specify the number
	 * of pixels of horizontal and vertical margin that will be placed along the
	 * left, right, top, and bottom edges of the text.
	 *
	 * @param left
	 *        left margin size (pixels)
	 * @param right
	 *        right margin size (pixels)
	 * @param top
	 *        top margin size (pixels)
	 * @param bottom
	 *        bottom margin size (pixels)
	 *
	 * @return this
	 */
	public TextSection setMargins(final int left, final int right, final int top, final int bottom) {
		this.margins = new int[] { left, right, top, bottom };
		return fireEvent();
	}

	void addModifyListener(final TextSectionModifyListener listener) {
		listeners.add(listener);
	}

	void removeModifyListener(final TextSectionModifyListener listener) {
		listeners.remove(listener);
	}

	private TextSection fireEvent() {
		listeners.forEach(l -> l.sectionModified(this));
		return this;
	}
}