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

package org.genmapp.genmappimport.ui.theme;

import java.awt.Font;


/**
 *
 */
public enum ImportDialogFontTheme {
	TITLE_FONT(new Font("Sans-serif", Font.BOLD, 18)),
	SELECTED_COL_FONT(new Font("Sans-serif", Font.BOLD, 14)),
	SELECTED_FONT(new Font("Sans-serif", Font.BOLD, 14)),
	UNSELECTED_FONT(new Font("Sans-serif", Font.PLAIN, 14)),
	KEY_FONT(new Font("Sans-Serif", Font.BOLD, 14)),
	LABEL_FONT(new Font("Sans-serif", Font.BOLD, 14)),
	LABEL_ITALIC_FONT(new Font("Sans-serif", 3, 14)),
	ITEM_FONT(new Font("Sans-serif", Font.BOLD, 12)),
	ITEM_FONT_LARGE(new Font("Sans-serif", Font.BOLD, 14));

	private Font font;

	private ImportDialogFontTheme(Font font) {
		this.font = font;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Font getFont() {
		return font;
	}
}
