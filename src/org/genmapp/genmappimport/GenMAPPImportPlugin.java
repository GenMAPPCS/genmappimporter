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

package org.genmapp.genmappimport;

import java.util.Collection;
import java.util.Map;

import org.genmapp.genmappimport.actions.ImportAttributeTableAction;

import cytoscape.Cytoscape;
import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandHandler;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandNamespace;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;

/**
 * Main class for GenMAPP Import plugin.
 * 
 * @author Keiichiro Ono, Alex Pico, Allan Kuchinsky, Kristina Hanspers, Scooter
 *         Morris
 * 
 */
public class GenMAPPImportPlugin extends CytoscapePlugin {

	protected CytoscapeAction impAttTableAction = new ImportAttributeTableAction();

	/**
	 * Constructor for this plugin.
	 * 
	 */
	public GenMAPPImportPlugin() {

		// Register each menu item
		Cytoscape.getDesktop().getCyMenus().addAction(impAttTableAction, 5);

		// register cycommands
		try {
			// You must reserve your namespace first
			CyCommandNamespace ns = CyCommandManager
					.reserveNamespace("genmapp import");
			// Now register this handler as handling "open"
			CyCommandHandler oh = new OpenCommandHandler(ns);
		} catch (RuntimeException e) {
			// Handle already registered exceptions
			System.out.println(e);
		}

	}

	/**
	 * CyCommandHandler registration/execution
	 * 
	 * @author apico
	 * 
	 */
	class OpenCommandHandler extends AbstractCommandHandler {
		protected OpenCommandHandler(CyCommandNamespace ns) {
			super(ns);
			addArgument("open");
		}

		public String getHandlerName() {
			return "open";
		}

		public CyCommandResult execute(String command, Map<String, Object> args)
				throws CyCommandException {
			impAttTableAction.actionPerformed(null);
			return new CyCommandResult();
		}

		public CyCommandResult execute(String arg0, Collection<Tunable> arg1)
				throws CyCommandException {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
