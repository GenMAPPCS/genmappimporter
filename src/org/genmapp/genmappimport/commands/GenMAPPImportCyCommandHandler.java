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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.genmapp.genmappimport.reader.AttributeMappingParameters;
import org.genmapp.genmappimport.reader.DefaultAttributeTableReader;
import org.genmapp.genmappimport.reader.ExcelAttributeSheetReader;
import org.genmapp.genmappimport.reader.TextTableReader;
import org.genmapp.genmappimport.ui.ImportAttributeTableTask;
import org.genmapp.genmappimport.ui.ImportTextTableDialog;

import cytoscape.Cytoscape;
import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

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

		addDescription(GET_IMPORTED, "get parameters for last imported table");
		addArgument(GET_IMPORTED, ARG_SOURCE);
		addArgument(GET_IMPORTED, ARG_DEL);
		addArgument(GET_IMPORTED, ARG_LIST_DEL);
		addArgument(GET_IMPORTED, ARG_KEY);
		addArgument(GET_IMPORTED, ARG_ATTR_NAMES);
		addArgument(GET_IMPORTED, ARG_ATTR_TYPES);
		addArgument(GET_IMPORTED, ARG_LIST_TYPES);
		addArgument(GET_IMPORTED, ARG_FLAGS);
		addArgument(GET_IMPORTED, ARG_START_LINE);

		addDescription(IMPORT, "perform table import");
		addArgument(IMPORT, ARG_SOURCE);
		addArgument(IMPORT, ARG_DEL);
		addArgument(IMPORT, ARG_LIST_DEL);
		addArgument(IMPORT, ARG_KEY);
		addArgument(IMPORT, ARG_ATTR_NAMES);
		addArgument(IMPORT, ARG_ATTR_TYPES);
		addArgument(IMPORT, ARG_LIST_TYPES);
		addArgument(IMPORT, ARG_FLAGS);
		addArgument(IMPORT, ARG_START_LINE);

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

		} else if (IMPORT.equals(command)) {
			URL source = null;
			List<String> del;
			String listDel;
			int key = 0;
			String[] attrNames;
			Byte[] attrTypes;
			Byte[] listTypes;
			boolean[] flags;
			int startLine = 0;

			Object s = getArg(command, ARG_SOURCE, args);
			if (s instanceof URL) {
				source = (URL) s;
			} else if (s instanceof String) {
				try {
					source = new URL((String) s);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				source = null;
				// panic
			}
			Object d = getArg(command, ARG_DEL, args);
			if (d instanceof List) {
				del = (List<String>) d;
			} else if (d instanceof String) {
				del = new ArrayList<String>();
				// remove brackets
				d = ((String) d).substring(1, ((String) d).length() - 1);
				// parse at commma delimiters
				String[] list = ((String) d).split(",");
				if (null == list) { // single item
					del.add((String) d);
				} else {
					for (String item : list) {
						del.add(item);
					}
				}
			} else {
				del = null;
			}
			Object ld = getArg(command, ARG_LIST_DEL, args);
			if (ld instanceof String) {
				listDel = (String) ld;
			} else {
				listDel = null;
			}
			Object k = getArg(command, ARG_KEY, args);
			if (k instanceof Integer) {
				key = (Integer) k;
			} else if (k instanceof String) {
				if (((String) k).matches("\\d+")) {
					System.out.println("regex for digit match successful");
					key = new Integer((String) k);
				}
			} else {
				System.out.println("Regex for digit match failed!!!");
				key = 0;
			}
			Object an = getArg(command, ARG_ATTR_NAMES, args);
			if (an instanceof String[]) {
				attrNames = (String[]) an;
			} else if (an instanceof String) {
				// remove brackets
				an = ((String) an).substring(1, ((String) an).length() - 1);
				// parse at commma delimiters
				attrNames = ((String) an).split(",");
				if (null == attrNames) { // single item
					attrNames[0] = (String) an;
				}
			} else {
				attrNames = null;
			}
			Object at = getArg(command, ARG_ATTR_TYPES, args);
			if (at instanceof Byte[]) {
				attrTypes = (Byte[]) at;
			} else if (at instanceof String) {
				// remove brackets
				at = ((String) at).substring(1, ((String) at).length() - 1);
				// parse at commma delimiters
				String[] list = ((String) at).split(",");
				if (null == list) { // single item
					attrTypes = new Byte[1];
					attrTypes[0] = new Byte((String) at);
				} else {
					List<Byte> temp = new ArrayList<Byte>();
					for (String item : list) {
						byte b = new Byte(item);
						temp.add(b);
					}
					attrTypes = temp.toArray(new Byte[]{});
				}
			} else {
				attrTypes = null;
			}
			Object lt = getArg(command, ARG_LIST_TYPES, args);
			if (lt instanceof Byte[]) {
				listTypes = (Byte[]) lt;
			} else if (lt instanceof String) {
				// remove brackets
				lt = ((String) lt).substring(1, ((String) lt).length() - 1);
				// parse at commma delimiters
				String[] list = ((String) lt).split(",");
				if (null == list) { // single item
					listTypes = new Byte[1];
					listTypes[0] = new Byte((String) lt);
				} else {
					List<Byte> temp = new ArrayList<Byte>();
					for (String item : list) {
						System.out.println("check1");
						if (item.equals("null")) {
							System.out.println("check2");
							// TODO: fix this. now using a random default,
							// though
							// seems to result in null
							temp.add((byte) 4);
						} else {
							byte b = new Byte(item);
							temp.add(b);
						}
					}
					listTypes = temp.toArray(new Byte[]{});
				}
			} else {
				listTypes = null;
			}
			Object f = getArg(command, ARG_FLAGS, args);
			if (f instanceof boolean[]) {
				flags = (boolean[]) f;
			} else if (f instanceof String) {
				// remove brackets
				f = ((String) f).substring(1, ((String) f).length() - 1);
				// parse at commma delimiters
				String[] list = ((String) f).split(",");
				if (null == list) { // single item
					flags = new boolean[1];
					flags[0] = Boolean.parseBoolean((String) f);
				} else {
					flags = new boolean[list.length];
					int i = 0;
					for (String item : list) {
						boolean b = Boolean.parseBoolean(item);
						flags[i] = b;
					}
				}
			} else {
				flags = null;
			}
			Object sl = getArg(command, ARG_START_LINE, args);
			if (sl instanceof Integer) {
				startLine = (Integer) sl;
			} else if (sl instanceof String) {
				if (((String) sl).matches("\\d+")) {
					System.out.println("regex for digit match successful");
					startLine = new Integer((String) sl);
				}
			} else {
				System.out.println("Regex for digit match failed!!!");
				startLine = 0;
			}

			try {
				doImport(source, del, listDel, key, attrNames, attrTypes,
						listTypes, flags, startLine);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	/**
	 * Isolated Import step to be called indirectly by CyCommands as well as by
	 * internal code.
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
	 * @throws Exception
	 */
	public static void doImport(URL source, List<String> del, String listDel,
			int key, String[] attrNames, Byte[] attrTypes, Byte[] listTypes,
			boolean[] flags, int startLine) throws Exception {

		// Build mapping parameter object.
		final AttributeMappingParameters mapping;

		mapping = new AttributeMappingParameters(del, listDel, key, attrNames,
				attrTypes, listTypes, flags);

		if (source.toString().endsWith(ImportTextTableDialog.EXCEL_EXT)) {
			/*
			 * Read one sheet at a time
			 */
			POIFSFileSystem excelIn = new POIFSFileSystem(source.openStream());
			HSSFWorkbook wb = new HSSFWorkbook(excelIn);

			// Load all sheets in the table
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				HSSFSheet sheet = wb.getSheetAt(i);

				loadAnnotation(new ExcelAttributeSheetReader(sheet, mapping,
						startLine), source.toString());

			}
		} else {
			loadAnnotation(new DefaultAttributeTableReader(source, mapping,
					startLine, null), source.toString());

		}

	}
	/**
	 * Create task for annotation reader and run it. tablechanged
	 * 
	 * @param reader
	 * @param ontology
	 * @param source
	 */
	private static void loadAnnotation(TextTableReader reader, String source) {
		// Create LoadNetwork Task
		ImportAttributeTableTask task = new ImportAttributeTableTask(reader,
				source);

		// Configure JTask Dialog Pop-Up Box
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(false);

		// Execute Task in New Thread; pops open JTask Dialog Box.
		TaskManager.executeTask(task, jTaskConfig);
	}

}
