// CytoscapeWindow.java:  a yfiles, GUI tool for exploring genetic networks
//------------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//------------------------------------------------------------------------------
package cytoscape;
//------------------------------------------------------------------------------
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JOptionPane;

import java.io.File;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Vector;
import java.util.Enumeration;

import y.base.*;
import y.view.*;

import y.layout.Layouter;
import y.layout.GraphLayout;

import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.HierarchicLayouter;
import y.layout.organic.OrganicLayouter;
import y.layout.random.RandomLayouter;

import y.io.YGFIOHandler;
import y.io.GMLIOHandler;

import y.algo.GraphHider;

 // printing
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import javax.print.attribute.*;
import y.option.OptionHandler; 
import y.view.Graph2DPrinter;


import cytoscape.data.*;
import cytoscape.data.readers.*;
import cytoscape.data.servers.*;
import cytoscape.dialogs.*;
import cytoscape.layout.*;
import cytoscape.vizmap.*;
//-----------------------------------------------------------------------------------
public class CytoscapeWindow extends JPanel { // implements VizChooserClient {

  protected static final int DEFAULT_WIDTH = 700;
  protected static final int DEFAULT_HEIGHT = 700;

  protected cytoscape parentApp;
  protected Graph2D graph;
  protected String geometryFilename;
  protected String expressionDataFilename;

  protected JFrame mainFrame;
  protected JMenuBar menuBar;
  protected JMenu opsMenu;
  protected JToolBar toolbar;
  protected JLabel infoLabel;


  protected Cursor defaultCursor = Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR);
  protected Cursor busyCursor = Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR);

  protected Layouter layouter;

  protected Graph2DView graphView;
  protected ViewMode editGraphMode  = new EditGraphMode ();
  protected ViewMode readOnlyGraphMode = new ReadOnlyGraphMode ();
  protected ViewMode currentGraphMode = readOnlyGraphMode;
  protected ViewMode nodeAttributesPopupMode = new NodeAttributesPopupMode ();
  protected boolean viewModesInstalled = false;

  protected BioDataServer bioDataServer;
  protected String bioDataServerName;

  protected GraphObjAttributes nodeAttributes = new GraphObjAttributes ();
  protected GraphObjAttributes edgeAttributes = new GraphObjAttributes ();

  protected NodeVizMapper nodeVizMapper;
  protected EdgeVizMapper edgeVizMapper;
  // protected WindowVizMapper windowVizMapper;


  protected ExpressionData expressionData = null;

  protected final String goModeMenuLabel = "Show GeneOntology Info";
  protected final String expressionModeMenuLabel = "Show mRNA Expression";


  // protected VizChooser theVizChooser = new VizChooser();

  protected GraphHider graphHider;
  protected Vector subwindows = new Vector ();

  protected String windowTitle;
   // selected nodes can be displayed in a new window.  this next variable
   // provides a title for that new window
  protected String titleForCurrentSelection = null;
  protected CytoscapeConfig config;

  protected JMenuItem deleteSelectionMenuItem;
//------------------------------------------------------------------------------
public CytoscapeWindow (cytoscape parentApp,
                        CytoscapeConfig config,
                        Graph2D graph, 
                        ExpressionData expressionData,
                        BioDataServer bioDataServer,
                        GraphObjAttributes nodeAttributes,
                        GraphObjAttributes edgeAttributes,
                        String geometryFilename,
                        String expressionDataFilename,
                        String title,
                        boolean doFreshLayout)
   throws Exception
{
  this.parentApp = parentApp;
  this.graph = graph;
  this.geometryFilename = geometryFilename;
  this.expressionDataFilename = expressionDataFilename;
  this.bioDataServer = bioDataServer;
  this.expressionData = expressionData;

  if (nodeAttributes != null) 
    this.nodeAttributes = nodeAttributes;

  if (edgeAttributes != null)
    this.edgeAttributes = edgeAttributes;

  NodeVizMapperPropertiesAdapter nodePropsReader = 
       new NodeVizMapperPropertiesAdapter (config.getProperties ());
  nodeVizMapper = nodePropsReader.createNodeVizMapper ();

  EdgeVizMapperPropertiesAdapter edgePropsReader = 
      new EdgeVizMapperPropertiesAdapter (config.getProperties ());
  edgeVizMapper = edgePropsReader.createEdgeVizMapper ();

  if (title == null)
    this.windowTitle = "";
  else
    this.windowTitle = title;
  this.config = config;

  initializeWidgets ();

  displayCommonNodeNames ();
  displayGraph (doFreshLayout);

  mainFrame.setVisible (true);
  mainFrame.addWindowListener (parentApp);

    // load plugins last, after the main window is setup, since they will
    // often need access to all of the parts of a fully instantiated CytoscapeWindow

  PluginLoader pluginLoader = new PluginLoader (this, config, nodeAttributes, edgeAttributes);
  pluginLoader.load ();

} // ctor
//------------------------------------------------------------------------------
public void windowStateChanged (WindowEvent e)
{
  System.out.println ("--- windowStateChanged: " + e);
}
//------------------------------------------------------------------------------
public Graph2D getGraph ()
{  
  return graph;
}
//------------------------------------------------------------------------------
public void redrawGraph ()
{
  graphView.paintImmediately (0, 0, graphView.getWidth(), graphView.getHeight());
  int nodeCount = graphView.getGraph2D().nodeCount();
  int edgeCount = graphView.getGraph2D().edgeCount();
  infoLabel.setText ("  Nodes: " + nodeCount + " Edges: " + edgeCount);

} // redrawGraph
//------------------------------------------------------------------------------
public JFrame getMainFrame ()
{
  return mainFrame;
}
//------------------------------------------------------------------------------
public JMenuBar getMenuBar ()
{
  return menuBar;
}
//------------------------------------------------------------------------------
public void setMenuBar (JMenuBar newMenuBar)
{
  mainFrame.setJMenuBar (newMenuBar);
}
//------------------------------------------------------------------------------
public JToolBar getToolBar ()
{
  return toolbar;
}
//------------------------------------------------------------------------------
public ExpressionData getExpressionData ()
{
  return expressionData;
}
//------------------------------------------------------------------------------
public NodeVizMapper getNodeVizMapper ()
{
  return nodeVizMapper;
}
//------------------------------------------------------------------------------
public EdgeVizMapper getEdgeVizMapper ()
{
  return edgeVizMapper;
}
//------------------------------------------------------------------------------
protected void initializeWidgets ()
{
  setLayout (new BorderLayout ());  
  graphView = new Graph2DView ();
  add (graphView, BorderLayout.CENTER);
  graphView.setPreferredSize (new Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT));

  toolbar = createToolBar ();
  add (toolbar, BorderLayout.NORTH);

  infoLabel = new JLabel ();
  add (infoLabel, BorderLayout.SOUTH);

  mainFrame = new JFrame (windowTitle);
    
  mainFrame.setJMenuBar (createMenuBar ());
  mainFrame.setContentPane (this);
  mainFrame.pack ();
  setInteractivity (true);

} // initializeWidgets
//------------------------------------------------------------------------------
public String getCanonicalNodeName (Node node)
{
  return nodeAttributes.getCanonicalName (node);

} // getCanonicalNodeName
//------------------------------------------------------------------------------
public void displayGraph (boolean doLayout)
{
  if (graph == null) graph = new Graph2D ();

  OrganicLayouter ol = new OrganicLayouter ();
  ol.setActivateDeterministicMode (true);
  ol.setPreferredEdgeLength (80);
  layouter = ol;
  graphView.setGraph2D (graph);
  graphHider = new GraphHider (graph);

  if (doLayout) {
    applyLayout (false);
    }

  graphView.fitContent ();
  graphView.setZoom (graphView.getZoom ()*0.9);

  redrawGraph ();

} // displayGraph
//------------------------------------------------------------------------------
public void renderNodesAndEdges ()
{
  Node [] nodes = graphView.getGraph2D().getNodeArray();

  for (int i=0; i < nodes.length; i++) {
    Node node = nodes [i];
    String canonicalName = nodeAttributes.getCanonicalName (node);
    HashMap bundle = nodeAttributes.getAttributes (canonicalName);
    Color nodeColor = nodeVizMapper.getNodeFillColor (bundle);
    NodeRealizer nr = graphView.getGraph2D().getRealizer(node);
    nr.setFillColor (nodeColor);
    } // for i

  EdgeCursor cursor = graphView.getGraph2D().edges();
  cursor.toFirst ();

  for (int i=0; i < cursor.size (); i++) {
    Edge edge = cursor.edge ();
    String canonicalName = edgeAttributes.getCanonicalName (edge);
    HashMap bundle = edgeAttributes.getAttributes (canonicalName);
    Color color = edgeVizMapper.getEdgeColor (bundle);
    EdgeRealizer er = graphView.getGraph2D().getRealizer(edge);
    String sourceDecoration = edgeVizMapper.getSourceDecoration (bundle);
    if (sourceDecoration.equals ("arrow"))
       er.setSourceArrow (Arrow.STANDARD);
    String targetDecoration = edgeVizMapper.getTargetDecoration (bundle);
    if (targetDecoration.equals ("arrow"))
       er.setTargetArrow (Arrow.STANDARD);
    er.setLineColor (color);
    cursor.cyclicNext ();
    } // for i

  redrawGraph ();

} // renderNodesAndEdges
//------------------------------------------------------------------------------
public Node getNode (String canonicalNodeName)
{
  Node [] nodes = graphView.getGraph2D().getNodeArray();
  for (int i=0; i < nodes.length; i++) {
    Node node = nodes [i];
    String canonicalName = nodeAttributes.getCanonicalName (node);
    System.out.println (" -- checking " + canonicalNodeName + " against " + canonicalName + " " + node);
    if (canonicalNodeName.equals (canonicalName)) 
      return node;
    }

  return null;
  
} // getNode
//------------------------------------------------------------------------------
public GraphObjAttributes getNodeAttributes ()
{
  return nodeAttributes;
}
//------------------------------------------------------------------------------
public GraphObjAttributes getEdgeAttributes ()
{
  return edgeAttributes;
}
//------------------------------------------------------------------------------
/**
 * 
 */
protected void displayCommonNodeNames ()
{
  if (bioDataServer == null) return;

  Node [] nodes = graph.getNodeArray ();

  for (int i=0; i < nodes.length; i++) {
    Node node = nodes [i];
    NodeRealizer r = graphView.getGraph2D().getRealizer(node);
    String defaultName = r.getLabelText ();
    String newName = defaultName;
    try {
      String [] synonyms = bioDataServer.getSynonyms (defaultName);
      if (synonyms.length > 0) {
        newName = synonyms [0];
        }
      nodeAttributes.add ("commonName", defaultName, newName);
      r.setLabelText (newName);
      }
    catch (Exception ignoreForNow) {;}
    } // for i

} // displayCommonNodeNames
//------------------------------------------------------------------------------
public JMenu getOperationsMenu ()
{
  return opsMenu;
}
//------------------------------------------------------------------------------
public void setInteractivity (boolean newState)
{
  if (newState == true) { // turn interactivity ON
    if (!viewModesInstalled) {
      graphView.addViewMode (currentGraphMode);
      graphView.addViewMode (nodeAttributesPopupMode);
      viewModesInstalled = true;
      }
    graphView.setViewCursor (defaultCursor);
    setCursor (defaultCursor); 
    }
  else {  // turn interactivity OFF
    if (viewModesInstalled) {
      graphView.removeViewMode (currentGraphMode);
      graphView.removeViewMode (nodeAttributesPopupMode); 
      viewModesInstalled = false;
      }
    graphView.setViewCursor (busyCursor);
    setCursor (busyCursor);
    }

} // setInteractivity
//------------------------------------------------------------------------------
protected JMenuBar createMenuBar ()
{
  menuBar = new JMenuBar ();
  JMenu fileMenu = new JMenu ("File");

  JMenu loadSubMenu = new JMenu ("Load");
  fileMenu.add (loadSubMenu);
  JMenuItem mi = loadSubMenu.add (new LoadGMLFileAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_G, ActionEvent.CTRL_MASK));
  mi = loadSubMenu.add (new LoadInteractionFileAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_I, ActionEvent.CTRL_MASK));
  mi = loadSubMenu.add (new LoadExpressionMatrixAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_E, ActionEvent.CTRL_MASK));

  fileMenu.add (new SaveAsGMLAction ());
  fileMenu.add (new PrintAction ());

  mi = fileMenu.add (new CloseWindowAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_W, ActionEvent.CTRL_MASK));
  mi = fileMenu.add (new ExitAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

  menuBar.add (fileMenu);

  JMenu editMenu = new JMenu ("Edit");
  menuBar.add (editMenu);

  //JMenu modeMenu = new JMenu ("Mode");
  //menuBar.add (modeMenu);
  ButtonGroup modeGroup = new ButtonGroup ();
  JRadioButtonMenuItem readOnlyModeButton = new JRadioButtonMenuItem ("Read-only mode");
  JRadioButtonMenuItem editModeButton = new JRadioButtonMenuItem ("Edit mode for nodes and edges");
  modeGroup.add (readOnlyModeButton);
  modeGroup.add (editModeButton);
  //modeMenu.add (readOnlyModeButton);
  //modeMenu.add (editModeButton);
  editMenu.add (readOnlyModeButton);
  editMenu.add (editModeButton);
  readOnlyModeButton.setSelected (true);
  readOnlyModeButton.addActionListener (new ReadOnlyModeAction ());
  editModeButton.addActionListener (new EditModeAction ());
  editMenu.addSeparator ();

  deleteSelectionMenuItem = editMenu.add (new DeleteSelectedAction ());
  deleteSelectionMenuItem.setEnabled (false);


  JMenu selectiveDisplayMenu = new JMenu ("Select");
  selectiveDisplayMenu.setToolTipText ("Select nodes by different criteria");
  menuBar.add (selectiveDisplayMenu);
  selectiveDisplayMenu.add (new DeselectAllAction ());

  JMenu layoutMenu = new JMenu ("Layout");
  layoutMenu.setToolTipText ("Apply new layout algorithm to graph");
  menuBar.add (layoutMenu);
  layoutMenu.add (new CircularLayoutAction ());
  layoutMenu.add (new HierarchicalLayoutAction ());
  layoutMenu.add (new OrganicLayoutAction ());
  layoutMenu.add (new RandomLayoutAction ());
  // layoutMenu.add (new GroupWiseLayoutAction ());
  layoutMenu.add (new LayoutAction ());

  opsMenu = new JMenu ("Ops"); // always create the ops menu
  menuBar.add (opsMenu);

  selectiveDisplayMenu.add (new AlphabeticalSelectionAction ());
  mi = selectiveDisplayMenu.add (new DisplaySelectedInNewWindowAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_S, ActionEvent.CTRL_MASK));
  
  mi = selectiveDisplayMenu.add (new DisplayAttributesOfSelectedNodesAction ());
  mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_D, ActionEvent.CTRL_MASK));
  
  return menuBar;

} // createMenuBar
//------------------------------------------------------------------------------
protected JToolBar createToolBar ()
{
  JToolBar bar = new JToolBar ();
  bar.add (new ZoomAction (0.9));
  bar.add (new ZoomAction (1.1));
  bar.add (new ZoomSelectedAction ());
  bar.add (new FitContentAction ());
  bar.add (new ShowAllAction ());
  bar.add (new RenderAction ());

  bar.addSeparator ();
  // bar.add (new AppearanceControllerLauncherAction (nodeAttributes, edgeAttributes));
    
  return bar;

} // createToolBar
//------------------------------------------------------------------------------
public void selectNodesByName (String [] nodeNames)
{
  boolean clearAllSelectionsFirst = true;
  selectNodesByName (nodeNames, clearAllSelectionsFirst);
}
//------------------------------------------------------------------------------
/**
 * hide every node except those explicitly named.  canonical node names must
 * be used.
 */
protected void showNodesByName (String [] nodeNames)
{
  Graph2D g = graphView.getGraph2D ();
  graphHider.unhideAll ();
  Node [] nodes = graphView.getGraph2D().getNodeArray();

  for (int i=0; i < nodes.length; i++) {
    boolean matched = false;
    String graphNodeName = getCanonicalNodeName (nodes [i]);
    for (int n=0; n < nodeNames.length; n++) {
      if (nodeNames [n].equalsIgnoreCase (graphNodeName)) {
        matched = true;
        break;
        } // if equals
       } // for n
     if (!matched) 
       graphHider.hide (nodes [i]);
    } // for i

  redrawGraph ();

} // showNodesByName
//------------------------------------------------------------------------------
/**
 * a Vector version of showNodesByName
 */ 
public void showNodesByName (Vector uniqueNodeNames)
{
  showNodesByName ((String []) uniqueNodeNames.toArray (new String [0]));

} // showNodesByName (Vector)
//------------------------------------------------------------------------------
public void selectNodesByName (String [] nodeNames, boolean clearAllSelectionsFirst)
{
  Graph2D g = graphView.getGraph2D();
  Node [] nodes = graphView.getGraph2D().getNodeArray();

  for (int i=0; i < nodes.length; i++) {
    String graphNodeName = getCanonicalNodeName (nodes [i]);
    NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
    boolean matched = false;
    for (int n=0; n < nodeNames.length; n++)
      if (nodeNames [n].equalsIgnoreCase (graphNodeName)) {
        matched = true;
        break;
        }
    if (clearAllSelectionsFirst && !matched)
      nodeRealizer.setSelected (false);
    else if (matched)
      nodeRealizer.setSelected (true);
    } // for i

  redrawGraph ();

} // selectNodesByName
//------------------------------------------------------------------------------
public void deselectAllNodes ()
{
  Graph2D g = graphView.getGraph2D();
  Node [] nodes = graphView.getGraph2D().getNodeArray();

  for (int i=0; i < nodes.length; i++) {
    NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
    nodeRealizer.setSelected (false);
    } // for i

  redrawGraph ();

} // deselectAllNodes
//------------------------------------------------------------------------------
protected void selectNodesStartingWith (String key)
{
  setInteractivity (false);
  key = key.toLowerCase ();
  Graph2D g = graphView.getGraph2D();
  redrawGraph ();

  Node [] nodes = graphView.getGraph2D().getNodeArray();

  for (int i=0; i < nodes.length; i++) {
    String nodeName = graphView.getGraph2D().getLabelText (nodes [i]);
    boolean matched = false;
    if (nodeName.toLowerCase().startsWith (key))
      matched = true;
    else if (bioDataServer != null) {
      try {
        String [] synonyms = bioDataServer.getSynonyms (nodeName);
        for (int s=0; s < synonyms.length; s++)
          if (synonyms [s].toLowerCase().startsWith (key)) {
            matched = true;
            break;
         } // for s
       }
      catch (Exception ignoreForNow) {;}
      } // else if: checking synonyms
    setNodeSelected (nodes [i], matched);
    } // for i

  setInteractivity (true);
  redrawGraph ();

} // selectDisplyToNodesStartingWith ...
//------------------------------------------------------------------------------
protected void setNodeSelected (Node node, boolean visible)
{
  NodeRealizer r = graphView.getGraph2D().getRealizer(node);
  r.setSelected (visible);

} // setNodeSelected
//------------------------------------------------------------------------------
protected void updateEdgeVisibilityFromNodeVisibility ()
{
  for (EdgeCursor ec = graphView.getGraph2D().edges(); ec.ok(); ec.next()) {
    Edge e = ec.edge ();
    Node source = e.source ();
    boolean edgeShouldBeVisible = false;
    if (graphView.getGraph2D().getRealizer(source).isVisible ()) {
      Node target = e.target ();
      if (graphView.getGraph2D().getRealizer(target).isVisible ()) {
        edgeShouldBeVisible = true;
        } // if target node is visible
      } // if source node is visible
    EdgeRealizer er = graphView.getGraph2D().getRealizer(e);
    er.setVisible (edgeShouldBeVisible);
    } // for each edge

} // updateEdgeVisibilityFromNodeVisibility
//------------------------------------------------------------------------------
public void applyLayout (boolean animated)
{
  System.out.print ("starting layout...");  System.out.flush ();
  setInteractivity (false);
  layouter.doLayout (graphView.getGraph2D ());

  setInteractivity (true);
  System.out.println (" done");

} // applyLayout
//------------------------------------------------------------------------------
class PrintAction extends AbstractAction 
{
  PageFormat pageFormat;
  OptionHandler printOptions;
    
  PrintAction () {
    super ("Print");
    printOptions = new OptionHandler ("Print Options");
    printOptions.addInt ("Poster Rows",1);
    printOptions.addInt ("Poster Columns",1);
    printOptions.addBool ("Add Poster Coords",false);
    final String[] area = {"View","Graph"};
    printOptions.addEnum ("Clip Area",area,1);
    }

  public void actionPerformed (ActionEvent e) {

    Graph2DPrinter gprinter = new Graph2DPrinter (graphView);
    PrinterJob printJob = PrinterJob.getPrinterJob ();
    if (pageFormat == null) pageFormat = printJob.defaultPage ();
    printJob.setPrintable (gprinter, pageFormat);
      
    if (printJob.printDialog ()) try {
      printJob.print ();  
      }
    catch (Exception ex) {
      ex.printStackTrace ();
      }
    } // actionPerformed

} // inner class PrintAction
//------------------------------------------------------------------------------
protected class RenderAction extends AbstractAction  
{
   RenderAction () {super ("Render"); } 
   public void actionPerformed (ActionEvent e) {
     renderNodesAndEdges ();
     }

} // inner class RenderAction
//------------------------------------------------------------------------------
protected class DeleteSelectedAction extends AbstractAction   {
  DeleteSelectedAction () { super ("Delete Selected Nodes and Edges"); }

  public void actionPerformed (ActionEvent e) {
    Graph2D g = graphView.getGraph2D ();
    NodeCursor nc = g.selectedNodes (); 
    while (nc.ok ()) {
      Node node = nc.node ();
      g.removeNode (node);
      nc.next ();
      } // while
    EdgeCursor ec = g.selectedEdges ();
    while (ec.ok ()) {
      g.removeEdge (ec.edge ());
      ec.next ();
      }
    redrawGraph ();
    } // actionPerformed
  

} // inner class DeleteSelectedAction
//------------------------------------------------------------------------------
protected class LayoutAction extends AbstractAction   {
  LayoutAction () { super ("Refresh"); }
    
  public void actionPerformed (ActionEvent e) {
    applyLayout (false);
    redrawGraph ();
    }
}
//------------------------------------------------------------------------------
protected class CircularLayoutAction extends AbstractAction   {
  CircularLayoutAction () { super ("Circular"); }
    
  public void actionPerformed (ActionEvent e) {
    layouter = new CircularLayouter ();
    applyLayout (false);
    redrawGraph ();
    }
}
//------------------------------------------------------------------------------
protected class HierarchicalLayoutAction extends AbstractAction   {
  HierarchicalLayoutAction () { super ("Hierarchical"); }
    
  public void actionPerformed (ActionEvent e) {
    HierarchicLayouter hl = new HierarchicLayouter ();
    hl.setMinimalLayerDistance (40);
    hl.setMinimalNodeDistance (20);
    layouter = hl;
    applyLayout (false);
    redrawGraph ();
    }
}
//------------------------------------------------------------------------------
protected class OrganicLayoutAction extends AbstractAction   {
  OrganicLayoutAction () { super ("Organic"); }
    
  public void actionPerformed (ActionEvent e) {
    OrganicLayouter ol = new OrganicLayouter ();
    ol.setActivateDeterministicMode (true);
    ol.setPreferredEdgeLength(80);
    layouter = ol;
    applyLayout (false);
    redrawGraph ();
    }
}
//------------------------------------------------------------------------------
protected class RandomLayoutAction extends AbstractAction   {
  RandomLayoutAction () { super ("Random"); }
    
  public void actionPerformed (ActionEvent e) {
    layouter = new RandomLayouter ();
    applyLayout (false);
    redrawGraph ();
    }
}
//-----------------------------------------------------------------------------
protected class AlphabeticalSelectionAction extends AbstractAction   {
  AlphabeticalSelectionAction () { super ("By Name"); }

  public void actionPerformed (ActionEvent e) {
    String answer = 
      (String) JOptionPane.showInputDialog (mainFrame, 
              "Select nodes whose name (or synonym) starts with");
    if (answer != null && answer.length () > 0)
      selectNodesStartingWith (answer.trim ());
    }
}
//------------------------------------------------------------------------------
protected class DeselectAllAction extends AbstractAction   {
  DeselectAllAction () { super ("Deselect All"); }

  public void actionPerformed (ActionEvent e) {
    deselectAllNodes ();
    }
}
//------------------------------------------------------------------------------
protected class ReadOnlyModeAction extends AbstractAction   {
  ReadOnlyModeAction () { super ("Read only Mode"); }

  public void actionPerformed (ActionEvent e) {
    graphView.removeViewMode (currentGraphMode);
    currentGraphMode = readOnlyGraphMode;
    graphView.addViewMode (currentGraphMode);
    deleteSelectionMenuItem.setEnabled (false);
    }
}
//------------------------------------------------------------------------------
protected class EditModeAction extends AbstractAction   {
  EditModeAction () { super ("Edit Mode for Nodes and Edges"); }

  public void actionPerformed (ActionEvent e) {
    graphView.removeViewMode (currentGraphMode);
    currentGraphMode = editGraphMode;
    graphView.addViewMode (currentGraphMode);
    deleteSelectionMenuItem.setEnabled (true);
    }
}
//------------------------------------------------------------------------------
/**
 * a temporary debugging aid (pshannon, mar 2002): all attributes of the 
 * selected nodes are printed to stdout.
 */
protected class DisplayAttributesOfSelectedNodesAction extends AbstractAction {
  DisplayAttributesOfSelectedNodesAction () { super ("DEBUG: display attributes of selected nodes"); }
  public void actionPerformed (ActionEvent e) {
    Graph2D g = graphView.getGraph2D ();
    NodeCursor nc = g.selectedNodes (); 
    for (nc.toFirst (); nc.ok (); nc.next ()) { // get the canonical name of the old node
      String canonicalName = nodeAttributes.getCanonicalName (nc.node ());
      System.out.println (canonicalName + ": " + nodeAttributes.getAttributes (canonicalName));
      } // for
    }
}
//------------------------------------------------------------------------------
protected class DisplaySelectedInNewWindowAction extends AbstractAction   {
  DisplaySelectedInNewWindowAction () { super ("Display Selected Nodes in New Window"); }

  public void actionPerformed (ActionEvent e) {
    SelectedSubGraphFactory factory = new SelectedSubGraphFactory (graph, nodeAttributes, edgeAttributes);
    Graph2D subGraph = factory.getSubGraph ();
    GraphObjAttributes newNodeAttributes = factory.getNodeAttributes ();
    GraphObjAttributes newEdgeAttributes = factory.getEdgeAttributes ();

    String title = "selection";
    if (titleForCurrentSelection != null) 
      title = titleForCurrentSelection;
    try {
      boolean requestFreshLayout = true;
      CytoscapeWindow newWindow =
          new CytoscapeWindow  (parentApp, config, subGraph, expressionData, 
                                bioDataServer, newNodeAttributes, newEdgeAttributes, 
                                "dataSourceName", expressionDataFilename, title, 
                                requestFreshLayout);
      subwindows.add (newWindow);  
      }
    catch (Exception e00) {
      System.err.println ("exception when creating new window");
      e00.printStackTrace ();
      }

    } // actionPerformed

} // inner class DisplaySelectedInNewWindowAction
//------------------------------------------------------------------------------
class SelectedSubGraphMaker {

  Graph2D parentGraph;
  Graph2D subGraph;
  GraphObjAttributes parentNodeAttributes, parentEdgeAttributes;
  GraphObjAttributes newNodeAttributes, newEdgeAttributes;
  HashMap parentNameMap = new HashMap ();  // maps from commonName to canonicalName

  SelectedSubGraphMaker (Graph2D parentGraph, GraphObjAttributes nodeAttributes,
                         GraphObjAttributes edgeAttributes) {

    this.parentGraph = parentGraph;
    this.parentNodeAttributes = nodeAttributes;
    this.parentEdgeAttributes = edgeAttributes;

    NodeCursor nc = parentGraph.selectedNodes (); 

    for (nc.toFirst (); nc.ok (); nc.next ()) { 
      String canonicalName = parentNodeAttributes.getCanonicalName (nc.node ());
      if (canonicalName != null) {
        String commonName = (String) parentNodeAttributes.getValue ("commonName", canonicalName);
        if (commonName != null) 
           parentNameMap.put (commonName, canonicalName);
        } // if
      } // for nc

    EdgeCursor ec = parentGraph.selectedEdges (); 

    nc.toFirst ();
    subGraph = new Graph2D (parentGraph, nc);
    Node [] newNodes = subGraph.getNodeArray ();
    System.out.println ("nodes in new subgraph: " + newNodes.length);

    newNodeAttributes = (GraphObjAttributes) parentNodeAttributes.clone ();
    newNodeAttributes.clearNameMap ();
    
    for (int i=0; i < newNodes.length; i++) {
      Node newNode = newNodes [i];
      String commonName = subGraph.getLabelText (newNode);
      String canonicalName = (String) parentNameMap.get (commonName);
      NodeRealizer r = subGraph.getRealizer (newNode);
      r.setLabelText (canonicalName);
      newNodeAttributes.addNameMapping (canonicalName, newNode);
      System.out.println (" new graph, commonName: " + commonName + "   canonical: " + canonicalName); 
      }

    newEdgeAttributes = (GraphObjAttributes) parentEdgeAttributes.clone ();

    } // ctor

    Graph2D getSubGraph () { return subGraph; }
    GraphObjAttributes getNodeAttributes () { return newNodeAttributes; }
    GraphObjAttributes getEdgeAttributes () { return newEdgeAttributes; }

} // inner class SelectedSubGraphMaker
//------------------------------------------------------------------------------
protected class ShowConditionAction extends AbstractAction   {
  String conditionName;
  ShowConditionAction (String conditionName) { 
    super (conditionName);
    this.conditionName = conditionName;
    }

  public void actionPerformed (ActionEvent e) {
    System.out.println ("show " + conditionName);
    // displayNodesWithExpressionValues (conditionName);
    }
}
//------------------------------------------------------------------------------
protected void loadGML (String filename)
{
  GMLIOHandler ioh = new GMLIOHandler ();
  ioh.read (graphView.getGraph2D (), filename);
  graph = graphView.getGraph2D ();
  displayGraph (false);

} // loadGML
//------------------------------------------------------------------------------
protected void loadInteraction (String filename)
{
  InteractionsReader reader = new InteractionsReader (filename);
  reader.read ();
  graph = reader.getGraph ();
  edgeAttributes.add (reader.getEdgeAttributes ());
  displayGraph (true);

} // loadInteraction
//------------------------------------------------------------------------------
protected class ExitAction extends AbstractAction  {
  ExitAction () { super ("Exit"); }

  public void actionPerformed (ActionEvent e) {
    parentApp.exit ();
  }
}
//------------------------------------------------------------------------------
protected class CloseWindowAction extends AbstractAction  {
  CloseWindowAction () { super ("Close"); }

  public void actionPerformed (ActionEvent e) {
    mainFrame.dispose ();
  }
}
//------------------------------------------------------------------------------
protected class SaveAsGMLAction extends AbstractAction  
{
  SaveAsGMLAction () {super ("Save As GML..."); }
  public void actionPerformed (ActionEvent e) {
    File currentDirectory = new File (System.getProperty ("user.dir"));
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showSaveDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
      String name = chooser.getSelectedFile ().toString ();
      if (!name.endsWith (".gml")) name = name + ".gml";
      GMLIOHandler ioh = new GMLIOHandler ();
      ioh.write (graphView.getGraph2D (),name);
      } // if
    }

} // SaveAsAction
//------------------------------------------------------------------------------
protected class LoadGMLFileAction extends AbstractAction {
  LoadGMLFileAction () { super ("GML..."); }
    
  public void actionPerformed (ActionEvent e)  {
   File currentDirectory = new File (System.getProperty ("user.dir"));
   JFileChooser chooser = new JFileChooser (currentDirectory);
   if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
      String name = chooser.getSelectedFile ().toString ();
      geometryFilename = name;
      loadGML (name);
      } // if
    } // actionPerformed

} // inner class LoadAction
//------------------------------------------------------------------------------
protected class LoadInteractionFileAction extends AbstractAction {
  LoadInteractionFileAction() { super ("Interaction..."); }
    
  public void actionPerformed (ActionEvent e)  {
   File currentDirectory = new File (System.getProperty ("user.dir"));
   JFileChooser chooser = new JFileChooser (currentDirectory);
   if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
      String name = chooser.getSelectedFile ().toString ();
      loadInteraction (name);
      } // if
    } // actionPerformed

} // inner class LoadAction
//------------------------------------------------------------------------------
protected class LoadExpressionMatrixAction extends AbstractAction {
  LoadExpressionMatrixAction () { super ("Expression Matrix File..."); }
    
  public void actionPerformed (ActionEvent e)  {
   File currentDirectory = new File (System.getProperty ("user.dir"));
   JFileChooser chooser = new JFileChooser (currentDirectory);
   if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
      expressionDataFilename = chooser.getSelectedFile ().toString ();
      expressionData = new ExpressionData (expressionDataFilename);
      // incorporateExpressionData ();
      } // if
    } // actionPerformed

} // inner class LoadExpressionMatrix
//------------------------------------------------------------------------------
protected class DeleteSelectionAction extends AbstractAction {
  DeleteSelectionAction () { super ("Delete Selection"); }
  public void actionPerformed (ActionEvent e) {
    graphView.getGraph2D ().removeSelection ();
  redrawGraph ();
    }
  }
//------------------------------------------------------------------------------
protected class ZoomAction extends AbstractAction {
  double factor;
  ZoomAction (double factor) {
    super ("Zoom " +  (factor > 1.0 ? "In" : "Out"));
    this.factor = factor;
    }
    
  public void actionPerformed (ActionEvent e) {
    graphView.setZoom (graphView.getZoom ()*factor);
  redrawGraph ();
    }
  }
//------------------------------------------------------------------------------
protected class FitContentAction extends AbstractAction  {
   FitContentAction () { super ("Fit Content"); }
    public void actionPerformed (ActionEvent e) {
      graphView.fitContent ();
  redrawGraph ();
      }
}
//------------------------------------------------------------------------------
protected class ShowAllAction extends AbstractAction  {
   ShowAllAction () { super ("Show All"); }
    public void actionPerformed (ActionEvent e) {
      graphHider.unhideAll ();
      graphView.fitContent ();
      graphView.setZoom (graphView.getZoom ()*0.9);
  redrawGraph ();
      }
}
//------------------------------------------------------------------------------
protected class ZoomSelectedAction extends AbstractAction  {
  ZoomSelectedAction ()  { super ("Zoom Selected"); }
  public void actionPerformed (ActionEvent e) {
    Graph2D g = graphView.getGraph2D ();
    NodeCursor nc = g.selectedNodes (); 
    if (nc.ok ()) { //selected nodes present? 
       Rectangle2D box = g.getRealizer (nc.node ()).getBoundingBox ();
       for (nc.next (); nc.ok (); nc.next ())
        g.getRealizer (nc.node ()).calcUnionRect (box);
        graphView.zoomToArea (box.getX(),box.getY(),box.getWidth(),box.getHeight());
        if (graphView.getZoom () > 2.0) graphView.setZoom (2.0);
        redrawGraph ();
      }
    }
}
//------------------------------------------------------------------------------
protected class CursorTesterAction extends AbstractAction  {
   boolean busy = false;
   CursorTesterAction () {
     super ("Cursor test"); 
     }
   public void actionPerformed (ActionEvent e) {
     if (busy)
       setInteractivity (true);
     else
       setInteractivity (false);
     busy = !busy;
     }

} // CursorTester
//------------------------------------------------------------------------------
class EditGraphMode extends EditMode {
  EditGraphMode () { 
   super (); 
   allowNodeCreation (true);
   allowEdgeCreation (true);
   allowBendCreation (true);
   showNodeTips (true);
   showEdgeTips (true);
   System.out.println ("EditGraphMode, createEdgeMode: " + getCreateEdgeMode ());
   // setCreateEdgeMode (new CreateEdgeMode ());
   System.out.println ("EditGraphMode, createEdgeMode: " + getCreateEdgeMode ());
   }
  protected String getNodeTip (Node node) {
    String geneName = graphView.getGraph2D().getRealizer(node).getLabelText();
    String canonicalName = getCanonicalNodeName (node);
    if (canonicalName != null && canonicalName.length () > 0 && !canonicalName.equals (geneName))
      return geneName + " " + canonicalName;
    return geneName;
    } // getNodeTip

  protected void nodeCreated (Node newNode) {
    String defaultName = graphView.getGraph2D().getLabelText (newNode);
    HashMap nodeAttributeBundle = configureNewNode (newNode);
    String commonNameKey = "commonName";
    String commonName = defaultName;
    if (nodeAttributeBundle.containsKey (commonNameKey)) {
      commonName = (String) nodeAttributeBundle.get (commonNameKey);
      NodeRealizer r = graphView.getGraph2D().getRealizer(newNode);
      r.setLabelText (commonName);
      }
    String canonicalName = (String) nodeAttributeBundle.get ("canonicalName");
    if (canonicalName == null || canonicalName.length () < 1)
      canonicalName = commonName;

    if (canonicalName == null || canonicalName.length () < 1)
      canonicalName = defaultName;
   
    nodeAttributes.add (canonicalName, nodeAttributeBundle);
    nodeAttributes.addNameMapping (canonicalName, newNode);
    } // nodeCreated

  protected void edgeCreated (Edge e) {
    System.out.println ("edge created: " + e);
    }

  protected String getEdgeTip (Edge edge) {
    return edgeAttributes.getCanonicalName (edge);
    } // getEdgeTip

} // inner class EditGraphMode
//------------------------------------------------------------------------------
class CreateEdgeMode extends ViewMode {
  CreateEdgeMode () {
    super ();
    }
  protected void edgeCreated (Edge e) {
    System.out.println ("edge created: " + e);
    }

} // CreateEdgeMode 
//------------------------------------------------------------------------------
class ReadOnlyGraphMode extends EditMode {
  ReadOnlyGraphMode () { 
   super (); 
   allowNodeCreation (false);
   allowEdgeCreation (false);
   allowBendCreation (false);
   showNodeTips (true);
   showEdgeTips (true);
   }
  protected String getNodeTip (Node node) {
    String geneName = graphView.getGraph2D().getRealizer(node).getLabelText();
    String canonicalName = getCanonicalNodeName (node);
    if (canonicalName != null && canonicalName.length () > 0 && !canonicalName.equals (geneName))
      return geneName + " " + canonicalName;
    return geneName;
    } // getNodeTip

  protected String getEdgeTip (Edge edge) {
    return edgeAttributes.getCanonicalName (edge);
    } // getEdgeTip

} // inncer class ReadOnlyGraphMode
//------------------------------------------------------------------------------
protected class NodeAttributesPopupMode extends PopupMode {

  public JPopupMenu getNodePopup (Node v) {
    NodeRealizer r = graphView.getGraph2D().getRealizer(v);
    JDialog dialog = null;
    r.setSelected (false);
    String nodeName = r.getLabelText ();
    String [] nodeNames = new String [1];
    nodeNames [0] = nodeName;
    String currentCondition = null;
    dialog = new NodeAttributesPopupTable (mainFrame, nodeNames, bioDataServer, 
                                           currentCondition, expressionData,
                                           nodeAttributes);
    dialog.pack ();
    dialog.setLocationRelativeTo (mainFrame);
    dialog.setVisible (true);
    return null;
    }

  public JPopupMenu getPaperPopup (double x, double y) {
    return null;
    }
    
  public JPopupMenu getSelectionPopup (double x, double y) {
    Graph2D g = graphView.getGraph2D ();
    NodeCursor nc = g.selectedNodes (); 
    Vector nodeList = new Vector ();
    while (nc.ok ()) {
      Node node = nc.node ();
      NodeRealizer r = graphView.getGraph2D().getRealizer(node);
      String nodeName = r.getLabelText (); 
      nodeList.addElement (nodeName);
      nc.next ();
      }
    if (nodeList.size () > 0) {
      String [] nodeNames = new String [nodeList.size ()];
      for (int i=0; i < nodeList.size (); i++)
        nodeNames [i] = (String) nodeList.elementAt (i);
      JDialog dialog = null;
      String currentCondition = null;
      dialog = new NodeAttributesPopupTable (mainFrame, nodeNames, bioDataServer, 
                                             currentCondition, expressionData,
                                             nodeAttributes);
      dialog.pack ();
      dialog.setLocationRelativeTo (mainFrame);
      dialog.setVisible (true);
      } // if nodeList > 0     
    return null;
    }

} // inner class NodeAttributesPopupMode
//---------------------------------------------------------------------------------------
protected HashMap configureNewNode (Node node)
{
  OptionHandler options = new OptionHandler ("New Node");

  String [] attributeNames = nodeAttributes.getAttributeNames ();
  System.out.println ("attributes: " + attributeNames.length);

  if (attributeNames.length == 0) {
    options.addComment ("commonName is required; canonicalName is optional and defaults to commonName");
    options.addString ("commonName", "");
    options.addString ("canonicalName", "");
    }
  else for (int i=0; i < attributeNames.length; i++) {
    String attributeName = attributeNames [i];
    Class attributeClass = nodeAttributes.getClass (attributeName);
    if (attributeClass.equals ("string".getClass ()))
      options.addString (attributeName, "");
    else if (attributeClass.equals (new Double (0.0).getClass ()))
      options.addDouble (attributeName, 0);
    else if (attributeClass.equals (new Integer (0).getClass ()))
      options.addInt (attributeName, 0);
    } // else/for i
  
  options.showEditor ();

  HashMap result = new HashMap ();

  if (attributeNames.length == 0) {
    result.put ("commonName", (String) options.get ("commonName"));
    result.put ("canonicalName", (String) options.get ("canonicalName"));
    System.out.println ("result: " + result);
    }
  else for (int i=0; i < attributeNames.length; i++) {
    String attributeName = attributeNames [i];
    Class attributeClass = nodeAttributes.getClass (attributeName);
    if (attributeClass.equals ("string".getClass ()))
       result.put (attributeName, (String) options.get (attributeName));
    else if (attributeClass.equals (new Double (0.0).getClass ()))
       result.put (attributeName, (Double) options.get (attributeName));
    else if (attributeClass.equals (new Integer (0).getClass ()))
       result.put (attributeName, (Integer) options.get (attributeName));
    } // else/for i

  return result;

} // configureNode
//---------------------------------------------------------------------------------------
} // CytoscapeWindow
