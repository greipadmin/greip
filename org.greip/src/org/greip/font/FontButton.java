/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.font;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.greip.color.IColorChooserFactory;
import org.greip.common.DropDownButton;
import org.greip.common.Util;

/**
 * Instances of this class allow the user to select a font from all available
 * fonts in the system.
 *
 * @author Thomas Lorbeer
 */
public class FontButton extends DropDownButton {

	private FontData fontData;
	private RGB fontColor;
	private IColorChooserFactory colorChooserFactory;
	private Consumer<FontData> fontConsumer;
	private BiConsumer<FontData, RGB> fontBiConsumer;

	{
		FontList.touch();
	}

	/**
	 * Constructs a new instance of this class.
	 *
	 * @param parent
	 *        A composite which will be the parent of the new instance.
	 * @param style
	 *        The style bits of the button. You can use all styles decribed by
	 *        {@link Button} except SWT.PUSH, SWT.CHECK and SWT.RADIO.
	 *
	 * @exception IllegalArgumentException
	 *            <ul>
	 *            <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *            </ul>
	 * @exception SWTException
	 *            <ul>
	 *            <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread
	 *            that created the parent</li>
	 *            </ul>
	 */
	public FontButton(final Composite parent, final int style) {
		super(parent, style | SWT.DROP_DOWN);

		addListener(SWT.Selection, e -> {
			if (fontConsumer != null || fontBiConsumer != null) {
				if (chooseFont()) {
					Util.whenNotNull(fontConsumer, fc -> fc.accept(fontData));
					Util.whenNotNull(fontBiConsumer, fc -> fc.accept(fontData, fontColor));
				}
				e.type = SWT.None;
			}
		});
	}

	/**
	 * Returns the selected font data object or <code>null</code> if no font
	 * selected.
	 *
	 * @return the font data of the selected font
	 */
	public FontData getFontData() {
		return fontData;
	}

	/**
	 * Sets the initial font data. If no font data is set the system font is the
	 * default.
	 *
	 * @param fontData
	 *        the initial font data
	 */
	public void setFontData(final FontData fontData) {
		this.fontData = fontData;
	}

	/**
	 * Gets the selected font color or <code>null</code> if no font is selected
	 * or no color chooser factory is set.
	 *
	 * @return the new font color
	 *
	 * @see #setColorChooserFactory(IColorChooserFactory)
	 */
	public RGB getFontColor() {
		return fontColor;
	}

	/**
	 * Sets the initial font color. The font color is only used when a color
	 * chooser factory is defined.
	 *
	 * @param fontColor
	 *        the font color
	 *
	 * @see #setColorChooserFactory(IColorChooserFactory)
	 */
	public void setFontColor(final RGB fontColor) {
		this.fontColor = fontColor;
	}

	/**
	 * Sets the factory for creating the color chooser.
	 *
	 * @param factory
	 *        The factory instance or <code>null</code> if color selection not
	 *        needed.
	 *
	 * @see IColorChooserFactory
	 */
	public void setColorChooserFactory(final IColorChooserFactory factory) {
		colorChooserFactory = factory;
	}

	/**
	 * Opens a popup window and allows the user to select a font. If a color
	 * chooser is defined you can also select a color.
	 *
	 * @return <code>true</code> if a font was selected or <code>false</code>
	 *         otherwise.
	 *
	 * @see #setColorChooserFactory(IColorChooserFactory)
	 */
	public boolean chooseFont() {
		final FontChooserPopup popup = new FontChooserPopup(this);

		popup.createContent(colorChooserFactory);
		popup.setFontData(fontData);
		popup.setFontColor(fontColor);

		return popup.open(() -> {
			fontData = popup.getFontData();
			fontColor = popup.getFontColor();
		});
	}

	/**
	 * Sets the consumer for the selected font.
	 *
	 * @param consumer
	 *        The font consumer.
	 */
	public void setFontConsumer(final Consumer<FontData> fontConsumer) {
		this.fontConsumer = fontConsumer;
	}

	/**
	 * Sets the consumer for the selected font and color.
	 *
	 * @param consumer
	 *        The font consumer.
	 */
	public void setFontConsumer(final BiConsumer<FontData, RGB> fontConsumer) {
		this.fontBiConsumer = fontConsumer;
	}
}