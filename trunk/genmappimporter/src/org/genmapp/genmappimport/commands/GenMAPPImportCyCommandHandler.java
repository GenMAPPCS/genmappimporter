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
package org.genmapp.genmappimport.commands;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;

/**
 * CyCommandHandler registration/execution
 * 
 */
public class GenMAPPImportCyCommandHandler extends AbstractCommandHandler {

	public final static String NAMESPACE = "genmapp importer";
	public final static String CREATE_NETWORK = "create network";
	public final static String ARG_CREATE_NETWORK = "toggle";
	public final static String GET_SOURCE = "get source";

	public final static String GET_IMPORTED = "get imported";
	public final static String ARG_SOURCE = "source";
	public final static String ARG_DEL = "delimiter";
	public final static String ARG_LIST_DEL = "list delimiter";
	public final static String ARG_KEY = "key in file";
	public final static String ARG_ATTR_NAMES = "attribute names";
	public final static String ARG_ATTR_TYPES = "attribute types";
	public final static String ARG_LIST_TYPES = "list data types";
	public final static String ARG_FLAGS = "import flags";
	public final static String ARG_START_LINE = "start line number";

	public final static String IMPORT = "import";

	public static boolean createNetworkToggle = false;
	public static String importSourceUrl = null;

	public static Map<String, Object> importArgs = new HashMap<String, Object>();

	public GenMAPPImportCyCommandHandler() {
		super(CyCommandManager.reserveNamespace(NAMESPACE));

		addDescription(CREATE_NETWORK,
				"Set toggle to create network and view from imported table data");
		addArgument(CREATE_NETWORK, ARG_CREATE_NETWORK);

		addDescription(GET_SOURCE, "get URL for imported table");
		addArgument(GET_SOURCE);

		addDescription(GET_IMPORTED, "get all Reader Args for imported table");
		addArgument(GET_IMPORTED, ARG_SOURCE);
		addArgument(GET_IMPORTED, ARG_DEL);
		addArgument(GET_IMPORTED, ARG_LIST_DEL);
		addArgument(GET_IMPORTED, ARG_KEY);
		addArgument(GET_IMPORTED, ARG_ATTR_NAMES);
		addArgument(GET_IMPORTED, ARG_ATTR_TYPES);
		addArgument(GET_IMPORTED, ARG_LIST_TYPES);
		addArgument(GET_IMPORTED, ARG_FLAGS);
		addArgument(GET_IMPORTED, ARG_START_LINE);

	}

	public CyCommandResult execute(String command, Collection<Tunable> args)
			throws CyCommandException {
		return execute(command, createKVMap(args));
	}

	public CyCommandResult execute(String command, Map<String, Object> args)
			throws CyCommandException {
		CyCommandResult result = new CyCommandResult();

		for (String t : args.keySet()) {
			result.addMessage("Arg: " + t + " = " + args.get(t));
		}
		if (CREATE_NETWORK.equals(command)) {
			boolean val = Boolean.parseBoolean((String) args
					.get(ARG_CREATE_NETWORK));
			createNetworkToggle = val;
		} else if (GET_SOURCE.equals(command)) {
			result.addResult(importSourceUrl);
			result.addMessage("returning URL: " + importSourceUrl);
		} else if (GET_IMPORTED.equals(command)) {
			for (String t : importArgs.keySet()) {
				Object o = importArgs.get(t);
				String s = "[";
				result.addResult(t, o);
				if (null == o) {
					s = "null";
				} else if (o instanceof String[]) {
					String[] so = (String[]) o;
					for (String st : so) {
						s += st + ",";
					}
				} else if (o instanceof Byte[]) {
					System.out.println("Byte[]: " + o);
					Byte[] bo = (Byte[]) o;
					for (Byte b : bo) {
						if (null == b) {
							s += "null,";
						} else {
							s += b.toString() + ",";
						}
					}
				} else if (o instanceof boolean[]) {
					boolean[] bo = (boolean[]) o;
					for (boolean b : bo) {
						Boolean bb = ((Boolean) b);
						s += bb.toString() + ",";
					}
				} else {
					s = o.toString();
				}
				// finish off list strings
				if (s.startsWith("[")) {
					s = s.substring(0, s.length() - 1);
					s += "]";
				}
				result.addMessage("Arg: " + t + " = " + s);
			}

		} else {

			result.addError("Command not supported: " + command);
		}
		return (result);
	}

	/**
	 * Transform import args to Strings and store for CyCommandHandler
	 * 
	 * @param source
	 * @param del
	 * @param listDel
	 * @param key
	 * @param attrNames
	 * @param attrTypes
	 * @param listTypes
	 * @param flags
	 * @param startLine
	 */
	public static void setImportArgs(URL source, List<String> del,
			String listDel, int key, String[] attrNames, Byte[] attrTypes,
			Byte[] listTypes, boolean[] flags, int startLine) {

		importArgs.put(ARG_SOURCE, source);
		importArgs.put(ARG_DEL, del);
		importArgs.put(ARG_LIST_DEL, listDel);
		importArgs.put(ARG_KEY, (Integer) key);
		importArgs.put(ARG_ATTR_NAMES, attrNames);
		importArgs.put(ARG_ATTR_TYPES, attrTypes);
		importArgs.put(ARG_LIST_TYPES, listTypes);
		importArgs.put(ARG_FLAGS, flags);
		importArgs.put(ARG_START_LINE, (Integer) startLine);

	}
}
