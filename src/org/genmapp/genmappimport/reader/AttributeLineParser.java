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
	private List<String> keyList = new ArrayList<String>();
	private Map<String, Set<String>> primaryMap = new HashMap<String, Set<String>>();
	// private Map<String, Set<String>> secondaryMap = new HashMap<String,
	// Set<String>>();

	public static final String ID = "GeneID";
	public static final String CODE = "SystemCode";
	public static final String DATASET = "DATASET";
	public static final String MIXED = "MIXED";

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
	 * @param parts
	 */
	public void collectTableIds(String[] parts) {
		final String primaryKey = parts[amp.getKeyIndex()].trim();
		keyList.add(primaryKey);
	}

	/**
	 * @param parts
	 */
	public void performNetworkMappings() {

		Map<String, List<String>> typeKeysMap = new HashMap<String, List<String>>();
		Map<String, List<String>> keyNodesMap = new HashMap<String, List<String>>();

		// get all networks with views (ignore others)
		for (CyNetwork network : Cytoscape.getNetworkSet()) {
			if (Cytoscape.viewExists(network.getIdentifier())) {
				// check network-level system code
				String networkCode = Cytoscape.getNetworkAttributes()
						.getStringAttribute(network.getIdentifier(), CODE);
				if (networkCode != null) {
					// skip this network; it's already been id mapped
					continue;
				}
				// determine if there *is* a network-level system code
				// collect first 10 nodes
				List<String> keyList = new ArrayList<String>();
				int j = 10;
				if (network.nodesList().size() < 10)
					j = network.nodesList().size();
				for (int i = 0; i < j; i++) {
					keyList.add(((CyNode) network.nodesList().get(i))
							.getIdentifier());
				}
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("sourceid", keyList);
				try {
					CyCommandResult result = CyCommandManager.execute(
							"idmapping", "guess id type", args);
					if (null != result) {
						// we only trust unique hits
						if (((Set<String>) result.getResult()).size() == 1) {
							String type = null;
							for (String t : (Set<String>) result.getResult()) {
								type = t;
							}
							Cytoscape.getNetworkAttributes().setAttribute(
									network.getIdentifier(), CODE, type);
							// skip special cases, which will be mapped
							// naturally
							if (!type.equals(amp.getSecKeyType())
									&& !type.equals(amp.getKeyType())) {
								CyCommandResult result2 = mapIdentifiersByAttr(
										network, type);
							}
							continue; // continue to next network
						}
					}
				} catch (CyCommandException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// check node-level system codes
				for (CyNode cn : (List<CyNode>) network.nodesList()) {
					List<String> sk = (List<String>) Cytoscape
							.getNodeAttributes().getListAttribute(
									cn.getIdentifier(),
									"__" + amp.getSecKeyType());
					if (sk != null)
						if (sk.size() > 0) {
							continue; // next node
						}
					String pk = Cytoscape.getNodeAttributes()
							.getStringAttribute(cn.getIdentifier(), ID);
					String pkt = Cytoscape.getNodeAttributes()
							.getStringAttribute(cn.getIdentifier(), CODE);
					if (pk != null && pkt != null) {
						List<String> keys = typeKeysMap.get(pkt);
						if (null == keys) {
							keys = new ArrayList<String>();
						}
						keys.add(pk);
						typeKeysMap.put(pkt, keys);
						List<String> nodes = keyNodesMap.get(pk);
						if (null == nodes) {
							nodes = new ArrayList<String>();
						}
						nodes.add(cn.getIdentifier());
						keyNodesMap.put(pk, nodes);

						Cytoscape.getNetworkAttributes().setAttribute(
								network.getIdentifier(), CODE, MIXED);

					} else {
						// screw it! They need to try harder!
					}
				}

			}
		}

		// Finally, take collection of nodes to be mapped and map them
		for (String type : typeKeysMap.keySet()) {

			// first check if mapping is supported
			boolean greenLight = checkMappingSupported(type, amp
					.getSecKeyType());

			if (greenLight) {
				CyCommandResult result = mapIdentifiers(typeKeysMap.get(type),
						type);

				if (null != result) {
					Map<String, Set<String>> keyMappings = (Map<String, Set<String>>) result
							.getResult();
					for (String pkey : keyMappings.keySet()) {
						List<String> slist = new ArrayList<String>();
						for (String skey : keyMappings.get(pkey)) {
							slist.add(skey);
						}

						for (String node : keyNodesMap.get(pkey)) {
							try {
								amp.getAttributes().setListAttribute(node,
										"__" + amp.getSecKeyType(), slist);
							} catch (Exception e) {
								invalid.put(pkey, slist);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public void collectTableMappings() {
		final String primaryKeyType = amp.getKeyType();
		if (primaryKeyType.equals(amp.getSecKeyType()))
			return; // don't bother

		CyCommandResult result = mapIdentifiers(keyList, primaryKeyType);

		if (null != result) {
			Map<String, Set<String>> keyMappings = (Map<String, Set<String>>) result
					.getResult();
			for (String primaryKey : keyMappings.keySet()) {
				primaryMap.put(primaryKey, keyMappings.get(primaryKey));
				// for (String secondaryKey : keyMappings.get(primaryKey)) {
				// Set<String> tempSet = new HashSet<String>();
				// tempSet = secondaryMap.get(secondaryKey);
				// if (null == tempSet)
				// tempSet = new HashSet<String>();
				// tempSet.add(primaryKey);
				// secondaryMap.put(secondaryKey, tempSet);
				// }
			}
		}
	}

	private Boolean checkMappingSupported(String st, String tt) {

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("sourcetype", st);
		args.put("targettype", tt);
		try {
			CyCommandResult result = CyCommandManager.execute("idmapping",
					"check mapping supported", args);
			if (null != result) {
				Boolean b = (Boolean) result.getResult();
				return b;
			} else {
				return false;
			}
		} catch (CyCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if all else fails
		return false;
	}

	private CyCommandResult mapIdentifiers(List<String> l, String pkt) {
		final String skt = amp.getSecKeyType();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("sourceid", l);
		args.put("sourcetype", pkt);
		args.put("targettype", skt);
		CyCommandResult result = null;
		try {
			result = CyCommandManager.execute("idmapping", "general mapping",
					args);
		} catch (CyCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String msg : result.getMessages()) {
			// System.out.println(msg);
		}
		return result;
	}

	private CyCommandResult mapIdentifiersByAttr(CyNetwork net, String pkt) {
		final String skt = amp.getSecKeyType();

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("networklist", net);
		args.put("sourceattr", "ID");
		args.put("sourcetype", pkt);
		args.put("targetattr", "__" + skt);
		args.put("targettype", skt);
		CyCommandResult result = null;
		try {
			result = CyCommandManager.execute("idmapping",
					"attribute based mapping", args);
		} catch (CyCommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String msg : result.getMessages()) {
			// System.out.println(msg);
		}
		return result;
	}

	/**
	 * Import everything regardless associated nodes/edges exist or not.
	 * 
	 * @param parts
	 *            fields in a row
	 */
	@SuppressWarnings("unchecked")
	public void parseAll(String[] parts) {
		// Get key
		final String primaryKey = parts[amp.getKeyIndex()].trim();
		Set<String> secKeys = primaryMap.get(primaryKey);

		// collect existing primary and secondary key nodes
		Node n = Cytoscape.getCyNode(primaryKey, false);
		List<Node> snlist = new ArrayList<Node>();
		if (secKeys != null) {
			for (String secondaryKey : secKeys) {
				Node sn = Cytoscape.getCyNode(secondaryKey, false);
				if (null != sn) {
					snlist.add(sn);
				}
			}
		}
		
		/*
		 * Perform mapping on a per network basis to track associations with
		 * datasets
		 */
		for (CyNetwork network : Cytoscape.getNetworkSet()) {
			if (Cytoscape.viewExists(network.getIdentifier())) {
				// check network-level system code
				String networkCode = Cytoscape.getNetworkAttributes()
						.getStringAttribute(network.getIdentifier(), CODE);
				if (networkCode == DATASET) {
					// skip this network; it's another dataset!
					continue;
				}
				for (CyNode cn : (List<CyNode>) network.nodesList()) {
					boolean didMap = false;
					/*
					 * First, check against primary key nodes
					 */
					if (null != n) {
						if (n == cn) {
							mapAttributes(amp.getKeyIndex(),
									cn.getIdentifier(), parts);
							amp.addMappedNetwork(network);
						}
					}
					/*
					 * Then against secondary keys
					 */
					if (snlist.size() > 0) {
						if (snlist.contains(cn)) {
							mapAttributes(-1, cn.getIdentifier(), parts);
							amp.addMappedNetwork(network);
						}
					}
					/*
					 * Then, we check secondary keys in secondary column (e.g.,
					 * "__Ensembl Yeast") per network with view
					 */
					List<String> sk = (List<String>) Cytoscape
							.getNodeAttributes().getListAttribute(
									cn.getIdentifier(),
									"__" + amp.getSecKeyType());
					if (sk != null) {
						if (sk.size() > 0) {
							for (String secondaryKey : secKeys) {
								if (sk.contains(secondaryKey)) {
									didMap = mapAttributes(-1, cn
											.getIdentifier(), parts);
									amp.addMappedNetwork(network);
									break; // skip remaining secKeys
								}
							}
							if (didMap)
								continue; // next node
						}
					}
					/*
					 * Then check if primaryKey matches ID/CODE directly
					 */
					String pk = Cytoscape.getNodeAttributes()
							.getStringAttribute(cn.getIdentifier(), ID);
					String pkt = Cytoscape.getNodeAttributes()
							.getStringAttribute(cn.getIdentifier(), CODE);
					if (pk != null && pkt != null) {
						if (pkt.equals(amp.getKeyType())
								&& pk.equals(primaryKey)) {
							mapAttributes(amp.getKeyIndex(),
									cn.getIdentifier(), parts);
							amp.addMappedNetwork(network);

						}
					}
				}

			}
		}

		/*
		 * Finally, we create nodes if a new network is requested
		 */
		if (DatasetCommandHandler.createNetworkToggle) {
			n = Cytoscape.getCyNode(primaryKey, true);
			buildNodeList(n.getRootGraphIndex());
			mapAttributes(amp.getKeyIndex(), primaryKey, parts);
		}
	}
	
	/**
	 * @param skipIndex
	 * @param nkey
	 * @param parts
	 * @return
	 */
	private boolean mapAttributes(int skipIndex, String nkey, String[] parts) {

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
		// add primary key as new attribute
		String pkey = parts[amp.getKeyIndex()].trim();
		String ptype = amp.getKeyType();
		List<String> plist = (List<String>) Cytoscape.getNodeAttributes()
				.getListAttribute(nkey, "__" + ptype);
		if (null == plist) {
			plist = new ArrayList<String>();
		}
		if (!plist.contains(pkey)) {
			plist.add(pkey);
		}
		try {
			amp.getAttributes().setListAttribute(nkey, "__" + ptype, plist);
		} catch (Exception e) {
			invalid.put(nkey, plist);
		}

		// add secondary key as new attribute
		// only applies to special cases where mapping has been skipped
		if (pkey.equals(nkey) || primaryMap.get(pkey).contains(nkey)) {
			String stype = amp.getSecKeyType();
			List<String> slist = new ArrayList<String>();
			for (String skey : primaryMap.get(pkey)) {
				slist.add(skey);
			}
			try {
				amp.getAttributes().setListAttribute(nkey, "__" + stype, slist);
			} catch (Exception e) {
				invalid.put(nkey, slist);
			}
		}
		return true;
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
	 * Compile a list of nodes from data file
	 * 
	 * @param n
	 */
	private void buildNodeList(final int n) {
		nodeList.add(n);
	}

	/**
	 * Get list of nodes
	 * 
	 * @return
	 */
	public int[] getNodeIndexList() {
		int[] nodeIndexList = new int[nodeList.size()];
		for (int i = 0; i < nodeList.size(); i++) {
			nodeIndexList[i] = nodeList.get(i);
		}
		return nodeIndexList;
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
