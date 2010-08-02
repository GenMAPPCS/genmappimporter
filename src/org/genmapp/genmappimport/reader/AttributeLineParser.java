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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genmapp.genmappimport.reader.TextTableReader.ObjectType;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**
 * Take a row of data, analyze it, and map to CyAttributes.
 * 
 */
public class AttributeLineParser {
	private AttributeMappingParameters amp;
	private Map<String, Object> invalid = new HashMap<String, Object>();

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
	public void parseAll(String[] parts) {
		// Get key
		final String primaryKey = parts[amp.getKeyIndex()].trim();
		final int partsLen = parts.length;

		// Create new nodes when necessary
		Cytoscape.getCyNode(primaryKey, true);

		// map attributes
		for (int i = 0; i < partsLen; i++) {
			if (i != amp.getKeyIndex()) {
				if (parts[i] == null) {
					continue;
				} else {
					mapAttribute(primaryKey, parts[i].trim(), i);
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

		// System.out.println("Index = "+ amp.getAttributeNames()[index] +", "+
		// key +" = "+ entry);

		switch (type) {
		case CyAttributes.TYPE_BOOLEAN:

			Boolean newBool;

			try {
				newBool = new Boolean(entry);
				amp.getAttributes().setAttribute(key,
						amp.getAttributeNames()[index], newBool);
			} catch (Exception e) {
				invalid.put(key, entry);
			}

			break;

		case CyAttributes.TYPE_INTEGER:

			Integer newInt;

			try {
				newInt = new Integer(entry);
				amp.getAttributes().setAttribute(key,
						amp.getAttributeNames()[index], newInt);
			} catch (Exception e) {
				invalid.put(key, entry);
			}

			break;

		case CyAttributes.TYPE_FLOATING:

			Double newDouble;

			try {
				newDouble = new Double(entry);
				amp.getAttributes().setAttribute(key,
						amp.getAttributeNames()[index], newDouble);
			} catch (Exception e) {
				invalid.put(key, entry);
			}

			break;

		case CyAttributes.TYPE_STRING:
			try {
				amp.getAttributes().setAttribute(key,
						amp.getAttributeNames()[index], entry);
			} catch (Exception e) {
				invalid.put(key, entry);
			}

			break;

		case CyAttributes.TYPE_SIMPLE_LIST:

			/*
			 * In case of list, do not overwrite. Get the existing list, and add
			 * it to the list.
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

		default:
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
			case CyAttributes.TYPE_BOOLEAN:
				listAttr.add(Boolean.parseBoolean(listItem.trim()));

				break;

			case CyAttributes.TYPE_INTEGER:
				listAttr.add(Integer.parseInt(listItem.trim()));

				break;

			case CyAttributes.TYPE_FLOATING:
				listAttr.add(Double.parseDouble(listItem.trim()));

				break;

			case CyAttributes.TYPE_STRING:
				listAttr.add(listItem.trim());

				break;

			default:
				break;
			}
		}

		return listAttr;
	}
}
