/** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and the
 ** Institute for Systems Biology and the Whitehead Institute
 ** have no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall the
 ** Institute for Systems Biology and the Whitehead Institute
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if the
 ** Institute for Systems Biology and the Whitehead Institute
 ** have been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
 //-------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//-------------------------------------------------------------------------
package cytoscape.view;
//-------------------------------------------------------------------------
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

//imports for giny graph library support
import giny.util.SpringEmbeddedLayouter;
import giny.view.GraphView;
import giny.view.EdgeView;
import giny.view.NodeView;
import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;
import cytoscape.util.GinyFactory;  //for creating Giny objects

import cytoscape.*;
import cytoscape.plugin.PluginListener;
import cytoscape.plugin.PluginEvent;
import cytoscape.plugin.AbstractPlugin;
import cytoscape.plugin.PluginUpdateList;
import cytoscape.data.ExpressionData;
import cytoscape.data.GraphObjAttributes;

//import cytoscape.view.*;
import cytoscape.visual.*;
import cytoscape.visual.ui.VizMapUI;
//-------------------------------------------------------------------------
/**
 * This class represents a visible window displaying a network. It includes
 * all of the UI components and the the graph view.
 */
public class CyWindow extends JPanel implements
        GraphViewChangeListener,
        CyNetworkListener,
        PluginListener,
        NetworkView {

    protected static final int DEFAULT_WIDTH = 700;
    protected static final int DEFAULT_HEIGHT = 700;
    Paint DEFAULT_PAINT = Color.yellow;

    protected CytoscapeObj globalInstance;
    protected CyNetwork network;

    protected JFrame mainFrame;
    protected CyMenus cyMenus;
    protected JLabel infoLabel;

    protected String defaultWindowTitle = "Cytoscape " + CytoscapeVersion.version + "     ";
    protected String windowTitle;

    protected GraphView  view;
    protected Component display;
    /**
     * An object that keeps the <code>RootGraph</code>'s <code>GraphPerspective</code>
     * synchronized with its <code>GraphView</code>.
     */
    protected GraphViewController graphViewController;

    protected boolean currentInteractivityState = false;

    /** contains mappings from network properties and attributes to visual
     *  properties such as the sizes and colors of nodes and edges.
     */
    protected VisualMappingManager vizMapper;

    /** user interface to the
     *  {@link VisualMappingManager VisualMappingManager}
     *  {@link #vizMapper vizMapper}.
     */
    protected VizMapUI vizMapUI;

    //flag indicating whether the vizmapper is enabled
    protected boolean visualMapperEnabled = true;

    //save constructor variable here to draw graph later
    protected boolean windowDisplayed = false;

    // timestamp of last received event (with currentTimeMills())
    protected long lastPluginRegistryUpdate;
//------------------------------------------------------------------------------
/**
 * Main constructor.
 *
 * @param globalInstance  contains globally unique objects such as
 *                        the CytoscapeConfig and bioDataServer
 * @param network  the network to be displayed in this window
 * @param title  the frame title; a default value is used
 *                     if this is null
 */
public CyWindow(CytoscapeObj globalInstance, CyNetwork network, String title) {
    this.globalInstance = globalInstance;
    this.network = network;
    network.addCyNetworkListener(this);

    if (title == null) {
        this.windowTitle = defaultWindowTitle;
    } else {
        this.windowTitle = defaultWindowTitle + title;
    }

    setLayout( new BorderLayout() );

    this.infoLabel = new JLabel();
    this.infoLabel.setBackground(Color.BLUE);
    add(infoLabel, BorderLayout.SOUTH);
    updateStatusLabel(0, 0);
    this.mainFrame = new JFrame(windowTitle);

    

    //need to create a graph view before creating menu actions that
    //want access to the view
    createGraphView();

    mainFrame.setContentPane(this);
    //create the menu objects
    this.cyMenus = new CyMenus(this);
    cyMenus.initializeMenus();
    add(cyMenus.getToolBar(), BorderLayout.NORTH);
    mainFrame.setJMenuBar(cyMenus.getMenuBar());

    // toggleVisualMapperEnabled();

    installGraphView();
    // load vizmapper after menus are done and graph is available
    loadVizMapper();
    //applyLayout(); do not layout by the default, too slow
    redrawGraph(false, true);
    //view.fitContent();
    //view.setZoom(view.getZoom()*0.9);
    setInteractivity(true);

    //add a listener to save the visual mapping catalog on exit
    //this should eventually be replaced by a method in Cytoscape.java itself
    //to save the catalog just before exiting the program
    final CytoscapeObj theCytoscapeObj = globalInstance;
    final CyWindow thisWindow = this;
    mainFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
            theCytoscapeObj.saveCalculatorCatalog();
        }
        public void windowClosed() {
            theCytoscapeObj.getPluginRegistry().removePluginListener(thisWindow);
        }
    });
    //add the parent app as a listener, to manage the session when this window closes
    //is this strictly necessary, since cytoscape.java listens for
    //WindowOpened events? -AM 2003/06/24
    mainFrame.addWindowListener( globalInstance.getParentApp() );
    //poll Plugin Registry for immediate plugin load set
    PluginUpdateList pul = globalInstance.getPluginRegistry().getPluginsLoadedSince(0);
    Class neededPlugin[] = pul.getPluginArray();
    for (int i = 0; i < neededPlugin.length; i++) {
        AbstractPlugin.loadPlugin(neededPlugin[i], globalInstance, this);
    }
    lastPluginRegistryUpdate = pul.getTimestamp();
    //add self as listener to the PluginRegistry from the shared CytoscapeObj
    globalInstance.getPluginRegistry().addPluginListener(this);
}

//------------------------------------------------------------------------------
//---------INITIALIZATION METHODS-----------------------------------------------
//------------------------------------------------------------------------------

/**
 * Creates a new graph view, replacing the old if necessary
 */
protected void createGraphView() {
    GraphView newView = GinyFactory.createGraphView(network.getGraphPerspective());
    //not sure why we have to disable the selections before enabling them -AM 2004-04-05
    newView.disableNodeSelection();
    newView.disableEdgeSelection();
    if (this.view != null) {
        this.view.removeGraphViewChangeListener(this);
        if (this.view.nodeSelectionEnabled()) {newView.enableNodeSelection();}
        if (this.view.edgeSelectionEnabled()) {newView.enableEdgeSelection();}
        //if no previous view, then menu items will set selection state
    }
    //now we can switch to the new view
    this.view = newView;

    newView.addGraphViewChangeListener(this);
    // Add the GraphViewController as a listener to the graphPerspective
    // so that it keeps is synchronized to graphView
    if(this.graphViewController == null){
        this.graphViewController = new GraphViewController();
    } else {
        this.graphViewController.removeAllGraphViews();
    }
    boolean added = this.graphViewController.addGraphView(newView);
    if(!added){
        // This should never happen, but just in case
        System.err.println("1. In CyWindow.updateGraphView(): Could not add this.view to "
                           + " this.graphViewController.");
    }

    addViewContextMenus();

    /*
     * These are initial values for the view parameters, which are mostly
     * redundant since the vizmapper controls these. The might be useful if
     * the vizmapper is not available or disabled.
    view.setBackgroundPaint(Color.BLACK);

    Iterator i = view.getNodeViewsIterator();
    while ( i.hasNext()) {
        NodeView nv = (NodeView)i.next();
        nv.getLabel().setText( nv.getNode().getIdentifier() );
        nv.setShape( NodeView.ELLIPSE );
        nv.setUnselectedPaint( Color.lightGray );
        nv.setSelectedPaint( ((Color)nv.getUnselectedPaint()).darker() );
        nv.setBorderPaint(Color.black);
    }

    //edges
    java.util.List edges = view.getEdgeViewsList();
    for ( Iterator ie= edges.iterator(); ie.hasNext(); ) {
        EdgeView ev = (EdgeView)ie.next();
        ev.setUnselectedPaint(Color.blue);
        //ev.setTargetEdgeEnd(EdgeView.ARROW_END);
        //ev.setTargetEdgeEndPaint(Color.white);
        //ev.setSourceEdgeEndPaint(Color.white);
        //ev.setLineType(EdgeView.CURVED_LINES);
        ev.setStroke(new BasicStroke(5f));
    }
    */
}

/**
 * install the Graph View in the displayed window frame
 */
protected void installGraphView() {
    Component oldDisplay = null;
    if (display != null) {
        oldDisplay = display;
    }

    display = view.getComponent();
    add( display, BorderLayout.CENTER);
    //the tool bar is usually already in the window; the following is a trick
    //to force the tool bar to be displayed properly after changing the view
    if (this.cyMenus != null) {
        add(cyMenus.getToolBar(), BorderLayout.NORTH);
    }

    //redrawGraph(false, true);
    if (oldDisplay != null){
        this.remove(oldDisplay);
    }
}
//------------------------------------------------------------------------------
/**
 * Adds some useful context menus to the graph view. These should probably be
 * moved to a different place to enable menu customization.
 */
protected void addViewContextMenus() {
	// Add some Node Context Menu Items

  view.addContextMethod( "class phoebe.PNodeView",
                         "cytoscape.graphutil.NodeAction",
                         "getTitle",
                         new Object[] { ( NetworkView )this } );

  view.addContextMethod( "class phoebe.PNodeView",
                         "cytoscape.graphutil.NodeAction",
                         "openWebInfo",
                         new Object[] { ( NetworkView )this } );


  view.addContextMethod( "class phoebe.PNodeView",
                         "cytoscape.graphutil.NodeAction",
                         "viewNodeAttributeBrowser",
                         new Object[] { ( NetworkView )this } );

  view.addContextMethod( "class phoebe.PNodeView",
                         "cytoscape.graphutil.NodeAction",
                         "editNode",
                         new Object[] { ( NetworkView )this } );

   view.addContextMethod( "class phoebe.PNodeView",
                         "cytoscape.graphutil.NodeAction",
                         "changeFirstNeighbors",
                         new Object[] {view } );

  view.addContextMethod( "edu.umd.cs.piccolo.PNodeView",
                         "cytoscape.graphutil.NodeAction",
                         "zoomToNode",
                         new Object[] {view } );




  // Add some Edge Context Menus
  view.addContextMethod( "class phoebe.PEdgeView",
                         "cytoscape.graphutil.EdgeAction",
                         "getTitle",
                         new Object[] { ( NetworkView )this } );
  view.addContextMethod( "class phoebe.PEdgeView",
                         "cytoscape.graphutil.EdgeAction",
                         "editEdge",
                         new Object[] { ( NetworkView )this } );
  view.addContextMethod( "class phoebe.PEdgeView",
                         "cytoscape.graphutil.EdgeAction",
                         "viewEdgeAttributeBrowser",
                         new Object[] { ( NetworkView )this } );
   view.addContextMethod( "class phoebe.PEdgeView",
                          "cytoscape.graphutil.EdgeAction",
                          "openWebInfo",
                          new Object[] { ( NetworkView )this } );


  // Add some Edge-end Context menus
  view.addContextMethod( "class phoebe.util.PEdgeEndIcon",
                         "cytoscape.graphutil.EdgeAction",
                         "edgeEndColor",
                         new Object[] {view } );
  view.addContextMethod( "class phoebe.util.PEdgeEndIcon",
                         "cytoscape.graphutil.EdgeAction",
                         "edgeEndBorderColor",
                         new Object[] {view } );

}

//------------------------------------------------------------------------------
/**
 * Creates the vizmapper and it's UI, making sure that a visual style
 * is selected, either from the config or a default value.
 */
protected void loadVizMapper() {

    // BUG: vizMapper.applyAppearances() gets called twice here

    CalculatorCatalog calculatorCatalog = getCytoscapeObj().getCalculatorCatalog();
    //try to get visual style from properties
    Properties configProps = getCytoscapeObj().getConfiguration().getProperties();
    VisualStyle vs = null;
    String vsName = configProps.getProperty("visualStyle");
    if (vsName != null) {vs = calculatorCatalog.getVisualStyle(vsName);}
    if (vs == null) {//none specified, or not found; use the default
        vs = calculatorCatalog.getVisualStyle("default");
    }

    //create the vizMapping objects
    this.vizMapper = new VisualMappingManager(this, calculatorCatalog, vs,
                                              getCytoscapeObj().getLogger());
    this.vizMapUI = new VizMapUI(this.vizMapper, this.mainFrame);
    //make UI a listener on the manager
    vizMapper.addChangeListener( vizMapUI.getStyleSelector() );

    // easy-access visual styles changer
    JToolBar toolBar = getCyMenus().getToolBar();
    JComboBox styleBox = vizMapUI.getStyleSelector().getToolbarComboBox();
    Dimension newSize = new Dimension(150, (int)styleBox.getPreferredSize().getHeight());
    styleBox.setMaximumSize(newSize);
    styleBox.setPreferredSize(newSize);
    toolBar.add(styleBox);
    toolBar.addSeparator();
}
//------------------------------------------------------------------------------
/**
 * Actually displays the window. Nothing will appear on the screen until
 * this method gets called. This allows other objects to operate on this
 * object after construction but before it gets displayed to the user.<P>
 *
 * Any calls after the first force this window to revalidate and redraw
 * itself, usually only needed internally when the components are changed.
 */
public void showWindow(int width, int height) {
	mainFrame.pack();
	mainFrame.setSize(width, height);
	this.setVisible(true);
	mainFrame.setVisible(true);
}

/**
 * Show window with default width and height if not currently sized.
 * If size is currently set, this function will use the current size
 * repack and zoom the window to the current size.
 */
public void showWindow() {
    if (mainFrame.isShowing()) {
        this.showWindow(mainFrame.getWidth(), mainFrame.getHeight());
    } else {
	this.showWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        //these methods have to wait until the window size is set, which only
        //happens after an initial call to showWindow
        view.fitContent();
        view.setZoom(view.getZoom()*0.9);
    }
}

//------------------------------------------------------------------------------
//----------SET/GET METHODS-----------------------------------------------------
//------------------------------------------------------------------------------

/**
 * Returns a reference to the global Cytoscape object.
 */
public CytoscapeObj getCytoscapeObj() {return globalInstance;}
//------------------------------------------------------------------------------
/**
 * returns the network displayed in this window.
 */
public CyNetwork getNetwork() {return network;}
//------------------------------------------------------------------------------

/**
 * Returns the UI component that renders the displayed graph.
 */
public GraphView getView() {return view;}
//------------------------------------------------------------------------------
/**
 * Returns the <code>cytoscape.view.GraphViewController</code> that keeps
 * the <code>giny.model.GraphPerspective</code> contained in <code>CyNetwork</code>
 * synchronized to the <code>giny.view.GraphView</code> in this <code>CyWindow</code>.
 *
 * @return a <code>cytoscape.view.GraphViewController</code>
 */
public GraphViewController getGraphViewController (){
    return this.graphViewController;
}
//------------------------------------------------------------------------------
/**
 * Returns the visual mapping manager that controls the appearance
 * of nodes and edges in this display.
 */
public VisualMappingManager getVizMapManager() {return vizMapper;}
//------------------------------------------------------------------------------
/**
 * returns the top-level UI object for the visual mapper.
 */
public VizMapUI getVizMapUI() {return vizMapUI;}
//------------------------------------------------------------------------------
/**
 * returns the main window frame. Note that this class is itself
 * an instance of JPanel, and is the content pane of the main frame.
 */
public JFrame getMainFrame() {return mainFrame;}
//------------------------------------------------------------------------------
/**
 * Returns the title of the window, guaranteed to be non-null.
 */
public String getWindowTitle() {return windowTitle;}
//------------------------------------------------------------------------------
/**
 * Sets the window title. The actual title (as displayed on the
 * frame title bar, and returned by a call to getWindowTitle)
 * is a concatenation of a default header string and the argument
 */
public void setWindowTitle(String newTitle) {
    windowTitle = defaultWindowTitle +  newTitle;
    getMainFrame().setTitle(windowTitle);
}
//------------------------------------------------------------------------------
/**
 * Returns the object that holds references to the menu bars and
 * several of the major submenus.
 */
public CyMenus getCyMenus() {return cyMenus;}

//------------------------------------------------------------------------------
//-------------ACTION METHODS---------------------------------------------------
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
/**
 * Changes the network displayed in this window. This display will
 * be updated to show and reference this new network, and new listeners
 * will be attached to the network and its graph. Note that this switch
 * will take place even if the new network is the same object as the
 * current network (this case is useful when a new graph is loaded into
 * an existing network object).
 *
 * If the newNetwork argument is null, this method does nothing.
 *
 * @param newNetwork  the new network to display
 */
public void setNewNetwork( CyNetwork newNetwork ) {
    if (newNetwork == null) {return;}

    this.network.removeCyNetworkListener(this);
    this.network = newNetwork;
    newNetwork.addCyNetworkListener(this);

    switchGraph();
}
//------------------------------------------------------------------------------
/**
 * Called when a new network object has been installed, or the graph in the
 * current network has been replaced. Creates a new view for that graph and
 * installs it in the window.
 */
protected void switchGraph() {
    setInteractivity(false);
    createGraphView();
    installGraphView();

    //applyLayout();
    redrawGraph(false, true);

    view.fitContent();
    view.setZoom(view.getZoom()*0.9);
    updateStatusLabel(0, 0);


    //this call forces the window to revalidate itself

    showWindow();
    setInteractivity(true);
}
//------------------------------------------------------------------------------
/**
 * Sets the interactivity state of this window. If the argument is
 * false. disables user interaction with the graph view and menus.
 * If the argument is true, user interaction is enabled.
 *
 * Currently, interaction with the view is never disabled since
 * Giny doesn't have this feature available yet.
 *
 * This class is initialized with interactivity off; it is turned
 * on by the showWindow method.
 */
public void setInteractivity (boolean newState) {
    if (currentInteractivityState == newState) {return;}
    currentInteractivityState = newState;
    if (newState == true) { // turn interactivity ON
        Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        setCursor(defaultCursor);
    } else {  // turn interactivity OFF
        Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        setCursor(busyCursor);
    }

    // disable/enable UI components - added by iliana 2003-03-03
    MenuElement [] menuBarEls = getCyMenus().getMenuBar().getSubElements();
    for(int i = 0; i < menuBarEls.length; i++) {
        Component comp = menuBarEls[i].getComponent();
        comp.setEnabled(newState);
    }

    getCyMenus().getToolBar().setEnabled(newState);
} // setInteractivity
//------------------------------------------------------------------------------
/**
 * Redraws the graph - equivalent to redrawGraph(false, true).
 * That is, no new layout will be performed, but the visual
 * appearances will be reapplied.
 */
public void redrawGraph() {
    // Do not do a layout, but apply appearances
    redrawGraph(false,true);
}
//------------------------------------------------------------------------------
/**
 * Redraws the graph - equivalent to redrawGraph(doLayout, true).
 * That is, the visual appearances will be reapplied, and layout
 * will be done iff the argument is true.
 */
public void redrawGraph(boolean doLayout) {
    // apply appearances by default
    redrawGraph(doLayout, true);
}
//------------------------------------------------------------------------------
/**
 * Redraws the graph. A new layout will be performed if the first
 * argument is true, and the visual appearances will be recalculated
 * and reapplied by the visual mapper if the second argument is true
 * and the visual mapper is not disabled.
 */
public void redrawGraph(boolean doLayout, boolean applyAppearances) {
    if (view == null) {return;}
    if (doLayout) {applyLayout(view);}
    if (applyAppearances) {applyVizmapSettings();}
    view.updateView();
    updateStatusLabel(0,0);
}
//------------------------------------------------------------------------------
/**
 * Performs a layout operation on the giny graph displayed in this window,
 * using the default layouter for now
 */
public void applyLayout(GraphView lview) {
	SpringEmbeddedLayouter lay = new SpringEmbeddedLayouter(lview);
	lay.doLayout();
}
//------------------------------------------------------------------------------
/**
 * Applies a layout to only the selected nodes or edges. Does this work?
 */
public void applySelLayout() {
    if (view == null) return;

    int[] selNodes = view.getSelectedNodeIndices();
    int[] selEdges = view.getSelectedEdgeIndices();
    GraphView selView =
        GinyFactory.createGraphView(view.getRootGraph().createGraphPerspective(selNodes, selEdges));
    applyLayout(selView);
  }
//------------------------------------------------------------------------------
/**
 * Uses the visual mapper to calculate and set the visual appearance
 * of nodes, edges, and certain global properties using the node and
 * edge data attributes.
 *
 * This method does nothing if the visual mapper is currently disabled.
 *
 * @see VisualMappingManager
 */
protected void applyVizmapSettings() {
    if (getVizMapManager() != null && this.visualMapperEnabled) {
        getVizMapManager().applyAppearances();
    }
}
//------------------------------------------------------------------------------
/**
 * Enables the visual mapper if the argument is true, otherwise disables the
 * visual mapper. When the vsual mapper is disabled, the corresponding menu items
 * are disabled and the visual mappings will not be reapplied when the graph
 * is redrawn, even if requested.
 *
 * When enabling the visual mapper, this method forces a redraw of the graph
 * to reapply the current visual style.
 */
public void setVisualMapperEnabled(boolean newState) {
    if (this.visualMapperEnabled != newState) {
        this.visualMapperEnabled = newState;
        getCyMenus().setVisualMapperItemsEnabled(newState);
        if (newState == true) {redrawGraph(false, true);}
    }
}
//------------------------------------------------------------------------------
/**
 * If enabled, disable the visual mapper (and vice versa). Menu items and
 * redraw behavior follow the semantics of setVisualMapperEnabled()
 */
public void toggleVisualMapperEnabled() {
    setVisualMapperEnabled(!visualMapperEnabled);
}
//------------------------------------------------------------------------------
/**
 * This method responds to the given CyNetworkEvent If the source of this event
 * is the network displayed in this window. Currently, this method turns off
 * interactivity when a BEGIN event is received, and turns it back on when an
 * END event is received.
 */
public void onCyNetworkEvent(CyNetworkEvent event) {
    if (event.getNetwork() != this.getNetwork()) {return;}
    if (event.getType() == CyNetworkEvent.BEGIN) {
        setInteractivity(false);
    } else if (event.getType() == CyNetworkEvent.END) {
        setInteractivity(true);
    } else if (event.getType() == CyNetworkEvent.GRAPH_REPLACED) {
        switchGraph();
    }
}
//------------------------------------------------------------------------------
/**
 * Implementation of the GraphViewChangeListener interface. Triggers a change
 * in the status label, updating the number of hidden nodes/edges.
 */
public void graphViewChanged ( GraphViewChangeEvent event) {
    int [] nodes = event.getHiddenNodeIndices();
    int[] edges = event.getHiddenEdgeIndices();
    //these arrays apparently can be null; count these as 0 hidden -AM 2004-03-30
    int nodeCount = (nodes == null) ? 0 : nodes.length;
    int edgeCount = (edges == null) ? 0 : edges.length;
    updateStatusLabel(nodeCount, edgeCount);
}
//------------------------------------------------------------------------------
/**
 * Implemenation of the PluginListener interface. Triggers update of
 * currently loaded plugins.
 */
public void pluginRegistryChanged(PluginEvent event) {
    //poll Plugin Registry for new plugins since last update
    PluginUpdateList pul = globalInstance.getPluginRegistry().getPluginsLoadedSince(lastPluginRegistryUpdate);
    Class neededPlugin[] = pul.getPluginArray();
    for (int i = 0; i < neededPlugin.length; i++) {
        AbstractPlugin.loadPlugin(neededPlugin[i], globalInstance, this);
    }
    lastPluginRegistryUpdate = pul.getTimestamp();
}
//------------------------------------------------------------------------------
/**
 * Resets the info label status bar text with the current number of
 * nodes, edges, selected nodes, and selected edges.
 */
public void updateStatusLabel(int hiddenNodes, int hiddenEdges) {
    if (getView() == null) {
        infoLabel.setText("No graph specified for the display  ");
        return;
    }

    int nodeCount = view.getNodeViewCount();
    int edgeCount = view.getEdgeViewCount();
    int selectedNodes = view.getSelectedNodes().size();
    int selectedEdges = view.getSelectedEdges().size();

    infoLabel.setText("  Nodes: " + nodeCount
                      + " ("+selectedNodes+" selected)"
		      + " ("+hiddenNodes + " hidden)"
                      + " Edges: " + edgeCount
                      + " ("+selectedEdges+" selected)"
		      + " (" +hiddenEdges+ " hidden)");
}
//------------------------------------------------------------------------------
/**
 * NOTE: This method currently does nothing. When implemented,
 * sets the editing mode to allow the user to edit the graph.
 * This changes the graph mode and the menu options available.
 */
public void switchToEditMode() {
}
//------------------------------------------------------------------------------
/**
 * NOTE: This method currently does nothing. When implemented,
 * sets the editing mode to prevent the user from editing the graph.
 * This chagnes the graph mode and the menu options available.
 */
public void switchToReadOnlyMode() {
}
//------------------------------------------------------------------------------
}

