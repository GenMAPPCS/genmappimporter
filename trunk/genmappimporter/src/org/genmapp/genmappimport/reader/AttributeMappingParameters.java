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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**
 * Parameter object for text table <---> CyAttributes mapping.
 * 
 */
public class AttributeMappingParameters {

	public static final String ID = "ID";
	private static final String DEF_LIST_DELIMITER = PIPE.toString();
	private static final String DEF_DELIMITER = TAB.toString();
	private final int keyIndex;
	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	private List<String> delimiters;
	private String listDelimiter;
	private boolean[] importFlag;
	private Map<String, List<String>> attr2id;
	private CyAttributes attributes;

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
	/**
	 * @param delimiters
	 * @param listDelimiter
	 * @param keyIndex
	 * @param attrNames
	 * @param attributeTypes
	 * @param listAttributeTypes
	 * @throws Exception
	 */
	public AttributeMappingParameters(final List<String> delimiters,
			final String listDelimiter, final int keyIndex,
			final String[] attrNames, Byte[] attributeTypes,
			Byte[] listAttributeTypes, boolean[] importFlag)

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
		if (importFlag == null) {
			this.importFlag = new boolean[attrNames.length];

			for (int i = 0; i < this.importFlag.length; i++) {
				this.importFlag[i] = true;
			}
		} else {
			this.importFlag = importFlag;
		}
		attributes = Cytoscape.getNodeAttributes();

		// final Iterator<Node> it;
		// it = Cytoscape.getRootGraph().nodesIterator();
		// if ((this.mappingAttribute != null)
		// && !this.mappingAttribute.equals(ID)) {
		// buildAttribute2IDMap(it);
		// }
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
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
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

	// /**
	// * Building hashmap for attribute <--> object ID mapping.
	// *
	// * NOT USED HERE. BUT MAY BE USEFUL CODE.
	// *
	// */
	// private void buildAttribute2IDMap(Iterator<Node> it) {
	// // Mapping from attribute value to object ID.
	// attr2id = new HashMap<String, List<String>>();
	//
	// String objectID = null;
	// Object valObj = null;
	//
	// while (it.hasNext()) {
	//
	// Node node = (Node) it.next();
	// objectID = node.getIdentifier();
	//
	// if (CyAttributesUtils.getClass(mappingAttribute, attributes) ==
	// List.class) {
	// valObj = attributes
	// .getListAttribute(objectID, mappingAttribute);
	// } else if (CyAttributesUtils.getClass(mappingAttribute, attributes) !=
	// Map.class) {
	// valObj = attributes.getAttribute(objectID, mappingAttribute);
	// }
	//
	// // Put the <attribute value>-<object ID list> pair to the Map
	// // object.
	// if (valObj != null) {
	// if (valObj instanceof List) {
	// List keys = (List) valObj;
	//
	// for (Object key : keys) {
	// if (key != null) {
	// putAttrValue(key.toString(), objectID);
	// }
	// }
	// } else {
	// putAttrValue(valObj.toString(), objectID);
	// }
	//
	// // if (attr2id.containsKey(attributeValue)) {
	// // objIdList = (List<String>) attr2id.get(attributeValue);
	// // } else {
	// // objIdList = new ArrayList<String>();
	// // }
	// //
	// // objIdList.add(objectID);
	// // attr2id.put(attributeValue, objIdList);
	// }
	// }
	// }
	//
	// private void putAttrValue(String attributeValue, String objectID) {
	// List<String> objIdList = null;
	//
	// if (attr2id.containsKey(attributeValue)) {
	// objIdList = attr2id.get(attributeValue);
	// } else {
	// objIdList = new ArrayList<String>();
	// }
	//
	// objIdList.add(objectID);
	// attr2id.put(attributeValue, objIdList);
	// }

}
