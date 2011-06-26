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
package org.genmapp.genmappimport.actions;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.genmapp.genmappimport.ui.ImportTextTableDialog;

/**
 * Display dialog for importing data as networks from text/Excel files.<br>
 * 
 */
@SuppressWarnings("serial")
public class ImportAttributeTableAction extends CytoscapeAction {
	/**
	 * Creates a new ImportAttributeTableAction object.
	 */
	public ImportAttributeTableAction() {
		super("Dataset from Table (Text/MS Excel)...");
		setPreferredMenu("File.Import");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cytoscape.util.CytoscapeAction#actionPerformed(java.awt.event.ActionEvent
	 * )
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		ImportTextTableDialog iad;

		try {
			iad = new ImportTextTableDialog(Cytoscape.getDesktop(), true,
					ImportTextTableDialog.SIMPLE_ATTRIBUTE_IMPORT);
			iad.pack();
			iad.setLocationRelativeTo(Cytoscape.getDesktop());
			iad.setVisible(true);
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
