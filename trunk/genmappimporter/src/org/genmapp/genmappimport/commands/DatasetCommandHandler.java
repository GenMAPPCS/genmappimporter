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

import java.awt.event.ActionEvent;
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
import org.genmapp.genmappimport.actions.ImportAttributeTableAction;
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
public class DatasetCommandHandler extends AbstractCommandHandler {

	public final static String NAMESPACE = "genmappimporter";

	private final static String OPEN_DIALOG = "open dialog";

	public final static String CREATE_NETWORK = "create network";
	public final static String ARG_CREATE_NETWORK = "toggle";

	// public final static String GET_SOURCE = "get source";
	//
	// public final static String GET_IMPORTED = "get imported";

	public final static String IMPORT = "import";
	public final static String ARG_SOURCE = "source";
	public final static String ARG_DELS = "delimiters";
	public final static String ARG_LIST_DEL = "listdelimiter";
	public final static String ARG_KEY = "key";
	public final static String ARG_KEY_TYPE = "keytype";
	public final static String ARG_SEC_KEY_TYPE = "secondarykeytype";
	public final static String ARG_ATTR_NAMES = "attributenames";
	public final static String ARG_ATTR_TYPES = "attributetypes";
	public final static String ARG_LIST_TYPES = "listtypes";
	public final static String ARG_FLAGS = "importflags";
	public final static String ARG_START_LINE = "startline";
	public final static String ARG_ROWS = "rows";

	// public final static String REIMPORT_DATASET = "reimport";
	// public final static String ARG_COM = "command";

	public static boolean createNetworkToggle = true;

	// master Map'o'Map to store all import args keyed by source url
	// public static Map<URL, Map<String, Object>> importArgsMap = new
	// HashMap<URL, Map<String, Object>>();

	public DatasetCommandHandler() {
		super(CyCommandManager.reserveNamespace(NAMESPACE));

		addDescription(OPEN_DIALOG, "Open main dialog");
		addArgument(OPEN_DIALOG);

		addDescription(CREATE_NETWORK,
				"Set toggle to create network and view from imported table data");
		addArgument(CREATE_NETWORK, ARG_CREATE_NETWORK);

		addDescription(IMPORT, "perform new table import");
		addArgument(IMPORT, ARG_SOURCE);
		addArgument(IMPORT, ARG_DELS);
		addArgument(IMPORT, ARG_LIST_DEL);
		addArgument(IMPORT, ARG_KEY);
		addArgument(IMPORT, ARG_KEY_TYPE);
		addArgument(IMPORT, ARG_SEC_KEY_TYPE);
		addArgument(IMPORT, ARG_ATTR_NAMES);
		addArgument(IMPORT, ARG_ATTR_TYPES);
		addArgument(IMPORT, ARG_LIST_TYPES);
		addArgument(IMPORT, ARG_FLAGS);
		addArgument(IMPORT, ARG_START_LINE);
		addArgument(IMPORT, ARG_ROWS);

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
		if (OPEN_DIALOG.equals(command)) {
			ImportAttributeTableAction iata = new ImportAttributeTableAction();
			iata.actionPerformed(new ActionEvent(iata,
					ActionEvent.ACTION_PERFORMED, command));
		} else if (CREATE_NETWORK.equals(command)) {
			boolean val = Boolean.parseBoolean((String) args
					.get(ARG_CREATE_NETWORK));
			createNetworkToggle = val;
		} else if (IMPORT.equals(command)) {
			URL source = null;
			List<String> del;
			String listDel;
			int key = 0;
			String keyType;
			String secKeyType;
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
			Object d = getArg(command, ARG_DELS, args);
			if (d instanceof List) {
				del = (List<String>) d;
			} else if (d instanceof String) {
				del = new ArrayList<String>();
				// escape the escape characters
				d = ((String) d).replaceAll("\t", "\\\\t");
				// remove brackets, if they are there
				if (((String) d).startsWith("[") && ((String) d).endsWith("]"))
					d = ((String) d).substring(1, ((String) d).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) d).split(",");
				for (String item : list) {
					del.add(item);
				}
			} else {
				del = null;
			}
			Object ld = getArg(command, ARG_LIST_DEL, args);
			if (ld instanceof String) {
				// escape the escape characters
				ld = ((String) ld).replaceAll("\t", "\\\\t");
				listDel = (String) ld;
			} else {
				listDel = null;
			}
			Object k = getArg(command, ARG_KEY, args);
			if (k instanceof Integer) {
				key = (Integer) k;
			} else if (k instanceof String) {
				if (((String) k).matches("\\d+")) {
					key = new Integer((String) k);
				}
			} else {
				key = 0;
			}
			Object kt = getArg(command, ARG_KEY_TYPE, args);
			if (kt instanceof String) {
				keyType = (String) kt;
			} else {
				keyType = null;
			}
			Object skt = getArg(command, ARG_SEC_KEY_TYPE, args);
			if (skt instanceof String) {
				secKeyType = (String) skt;
			} else {
				secKeyType = null;
			}
			Object an = getArg(command, ARG_ATTR_NAMES, args);
			if (an instanceof String[]) {
				attrNames = (String[]) an;
			} else if (an instanceof String) {
				// remove brackets, if they are there
				if (((String) an).startsWith("[")
						&& ((String) an).endsWith("]"))
					an = ((String) an).substring(1, ((String) an).length() - 1);
				// parse at comma delimiters
				attrNames = ((String) an).split(",");
			} else {
				attrNames = null;
			}
			Object at = getArg(command, ARG_ATTR_TYPES, args);
			if (at instanceof Byte[]) {
				attrTypes = (Byte[]) at;
			} else if (at instanceof String) {
				// remove brackets, if they are there
				if (((String) at).startsWith("[")
						&& ((String) at).endsWith("]"))
					at = ((String) at).substring(1, ((String) at).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) at).split(",");
				List<Byte> temp = new ArrayList<Byte>();
				for (String item : list) {
					byte b = new Byte(item);
					temp.add(b);
				}
				attrTypes = temp.toArray(new Byte[]{});
			} else {
				attrTypes = null;
			}
			Object lt = getArg(command, ARG_LIST_TYPES, args);
			if (lt instanceof Byte[]) {
				listTypes = (Byte[]) lt;
			} else if (lt instanceof String) {
				// remove brackets, if they are there
				if (((String) lt).startsWith("[")
						&& ((String) lt).endsWith("]"))
					lt = ((String) lt).substring(1, ((String) lt).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) lt).split(",");
				List<Byte> temp = new ArrayList<Byte>();
				for (String item : list) {
					if (item.equals("null")) {
						// TODO: fix this. now using a random default,
						// though seems to result in null
						temp.add((byte) 4);
					} else {
						byte b = new Byte(item);
						temp.add(b);
					}
				}
				listTypes = temp.toArray(new Byte[]{});
			} else {
				listTypes = null;
			}
			Object f = getArg(command, ARG_FLAGS, args);
			if (f instanceof boolean[]) {
				flags = (boolean[]) f;
			} else if (f instanceof String) {
				// remove brackets, if they are there
				if (((String) f).startsWith("[") && ((String) f).endsWith("]"))
					f = ((String) f).substring(1, ((String) f).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) f).split(",");
				flags = new boolean[list.length];
				int i = 0;
				for (String item : list) {
					boolean b = Boolean.parseBoolean(item);
					flags[i] = b;
				}
			} else {
				flags = null;
			}
			Object sl = getArg(command, ARG_START_LINE, args);
			if (sl instanceof Integer) {
				startLine = (Integer) sl;
			} else if (sl instanceof String) {
				if (((String) sl).matches("\\d+")) {
					startLine = new Integer((String) sl);
				}
			} else {
				startLine = 0;
			}

			try {
				// setImportArgs(source, del, listDel, key, keyType, secKeyType,
				// attrNames, attrTypes, listTypes, flags, startLine);
				doImport(source, del, listDel, key, keyType, secKeyType,
						attrNames, attrTypes, listTypes, flags, startLine);
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
	 * Isolated Import step to be called indirectly by CyCommands as well as by
	 * internal code.
	 * 
	 * @param source
	 * @param del
	 * @param listDel
	 * @param key
	 * @param keyType
	 * @param attrNames
	 * @param attrTypes
	 * @param listTypes
	 * @param flags
	 * @param startLine
	 * @throws Exception
	 */
	public static void doImport(URL source, List<String> del, String listDel,
			int key, String keyType, String secKeyType, String[] attrNames,
			Byte[] attrTypes, Byte[] listTypes, boolean[] flags, int startLine)
			throws Exception {

		// Build mapping parameter object.
		final AttributeMappingParameters mapping;

		mapping = new AttributeMappingParameters(source, del, listDel, key,
				keyType, secKeyType, attrNames, attrTypes, listTypes, flags,
				startLine);

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
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(false);
		TaskManager.executeTask(task, jTaskConfig);

	}

	/**
	 * Send new dataset over to Workspaces for registration and mapping
	 * 
	 * @param title
	 */
	public static void updateWorkspaces2(String name, String type,
			List<Integer> nodes, List<String> attrs) {
		final String UPDATE_DATASETS = "update datasets";
		final String ARG_DATASET_NAME = "name";
		final String ARG_DATASET_TYPE = "type";
		final String ARG_DATASET_NODES = "nodes";
		final String ARG_DATASET_ATTRS = "attrs";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(ARG_DATASET_NAME, name);
		args.put(ARG_DATASET_TYPE, type);
		args.put(ARG_DATASET_NODES, nodes);
		args.put(ARG_DATASET_ATTRS, attrs);
		try {
			CyCommandManager.execute("workspaces", UPDATE_DATASETS, args);
		} catch (CyCommandException cce) {
			// TODO Auto-generated catch block
			cce.printStackTrace();
		} catch (RuntimeException cce) {
			// TODO Auto-generated catch block
			cce.printStackTrace();
		}
	}

	/**
	 * This little ditty takes the variety of objects associated with the import
	 * of GenMAPP datasets and transforms them into strings. The string form of
	 * objects are useful for generating CyCommand scripts and for storing as
	 * String attributes for persistence across sessions.
	 * 
	 * @param o
	 *            object to be transformed into a string
	 * @return
	 */
	public static String stringify(Object o) {
		String string = null;
		if (o instanceof String) {
			string = (String) o;
		} else if (o instanceof String[]) {
			string = "[";
			for (String s : (String[]) o) {
				string = string + s + ",";
			}
			string = string.substring(0, string.length() - 1);
			string = string + "]";
		} else if (o instanceof List) {
			if (((List) o).get(0) instanceof String) {
				string = "[";
				for (String s : (List<String>) o) {
					string = string + s + ",";
				}
				string = string.substring(0, string.length() - 1);
				string = string + "]";
			}
		} else if (o instanceof Byte[]) {
			string = "[";
			for (Byte b : (Byte[]) o) {
				if (null == b)
					string = string + "null,";
				else
					string = string + b.toString() + ",";
			}
			string = string.substring(0, string.length() - 1);
			string = string + "]";
		} else if (o instanceof boolean[]) {
			string = "[";
			for (boolean b : (boolean[]) o) {
				string = string + String.valueOf(b) + ",";
			}
			string = string.substring(0, string.length() - 1);
			string = string + "]";
		} else if (o instanceof Integer) {
			string = String.valueOf((Integer) o);
		} else if (o instanceof URL) {
			string = ((URL) o).toString();
		}
		return string;
	}

}
