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

import static org.genmapp.genmappimport.reader.TextFileDelimiters.PIPE;
import static org.genmapp.genmappimport.reader.TextFileDelimiters.TAB;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genmapp.genmappimport.commands.DatasetCommandHandler;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.util.CyNetworkNaming;

/**
 * Parameter object for text table <---> CyAttributes mapping.
 * 
 */
public class AttributeMappingParameters {

	public static final String ID = "ID";
	private static final String DEF_LIST_DELIMITER = PIPE.toString();
	private static final String DEF_DELIMITER = TAB.toString();
	private URL source;
	private String title;
	private final int keyIndex;
	private final String keyType;
	private String secondaryKeyType;
	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	private List<String> delimiters;
	private String listDelimiter;
	private boolean[] importFlags;
	private Integer startLine;
	private Map<String, List<String>> attr2id;
	private CyAttributes attributes;
	private Set<CyNetwork> mappedNetworks = new HashSet<CyNetwork>();;
	private String commandString;
	private Integer rowCount;

	/**
	 * Creates a new AttributeMappingParameters object.
	 * 
	 * @param source
	 *            url of source file
	 * @param delimiters
	 *            user-defined delimiters between data columns
	 * @param listDelimiter
	 *            user-defined delimiters between list values
	 * @param keyIndex
	 *            ID column
	 * @param keyType
	 *            ID type
	 * @param secKeyTypes
	 *            secondary ID types for mapping
	 * @param attrNames
	 *            column headers
	 * @param attributeTypes
	 *            data types per column
	 * @param listAttributeTypes
	 *            data types per list
	 * @param importFlags
	 *            columns to import
	 * @param startLine
	 *            row number to start importing
	 * @throws Exception
	 *             columns must have headers
	 */
	public AttributeMappingParameters(URL source,
			final List<String> delimiters, final String listDelimiter,
			final int keyIndex, final String keyType, final String secKeyType,
			final String[] attrNames, Byte[] attributeTypes,
			Byte[] listAttributeTypes, boolean[] importFlags, Integer startLine)
			throws Exception {

		if (attrNames == null) {
			throw new Exception("attributeNames should not be null.");
		}

		/*
		 * Error check: Key column number should be smaller than actual number
		 * of columns in the text table.
		 */
		if (attrNames.length < keyIndex) {
			throw new IOException("Key is out of range.");
		}

		/*
		 * These values should not be null!
		 */
		this.source = source;
		this.keyIndex = keyIndex;
		this.keyType = keyType;
		this.secondaryKeyType = secKeyType;
		this.attributeNames = attrNames;
		this.listAttributeTypes = listAttributeTypes;
		this.startLine = startLine;
		
		File tempFile = new File(source.toString());
		String t = tempFile.getName();
		String title = CyNetworkNaming.getSuggestedNetworkTitle(t);
		this.title = title;
		
		/*
		 * If delimiter is not available, use default value (TAB)
		 */
		if (delimiters == null) {
			this.delimiters = new ArrayList<String>();
			this.delimiters.add(DEF_DELIMITER);
		} else {
			this.delimiters = delimiters;
		}

		/*
		 * If list delimiter is null, use default "|"
		 */
		if (listDelimiter == null) {
			this.listDelimiter = DEF_LIST_DELIMITER;
		} else {
			this.listDelimiter = listDelimiter;
		}

		/*
		 * If not specified, import everything as String attributes.
		 */
		if (attributeTypes == null) {
			this.attributeTypes = new Byte[attrNames.length];

			for (int i = 0; i < attrNames.length; i++) {
				this.attributeTypes[i] = CyAttributes.TYPE_STRING;
			}
		} else {
			this.attributeTypes = attributeTypes;
		}

		/*
		 * Selective import of columns
		 */
		if (importFlags == null) {
			this.importFlags = new boolean[attrNames.length];

			for (int i = 0; i < this.importFlags.length; i++) {
				this.importFlags[i] = true;
			}
		} else {
			this.importFlags = importFlags;
		}
		attributes = Cytoscape.getNodeAttributes();

		// final Iterator<Node> it;
		// it = Cytoscape.getRootGraph().nodesIterator();
		// if ((this.mappingAttribute != null)
		// && !this.mappingAttribute.equals(ID)) {
		// buildAttribute2IDMap(it);
		// }

		// Build string version of cycommand to store in network attributes
		DatasetCommandHandler g;
		this.commandString = 
			DatasetCommandHandler.ARG_ATTR_NAMES+"=\""+DatasetCommandHandler.stringify(attrNames)+"\" " +
			DatasetCommandHandler.ARG_ATTR_TYPES+"=\""+DatasetCommandHandler.stringify(attributeTypes)+"\" " +
			DatasetCommandHandler.ARG_DELS+"=\""+DatasetCommandHandler.stringify(delimiters)+"\" " +
			DatasetCommandHandler.ARG_FLAGS+"=\""+DatasetCommandHandler.stringify(importFlags)+"\" " +
			DatasetCommandHandler.ARG_KEY+"=\""+DatasetCommandHandler.stringify(keyIndex)+"\" " +
			DatasetCommandHandler.ARG_KEY_TYPE+"=\""+DatasetCommandHandler.stringify(keyType)+"\" " +
			DatasetCommandHandler.ARG_LIST_DEL+"=\""+DatasetCommandHandler.stringify(listDelimiter)+"\" " +
			DatasetCommandHandler.ARG_LIST_TYPES+"=\""+DatasetCommandHandler.stringify(listAttributeTypes)+"\" " +
			DatasetCommandHandler.ARG_SEC_KEY_TYPE+"=\""+DatasetCommandHandler.stringify(secKeyType)+"\" " +
			DatasetCommandHandler.ARG_SOURCE+"=\""+DatasetCommandHandler.stringify(source)+"\" " +
			DatasetCommandHandler.ARG_START_LINE+"=\""+DatasetCommandHandler.stringify(startLine)+"\"";
		
	}

	/**
	 * @return the source
	 */
	public URL getSource() {
		return source;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the stringCommand
	 */
	public String getCommandString() {
		return commandString;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public CyAttributes getAttributes() {
		return attributes;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String[] getAttributeNames() {
		// TODO Auto-generated method stub
		return attributeNames;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getAttributeTypes() {
		return attributeTypes;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getListAttributeTypes() {
		return listAttributeTypes;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean[] getImportFlag() {
		// TODO Auto-generated method stub
		return importFlags;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getKeyIndex() {
		// TODO Auto-generated method stub
		return keyIndex;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getKeyType() {
		// TODO Auto-generated method stub
		return keyType;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getSecKeyType() {
		// TODO Auto-generated method stub
		return secondaryKeyType;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getListDelimiter() {
		// TODO Auto-generated method stub
		return listDelimiter;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List<String> getDelimiters() {
		return delimiters;
	}

	/**
	 * Get column count
	 * 
	 * @return number of data columns
	 */
	public int getColumnCount() {
		return attributeNames.length;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getDelimiterRegEx() {
		StringBuffer delimiterBuffer = new StringBuffer();
		delimiterBuffer.append("[");

		for (String delimiter : delimiters) {
			if (delimiter.equals(" += +")) {
				return " += +";
			}

			delimiterBuffer.append(delimiter);
		}

		delimiterBuffer.append("]");

		return delimiterBuffer.toString();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param attributeValue
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List<String> toID(String attributeValue) {
		return attr2id.get(attributeValue);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Map<String, List<String>> getAttributeToIDMap() {
		return attr2id;
	}

	public void addMappedNetwork(CyNetwork network) {
		this.mappedNetworks.add(network);
	}

	public Set<CyNetwork> getMappedNetworks() {
		return this.mappedNetworks;
	}

	/**
	 * @return the rowCount
	 */
	public Integer getRowCount() {
		return rowCount;
	}

	/**
	 * @param rowCount the rowCount to set
	 */
	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
		this.commandString = this.commandString + " "+DatasetCommandHandler.ARG_ROWS+"=\""+DatasetCommandHandler.stringify(rowCount)+"\"";
	}

}
