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

import giny.model.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private AttributeMappingParameters mapping;
	private final AttributeLineParser parser;

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
	 * @param mapping
	 *            attribute mapping parameter
	 * @param startLineNumber
	 *            row to start reading
	 * @param commentChar
	 *            character to indicate comment row(s) to be skipped
	 */
	public DefaultAttributeTableReader(final URL source,
			AttributeMappingParameters mapping, final int startLineNumber,
			final String commentChar) {
		this.source = source;
		this.mapping = mapping;
		this.startLineNumber = startLineNumber;
		this.parser = new AttributeLineParser(mapping);
		this.commentChar = commentChar;

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List getColumnNames() {
		List<String> colNamesList = new ArrayList<String>();

		for (String name : mapping.getAttributeNames()) {
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

		final String delimiter = mapping.getDelimiterRegEx();

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
				if (parts.length >= mapping.getKeyIndex() + 1) {
					try {
						parser.collectTableIds(parts);
					} catch (Exception ex) {
						System.out
								.println("Couldn't parse row for id mapping: "
										+ lineCount);
					}
					globalCounter++;
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

		final String delimiter = mapping.getDelimiterRegEx();

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
				if (parts.length >= mapping.getKeyIndex() + 1) {
					try {
						parser.parseAll(parts);
					} catch (Exception ex) {
						System.out
								.println("Couldn't parse row for attribute mapping: "
										+ lineCount);
					}
					globalCounter++;
				}
			}

			lineCount++;
		}

		is.close();
		bufRd.close();
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
