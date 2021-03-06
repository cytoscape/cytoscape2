/*
 File: XGMMLReader.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute of Systems Biology
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
package cytoscape.data.readers;

import giny.model.Edge;
import giny.model.Node;
import giny.view.EdgeView;
import giny.view.GraphView;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.data.attr.MultiHashMap;
import cytoscape.data.attr.MultiHashMapDefinition;
import cytoscape.data.writers.XGMMLWriter;
import cytoscape.groups.CyGroup;
import cytoscape.groups.CyGroupManager;
import cytoscape.groups.CyGroupViewer;
import cytoscape.layout.LayoutAdapter;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.task.TaskMonitor;
import cytoscape.util.FileUtil;
import cytoscape.util.PercentUtil;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.LineStyle;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualMappingManager;
import cytoscape.logger.CyLogger;
import ding.view.DGraphView;

import java.io.PushbackInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;


/**
 * XGMML file reader.<br>
 * This version is Metanode-compatible.
 *
 * @version 1.0
 * @since Cytoscape 2.3
 * @see cytoscape.data.writers.XGMMLWriter
 * @author kono
 *
 */
public class XGMMLReader extends AbstractGraphReader {
	// Graph Tags
	protected static final String GRAPH = "graph";
	protected static final String NODE = "node";
	protected static final String EDGE = "edge";
	protected static final String GRAPHICS = "graphics";
	protected static final String LABEL = "label";
	protected static final String SOURCE = "source";
	protected static final String TARGET = "target";

	// Shapes used in Cytoscape (not GML standard)
	// In GML, they are called "type"
	protected static final String RECTANGLE = "rectangle";
	protected static final String ELLIPSE = "ellipse";
	protected static final String LINE = "Line"; // This is the Polyline
	                                             // object.
	protected static final String POINT = "point";
	protected static final String DIAMOND = "diamond";
	protected static final String HEXAGON = "hexagon";
	protected static final String OCTAGON = "octagon";
	protected static final String PARALELLOGRAM = "parallelogram";
	protected static final String TRIANGLE = "triangle";
	protected static final String VEE = "vee";
	protected static final String ROUNDED_RECTANGLE = "rounded_rectangle";

	// XGMML shapes (these should be mapped to Cytoscape shapes
	protected static final String BOX = "box"; // Map to rectangle
	protected static final String CIRCLE = "circle"; // Map to ellipse
	protected static final String VELLIPSIS = "ver_ellipsis";
	protected static final String HELLIPSIS = "hor_ellipsis";
	protected static final String RHOMBUS = "rhombus"; // Map to parallelogram
	protected static final String PENTAGON = "pentagon"; // Map to hexagon

	protected static final String CY_NAMESPACE = "http://www.cytoscape.org";

	// XGMML file name to be loaded.
	private String networkName = null;
	private Double graphViewZoom;
	private Double graphViewCenterX;
	private Double graphViewCenterY;
	private InputStream networkStream;
	private XGMMLParser parser;
	private boolean nodeSizeLocked = false;

	private Properties prop = CytoscapeInit.getProperties();
	private String vsbSwitch = prop.getProperty("visualStyleBuilder");

	// For exception handling
	private TaskMonitor taskMonitor;
	private PercentUtil percentUtil;
	private int nextID = 0; // Used to assign ID's to nodes that didn't have them
	private CyLogger logger = null;


	/**
	 * Constructor.<br>
	 * This is for local XGMML file.
	 *
	 * @param fileName  File name of local XGMML file.
	 * @throws FileNotFoundException
	 *
	 */
	public XGMMLReader(final String fileName) {
		this(fileName, null);
	}


	/**
	 * Constructor.<br>
	 * This is usually used for remote file loading.
	 *
	 * @param is
	 *            Input stream of XGMML file,
	 *
	 */
	public XGMMLReader(InputStream is) {
		super("InputStream");
		this.networkStream = is;
		initialize();
	}


	/**
	 * Constructor.<br>
	 * This is usually used for remote file loading.
	 *
	 * @param is
	 *            Input stream of XGMML file,
	 *
	 */
	public XGMMLReader(InputStream is, String name) {
		super(name);

		this.networkStream = is;
		initialize();
	}


	/**
	 * Creates a new XGMMLReader object.
	 *
	 * @param fileName  DOCUMENT ME!
	 * @param monitor  DOCUMENT ME!
	 */
	public XGMMLReader(final String fileName, final TaskMonitor monitor) {
		super(fileName);
		this.taskMonitor = monitor;
		percentUtil = new PercentUtil(3);
		networkStream = FileUtil.getInputStream(fileName, monitor);
		initialize();
	}


	/**
 	 * Sets the task monitor we want to use
 	 *
 	 * @param monitor the TaskMonitor to use
 	 */
	public void setTaskMonitor(TaskMonitor monitor) {
		this.taskMonitor = monitor;
		percentUtil = new PercentUtil(3);
	}


	private void initialize() {
		logger = CyLogger.getLogger(XGMMLReader.class);

		final boolean attemptRepair = Boolean.getBoolean("cytoscape.xgmml.repair.bare.ampersands");
		if (attemptRepair) {
			networkStream = new RepairBareAmpersandsInputStream(networkStream, 512);
		}
	}


	/**
	 *  DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public void read() throws IOException {
		try {
			this.readXGMML();
		} catch (SAXException e) {
			if (taskMonitor != null) {
				taskMonitor.setException(e, e.getMessage());
			}
			// e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}


	/**
	 * Actual method to read XGMML documents.
	 *
	 * @throws IOException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 */
	private void readXGMML() throws SAXException, IOException {
		// Performance check
//		final long start = System.currentTimeMillis();
//		final MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
//		MemoryUsage heapUsage = mbean.getHeapMemoryUsage();
//		MemoryUsage nonHeapUsage = mbean.getNonHeapMemoryUsage();

//		logger.debug("Heap Memory status (SAX): used = " + heapUsage.getUsed()/1000+"KB");
//		logger.debug("Heap Memory status (SAX): MAX = " + heapUsage.getMax()/1000+"KB");
//		logger.debug("Non-heap Memory status (SAX): used = " + nonHeapUsage.getUsed()/1000+"KB");
//		logger.debug("Non-heap Memory status (SAX): MAX = " + nonHeapUsage.getMax()/1000+"KB");

		try {
			try {
				try {
					/*
					 * Read the file and map the entire XML document into data
					 * structure.
					 */
					if (taskMonitor != null) {
						taskMonitor.setPercentCompleted(-1);
						taskMonitor.setStatus("Reading XGMML data...");
					}

					// Get our parser
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();
					ParserAdapter pa = new ParserAdapter(sp.getParser());
					parser = new XGMMLParser();
					pa.setContentHandler(parser);
					pa.setErrorHandler(parser);
					pa.parse(new InputSource(networkStream));
					networkName = parser.getNetworkName();
				} catch (OutOfMemoryError oe) {
					/*
					 * It's not generally a good idea to catch OutOfMemoryErrors, but in
					 * this case, where we know the culprit (a file that is too large),
					 * we can at least try to degrade gracefully.
					 */
					System.gc();
					throw new XGMMLException("Out of memory error caught! The network being loaded is too large for the current memory allocation.  Use the -Xmx flag for the java virtual machine to increase the amount of memory available, e.g. java -Xmx1G cytoscape.jar -p plugins ....");
				} catch (ParserConfigurationException e) {
				} catch (SAXParseException e) {
					logger.error("XGMMLParser: fatal parsing error on line " + e.getLineNumber() + " -- '" + e.getMessage() + "'", e);
					throw e;
				}
			} finally {
				if (networkStream != null) {
					networkStream.close();
				}
			}
		}
		finally {
			networkStream = null;
		}

//		heapUsage = mbean.getHeapMemoryUsage();
//		nonHeapUsage = mbean.getNonHeapMemoryUsage();
//		long memend = Runtime.getRuntime().freeMemory();

//		logger.debug("============= Total time for " + networkName + " = "
//		                   + (System.currentTimeMillis() - start));
//		logger.debug("Heap memory after parsing = " + (heapUsage.getUsed() / 1000)
//			                   + "KB");
//		logger.debug("Non-heap memory after parsing = " + (nonHeapUsage.getUsed() / 1000)
//			                   + "KB");
	}


	public int[] getNodeIndicesArray() {
		return parser.getNodeIndicesArray();
	}


	public int[] getEdgeIndicesArray() {
		return parser.getEdgeIndicesArray();
	}


	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getNetworkID() {
		return networkName;
	}


	/**
	 * @return Returns the networkName.
	 * @uml.property name="networkName"
	 */
	public String getNetworkName() {
		return networkName;
	}


	/**
	 * getLayoutAlgorithm is called to get the Layout Algorithm that will be used
	 * to layout the resulting graph.  In our case, we just return a stub that will
	 * call our internal layout routine, which will just use the default layout, but
	 * with our task monitor
	 *
	 * @return the CyLayoutAlgorithm to use
	 */
	public CyLayoutAlgorithm getLayoutAlgorithm() {
		return new LayoutAdapter() {
			public void doLayout(CyNetworkView networkView, TaskMonitor monitor) {
				// Set 'networkView' as currentView, so that 
				//the VisualStyle created for this view will be applied to this 'networkView' only
				if (networkView != null && networkView.getIdentifier() != null){
					Cytoscape.setCurrentNetworkView(networkView.getIdentifier());
				}
				
				layout(networkView);
			}
		};
	}


	/**
	 * layout the graph based on the graphic attributes
	 *
	 * @param myView the view of the network we want to layout
	 */
	private void layout(CyNetworkView myView) {
		// logger.debug("layout");
		if ((myView == null) || (myView.nodeCount() == 0)) {
			return;
		}

		// Create our visual style creator.  We use the vsbSwitch to tell the style builder
		// whether to create the override attributes or not
		final boolean buildStyle = vsbSwitch == null || vsbSwitch.equals("on");

		VisualStyleBuilder graphStyle = new VisualStyleBuilder(parser.getNetworkName(), false);
		nodeSizeLocked = parser.getNodeSizeLocked();
		graphStyle.setNodeSizeLocked(nodeSizeLocked); // False by default

		// Set background color
		if (parser.getBackgroundColor() != null) {
			myView.setBackgroundPaint(parser.getBackgroundColor());
			graphStyle.setBackgroundColor(parser.getBackgroundColor());
		}

		layoutNodes(myView, graphStyle, buildStyle);
		layoutEdges(myView, graphStyle, buildStyle);

		if (buildStyle)
			graphStyle.buildStyle();
	}


	/**
	 * Layout nodes if view is available.
	 *
	 * @param myView
	 *            GINY's graph view object for the current network.
	 * @param graphStyle the visual style creator object
	 * @param buildStyle if true, build the graphical style
	 */
	private void layoutNodes(final GraphView myView, final VisualStyleBuilder graphStyle, boolean buildStyle) {
		int tempid = 0;
		NodeView view = null;
		HashMap<CyNode, Attributes> nodeGraphicsMap = parser.getNodeGraphics();

		for (CyNode node : nodeGraphicsMap.keySet()) {
			view = myView.getNodeView(node.getRootGraphIndex());
			String label = node.getIdentifier();

			if (view != null) {
				if (label != null) {
					view.getLabel().setText(label);
				} else {
					view.getLabel().setText("node(" + tempid + ")");
					tempid++;
				}

				if (nodeGraphicsMap != null) {
					layoutNodeGraphics(nodeGraphicsMap.get(node), view, graphStyle, buildStyle);
				}
			}
		}
	}


	/**
	 * Extract node graphics information from JAXB object.<br>
	 *
	 * @param graphics
	 *            Graphics information for a node as JAXB object.
	 * @param nodeView
	 *            Actual node view for the target node.
	 * @param graphStyle the visual style creator object
	 * @param buildStyle if true, build the graphical style
	 *
	 */
	private void layoutNodeGraphics(final Attributes graphics, final NodeView nodeView, final VisualStyleBuilder graphStyle,
	                                final boolean buildStyle) {
		// The identifier of this node
		String nodeID = nodeView.getNode().getIdentifier();

		// Location and size of the node
		double x;

		// Location and size of the node
		double y;

		// Location and size of the node
		double h;

		// Location and size of the node
		double w;

		x = XGMMLParser.getDoubleAttribute(graphics,"x");
		y = XGMMLParser.getDoubleAttribute(graphics,"y");
		h = XGMMLParser.getDoubleAttribute(graphics,"h");
		w = XGMMLParser.getDoubleAttribute(graphics,"w");

		nodeView.setXPosition(x);
		nodeView.setYPosition(y);

		if (buildStyle) {
			if (nodeSizeLocked && h != 0.0) {
				graphStyle.addProperty(nodeID, VisualPropertyType.NODE_SIZE, ""+h);
			} else { 
				if (h != 0.0)
					graphStyle.addProperty(nodeID, VisualPropertyType.NODE_HEIGHT, ""+h);
				if (w != 0.0)
					graphStyle.addProperty(nodeID, VisualPropertyType.NODE_WIDTH, ""+w);
			}
		}

		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

		// Set color
		if (buildStyle && XGMMLParser.getAttribute(graphics,"fill") != null) {
			String fillColor = XGMMLParser.getAttribute(graphics, "fill");
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_FILL_COLOR, fillColor);
			// nodeView.setUnselectedPaint(fillColor);
		}

		// Set border line color
		if (buildStyle && XGMMLParser.getAttribute(graphics,"outline") != null) {
			String outlineColor = XGMMLParser.getAttribute(graphics, "outline");
			// nodeView.setBorderPaint(outlineColor);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_BORDER_COLOR, outlineColor);
		}

		// Set border line width
		if (buildStyle && XGMMLParser.getAttribute(graphics,"width") != null) {
			String lineWidth = XGMMLParser.getAttribute(graphics,"width");
			// nodeView.setBorderWidth(lineWidth);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_LINE_WIDTH, lineWidth);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"nodeTransparency",CY_NAMESPACE) != null) {
			String opString = XGMMLParser.getAttributeNS(graphics,"nodeTransparency", CY_NAMESPACE);
			float opacity = (float)Double.parseDouble(opString)*255;
			// Opacity is saved as a float from 0-1, but internally we use 0-255
			// nodeView.setTransparency(opacity);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_OPACITY, ""+opacity);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics, "opacity", CY_NAMESPACE) != null) {
			String opString = XGMMLParser.getAttributeNS(graphics,"opacity", CY_NAMESPACE);
			float opacity = (float)Double.parseDouble(opString);
			// nodeView.setTransparency(opacity);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_OPACITY, opString);
		}

		// These are saved in the exported XGMML, but it's not clear how they get set
		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"nodeLabel", CY_NAMESPACE) != null) {
			String nodeLabel = XGMMLParser.getAttributeNS(graphics,"nodeLabel", CY_NAMESPACE);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_LABEL, nodeLabel);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"nodeLabelFont", CY_NAMESPACE) != null) {
			String nodeLabelFont = XGMMLParser.getAttributeNS(graphics,"nodeLabelFont", CY_NAMESPACE);
		
			String[] items = nodeLabelFont.split("-"); // e.g. nodeLabelFont = "Arial-0-24"
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_FONT_SIZE, items[2]);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_FONT_FACE, items[0]);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"borderLineType", CY_NAMESPACE) != null) {
			String borderLineType = XGMMLParser.getAttributeNS(graphics,"borderLineType", CY_NAMESPACE);
			graphStyle.addProperty(nodeID, VisualPropertyType.NODE_LINETYPE, borderLineType);
		}

		String type = XGMMLParser.getAttribute(graphics,"type");
		if (buildStyle && type != null) {
			if (type.equals("rhombus"))
				graphStyle.addProperty(nodeID, VisualPropertyType.NODE_SHAPE, "parallelogram");
			else
				graphStyle.addProperty(nodeID, VisualPropertyType.NODE_SHAPE, type);
		}
	}


	/**
	 * Layout edges if view is available.
	 *
	 * @param myView GINY's graph view object for the current network.
	 * @param graphStyle the visual style creator object
	 * @param buildStyle if true, build the graphical style
	 */
	private void layoutEdges(final GraphView myView, final VisualStyleBuilder graphStyle, final boolean buildStyle) {
		int tempid = 0;
		EdgeView view = null;
		HashMap<CyEdge, Attributes> edgeGraphicsMap = parser.getEdgeGraphics();
		if (edgeGraphicsMap == null) return;

		for (CyEdge edge: edgeGraphicsMap.keySet()) {
			view = myView.getEdgeView(edge.getRootGraphIndex());

			if (view != null) {
				layoutEdgeGraphics(edgeGraphicsMap.get(edge), view, graphStyle, buildStyle);
			} else {
				// If we don't have a view, assume that this is a hidden edge
				myView.getGraphPerspective().hideEdge(edge);
			}
		}
	}


	/**
	 * Layout an edge using the stored graphics attributes
	 *
	 * @param graphics
	 *            Graphics information for an edge as SAX attributes.
	 * @param edgeView
	 *            Actual edge view for the target edge.
	 *
	 */
	private void layoutEdgeGraphics(final Attributes graphics, final EdgeView edgeView, final VisualStyleBuilder graphStyle,
	                                final boolean buildStyle) {
		CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
		String edgeID = edgeView.getEdge().getIdentifier();

		if (buildStyle && XGMMLParser.getAttribute(graphics, "width") != null) {
			String lineWidth = XGMMLParser.getAttribute(graphics, "width");
			// edgeView.setStrokeWidth(lineWidth);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_LINE_WIDTH, lineWidth);
		}

		if (buildStyle && XGMMLParser.getAttribute(graphics, "fill") != null) {
			String edgeColor = XGMMLParser.getAttribute(graphics, "fill");
			// edgeView.setUnselectedPaint(edgeColor);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_COLOR, edgeColor);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics, "sourceArrow", CY_NAMESPACE) != null) {
			Integer arrowType = XGMMLParser.getIntegerAttributeNS(graphics, "sourceArrow", CY_NAMESPACE);
			ArrowShape shape = ArrowShape.getArrowShape(arrowType);
			String arrowName = shape.getName();
			// edgeView.setSourceEdgeEnd(arrowType);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_SRCARROW_SHAPE, arrowName);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics, "targetArrow", CY_NAMESPACE) != null) {
			Integer arrowType = XGMMLParser.getIntegerAttributeNS(graphics, "targetArrow", CY_NAMESPACE);
			ArrowShape shape = ArrowShape.getArrowShape(arrowType);
			String arrowName = shape.getName();
			// edgeView.setTargetEdgeEnd(arrowType);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_TGTARROW_SHAPE, arrowName);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics, "sourceArrowColor", CY_NAMESPACE) != null) {
			String arrowColor = XGMMLParser.getAttributeNS(graphics, "sourceArrowColor", CY_NAMESPACE);
			// edgeView.setSourceEdgeEndPaint(arrowColor);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_SRCARROW_COLOR, arrowColor);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"targetArrowColor", CY_NAMESPACE) != null) {
			String arrowColor = XGMMLParser.getAttributeNS(graphics, "targetArrowColor", CY_NAMESPACE);
			// edgeView.setTargetEdgeEndPaint(arrowColor);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_TGTARROW_COLOR, arrowColor);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics, "edgeLineType", CY_NAMESPACE) != null) {
			String value = XGMMLParser.getAttributeNS(graphics, "edgeLineType", CY_NAMESPACE);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_LINE_STYLE, value);
		}

		if (XGMMLParser.getAttributeNS(graphics, "curved", CY_NAMESPACE) != null) {
			String value = XGMMLParser.getAttributeNS(graphics, "curved", CY_NAMESPACE);
			if (value.equals("STRAIGHT_LINES")) {
				edgeView.setLineType(EdgeView.STRAIGHT_LINES);
			} else if (value.equals("CURVED_LINES")) {
				edgeView.setLineType(EdgeView.CURVED_LINES);
			}
		}

	 	if (XGMMLParser.getAttribute(graphics,"edgeHandleList") != null) {
			String handles[] = XGMMLParser.getAttribute(graphics, "edgeHandleList").split(";");
			List<Point2D> bendPoints = new ArrayList<Point2D>();
			for (int i = 0; i < handles.length; i++) {
				String points[] = handles[i].split(",");
				double x = (new Double(points[0])).doubleValue();
				double y = (new Double(points[1])).doubleValue();
				Point2D.Double point = new Point2D.Double();
				point.setLocation(x,y);
				bendPoints.add(point);
			}
			edgeView.getBend().setHandles(bendPoints);
		}

		// These are saved in the exported XGMML, but it's not clear how they get set
		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"edgeLabel", CY_NAMESPACE) != null) {
			String nodeLabel = XGMMLParser.getAttributeNS(graphics,"edgeLabel", CY_NAMESPACE);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_LABEL, nodeLabel);
		}

		if (buildStyle && XGMMLParser.getAttributeNS(graphics,"edgeLabelFont", CY_NAMESPACE) != null) {
			String edgeLabelFont = XGMMLParser.getAttributeNS(graphics,"edgeLabelFont", CY_NAMESPACE);
		
			String[] items = edgeLabelFont.split("-"); // e.g. edgeLabelFont = "Arial-0-24"
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_FONT_SIZE, items[2]);
			graphStyle.addProperty(edgeID, VisualPropertyType.EDGE_FONT_FACE, items[0]);
		}
	}


	/**
	 *  This method does all of our postprocessing after reading and parsing the file
	 *
	 * @param network the network we've read in
	 */
	public void doPostProcessing(final CyNetwork network) {
		parser.setMetaData(network);

		// Get the view.  Note that for large networks this might be the null view
		final CyNetworkView view = Cytoscape.getNetworkView(network.getIdentifier());

		// Now that we have a network, handle the groups
		// This is done here rather than in layout because layout is
		// only called when we create a view.  For large networks,
		// we don't create views by default, but groups should still
		// exist even when we don't create the view
		Map<CyNode,List<CyNode>>groupMap = parser.getGroupMap();
		Map<CyNode,List<CyEdge>>groupEdgeMap = parser.getGroupEdgeMap();
		if (groupMap != null) {
			CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

			CyGroup newGroup = null;
			String viewer = null;

			for (CyNode groupNode : groupMap.keySet()) {
				List<CyNode> childList = groupMap.get(groupNode);
				List<CyEdge> edgeList = groupEdgeMap.get(groupNode);

				viewer = nodeAttributes.getStringAttribute(groupNode.getIdentifier(), CyGroup.GROUP_VIEWER_ATTR);
				CyGroupViewer groupViewer = CyGroupManager.getGroupViewer(viewer);
				boolean isLocal = false;
				if (nodeAttributes.hasAttribute(groupNode.getIdentifier(), CyGroup.GROUP_LOCAL_ATTR))
					isLocal = nodeAttributes.getBooleanAttribute(groupNode.getIdentifier(), CyGroup.GROUP_LOCAL_ATTR);

				CyNetwork groupNetwork = null;

				if (isLocal)
					groupNetwork = network;

				// Note that we need to leave the group node in the network so that the saved
				// location information (if there is any) can be utilized by the group viewer.
				// This means that it will be the responsibility of the group viewer to remove
				// the node if they don't want it to be visible

				// Do we already have a view?
				if (view == null || view == Cytoscape.getNullNetworkView()) {
					// No, just create the group, but don't assign a viewer
					newGroup = CyGroupManager.createGroup(groupNode, childList, null, groupNetwork);
					network.hideNode(groupNode);
				} else {
					// Yes, see if the group already exists
					newGroup = CyGroupManager.getCyGroup(groupNode);
					if (newGroup == null) {
						// No, OK so create it and pass down the viewer
						newGroup = CyGroupManager.createGroup(groupNode, childList, viewer, groupNetwork);
					} else {
						// Either the group doesn't have a viewer or it has a different viewer -- change it
						CyGroupManager.setGroupViewer(newGroup, viewer, view, false);
					}
					// If the group viewer isn't loaded, we need to hide the group node -- otherwise,
					// we let the group viewer handle it
					if (groupViewer == null)
						network.hideNode(groupNode);
				}

				// OK, make sure we have all of our inner and outer edges added
				for (CyEdge edge: edgeList) {
					if (childList.contains(edge.getSource()) && childList.contains(edge.getTarget()))
						newGroup.addInnerEdge(edge);
					else
						newGroup.addOuterEdge(edge);
				}
			}

			// If we have a view, notify the group viewer
			if (view != null && view != Cytoscape.getNullNetworkView()) {
				CyGroupManager.setGroupViewer(newGroup, viewer, view, true);
			}
		}

		if (view == null || view == Cytoscape.getNullNetworkView())
			return;
		
		// Apply visual style before updateView().
		Cytoscape.getVisualMappingManager().applyAppearances();
		
		// set view zoom
		final Double zoomLevel = parser.getGraphViewZoomLevel();

		if (zoomLevel != null)
			view.setZoom(zoomLevel.doubleValue());

		// set view center
		final Point2D center = parser.getGraphViewCenter();

		if (center != null)
			((DGraphView) view).setCenter(center.getX(), center.getY());
	}


	private class RepairBareAmpersandsInputStream extends PushbackInputStream {
		private final byte[] encodedAmpersand = new byte[]{'a', 'm', 'p', ';'};
		public RepairBareAmpersandsInputStream(InputStream in) {
			super(in);
		}

		public RepairBareAmpersandsInputStream(InputStream in, int size) {
			super(in, size);
		}


		@Override
		public int read() throws IOException {
			int c;

			c = super.read();
			if (c == (int)'&') {
				byte[] b = new byte[7];
				int cnt;

				cnt = read(b);
				if (cnt > 0) {
					boolean isEntity;
					int i;

					isEntity = false;
					i = 0;
					while ((i < cnt) && (!isEntity)) {
						isEntity = (b[i] == ';');
						i++;
					}

					byte[] pb = new byte[cnt];
					for (int p = 0; p < cnt; p++) {
						pb[p] = b[p];
					}
					unread(pb);

					if (!isEntity) {
						unread(encodedAmpersand);
					}
				}
			}

			return c;
		}


		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}

			int cnt;
			int c = -1;

			cnt = 0;
			while (cnt < len) {
				c = read();
				if (c == -1) {
					break;
				}
				b[off] = (byte)c;
				off++;
				cnt++;
			}

			if ((c == -1) && (cnt == 0)) {
				cnt = -1;
			}

			return cnt;
		}
	}
}
