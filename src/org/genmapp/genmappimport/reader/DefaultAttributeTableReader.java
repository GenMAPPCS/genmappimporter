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
	 * Read table from the data source.
	 */
	public void readTable() throws IOException {
		final InputStream is = URLUtil.getInputStream(source);
		final BufferedReader bufRd = new BufferedReader(new InputStreamReader(
				is));
		String line;
		int lineCount = 0;

		/*
		 * Read & extract one line at a time. The line can be Tab delimited,
		 */
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
						parser.parseAll(parts);
					} catch (Exception ex) {
						System.out.println("Couldn't parse row: " + lineCount);
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
