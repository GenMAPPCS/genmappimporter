/*******************************************************************************
 * Copyright 2010 Alexander Pico
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.genmapp.genmappimport.ui.theme;

import javax.swing.ImageIcon;

/**
 * Iconset for Import Dialog GUI.
 * 
 */
public enum ImportDialogIconSets {
	STRING_ICON("images/ximian/stock_font-16.png"),
	INTEGER_ICON("images/ximian/stock_sort-row-ascending-16.png"),
	FLOAT_ICON("images/ximian/stock_format-scientific-16.png"),
	INT_ICON("images/ximian/stock_sort-row-ascending-16.png"),
	LIST_ICON("images/ximian/stock_navigator-list-box-toggle-16.png"),
	BOOLEAN_ICON("images/ximian/stock_form-radio-16.png"),
	ID_ICON("images/ximian/stock_3d-light-on-16.png"),
	INTERACTION_ICON("images/ximian/stock_interaction.png"),
	SPREADSHEET_ICON_LARGE("images/ximian/stock_new-spreadsheet-48.png"),
	REMOTE_SOURCE_ICON("images/ximian/stock_internet-16.png"),
	REMOTE_SOURCE_ICON_LARGE("images/ximian/stock_internet-32.png"),
	LOCAL_SOURCE_ICON("images/ximian/stock_data-sources-modified-16.png"),
	SPREADSHEET_ICON("images/ximian/stock_new-spreadsheet.png"),
	TEXT_FILE_ICON("images/ximian/stock_new-text-32.png"),
	RIGHT_ARROW_ICON("images/ximian/stock_right-16.png"),
	CAUTION_ICON("images/ximian/stock_dialog-warning-32.png"),
	CHECKED_ICON("images/ximian/stock_3d-apply-16.png"),
	UNCHECKED_ICON("images/ximian/stock_close-16.png");

	private String resourceLoc;

	private ImportDialogIconSets(String resourceLocation) {
		this.resourceLoc = resourceLocation;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public ImageIcon getIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource(resourceLoc));
	}
}
