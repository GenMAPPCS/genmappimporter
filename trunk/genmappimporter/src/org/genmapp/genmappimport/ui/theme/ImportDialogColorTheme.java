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

import java.awt.Color;

/**
 * Color theme for Import Dialogs.
 *
 */
public enum ImportDialogColorTheme {
	LABEL_COLOR(Color.black),
	KEY_ATTR_COLOR(Color.red),
	PRIMARY_KEY_COLOR(new Color(51, 51, 255)),
	ONTOLOGY_COLOR(new Color(0, 255, 255)),
	ALIAS_COLOR(new Color(51, 204, 0)),
	SPECIES_COLOR(new Color(182, 36, 212)),
	ATTRIBUTE_NAME_COLOR(new Color(102, 102, 255)),
	NOT_SELECTED_COL_COLOR(new Color(240, 240, 240)),
	SELECTED_COLOR(Color.BLACK),
	UNSELECTED_COLOR(Color.GRAY),

	//	HEADER_BACKGROUND_COLOR(new Color(165, 200, 254)),
	HEADER_BACKGROUND_COLOR(Color.WHITE),
	HEADER_UNSELECTED_BACKGROUND_COLOR(new Color(240, 240, 240)), NOT_LOADED_COLOR(Color.RED), 
	LOADED_COLOR(Color.GREEN),SOURCE_COLOR(new Color(204, 0, 204)), 
	INTERACTION_COLOR(new Color(255, 0, 51)),TARGET_COLOR(new Color(255, 102, 0)), 
	EDGE_ATTR_COLOR(Color.BLUE);
	private Color color;

	private ImportDialogColorTheme(Color color) {
		this.color = color;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Color getColor() {
		return color;
	}
}
