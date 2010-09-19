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
package org.genmapp.genmappimport.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genmapp.genmappimport.commands.DatasetCommandHandler;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.util.CyNetworkNaming;
import cytoscape.util.URLUtil;

/**
 * Basic text table reader for attributes.<br>
 * 
 * <p>
 * based on the given parameters, map the text table to CyAttributes.
 * </p>
 * 
 */
public class DefaultAttributeTableReader implements TextTableReader {

	private final URL source;
	private AttributeMappingParameters amp;
	private final AttributeLineParser parser;
	private final String NET_ATTR_DATASETS = "org.genmapp.datasets_1.0";
	private final String NET_ATTR_DATASET_PREFIX = "org.genmapp.dataset.";

	// Number of mapped attributes.
	private int globalCounter = 0;

	// Reader will read entries from this line.
	private final int startLineNumber;

	// Lines beginning with this character will be considered as comment lines.
	private String commentChar = null;

	// Lines beginning with this character will be considered as comment lines.
	public String taskmonitorStatus = null;

	/**
	 * Creates a new DefaultAttributeTableReader object.
	 * 
	 * @param source
	 *            Source file URL (can be remote or local file path)
	 * @param amp
	 *            attribute mapping parameter
	 * @param startLineNumber
	 *            row to start reading
	 * @param commentChar
	 *            character to indicate comment row(s) to be skipped
	 */
	public DefaultAttributeTableReader(final URL source,
			AttributeMappingParameters amp, final int startLineNumber,
			final String commentChar) {
		this.source = source;
		this.amp = amp;
		this.startLineNumber = startLineNumber;
		this.parser = new AttributeLineParser(amp);
		this.commentChar = commentChar;

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List getColumnNames() {
		List<String> colNamesList = new ArrayList<String>();

		for (String name : amp.getAttributeNames()) {
			colNamesList.add(name);
		}

		return colNamesList;
	}

	/**
	 * Read table from the data source, first for identifier mapping.
	 */
	public void firstRead() throws IOException {
		InputStream is = URLUtil.getInputStream(source);
		BufferedReader bufRd = new BufferedReader(new InputStreamReader(is));
		String line;
		int lineCount = 0;

		String[] parts = null;

		final String delimiter = amp.getDelimiterRegEx();

		while ((line = bufRd.readLine()) != null) {
			/*
			 * Ignore Empty & Comment lines.
			 */
			if ((commentChar != null) && line.startsWith(commentChar)) {
				// Do nothing
			} else if ((lineCount >= startLineNumber)
					&& (line.trim().length() > 0)) {
				parts = line.split(delimiter);
				// If key does not exists, ignore the line.
				if (parts.length >= amp.getKeyIndex() + 1) {
					try {
						parser.collectTableIds(parts);
					} catch (Exception ex) {
						System.out
								.println("Couldn't parse row for id mapping: "
										+ lineCount);
					}
				}
			}

			lineCount++;
		}
		is.close();
		bufRd.close();
		
		// perform actual id mapping (can be slow via web services)
		parser.collectTableMappings();
		
		//perform network id mappings (can be slow via web services)
		parser.performNetworkMappings();
	}
		

	/**
	 * Read table from the data source, this time for attribute mapping.
	 */
	public void readTable() throws IOException {
		InputStream is = URLUtil.getInputStream(source);
		BufferedReader bufRd = new BufferedReader(new InputStreamReader(is));
		String line;
		int lineCount = 0;

		String[] parts = null;

		final String delimiter = amp.getDelimiterRegEx();

		is = URLUtil.getInputStream(source);
		bufRd = new BufferedReader(new InputStreamReader(is));
		while ((line = bufRd.readLine()) != null) {
			/*
			 * Ignore Empty & Comment lines.
			 */
			if ((commentChar != null) && line.startsWith(commentChar)) {
				// Do nothing
			} else if ((lineCount >= startLineNumber)
					&& (line.trim().length() > 0)) {
				parts = line.split(delimiter);
				// If key does not exists, ignore the line.
				if (parts.length >= amp.getKeyIndex() + 1) {
					try {
						parser.parseAll(parts);
					} catch (Exception ex) {
						System.out
								.println("Couldn't parse row for attribute mapping: "
										+ lineCount);
					}
				}
			}

			lineCount++;
			
		}
		globalCounter = lineCount;
		amp.setRowCount(lineCount);

		is.close();
		bufRd.close();
		
		// Tag Networks
		String title = amp.getTitle();
		String commandString = amp.getCommandString();

		for (CyNetwork network : amp.getMappedNetworks()){
			String netid = network.getIdentifier();
			List<String> sourcelist = new ArrayList<String>();
			if (Cytoscape.getNetworkAttributes().hasAttribute(netid, NET_ATTR_DATASETS)){
				sourcelist = (List<String>) Cytoscape.getNetworkAttributes().getListAttribute(netid, NET_ATTR_DATASETS);
				if (!sourcelist.contains(title)){
					sourcelist.add(title);
					Cytoscape.getNetworkAttributes().setListAttribute(netid, NET_ATTR_DATASETS, sourcelist);
				}
			} else {
				sourcelist.add(title);
				Cytoscape.getNetworkAttributes().setListAttribute(netid, NET_ATTR_DATASETS, sourcelist);	
			}
			Cytoscape.getNetworkAttributes().setAttribute(netid, NET_ATTR_DATASET_PREFIX + title, commandString);
		}
		
		// Add dataset to Workspaces
		DatasetCommandHandler.updateWorkspaces(title, commandString);

//		Map<String, Object> args = new HashMap<String, Object>();
//		args.put("url", source);
//		args.put("displayname", title);
//		args.put("rows", lineCount);
//		try {
//			CyCommandResult result = CyCommandManager.execute("workspaces", "add dataset", args);
//		} catch (CyCommandException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (RuntimeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	/**
	 * Produce report on import stats
	 * 
	 * @return string
	 */
	public String getReport() {
		final StringBuilder sb = new StringBuilder();
		final Map<String, Object> invalid = parser.getInvalidMap();
		sb.append(globalCounter + " rows were loaded.");

		if (invalid.size() > 0) {
			sb
					.append("\n\nThe following enties are invalid and were not imported:\n");
			int limit = 10;
			for (String key : invalid.keySet()) {
				sb.append(key + " = " + invalid.get(key) + "\n");
				if (limit-- <= 0) {
					sb.append("Approximately " + (invalid.size() - 10)
							+ " additional entries were not imported...");
					break;
				}
			}
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.genmapp.genmappimport.reader.TextTableReader#getNodeList()
	 */
	public int[] getNodeIndexList() {
		return parser.getNodeIndexList();
	}


}
