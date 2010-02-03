
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
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
import cytoscape.view.CyMenus;


/**
 * Main class for GenMAPP Import plugin.
 *
 * @version 0.1
 * @since Cytoscape 2.6
 * @author Keiichiro Ono, Alex Pico, Allan Kuchinsky, Kristina Hanspers, Scooter Morris
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
