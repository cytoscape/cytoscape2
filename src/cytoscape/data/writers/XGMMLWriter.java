/*
 File: XGMMLWriter.java 
 
 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
 The Cytoscape Consortium is: 
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Pasteur Institute
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

package cytoscape.data.writers;

import giny.view.Bend;
import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.attr.MultiHashMap;
import cytoscape.data.attr.MultiHashMapDefinition;
import cytoscape.data.readers.MetadataParser;
import cytoscape.generated2.Att;
import cytoscape.generated2.Edge;
import cytoscape.generated2.Graph;
import cytoscape.generated2.Graphics;
import cytoscape.generated2.Node;
import cytoscape.generated2.ObjectFactory;
import cytoscape.generated2.RdfRDF;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.LineType;
import ding.view.DGraphView;

/**
 * 
 * Write network and attributes in XGMML format and <br>
 * marshall it in a streme.<br>
 * 
 * @version 1.0
 * @since Cytoscape 2.3
 * @see cytoscape.data.readers.XGMMLReader
 * @author kono
 * 
 */
public class XGMMLWriter {

	// Package to be used for data binding.
	private static final String PACKAGE_NAME = "cytoscape.generated2";
	
	// File format version.  For compatibility.
	private static final String FORMAT_VERSION = "documentVersion";
	private static final float VERSION = (float) 1.0;
	
	private static final String METADATA_NAME = "networkMetadata";
	private static final String METADATA_ATTR_NAME = "Network Metadata";

	// GML-Compatible Pre-defined Shapes
	protected static final String RECTANGLE = "rectangle";
	protected static final String ELLIPSE = "ellipse";
	protected static final String LINE = "Line";
	protected static final String POINT = "point";
	protected static final String DIAMOND = "diamond";
	protected static final String HEXAGON = "hexagon";
	protected static final String OCTAGON = "octagon";
	protected static final String PARALELLOGRAM = "parallelogram";
	protected static final String TRIANGLE = "triangle";

	// Node types
	protected static final String NORMAL = "normal";
	protected static final String METANODE = "metanode";
	protected static final String REFERENCE = "reference";

	// Object types
	protected static final int NODE = 1;
	protected static final int EDGE = 2;
	protected static final int NETWORK = 3;

	public static final String BACKGROUND = "backgroundColor";
	public static final String GRAPH_VIEW_ZOOM = "GRAPH_VIEW_ZOOM";
	public static final String GRAPH_VIEW_CENTER_X = "GRAPH_VIEW_CENTER_X";
	public static final String GRAPH_VIEW_CENTER_Y = "GRAPH_VIEW_CENTER_Y";

	private CyAttributes nodeAttributes;
	private CyAttributes edgeAttributes;
	private CyAttributes networkAttributes;
	private String[] nodeAttNames = null;
	private String[] edgeAttNames = null;
	private String[] networkAttNames = null;

	private CyNetwork network;
	private CyNetworkView networkView;

	private ArrayList nodeList;
	private ArrayList metanodeList;
	private ArrayList edgeList;

	JAXBContext jc;
	ObjectFactory objFactory;

	MetadataParser mdp;
	Graph graph = null;

	// Default CSS file name. Maybe used in the future.
	private static final String CSS_FILE = "base.css";

	// These types are permitted by the XGMML standard
	protected static final String FLOAT_TYPE = "real";
	protected static final String INT_TYPE = "integer";
	protected static final String STRING_TYPE = "string";
	protected static final String LIST_TYPE = "list";

	// These types are not permitted by the XGMML standard
	protected static final String BOOLEAN_TYPE = "boolean";
	protected static final String MAP_TYPE = "map";
	protected static final String COMPLEX_TYPE = "complex";

	/**
	 * Constructor.<br>
	 * Initialize data objects to be saved in XGMML file.<br>
	 * 
	 * @param network
	 *            CyNetwork object to be saved.
	 * @param view
	 *            CyNetworkView for the network.
	 * @throws URISyntaxException
	 * @throws JAXBException
	 */
	public XGMMLWriter(CyNetwork network, CyNetworkView view)
			throws JAXBException, URISyntaxException {
		this.network = network;
		this.networkView = view;

		nodeAttributes = Cytoscape.getNodeAttributes();
		edgeAttributes = Cytoscape.getEdgeAttributes();
		networkAttributes = Cytoscape.getNetworkAttributes();

		nodeList = new ArrayList();
		metanodeList = new ArrayList();
		edgeList = new ArrayList();

		nodeAttNames = nodeAttributes.getAttributeNames();
		edgeAttNames = edgeAttributes.getAttributeNames();
		networkAttNames = networkAttributes.getAttributeNames();

		initialize();
	}

	/**
	 * Make JAXB-generated objects for the XGMML file.
	 * 
	 * @throws JAXBException
	 * @throws URISyntaxException
	 */
	private void initialize() throws JAXBException, URISyntaxException {
		objFactory = new ObjectFactory();
		RdfRDF metadata = null;
		Att graphAtt = null;
		Att formatVersion = null;
		Att globalGraphics = null;

		jc = JAXBContext.newInstance(PACKAGE_NAME, this.getClass().getClassLoader());
		graph = objFactory.createGraph();
		
		graphAtt = objFactory.createAtt();
		
		// Document version.  This maybe used in the later versions.
		formatVersion = objFactory.createAtt();
		formatVersion.setName(FORMAT_VERSION);
		formatVersion.setValue(Float.toString(VERSION));
		graph.getAtt().add(formatVersion);
		
		graph.setId(network.getTitle()); // This is the name of network, NOT
											// rootgraph index!!
		graph.setLabel(network.getTitle());

		// Metadata
		mdp = new MetadataParser(network);
		metadata = mdp.getMetadata();

		graphAtt.setName(METADATA_NAME);
		graphAtt.getContent().add(metadata);
		graph.getAtt().add(graphAtt);

		// Store background color
		if (networkView != null) {
			globalGraphics = objFactory.createAtt();
			globalGraphics.setName(BACKGROUND);

			globalGraphics.setValue(paint2string(networkView
					.getBackgroundPaint()));
			graph.getAtt().add(globalGraphics);
		}
	}

	/**
	 * Write the XGMML file.<br>
	 * This method creates all JAXB objects from Cytoscape internal<br>
	 * data structure, and them marshall it into an XML (XGMML) document.<br>
	 * 
	 * @param writer
	 *            Witer to create XGMML file
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void write(final Writer writer) throws JAXBException, IOException {

		writeBaseNodes();
		writeMetanodes();

		// Create edge objects
		writeEdges();

		// write out network attributes
		writeNetworkAttributes();

		// This creates the header of the XML document.
		writer.write("<?xml version='1.0'?>\n");

		// Will be restored when CSS is ready.
		// writer.write("<?xml-stylesheet type='text/css' href='" + CSS_FILE
		// + "' ?>\n");

		final Marshaller m = jc.createMarshaller();

		// Set proper namespace prefix (mainly for metadata)
		try {
			m.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
					new NamespacePrefixMapperImpl());
		} catch (PropertyException e) {
			// if the JAXB provider doesn't recognize the prefix mapper,
			// it will throw this exception. Since being unable to specify
			// a human friendly prefix is not really a fatal problem,
			// you can just continue marshalling without failing
			e.printStackTrace();
		}

		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(graph, writer);
	}

	/**
	 * Map Cytoscape edge data into JAXB object.<br>
	 * 
	 * @throws JAXBException
	 */
	private void writeEdges() throws JAXBException {
		final Iterator it = network.edgesIterator();

		CyEdge curEdge = null;
		Edge jxbEdge = null;

		while (it.hasNext()) {
			curEdge = (CyEdge) it.next();
			jxbEdge = objFactory.createEdge();

			jxbEdge.setId(curEdge.getIdentifier());
			jxbEdge.setLabel(curEdge.getIdentifier());

			jxbEdge.setSource(Integer.toString(curEdge.getSource()
					.getRootGraphIndex()));
			jxbEdge.setTarget(Integer.toString(curEdge.getTarget()
					.getRootGraphIndex()));

			if (networkView != Cytoscape.getNullNetworkView()) {
				final Graphics edgeGraphics = getGraphics(EDGE, networkView.getEdgeView(curEdge));
				if (edgeGraphics != null) {
					jxbEdge.setGraphics(edgeGraphics);
				}
			}
			attributeWriter(EDGE, curEdge.getIdentifier(), jxbEdge);
			edgeList.add(curEdge);
			graph.getNodeOrEdge().add(jxbEdge);
		}
	}

	/**
	 * Map network CyAttributes to JAXB Att object.
	 * 
	 * @throws JAXBException
	 */
	private void writeNetworkAttributes() throws JAXBException {

		// these are attributes that live inside CyAttributes
		attributeWriter(NETWORK, network.getIdentifier(), null);

		if (networkView != null) {
			// lets also write the zoom
			saveViewZoom();

			// save the center of the view
			saveViewCenter();
		}
	}

	/**
	 * Extract attributes and map it to JAXB object
	 * 
	 * @param type -
	 *            type of attribute (node, edge, network)
	 * @param id -
	 *            id of node, edge, network (key into CyAttributes)
	 * @param target -
	 *            jaxb object
	 * 
	 * @throws JAXBException
	 */
	protected void attributeWriter(final int type, final String id, final Object target)
			throws JAXBException {

		// process type node
		if (type == NODE) {
			final Node targetNode = (Node) target;
			// process each attribute type
			for (int i = 0; i < nodeAttNames.length; i++) {
				if (nodeAttNames[i] == "node.width"
						|| nodeAttNames[i] == "node.height") {
					// Ignore
				} else if (nodeAttNames[i] == "nodeType") {
					final String nType = nodeAttributes.getStringAttribute(id,
							nodeAttNames[i]);
					if (nType != null) {
						targetNode.setName(nType);
					} else {
						targetNode.setName("base");
					}
				} else {
					targetNode.getAtt().add(createAttribute(id, nodeAttributes,
							nodeAttNames[i]));
				}
			}
		}
		// process type edge
		else if (type == EDGE) {
			// process each attribute type
			for (int i = 0; i < edgeAttNames.length; i++) {
				((Edge) target).getAtt().add(createAttribute(id, edgeAttributes, edgeAttNames[i]));
			}
		}
		// process type network
		else if (type == NETWORK) {
			// process each attribute type
			for (int i = 0; i < networkAttNames.length; i++) {
				// ignore Metadata object.
				if (!networkAttNames[i].equals(METADATA_ATTR_NAME)) {
					graph.getAtt().add(createAttribute(id, networkAttributes,
							networkAttNames[i]));
				}
			}
		}
	}

	/**
	 * Creates an attribute to write into XGMML file.
	 * 
	 * @param id -
	 *            id of node, edge or network
	 * @param attributes -
	 *            CyAttributes to load
	 * @param attNames -
	 *            ref to attribute names array
	 * @param attNamesIndex -
	 *            index into attribute names array
	 * @return att - Att to return (gets written into xgmml file)
	 * 
	 * @throws JAXBException
	 */
	private Att createAttribute(final String id, final CyAttributes attributes,
			final String attributeName) throws JAXBException {

		// create an attribute and its type
		Att attr = objFactory.createAtt();
		final byte attType = attributes.getType(attributeName);

		// process float
		if (attType == CyAttributes.TYPE_FLOATING) {
			Double dAttr = attributes.getDoubleAttribute(id, attributeName);
			attr.setName(attributeName);
			attr.setLabel(attributeName);
			attr.setType(FLOAT_TYPE);
			if (dAttr != null)
				attr.setValue(dAttr.toString());
		}
		// process integer
		else if (attType == CyAttributes.TYPE_INTEGER) {
			Integer iAttr = attributes.getIntegerAttribute(id, attributeName);
			attr.setName(attributeName);
			attr.setLabel(attributeName);
			attr.setType(INT_TYPE);
			if (iAttr != null)
				attr.setValue(iAttr.toString());
		}
		// process string
		else if (attType == CyAttributes.TYPE_STRING) {
			String sAttr = attributes.getStringAttribute(id, attributeName);
			attr.setName(attributeName);
			attr.setLabel(attributeName);
			attr.setType(STRING_TYPE);
			if (sAttr != null) {
				attr.setValue(sAttr);
			} else if (attributeName == "nodeType") {
				attr.setValue(NORMAL);
			}
		}
		// process boolean
		else if (attType == CyAttributes.TYPE_BOOLEAN) {
			Boolean bAttr = attributes.getBooleanAttribute(id, attributeName);
			attr.setName(attributeName);
			attr.setLabel(attributeName);
			attr.setType(BOOLEAN_TYPE);
			if (bAttr != null)
				attr.setValue(bAttr.toString());
		}
		// process simple list
		else if (attType == CyAttributes.TYPE_SIMPLE_LIST) {
			// get the attribute list
			final List listAttr = attributes.getAttributeList(id, attributeName);
			// set attribute name and label
			attr.setName(attributeName);
			attr.setLabel(attributeName);
			attr.setType(LIST_TYPE);
			// interate through the list
			final Iterator listIt = listAttr.iterator();
			while (listIt.hasNext()) {
				// get the attribute from the list
				final Object obj = listIt.next();
				// create a "child" attribute to store in xgmml file
				Att memberAttr = objFactory.createAtt();
				// set child attribute value & label
				memberAttr.setValue(obj.toString());
				memberAttr.setType(checkType(obj));
				// add child attribute to parent
				attr.getContent().add(memberAttr);
			}
		}
		// process simple map
		else if (attType == CyAttributes.TYPE_SIMPLE_MAP) {
			// get the attribute map
			final Map mapAttr = attributes.getAttributeMap(id, attributeName);
			// set our attribute name and label
			attr.setName(attributeName);
			attr.setLabel(attributeName);
			attr.setType(MAP_TYPE);
			// interate through the map
			final Iterator mapIt = mapAttr.keySet().iterator();
			while (mapIt.hasNext()) {
				// get the attribute from the map
				Object obj = mapIt.next();
				String key = (String) obj;
				// create a "child" attribute to store in xgmml file
				Att memberAttr = objFactory.createAtt();
				// set child attribute name, label, and value
				memberAttr.setName(key);
				memberAttr.setType(checkType(mapAttr.get(key)));
				memberAttr.setValue(mapAttr.get(key).toString());
				// add child attribute to parent
				attr.getContent().add(memberAttr);
			}
		}
		// process complex type
		else if (attType == CyAttributes.TYPE_COMPLEX) {
			attr = createComplexAttribute(id, attributes, attributeName);
		}

		// outta here
		return attr;
	}

	/**
	 * Creates an attribute to write into XGMML file from an attribute whose
	 * type is COMPLEX.
	 * 
	 * @param id -
	 *            id of node, edge or network
	 * @param attributes -
	 *            CyAttributes to load
	 * @param attributeName -
	 *            name of attribute
	 * @return att - Att to return (gets written into xgmml file)
	 * 
	 * @throws JAXBException
	 */
	private Att createComplexAttribute(String id, CyAttributes attributes,
			String attributeName) throws JAXBException {

		// the attribute to return
		Att attrToReturn = objFactory.createAtt();

		// get the multihashmap definition
		MultiHashMap mmap = attributes.getMultiHashMap();
		MultiHashMapDefinition mmapDef = attributes.getMultiHashMapDefinition();

		// get number & types of dimensions
		byte[] dimTypes = mmapDef
				.getAttributeKeyspaceDimensionTypes(attributeName);

		// set top level attribute name, label
		attrToReturn.setType(COMPLEX_TYPE);
		attrToReturn.setLabel(attributeName);
		attrToReturn.setName(attributeName);
		attrToReturn.setValue(String.valueOf(dimTypes.length));

		// grab the complex attribute structure
		Map complexAttributeStructure = getComplexAttributeStructure(mmap, id,
				attributeName, null, 0, dimTypes.length);

		// determine val type, get its string equilvalent to store in xgmml
		String valTypeStr = getType(mmapDef
				.getAttributeValueType(attributeName));

		// walk the structure
		Iterator complexAttributeIt = complexAttributeStructure.keySet()
				.iterator();
		while (complexAttributeIt.hasNext()) {
			// grab the next key and map to add to xgmml
			Object key = complexAttributeIt.next();
			Map thisKeyMap = (Map) complexAttributeStructure.get(key);
			// create an Att instance for this key
			// and set its name, label, & value
			Att thisKeyAttr = objFactory.createAtt();
			thisKeyAttr.setType(getType(dimTypes[0]));
			thisKeyAttr.setLabel(key.toString());
			thisKeyAttr.setName(key.toString());
			thisKeyAttr.setValue(String.valueOf(thisKeyMap.size()));
			// now lets walk the keys structure and add to its attributes
			// content
			thisKeyAttr.getContent().add(
					walkComplexAttributeStructure(null, thisKeyMap, valTypeStr,
							dimTypes, 1));
			// this keys attribute should get added to the attribute we wil
			// return
			attrToReturn.getContent().add(thisKeyAttr);
		}

		// outta here
		return attrToReturn;
	}

	/**
	 * Returns a map where the key(s) are each key in the attribute key space,
	 * and the value is another map or the attribute value.
	 * 
	 * For example, if the following key:
	 * 
	 * {externalref1}{authors}{1} pointed to the following value:
	 * 
	 * "author 1 name",
	 * 
	 * Then we would have a Map where the key is externalref1, the value is a
	 * Map where the key is {authors}, the value is a Map where the key is {1},
	 * the value is "author 1 name".
	 * 
	 * @param mmap -
	 *            reference to MultiHashMap used by CyAttributes
	 * @param id -
	 *            id of node, edge or network
	 * @param attributeName -
	 *            name of attribute
	 * @param keys -
	 *            array of objects which store attribute keys
	 * @param keysIndex -
	 *            index into keys array we should add the next key
	 * @param numKeyDimensions -
	 *            the number of keys used for given attribute name
	 * @return Map - ref to Map interface
	 */
	private Map getComplexAttributeStructure(MultiHashMap mmap, String id,
			String attributeName, Object[] keys, int keysIndex,
			int numKeyDimensions) {

		// out of here if we've interated through all dimTypes
		if (keysIndex == numKeyDimensions)
			return null;

		// the hashmap to return
		Map keyHashMap = new HashMap();

		// create a new object array to store keys for this interation
		// copy all exisiting keys into it
		Object[] newKeys = new Object[keysIndex + 1];
		for (int lc = 0; lc < keysIndex; lc++) {
			newKeys[lc] = keys[lc];
		}

		// get the key span
		Iterator keyspan = mmap.getAttributeKeyspan(id, attributeName, keys);
		while (keyspan.hasNext()) {
			Object newKey = keyspan.next();
			newKeys[keysIndex] = newKey;
			Map nextLevelMap = getComplexAttributeStructure(mmap, id,
					attributeName, newKeys, keysIndex + 1, numKeyDimensions);
			Object objectToStore = (nextLevelMap == null) ? mmap
					.getAttributeValue(id, attributeName, newKeys)
					: nextLevelMap;
			keyHashMap.put(newKey, objectToStore);
		}

		// outta here
		return keyHashMap;
	}

	/**
	 * Walks a complex attribute map and creates a complex attribute on behalf
	 * of createComplexAttribute().
	 * 
	 * @param parentAttr -
	 *            ref to a parentAttr we will be adding to (in certain cases
	 *            this can be null)
	 * @param complexAttributeStructure -
	 *            ref to Map returned from a prior call to
	 *            getComplexAttributeStructure.
	 * @param attributeType -
	 *            the type (string, boolean, float, int) of the attribute value
	 *            this tree describes
	 * @param dimTypes -
	 *            a byte array returned from a prior call to
	 *            getAttributeKeyspaceDimensionTypes(attributeName);
	 * @param dimTypesIndex -
	 *            the index into the dimTypes array we are should work on
	 * @return att - ref to Att which describes the complex type attribute. An
	 *         example/description is as follows:
	 * 
	 * For an arbitrarily complex data structure, like a pseudo hash with the
	 * following structure:
	 * 
	 * {"externalref1"}->{"authors"}->{1}->"author1 name";
	 * {"externalref1"}->{"authors"}->{2}->"author2 name";
	 * {"externalref1"}->{"authors"}->{3}->"author3 name";
	 * 
	 * where the keys externalref1 and authors are strings, and keys 1, 2, 3 are
	 * integers, and the values (author1 name, author2 name, author3 name) are
	 * strings, we would have the following attributes written to the xgmml
	 * file:
	 * 
	 * <att label="complex" name="publication references" value="3"> <att
	 * label="string" name="externalref1" value="1"> <att label="string"
	 * name="authors" value="3"> <att label="int" name="2" value="1"> <att
	 * label="string" value="author2 name"/> </att> <att label="int" name="1"
	 * value="1"> <att label="string" value="author1 name"/> </att> <att
	 * label="int" name="3" value="1"> <att label="string" value="author3
	 * name"/> </att> </att> </att> </att>
	 * 
	 * Notes: - value attribute property for keys is assigned the number of
	 * sub-elements the key references - value attribute property for values is
	 * equal to the value - name attribute property for attributes is only set
	 * for keys, and the value of this property is the key name. - label
	 * attribute property is equal to the data type of the key or value. - name
	 * attribute properties are only set for keys
	 * 
	 * @throws JAXBException
	 * @throws IllegalArgumentException
	 */
	private Att walkComplexAttributeStructure(Att parentAttr,
			Map complexAttributeStructure, String attributeType,
			byte[] dimTypes, int dimTypesIndex) throws JAXBException,
			IllegalArgumentException {

		// att to return
		Att attrToReturn = null;

		Iterator mapIt = complexAttributeStructure.keySet().iterator();
		while (mapIt.hasNext()) {
			Object key = mapIt.next();
			Object possibleAttributeValue = complexAttributeStructure.get(key);
			if (possibleAttributeValue instanceof Map) {
				// we need to create an instance of Att to return
				attrToReturn = objFactory.createAtt();
				// we have a another map
				attrToReturn.setType(getType(dimTypes[dimTypesIndex]));
				attrToReturn.setLabel(key.toString());
				attrToReturn.setName(key.toString());
				attrToReturn.setValue(String
						.valueOf(((Map) possibleAttributeValue).size()));
				// walk the next map
				// note: we check returned attribute address to make sure we are
				// not adding to ourselves
				Att returnedAttribute = walkComplexAttributeStructure(
						attrToReturn, (Map) possibleAttributeValue,
						attributeType, dimTypes, dimTypesIndex + 1);
				// if this is a new att, add it to the Att we will be returning
				if (returnedAttribute != attrToReturn)
					attrToReturn.getContent().add(returnedAttribute);
			} else {
				// if we are here, we must be adding attributes to the
				// parentAttr
				if (parentAttr == null) {
					throw new IllegalArgumentException(
							"Att argument should not be null.");
				}
				// the attribute to return this round is our parent, we just
				// attach stuff to it
				attrToReturn = parentAttr;
				// create our key attribute
				Att keyAttr = objFactory.createAtt();
				keyAttr.setType(getType(dimTypes[dimTypesIndex]));
				keyAttr.setLabel(key.toString());
				keyAttr.setName(key.toString());
				keyAttr.setValue(String.valueOf(1));
				// create our value attribute
				Att valueAttr = objFactory.createAtt();
				valueAttr.setType(attributeType);
				valueAttr.setValue(possibleAttributeValue.toString());
				keyAttr.getContent().add(valueAttr);
				attrToReturn.getContent().add(keyAttr);
			}
		}

		// outta here
		return attrToReturn;
	}

	/**
	 * Get Graphics object mapped from node/edge views.<br>
	 * 
	 * @param type
	 *            Object type. NODE or EDGE.
	 * @param target
	 *            Object view.
	 * @return JAXB-generated Graphics object.
	 * @throws JAXBException
	 */
	private Graphics getGraphics(final int type, final Object target) throws JAXBException {

		if (target == null) {
			return null;
		}

		final Graphics graphics = objFactory.createGraphics();

		/*
		 * This section is for node graphics
		 */
		if (type == NODE) {
			final NodeView curNodeView = (NodeView) target;
			
			/*
			 * In case node is hidden, we cannot get the show and extract node view.
			 */
			boolean hiddenNodeFlag = false;
			
			if(curNodeView.getWidth() == -1) {
				networkView.showGraphObject(curNodeView);
				hiddenNodeFlag = true;
			}
			
			/**
			 * GML compatible attributes
			 */
			// Node shape
			graphics.setType(number2shape(curNodeView.getShape()));

			// Node size and position
			graphics.setH(curNodeView.getHeight());
			graphics.setW(curNodeView.getWidth());
			graphics.setX(curNodeView.getXPosition());
			graphics.setY(curNodeView.getYPosition());

			// Node color
			graphics.setFill(paint2string(curNodeView.getUnselectedPaint()));

			// Node border basic info.
			final BasicStroke borderType = (BasicStroke) curNodeView.getBorder();
			graphics.setWidth(BigInteger.valueOf((long) borderType.getLineWidth()));
			graphics.setOutline(paint2string(curNodeView.getBorderPaint()));

			/**
			 * Extended attributes supported by GINY
			 */
			// Store Cytoscap-local graphical attributes
			final Att cytoscapeNodeAttr = objFactory.createAtt();
			cytoscapeNodeAttr.setName("cytoscapeNodeGraphicsAttributes");

			final Att transparency = objFactory.createAtt();
			final Att nodeLabelFont = objFactory.createAtt();
			final Att borderLineType = objFactory.createAtt();

			transparency.setName("nodeTransparency");
			nodeLabelFont.setName("nodeLabelFont");
			borderLineType.setName("borderLineType");

			transparency.setValue(Double
					.toString(curNodeView.getTransparency()));
			nodeLabelFont
					.setValue(encodeFont(curNodeView.getLabel().getFont()));

			// Where should we store line-type info???
			final float[] dash = borderType.getDashArray();
			if (dash == null) {
				// System.out.println("##Border is NORMAL LINE");
				borderLineType.setValue("solid");
			} else {
				// System.out.println("##Border is DASHED LINE");
				String dashArray = null;
				final StringBuffer dashBuf = new StringBuffer();
				for (int i = 0; i < dash.length; i++) {
					dashBuf.append(Double.toString(dash[i]));
					if (i < dash.length - 1) {
						dashBuf.append(",");
					}
				}
				dashArray = dashBuf.toString();
				borderLineType.setValue(dashArray);
			}
			cytoscapeNodeAttr.getContent().add(transparency);
			cytoscapeNodeAttr.getContent().add(nodeLabelFont);
			cytoscapeNodeAttr.getContent().add(borderLineType);

			graphics.getAtt().add(cytoscapeNodeAttr);

			/*
			 * Hide the node if necessary
			 */
			if(hiddenNodeFlag) {
				networkView.hideGraphObject(curNodeView);
			}
			return graphics;
		} else if (type == EDGE) {
			final EdgeView curEdgeView = (EdgeView) target;
			
			/**
			 * GML compatible attributes
			 */
			// Width
			graphics.setWidth(BigInteger.valueOf((long) curEdgeView
					.getStrokeWidth()));
			// Color
			graphics.setFill(paint2string(curEdgeView.getUnselectedPaint()));

			/**
			 * Extended attributes supported by GINY
			 */
			// Store Cytoscap-local graphical attributes
			final Att cytoscapeEdgeAttr = objFactory.createAtt();
			cytoscapeEdgeAttr.setName("cytoscapeEdgeGraphicsAttributes");

			final Att sourceArrow = objFactory.createAtt();
			final Att targetArrow = objFactory.createAtt();
			final Att edgeLabelFont = objFactory.createAtt();
			final Att edgeLineType = objFactory.createAtt();
			final Att sourceArrowColor = objFactory.createAtt();
			final Att targetArrowColor = objFactory.createAtt();

			// Bend
			final Att bend = objFactory.createAtt();

			// Curved (Bezier Curves) or Straight line
			final Att curved = objFactory.createAtt();

			sourceArrow.setName("sourceArrow");
			targetArrow.setName("targetArrow");
			edgeLabelFont.setName("edgeLabelFont");
			edgeLineType.setName("edgeLineType");
			sourceArrowColor.setName("sourceArrowColor");
			targetArrowColor.setName("targetArrowColor");
			bend.setName("edgeBend");
			curved.setName("curved");

			sourceArrow.setValue(Integer.toString(curEdgeView
					.getSourceEdgeEnd()));
			targetArrow.setValue(Integer.toString(curEdgeView
					.getTargetEdgeEnd()));

			edgeLabelFont
					.setValue(encodeFont(curEdgeView.getLabel().getFont()));

			edgeLineType.setValue(lineTypeBuilder(curEdgeView).toString());

			// Extract bend information
			final Bend bendData = curEdgeView.getBend();
			final List handles = bendData.getHandles();

			final Iterator bendIt = handles.iterator();
			while (bendIt.hasNext()) {
				final java.awt.geom.Point2D handle = (Point2D) bendIt.next();
				final Att handlePoint = objFactory.createAtt();
				final Att handleX = objFactory.createAtt();
				final Att handleY = objFactory.createAtt();

				handlePoint.setName("handle");
				handleX.setName("x");
				handleY.setName("y");

				handleX.setValue(Double.toString(handle.getX()));
				handleY.setValue(Double.toString(handle.getY()));
				handlePoint.getContent().add(handleX);
				handlePoint.getContent().add(handleY);
				bend.getContent().add(handlePoint);
			}

			// Set curved or not
			if (curEdgeView.getLineType() == EdgeView.CURVED_LINES) {
				curved.setValue("CURVED_LINES");
			} else if (curEdgeView.getLineType() == EdgeView.STRAIGHT_LINES) {
				curved.setValue("STRAIGHT_LINES");
			}

			sourceArrowColor.setValue(paint2string(curEdgeView
					.getSourceEdgeEndPaint()));
			targetArrowColor.setValue(paint2string(curEdgeView
					.getTargetEdgeEndPaint()));

			cytoscapeEdgeAttr.getContent().add(sourceArrow);
			cytoscapeEdgeAttr.getContent().add(targetArrow);
			cytoscapeEdgeAttr.getContent().add(edgeLabelFont);
			cytoscapeEdgeAttr.getContent().add(edgeLineType);
			cytoscapeEdgeAttr.getContent().add(sourceArrowColor);
			cytoscapeEdgeAttr.getContent().add(targetArrowColor);
			if (bend.getContent().size() != 0) {
				cytoscapeEdgeAttr.getContent().add(bend);
			}
			cytoscapeEdgeAttr.getContent().add(curved);
			graphics.getAtt().add(cytoscapeEdgeAttr);

			return graphics;
		}
		return null;
	}

	/**
	 * This is for metanode. Currently, this is not used.
	 * 
	 * @param node
	 * @param metanode
	 * @param childrenIndices
	 * @throws JAXBException
	 */
	private void expand(CyNode node, Node metanode, int[] childrenIndices)
			throws JAXBException {
		CyNode childNode = null;
		Att children = objFactory.createAtt();
		children.setName("metanodeChildren");
		Graph subGraph = objFactory.createGraph();
		Node jxbChildNode = null;

		// test

		for (int i = 0; i < childrenIndices.length; i++) {
			childNode = (CyNode) Cytoscape.getRootGraph().getNode(
					childrenIndices[i]);

			jxbChildNode = objFactory.createNode();
			jxbChildNode.setId(childNode.getIdentifier());
			jxbChildNode.setLabel(childNode.getIdentifier());
			subGraph.getNodeOrEdge().add(jxbChildNode);
			int[] grandChildrenIndices = network
					.getRootGraph()
					.getNodeMetaChildIndicesArray(childNode.getRootGraphIndex());
			if (grandChildrenIndices == null
					|| grandChildrenIndices.length == 0) {
				attributeWriter(NODE, childNode.getIdentifier(), jxbChildNode);
				metanode.setGraphics(getGraphics(NODE, networkView
						.getNodeView(node)));
			} else {
				expand(childNode, jxbChildNode, grandChildrenIndices);
			}
		}
		attributeWriter(NODE, metanode.getId(), metanode);

		children.getContent().add(subGraph);
		metanode.getAtt().add(children);
	}

	/**
	 * Convert enumerated shapes into human-readable string.<br>
	 * 
	 * @param type
	 *            Enumerated node shape.
	 * @return Shape in string.
	 */
	private String number2shape(final int type) {
		if (type == NodeView.ELLIPSE) {
			return ELLIPSE;
		} else if (type == NodeView.RECTANGLE) {
			return RECTANGLE;
		} else if (type == NodeView.DIAMOND) {
			return DIAMOND;
		} else if (type == NodeView.HEXAGON) {
			return HEXAGON;
		} else if (type == NodeView.OCTAGON) {
			return OCTAGON;
		} else if (type == NodeView.PARALELLOGRAM) {
			return PARALELLOGRAM;
		} else if (type == NodeView.TRIANGLE) {
			return TRIANGLE;
		} else {
			return null;
		}
	}

	/**
	 * Convert color (paint) to RGB string.<br>
	 * 
	 * @param p
	 *            Paint object to be converted.
	 * @return Color in RGB string.
	 */
	private String paint2string(final Paint p) {

		final Color c = (Color) p;
		return ("#"// +Integer.toHexString(c.getRGB());
				+ Integer.toHexString(256 + c.getRed()).substring(1)
				+ Integer.toHexString(256 + c.getGreen()).substring(1) + Integer
				.toHexString(256 + c.getBlue()).substring(1));
	}

	/**
	 * Map CyNode data onto JAXB objects.<br>
	 * 
	 * @throws JAXBException
	 */
	private void writeBaseNodes() throws JAXBException {

		Node jxbNode = null;
		CyNode curNode = null;

		final Iterator it = network.nodesIterator();

		while (it.hasNext()) {
			curNode = (CyNode) it.next();
			jxbNode = objFactory.createNode();
			jxbNode.setId(Integer.toString(curNode.getRootGraphIndex()));
			jxbNode.setLabel(curNode.getIdentifier());
			jxbNode.setName("base");

			// Add graphics if available
			if (networkView != null) {
				final NodeView curNodeView = networkView.getNodeView(curNode);
				if (curNodeView != null) {
					jxbNode.setGraphics(getGraphics(NODE, curNodeView));
				}
			}

			attributeWriter(NODE, curNode.getIdentifier(), jxbNode);
			if (isMetanode(curNode)) {
				nodeList.add(curNode);
				metanodeList.add(curNode);
				expandChildren(curNode);
			} else {
				nodeList.add(curNode);
				graph.getNodeOrEdge().add(jxbNode);
			}

		}
	}

	/**
	 * Build a JAXB Node object from a CyNode.<br>
	 * 
	 * @param node
	 *            CyNode to be mapped.
	 * @return JAXB Node object.
	 * @throws JAXBException
	 */
	private Node buildJAXBNode(final CyNode node) throws JAXBException {
		Node jxbNode = null;

		jxbNode = objFactory.createNode();
		jxbNode.setId(Integer.toString(node.getRootGraphIndex()));
		jxbNode.setLabel(node.getIdentifier());

		if (networkView != Cytoscape.getNullNetworkView()) {
			jxbNode.setGraphics(getGraphics(NODE, networkView.getNodeView(node)));
		}
		attributeWriter(NODE, node.getIdentifier(), jxbNode);
		return jxbNode;
	}

	/**
	 * This is for Metanodes.
	 * 
	 * @param node
	 * @throws JAXBException
	 */
	private void expandChildren(final CyNode node) throws JAXBException {

		CyNode childNode = null;
		Node jxbNode = null;

		final int[] childrenIndices = network.getRootGraph()
				.getNodeMetaChildIndicesArray(node.getRootGraphIndex());

		for (int i = 0; i < childrenIndices.length; i++) {
			childNode = (CyNode) network.getRootGraph().getNode(
					childrenIndices[i]);

			if (isMetanode(childNode)) {
				metanodeList.add(childNode);
				nodeList.add(childNode);
				expandChildren(childNode);

			} else {
				nodeList.add(childNode);
				jxbNode = buildJAXBNode(childNode);
				jxbNode.setName("base");
				graph.getNodeOrEdge().add(jxbNode);
			}
		}
	}

	/**
	 * Metanode has different format in XML. It is a node with subgraph.
	 * 
	 * @throws JAXBException
	 * 
	 */
	private void writeMetanodes() throws JAXBException {
		final Iterator it = metanodeList.iterator();

		while (it.hasNext()) {
			final CyNode curNode = (CyNode) it.next();
			Node jxbNode = null;
			jxbNode = buildJAXBNode(curNode);
			jxbNode.setName("metaNode");

			final int[] childrenIndices = network.getRootGraph()
					.getNodeMetaChildIndicesArray(curNode.getRootGraphIndex());
			final Att children = objFactory.createAtt();
			children.setName("metanodeChildren");
			final Graph subGraph = objFactory.createGraph();

			for (int i = 0; i < childrenIndices.length; i++) {
				Node childJxbNode = null;

				childJxbNode = objFactory.createNode();
				childJxbNode.setId(((CyNode) network.getRootGraph().getNode(
						childrenIndices[i])).getIdentifier());

				childJxbNode.setName("reference");
				subGraph.getNodeOrEdge().add(childJxbNode);

			}
			children.getContent().add(subGraph);
			jxbNode.getAtt().add(children);
			graph.getAtt().add(jxbNode);
		}

	}

	/**
	 * Returns true if the node is a metanode.
	 * 
	 * @param node
	 * @return
	 */
	private boolean isMetanode(final CyNode node) {

		final int[] childrenIndices = network.getRootGraph()
				.getNodeMetaChildIndicesArray(node.getRootGraphIndex());
		if (childrenIndices == null || childrenIndices.length == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Encode font into a human-readable string.<br>
	 * 
	 * @param font
	 *            Font object.
	 * @return String extracted from the given Font object.
	 */
	private String encodeFont(final Font font) {
		// Encode font into "fontname-style-pointsize" string
		return font.getName() + "-" + font.getStyle() + "-"
		+ font.getSize();
	}

	/**
	 * Check the type of Attributes.
	 * 
	 * @param obj
	 * @return Attribute type in string.
	 * 
	 */
	private String checkType(final Object obj) {
		if (obj.getClass() == String.class) {
			return STRING_TYPE;
		} else if (obj.getClass() == Integer.class) {
			return INT_TYPE;
		} else if (obj.getClass() == Double.class
				|| obj.getClass() == Float.class) {
			return FLOAT_TYPE;
		} else if (obj.getClass() == Boolean.class) {
			return BOOLEAN_TYPE;
		} else
			return null;
	}

	/**
	 * Given a byte describing a MultiHashMapDefinition TYPE_*, return the
	 * proper XGMMLWriter type.
	 * 
	 * @param dimType -
	 *            byte as described in MultiHashMapDefinition
	 * @return String
	 */
	private String getType(final byte dimType) {

		if (dimType == MultiHashMapDefinition.TYPE_BOOLEAN)
			return BOOLEAN_TYPE;
		if (dimType == MultiHashMapDefinition.TYPE_FLOATING_POINT)
			return FLOAT_TYPE;
		if (dimType == MultiHashMapDefinition.TYPE_INTEGER)
			return INT_TYPE;
		if (dimType == MultiHashMapDefinition.TYPE_STRING)
			return STRING_TYPE;

		// houston we have a problem
		return null;
	}

	/**
	 * Get line type of the given edge.<br>
	 * NOTE: This is not a GINY's line type!<br>
	 * 
	 * @param view
	 *            EdgeView
	 * @return LineType of the edge
	 * 
	 */
	private LineType lineTypeBuilder(final EdgeView view) {

		LineType lineType = LineType.LINE_1;
		final BasicStroke stroke = (BasicStroke) view.getStroke();
		final float[] dash = stroke.getDashArray();
		final float width = stroke.getLineWidth();
		
		if (dash == null) {
			// Normal line. check width
			if (width == 1.0) {
				lineType = LineType.LINE_1;
			} else if (width == 2.0) {
				lineType = LineType.LINE_2;
			} else if (width == 3.0) {
				lineType = LineType.LINE_3;
			} else if (width == 4.0) {
				lineType = LineType.LINE_4;
			} else if (width == 5.0) {
				lineType = LineType.LINE_5;
			} else if (width == 6.0) {
				lineType = LineType.LINE_6;
			} else if (width == 7.0) {
				lineType = LineType.LINE_7;
			}
			// System.out.println("SOLID: " + width);
		} else {
			if (width == 1.0) {
				lineType = LineType.DASHED_1;
			} else if (width == 2.0) {
				lineType = LineType.DASHED_2;
			} else if (width == 3.0) {
				lineType = LineType.DASHED_3;
			} else if (width == 4.0) {
				lineType = LineType.DASHED_4;
			} else if (width == 5.0) {
				lineType = LineType.DASHED_5;
			}
			// System.out.println("DASH: " + width);
		}

		return lineType;
	}

	/**
	 * Saves the zoom level.
	 */
	private void saveViewZoom() throws JAXBException {

		// the attribute to write
		final Att attr = objFactory.createAtt();

		// lets get the zoom value
		final Double dAttr = new Double(networkView.getZoom());

		// set the attribute name, label, and value
		attr.setName(GRAPH_VIEW_ZOOM);
		attr.setLabel(GRAPH_VIEW_ZOOM);
		attr.setType(FLOAT_TYPE);
		if (dAttr != null) {
			attr.setValue(dAttr.toString());
		}
		// add attribute to graph object
		graph.getAtt().add(attr);
	}

	/**
	 * Saves the view center coordinates.
	 */
	private void saveViewCenter() throws JAXBException {

		// attribute names
		final String[] coordinates = { GRAPH_VIEW_CENTER_X, GRAPH_VIEW_CENTER_Y };

		// the view center
		final Point2D center = ((DGraphView) networkView).getCenter();

		// process both x & y coordinates
		for (int lc = 0; lc < 2; lc++) {
			// the attribute to write - x coord
			final Att attr = objFactory.createAtt();
			double doubleCoord = (lc == 0) ? center.getX() : center.getY();
			Double coord = new Double(doubleCoord);
			attr.setName(coordinates[lc]);
			attr.setLabel(coordinates[lc]);
			attr.setType(FLOAT_TYPE);
			if (coord != null)
				attr.setValue(coord.toString());
			graph.getAtt().add(attr);
		}
	}
}

class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

	/**
	 * Returns a preferred prefix for the given namespace URI.
	 * 
	 * This method is intended to be overrided by a derived class.
	 * 
	 * @param namespaceUri
	 *            The namespace URI for which the prefix needs to be found.
	 *            Never be null. "" is used to denote the default namespace.
	 * @param suggestion
	 *            When the content tree has a suggestion for the prefix to the
	 *            given namespaceUri, that suggestion is passed as a parameter.
	 *            Typicall this value comes from the QName.getPrefix to show the
	 *            preference of the content tree. This parameter may be null,
	 *            and this parameter may represent an already occupied prefix.
	 * @param requirePrefix
	 *            If this method is expected to return non-empty prefix. When
	 *            this flag is true, it means that the given namespace URI
	 *            cannot be set as the default namespace.
	 * 
	 * @return null if there's no prefered prefix for the namespace URI. In this
	 *         case, the system will generate a prefix for you.
	 * 
	 * Otherwise the system will try to use the returned prefix, but generally
	 * there's no guarantee if the prefix will be actually used or not.
	 * 
	 * return "" to map this namespace URI to the default namespace. Again,
	 * there's no guarantee that this preference will be honored.
	 * 
	 * If this method returns "" when requirePrefix=true, the return value will
	 * be ignored and the system will generate one.
	 */
	public String getPreferredPrefix(final String namespaceUri, final String suggestion,
			boolean requirePrefix) {
		// I want this namespace to be mapped to "xsi"
		if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri))
			return "xsi";

		// For RDF.
		if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceUri))
			return "rdf";

		// Dublin core semantics.
		if ("http://purl.org/dc/elements/1.1/".equals(namespaceUri))
			return "dc";

		// otherwise I don't care. Just use the default suggestion, whatever it
		// may be.
		return suggestion;
	}

	public String[] getPreDeclaredNamespaceUris() {
		return new String[] { "http://www.w3.org/2001/XMLSchema-instance",
				"http://www.w3.org/1999/xlink",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"http://purl.org/dc/elements/1.1/" };
	}
}
