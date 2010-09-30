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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genmapp.genmappimport.commands.DatasetCommandHandler;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;

/**
 * Take a row of data, analyze it, and map to CyAttributes.
 * 
 */
public class AttributeLineParser {
	private AttributeMappingParameters amp;
	private Map<String, Object> invalid = new HashMap<String, Object>();
	private List<Integer> nodeList = new ArrayList<Integer>();

	public static final String CODE = "SystemCode";
	public static final String DATASET = "DATASET";


	/**
	 * Creates a new AttributeLineParser object.
	 * 
	 * @param amp
	 *            attribute mapping parameter
	 */
	public AttributeLineParser(AttributeMappingParameters amp) {
		this.amp = amp;
	}


	/**
	 * Import everything regardless associated nodes/edges exist or not.
	 * 
	 * @param parts
	 *            fields in a row
	 */
	@SuppressWarnings("unchecked")
	public void parseAll(String[] parts, List<CyNetwork> networkList) {
		// Get key
		final String primaryKey = parts[amp.getKeyIndex()].trim();

		// create nodes every time
		CyNode n = Cytoscape.getCyNode(primaryKey, true);
		nodeList.add(n.getRootGraphIndex());

		// then, map attributes from file to datanodes
		mapAttributes(amp.getKeyIndex(), n.getIdentifier(), parts);

	
	}

	/**
	 * @param skipIndex
	 * @param nkey
	 * @param parts
	 * @return
	 */
	private void mapAttributes(int skipIndex, String nkey, String[] parts) {

		final int partsLen = parts.length;
		// map attributes
		for (int i = 0; i < partsLen; i++) {
			if ((i != skipIndex) && amp.getImportFlag()[i]) {
				if (parts[i] == null) {
					continue;
				} else {
					mapAttribute(nkey, parts[i].trim(), i);
				}
			}
		}
	}

	/**
	 * Based on the attribute types, map the entry to CyAttributes.<br>
	 * 
	 * @param key
	 *            node id
	 * @param entry
	 *            attribute value
	 * @param index
	 *            index for attribute name
	 */
	private void mapAttribute(final String key, final String entry,
			final int index) {
		final Byte type = amp.getAttributeTypes()[index];
		
		switch (type) {
			case CyAttributes.TYPE_BOOLEAN :

				Boolean newBool;

				try {
					newBool = new Boolean(entry);
					amp.getAttributes().setAttribute(key,
							amp.getAttributeNames()[index], newBool);
				} catch (Exception e) {
					invalid.put(key, entry);
				}

				break;

			case CyAttributes.TYPE_INTEGER :

				Integer newInt;

				try {
					newInt = new Integer(entry);
					amp.getAttributes().setAttribute(key,
							amp.getAttributeNames()[index], newInt);
				} catch (Exception e) {
					invalid.put(key, entry);
				}

				break;

			case CyAttributes.TYPE_FLOATING :

				Double newDouble;

				try {
					newDouble = new Double(entry);
					amp.getAttributes().setAttribute(key,
							amp.getAttributeNames()[index], newDouble);
				} catch (Exception e) {
					invalid.put(key, entry);
				}

				break;

			case CyAttributes.TYPE_STRING :
				try {
					amp.getAttributes().setAttribute(key,
							amp.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key, entry);
				}

				break;

			case CyAttributes.TYPE_SIMPLE_LIST :

				/*
				 * In case of list, do not overwrite. Get the existing list, and
				 * add it to the list.
				 * 
				 * List items have data types, so we need to extract it first.
				 */
				final Byte[] listTypes = amp.getListAttributeTypes();
				final Byte listType;

				if (listTypes != null) {
					listType = listTypes[index];
				} else {
					listType = CyAttributes.TYPE_STRING;
				}

				List curList = amp.getAttributes().getListAttribute(key,
						amp.getAttributeNames()[index]);

				if (curList == null) {
					curList = new ArrayList();
				}

				curList.addAll(buildList(entry, listType));
				try {
					amp.getAttributes().setListAttribute(key,
							amp.getAttributeNames()[index], curList);
				} catch (Exception e) {
					invalid.put(key, entry);
				}

				break;

			default :
				try {
					amp.getAttributes().setAttribute(key,
							amp.getAttributeNames()[index], entry);
				} catch (Exception e) {
					invalid.put(key, entry);
				}
		}
	}

	protected Map getInvalidMap() {
		return invalid;
	}


	/**
	 * Get list of nodes
	 * 
	 * @return
	 */
	public List<Integer> getNodeIndexList() {
		return nodeList;
	}

	/**
	 * If an entry is a list, split the string and create new List Attribute.
	 * 
	 * @return listAttr new list attribute
	 */
	private List buildList(final String entry, final Byte dataType) {
		if (entry == null) {
			return null;
		}

		final String[] parts = (entry.replace("\"", "")).split(amp
				.getListDelimiter());

		final List listAttr = new ArrayList();

		for (String listItem : parts) {
			switch (dataType) {
				case CyAttributes.TYPE_BOOLEAN :
					listAttr.add(Boolean.parseBoolean(listItem.trim()));

					break;

				case CyAttributes.TYPE_INTEGER :
					listAttr.add(Integer.parseInt(listItem.trim()));

					break;

				case CyAttributes.TYPE_FLOATING :
					listAttr.add(Double.parseDouble(listItem.trim()));

					break;

				case CyAttributes.TYPE_STRING :
					listAttr.add(listItem.trim());

					break;

				default :
					break;
			}
		}

		return listAttr;
	}
}
