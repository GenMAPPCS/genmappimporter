/*
 Copyright 2010 Alexander Pico
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 	
 	http://www.apache.org/licenses/LICENSE-2.0 
 	
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License. 
 */

package org.genmapp.genmappimport.ui;

import static org.genmapp.genmappimport.reader.TextFileDelimiters.PIPE;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.HEADER_BACKGROUND_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.HEADER_UNSELECTED_BACKGROUND_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.PRIMARY_KEY_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.SELECTED_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.UNSELECTED_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogFontTheme.SELECTED_COL_FONT;
import static org.genmapp.genmappimport.ui.theme.ImportDialogFontTheme.SELECTED_FONT;
import static org.genmapp.genmappimport.ui.theme.ImportDialogFontTheme.UNSELECTED_FONT;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.CHECKED_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.FLOAT_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.INTEGER_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.LIST_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.STRING_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.UNCHECKED_ICON;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import cytoscape.data.CyAttributes;

/**
 * Cell and table header renderer for preview table.
 * 
 */
public class AttributePreviewTableCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	public static final int PARAMETER_NOT_EXIST = -1;
	private final static String DEF_LIST_DELIMITER = PIPE.toString();
	private int keyInFile;
	private boolean[] importFlag;
	private String listDelimiter;

	/**
	 * Creates a new AttributePreviewTableCellRenderer object.
	 * 
	 * @param primaryKey
	 *            DOCUMENT ME!
	 * @param aliases
	 *            DOCUMENT ME!
	 * @param ontologyColumn
	 *            DOCUMENT ME!
	 * @param species
	 *            DOCUMENT ME!
	 * @param importFlag
	 *            DOCUMENT ME!
	 * @param listDelimiter
	 *            DOCUMENT ME!
	 */
	public AttributePreviewTableCellRenderer(int primaryKey,
			boolean[] importFlag, final String listDelimiter) {
		super();
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		this.keyInFile = primaryKey;
		if (importFlag != null) 
			this.importFlag = importFlag;
		if (listDelimiter == null) {
			this.listDelimiter = DEF_LIST_DELIMITER;
		} else {
			this.listDelimiter = listDelimiter;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * @param flag
	 *            DOCUMENT ME!
	 */
	public void setImportFlag(int index, boolean flag) {
		if ((importFlag != null) && (importFlag.length > index)) {
			importFlag[index] = flag;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean getImportFlag(int index) {
		if ((importFlag != null) && (importFlag.length > index)) {
			return importFlag[index];
		}

		return false;
	}

	/**
	 * Table cell renderer component
	 * 
	 * @param table
	 * @param value
	 * @param isSelected
	 * @param hasFocus
	 * @param row
	 * @param column
	 * 
	 * @return component
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setHorizontalAlignment(DefaultTableCellRenderer.CENTER);

		if (column == keyInFile) {
			setForeground(PRIMARY_KEY_COLOR.getColor());
			// introduce elsif here to highlight additional column types using
			// color
		} else {
			setForeground(Color.BLACK);
		}

		setText((value == null) ? "" : value.toString());
		setBackground(Color.WHITE);
		setFont(SELECTED_COL_FONT.getFont());

		return this;
	}
}

/**
 * For rendering table header.
 * 
 */
class HeaderRenderer implements TableCellRenderer {
	private final TableCellRenderer tcr;

	/**
	 * Creates a new HeaderRenderer object.
	 * 
	 * @param tcr
	 *            DOCUMENT ME!
	 * @param dataTypes
	 *            DOCUMENT ME!
	 */
	public HeaderRenderer(TableCellRenderer tcr, Byte[] dataTypes) {
		this.tcr = tcr;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param tbl
	 *            DOCUMENT ME!
	 * @param val
	 *            DOCUMENT ME!
	 * @param isS
	 *            DOCUMENT ME!
	 * @param hasF
	 *            DOCUMENT ME!
	 * @param row
	 *            DOCUMENT ME!
	 * @param col
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Component getTableCellRendererComponent(JTable tbl, Object val,
			boolean isS, boolean hasF, int row, int col) {
		final JLabel columnName = (JLabel) tcr.getTableCellRendererComponent(
				tbl, val, isS, hasF, row, col);
		final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) tbl
				.getCellRenderer(0, col);
		final boolean flag = rend.getImportFlag(col);

		if (flag) {
			columnName.setFont(SELECTED_FONT.getFont());
			columnName.setForeground(SELECTED_COLOR.getColor());
			columnName.setBackground(HEADER_BACKGROUND_COLOR.getColor());
		} else {
			columnName.setFont(UNSELECTED_FONT.getFont());
			columnName.setForeground(UNSELECTED_COLOR.getColor());
			columnName.setBackground(HEADER_UNSELECTED_BACKGROUND_COLOR
					.getColor());
		}

		if (flag) {
			columnName.setIcon(CHECKED_ICON.getIcon());
		} else {
			columnName.setIcon(UNCHECKED_ICON.getIcon());
		}

		return columnName;
	}

	private static ImageIcon getDataTypeIcon(byte dataType) {
		ImageIcon dataTypeIcon = null;

		if (dataType == CyAttributes.TYPE_STRING) {
			dataTypeIcon = STRING_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_INTEGER) {
			dataTypeIcon = INTEGER_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_FLOATING) {
			dataTypeIcon = FLOAT_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_BOOLEAN) {
			dataTypeIcon = BOOLEAN_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_SIMPLE_LIST) {
			dataTypeIcon = LIST_ICON.getIcon();
		}

		return dataTypeIcon;
	}
}
