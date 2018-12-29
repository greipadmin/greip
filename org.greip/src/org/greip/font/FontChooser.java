/**
 * Copyright (c) 2018 by Thomas Lorbeer. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 **/
package org.greip.font;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.greip.color.AbstractColorChooser;
import org.greip.color.IColorChooserFactory;
import org.greip.common.Util;
import org.greip.nls.Messages;
import org.greip.separator.LineStyle;
import org.greip.separator.Separator;

public class FontChooser extends Composite {

	private static final int TABLE_HEIGHT = 121;
	private static final int SIZE_TABLE_WIDTH = 40;

	private final FontTable tblFonts;
	private final Table tblSize;
	private final Button chkBold;
	private final Button chkItalic;
	private final Spinner spiSize;

	private AbstractColorChooser colorChooser;

	private FontData fontData;
	private RGB fontColor;

	public FontChooser(final Composite parent, final IColorChooserFactory colorChooserFactory) {
		super(parent, SWT.NO_FOCUS);

		addListener(SWT.Traverse, e -> Util.when(e.detail == SWT.TRAVERSE_RETURN, this::propagateNewFont));

		final GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(3).margins(10, 10).spacing(10, 5).create();
		setLayout(layout);

		new Label(this, SWT.NONE).setText(Messages.Font);
		new Label(this, SWT.NONE).setText(Messages.Size);
		new Label(this, SWT.NONE).setText("");

		tblFonts = new FontTable(this);
		tblFonts.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, TABLE_HEIGHT).span(1, 4).create());
		tblFonts.addListener(SWT.DefaultSelection, e -> propagateNewFont());

		spiSize = new Spinner(this, SWT.BORDER);
		spiSize.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).create());
		spiSize.setMaximum(100);
		spiSize.setMinimum(3);

		final Composite optionGroup = new Composite(this, SWT.NO_FOCUS);
		optionGroup.setLayout(GridLayoutFactory.fillDefaults().numColumns(colorChooserFactory == null ? 1 : 2).create());
		optionGroup.setLayoutData(new GridData(SWT.CENTER, colorChooserFactory == null ? SWT.BOTTOM : SWT.CENTER, false, false, 1,
				colorChooserFactory == null ? 4 : 1));

		chkBold = new Button(optionGroup, SWT.CHECK);
		chkBold.setText(Messages.Bold);

		chkItalic = new Button(optionGroup, SWT.CHECK);
		chkItalic.setText(Messages.Italic);

		tblSize = createSizeTable(this);
		tblSize.addListener(SWT.DefaultSelection, e -> propagateNewFont());
		creatSizeItems(tblSize);

		final int height = TABLE_HEIGHT - spiSize.getSize().y - layout.verticalSpacing;
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, height).span(1, 3).applyTo(tblSize);

		if (colorChooserFactory != null) {
			final Separator line = new Separator(this, SWT.NONE);
			line.setLineStyle(LineStyle.Dot);
			line.setLineColor(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			line.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 2));

			colorChooser = colorChooserFactory.create(this);
			colorChooser.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
			colorChooser.addListener(SWT.Traverse, e -> Util.when(e.detail == SWT.TRAVERSE_RETURN, this::propagateNewFont));
			colorChooser.addListener(SWT.Selection, e -> propagateNewFont());
		}

		tblFonts.addListener(SWT.Activate, e -> {
			tblFonts.showSelection();
			tblSize.showSelection();
			Util.whenNotNull(colorChooser, cc -> cc.setRGB(cc.getRGB()));
		});
	}

	private static void creatSizeItems(final Table table) {
		for (final int size : new int[] { 7, 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 32, 36, 40, 48, 64, 72, 80 }) {
			new TableItem(table, SWT.NONE).setText(Integer.toString(size));
		}
	}

	public final Table createSizeTable(final Composite parent) {
		final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER);
		new TableColumn(table, SWT.NONE).setWidth(SIZE_TABLE_WIDTH);

		table.addListener(SWT.Selection, e -> {
			final int size = Integer.valueOf(table.getSelection()[0].getText()).intValue();
			spiSize.setSelection(size);
		});

		return table;
	}

	public FontData getFontData() {
		return fontData;
	}

	public void setFontData(final FontData fontData) {
		checkWidget();

		final FontData fd = Optional.ofNullable(fontData).orElseGet(() -> getDisplay().getSystemFont().getFontData()[0]);

		findTableItem(tblFonts, fd.getName()).ifPresent(tblFonts::setSelection);
		findTableItem(tblSize, Integer.toString(fd.getHeight())).ifPresent(tblSize::setSelection);
		spiSize.setSelection(fd.getHeight());
		chkBold.setSelection((fd.getStyle() & SWT.BOLD) != 0);
		chkItalic.setSelection((fd.getStyle() & SWT.ITALIC) != 0);
	}

	private static Optional<TableItem> findTableItem(final Table table, final String text) {
		return Stream.of(table.getItems()).filter(item -> item.getText().equals(text)).findFirst();
	}

	private void propagateNewFont() {
		final String fontName = tblFonts.getItem(tblFonts.getSelectionIndex()).getText();
		final int fontHeight = spiSize.getSelection();
		final int fontStyle = (chkBold.getSelection() ? SWT.BOLD : SWT.NONE) | (chkItalic.getSelection() ? SWT.ITALIC : SWT.NONE);

		fontData = new FontData(fontName, fontHeight, fontStyle);
		if (colorChooser != null) fontColor = colorChooser.getRGB();

		notifyListeners(SWT.Selection, new Event());
	}

	public void setFontColor(final RGB fontColor) {
		Util.when(colorChooser != null && fontColor != null, () -> colorChooser.setRGB(fontColor));
	}

	public RGB getFontColor() {
		return fontColor;
	}
}