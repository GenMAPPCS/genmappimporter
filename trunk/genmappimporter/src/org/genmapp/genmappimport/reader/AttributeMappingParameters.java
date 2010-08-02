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

import static org.genmapp.genmappimport.reader.TextFileDelimiters.PIPE;
import static org.genmapp.genmappimport.reader.TextFileDelimiters.TAB;
import static org.genmapp.genmappimport.reader.TextTableReader.ObjectType.NODE;
import giny.model.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.genmapp.genmappimport.reader.TextTableReader.ObjectType;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;
import cytoscape.data.synonyms.Aliases;

/**
 * Parameter object for text table <---> CyAttributes mapping.
 * 
 */
public class AttributeMappingParameters implements MappingParameter {

	public static final String ID = "ID";
	private static final String DEF_LIST_DELIMITER = PIPE.toString();
	private static final String DEF_DELIMITER = TAB.toString();
	private final ObjectType objectType;
	private final int keyIndex;
	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	private final String mappingAttribute;
	private List<String> delimiters;
	private String listDelimiter;
	private boolean[] importFlag;
	private Map<String, List<String>> attr2id;
	private CyAttributes attributes;
	private Aliases existingAliases;

	// Case sensitivity
	private Boolean caseSensitive;

	/**
	 * Creates a new AttributeMappingParameters object.
	 * 
	 * @param delimiters
	 *            user-defined delimiters between data columns
	 * @param listDelimiter
	 *            user-defined delimiters between list values
	 * @param keyIndex
	 *            ID column
	 * @param attrNames
	 *            column headers
	 * @param attributeTypes
	 *            data types per column
	 * @param listAttributeTypes
	 *            data types per list
	 * 
	 * @throws Exception
	 *             columns must have headers
	 */
	public AttributeMappingParameters(final List<String> delimiters,
			final String listDelimiter, final int keyIndex,
			final String[] attrNames, Byte[] attributeTypes,
			Byte[] listAttributeTypes)

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
		this.keyIndex = keyIndex;
		this.attributeNames = attrNames;
		
		this.listAttributeTypes = listAttributeTypes;
		this.caseSensitive = false; // case insensitive node mapping
		this.objectType = NODE;
		this.mappingAttribute = ID;
		this.importFlag = new boolean[attrNames.length];
		for (int i = 0; i < this.importFlag.length; i++) {
			this.importFlag[i] = true; // import everything
		}

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

		final Iterator<Node> it;
			attributes = Cytoscape.getNodeAttributes();
			existingAliases = Cytoscape.getOntologyServer().getNodeAliases();
			it = Cytoscape.getRootGraph().nodesIterator();

		if ((this.mappingAttribute != null)
				&& !this.mappingAttribute.equals(ID)) {
			buildAttribute2IDMap(it);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Aliases getAlias() {
		return existingAliases;
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
		return importFlag;
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
	public String getListDelimiter() {
		// TODO Auto-generated method stub
		return listDelimiter;
	}

	/**
	 * Returns attribute name for mapping.
	 * 
	 * @return Key CyAttribute name for mapping.
	 */
	public String getMappingAttribute() {
		return mappingAttribute;
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

	public Boolean getCaseSensitive() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectType getObjectType() {
		// TODO Auto-generated method stub
		return null;
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

	/**
	 * Building hashmap for attribute <--> object ID mapping.
	 * 
	 */
	private void buildAttribute2IDMap(Iterator<Node> it) {
		// Mapping from attribute value to object ID.
		attr2id = new HashMap<String, List<String>>();

		String objectID = null;
		Object valObj = null;

		while (it.hasNext()) {

			Node node = (Node) it.next();
			objectID = node.getIdentifier();

			if (CyAttributesUtils.getClass(mappingAttribute, attributes) == List.class) {
				valObj = attributes
						.getListAttribute(objectID, mappingAttribute);
			} else if (CyAttributesUtils.getClass(mappingAttribute, attributes) != Map.class) {
				valObj = attributes.getAttribute(objectID, mappingAttribute);
			}

			// Put the <attribute value>-<object ID list> pair to the Map
			// object.
			if (valObj != null) {
				if (valObj instanceof List) {
					List keys = (List) valObj;

					for (Object key : keys) {
						if (key != null) {
							putAttrValue(key.toString(), objectID);
						}
					}
				} else {
					putAttrValue(valObj.toString(), objectID);
				}

				// if (attr2id.containsKey(attributeValue)) {
				// objIdList = (List<String>) attr2id.get(attributeValue);
				// } else {
				// objIdList = new ArrayList<String>();
				// }
				//
				// objIdList.add(objectID);
				// attr2id.put(attributeValue, objIdList);
			}
		}
	}

	private void putAttrValue(String attributeValue, String objectID) {
		List<String> objIdList = null;

		if (attr2id.containsKey(attributeValue)) {
			objIdList = attr2id.get(attributeValue);
		} else {
			objIdList = new ArrayList<String>();
		}

		objIdList.add(objectID);
		attr2id.put(attributeValue, objIdList);
	}

}
