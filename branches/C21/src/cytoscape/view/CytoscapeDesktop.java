package cytoscape.view;

import cytoscape.CytoscapeObj;
import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.CyEdge;

import cytoscape.view.CyMenus;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CyNodeView;
import cytoscape.view.CyEdgeView;

import cytoscape.visual.*;
import cytoscape.visual.ui.*;

import cytoscape.plugin.*;

import cytoscape.giny.*;



import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

/**
 * The CytoscapeDesktop is the central Window
 * for working with Cytoscape
 */

public class CytoscapeDesktop 
  extends 
    JFrame 
  implements
    PropertyChangeListener,
    PluginListener {
   


  //--------------------//
  // Static variables

  public static String NETWORK_VIEW_FOCUSED = "NETWORK_VIEW_FOCUSED";
  public static String NETWORK_VIEW_FOCUS = "NETWORK_VIEW_FOCUS";
  public static String NETWORK_VIEW_CREATED = "NETWORK_VIEW_CREATED";

  // state variables
  public static String VISUAL_STYLE = "VISUAL_STYLE";
  public static String VIZMAP_ENABLED = "VIZMAP_ENABLED";


  /**
   * Displays all network views in TabbedPanes
   * ( like Mozilla )
  */
  public static int TABBED_VIEW = 0;

  /**
   * Displays all network views in JInternalFrames, using 
   * the mock desktop interface. ( like MS Office )
   */
  public static int INTERNAL_VIEW = 1;

  /**
   * Displays all network views in JFrames, so each Network
   * has its own window. ( like the GIMP )
   */
  public static int EXTERNAL_VIEW = 2;

  //--------------------//
  // Member varaibles
  
  /**
   * The type of view, should be considered final
   */
  protected int VIEW_TYPE;

  protected VisualStyle defaultVisualStyle;

  /**
   * The network panel that sends out events when 
   * a network is selected from the Tree that it contains.
   */
  protected NetworkPanel networkPanel;

  /**
   * The CyMenus object provides access to the all of the
   * menus and toolbars that will be needed.
   */
  protected CyMenus cyMenus;

  /**
   * The NetworkViewManager can support three types of interfaces.
   * Tabbed/InternalFrame/ExternalFrame
   */
  protected NetworkViewManager networkViewManager;

  
  //--------------------//
  // Event Support

  /**
   * provides support for property change events
   */
   protected SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport( this );

  /**
   * The GraphViewController for all NetworkViews that we know about
   */
   protected GraphViewController graphViewController;

  //--------------------//
  // VizMap Variables

  /** 
   * Provides Operations for Mapping Data Attributes of CyNetworks
   * to CyNetworkViews
   */
  protected VisualMappingManager vizMapper;
  
  /** user interface to the
   *  {@link VisualMappingManager VisualMappingManager}
   *  {@link #vizMapper vizMapper}.
   */
  protected VizMapUI vizMapUI;
 
  protected String currentNetworkID;
  protected String currentNetworkViewID;

  //--------------------//
  // Plugin Variables
  
  /**
   * timestamp of last received event (with currentTimeMills())
   */
   protected long lastPluginRegistryUpdate;

  protected boolean eventMessed = true;

  //----------------------------------------//
  // Constructors
  //----------------------------------------//

  /**
   * The Default constructor uses a TabbedView
   */
  public CytoscapeDesktop () {
    this( TABBED_VIEW );
  }

  /**
   * Create a CytoscapeDesktop that conforms the given view type.
   * @param view_type one of the ViewTypes
   */
  public CytoscapeDesktop ( int view_type ) {
    super( "Cytoscape Desktop" );
    this.VIEW_TYPE = view_type;
    initialize();
  }
  
  protected void initialize () {
  
    JPanel main_panel = new JPanel();

    main_panel.setLayout( new BorderLayout() );

    //------------------------------//
    // Set up the Panels, Menus, and Event Firing

    networkPanel = new NetworkPanel( this );
    cyMenus = new CyMenus();
    networkViewManager = new NetworkViewManager( this );


    // Listener Setup
    //----------------------------------------
    //              |----------|
    //              | CyMenus  |
    //              |----------|
    //                  |
    //                  |
    //  |-----|      |---------|    |------|  |-------|
    //  | N P |------| Desktop |----| NVM  |--| Views |
    //  |-----|      |---------|    |------|  |-------|
    //                   | 
    //                   |
    //              |-----------|
    //              | Cytoscape |
    //              |-----------|

    // The CytoscapeDesktop listens to NETWORK_VIEW_CREATED events, 
    // and passes them on, The NetworkPanel listens for them
    // The Desktop also keeps Cytoscape up2date, but NOT via events
    Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener( this );

    // The Networkviewmanager listens to the CytoscapeDesktop to know when to
    // put new NetworkViews in the userspace and to get passed focus events from 
    // the NetworkPanel. The CytoscapeDesktop also listens to the NVM
    this.getSwingPropertyChangeSupport().addPropertyChangeListener( networkViewManager );
    networkViewManager.getSwingPropertyChangeSupport().addPropertyChangeListener( this );

    // The NetworkPanel listens to the CytoscapeDesktop for NETWORK_CREATED_EVENTS a
    // as well as for passing focused events from the Networkviewmanager. The
    // CytoscapeDesktop also listens to the NetworkPanel
    this.getSwingPropertyChangeSupport().addPropertyChangeListener( networkPanel );
    networkPanel.getSwingPropertyChangeSupport().addPropertyChangeListener( this );


    cyMenus.initializeMenus();

    // create the CytoscapeDesktop
    if ( VIEW_TYPE == TABBED_VIEW ) {
      // eveything gets put into this one window
      //JScrollPane scroll_panel = new JScrollPane( networkPanel );
      JScrollPane scroll_tab = new JScrollPane( networkViewManager.getTabbedPane() );


      JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                         false,
                                         networkPanel,
                                         scroll_tab );
      split.setOneTouchExpandable( true );
    //   JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
//                                          false,
//                                          networkPanel,
//                                          networkViewManager.getTabbedPane() );
      main_panel.add( split, BorderLayout.CENTER );
      main_panel.add(cyMenus.getToolBar(), BorderLayout.NORTH);
      setJMenuBar(cyMenus.getMenuBar());
    }

    else if ( VIEW_TYPE == INTERNAL_VIEW ) {
      // eveything gets put into this one window
      JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                         false,
                                         networkPanel,
                                         networkViewManager.getDesktopPane() );
     main_panel.add( split, BorderLayout.CENTER );
     main_panel.add(cyMenus.getToolBar(), BorderLayout.NORTH);
     setJMenuBar(cyMenus.getMenuBar());
    }
    
    else if ( VIEW_TYPE == EXTERNAL_VIEW ) {
      // just the NetworkPanel and the Menus get put into the Main Pane
      main_panel.add( networkPanel );
      cyMenus.getToolBar().setOrientation( JToolBar.VERTICAL );
      main_panel.add(cyMenus.getToolBar(), BorderLayout.EAST);
      
      JFrame menuFrame = new JFrame("Cytoscape Menus");
      menuFrame.setJMenuBar(cyMenus.getMenuBar());
      menuFrame.setVisible( true );
    }

    

    //------------------------------//
    // Set up the VizMapper
    setupVizMapper( main_panel );
    
    //------------------------------//
    // Window Closing, Program Shutdown

    //add a listener to save the visual mapping catalog on exit
    //this should eventually be replaced by a method in Cytoscape.java itself
    //to save the catalog just before exiting the program
    //TODO: Allow other things to be notified if the
    //      Program is exiting.
    final CytoscapeObj theCytoscapeObj = Cytoscape.getCytoscapeObj();
    final CytoscapeDesktop thisWindow = this;
    addWindowListener(new WindowAdapter() {
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
    addWindowListener( Cytoscape.getCytoscapeObj().getParentApp() );
   


    // show the Desktop
    setContentPane( main_panel );
    pack();
    if ( VIEW_TYPE != EXTERNAL_VIEW )
      setSize( 700, 700 );
    setVisible( true );

  }

  /**
   * Return the view type for this CytoscapeDesktop
   */
  protected int getViewType () {
    return VIEW_TYPE;
  }

  /**
   * Returns the visual mapping manager that controls the appearance
   * of nodes and edges in this display.
   */
  public VisualMappingManager getVizMapManager() {return vizMapper;}

  /**
   * returns the top-level UI object for the visual mapper.
   */
  public VizMapUI getVizMapUI() {return vizMapUI;}
  
  /**
   * Load in the Plugins
   */
  protected void setupPlugins () {
    //poll Plugin Registry for immediate plugin load set
    PluginUpdateList pul = Cytoscape.getCytoscapeObj().getPluginRegistry().getPluginsLoadedSince(0);
    Class neededPlugin[] = pul.getPluginArray();
    for (int i = 0; i < neededPlugin.length; i++) {
      // AbstractPlugin.loadPlugin(neededPlugin[i], Cytoscape.getCytoscapeObj(), this);
    }
    lastPluginRegistryUpdate = pul.getTimestamp();
    //add self as listener to the PluginRegistry from the shared CytoscapeObj
    Cytoscape.getCytoscapeObj().getPluginRegistry().addPluginListener(this);
  }

  /**
   * Implemenation of the PluginListener interface. Triggers update of
   * currently loaded plugins.
   */
  public void pluginRegistryChanged(PluginEvent event) {
    //poll Plugin Registry for new plugins since last update
    PluginUpdateList pul = Cytoscape.getCytoscapeObj().getPluginRegistry().getPluginsLoadedSince(lastPluginRegistryUpdate);
    Class neededPlugin[] = pul.getPluginArray();
    for (int i = 0; i < neededPlugin.length; i++) {
      // AbstractPlugin.loadPlugin(neededPlugin[i], globalInstance, this);
    }
    lastPluginRegistryUpdate = pul.getTimestamp();
  }

  /**
   * Create the VizMapper and the UI for it.
   */
  protected void setupVizMapper ( JPanel panel ) {
    
    // BUG: vizMapper.applyAppearances() gets called twice here
    
    CalculatorCatalog calculatorCatalog = Cytoscape.getCytoscapeObj().getCalculatorCatalog();

    //try to get visual style from properties
    Properties configProps = Cytoscape.getCytoscapeObj().getConfiguration().getProperties();
    VisualStyle vs = null;
    String vsName = configProps.getProperty("visualStyle");
    if (vsName != null) {vs = calculatorCatalog.getVisualStyle(vsName);}
    if (vs == null) {//none specified, or not found; use the default
        vs = calculatorCatalog.getVisualStyle("default");
    }

    // create the VisualMappingManager using default values for now
    // TODO: as CyNetworkViews get the focus the VizMapper will update
    //       and the constructor should reflect this.
    this.vizMapper = new VisualMappingManager( Cytoscape.getCurrentNetworkView(),
                                               calculatorCatalog, 
                                               vs,
                                               Cytoscape.getCytoscapeObj().getLogger());
    
    defaultVisualStyle = calculatorCatalog.getVisualStyle("default");

    // craete the VizMapUI
    this.vizMapUI = new VizMapUI( this.vizMapper, 
                                  this );
    
    // In order for the VizMapper to run when the StyleSelector is
    // run, it needs to listen to the selector.
    vizMapper.addChangeListener( vizMapUI.getStyleSelector() );

    // Add the StyleSelector to the ToolBar
    // TODO: maybe put this somewhere else to make it easier to make
    //       vertical ToolBars.

    JComboBox styleBox = vizMapUI.getStyleSelector().getToolbarComboBox();
    Dimension newSize = new Dimension( 150, (int)styleBox.getPreferredSize().getHeight());
    styleBox.setMaximumSize(newSize);
    styleBox.setPreferredSize(newSize);
    if ( VIEW_TYPE == EXTERNAL_VIEW ) {
      panel.add(styleBox , BorderLayout.SOUTH );
    } else {
      JToolBar toolBar = cyMenus.getToolBar();
      toolBar.add(styleBox);
      toolBar.addSeparator();
    }
  }


  protected void updateFocus ( String network_view_id ) {
      
    // deal with the old Network
    VisualStyle old_style = ( VisualStyle )vizMapUI.
      getStyleSelector().
      getToolbarComboBox().
      getSelectedItem();

    CyNetworkView old_view = Cytoscape.getCurrentNetworkView();
    old_view.setClientData( VISUAL_STYLE, old_style );
    old_view.setClientData( VIZMAP_ENABLED, new Boolean( old_view.getVisualMapperEnabled() ) );

    // set the current Network/View
    Cytoscape.setCurrentNetworkView( network_view_id );
    Cytoscape.setCurrentNetwork( network_view_id );
    
    // deal with the new Network
     CyNetworkView new_view = Cytoscape.getCurrentNetworkView();
     VisualStyle new_style = ( VisualStyle )new_view.getClientData( VISUAL_STYLE );
     Boolean vizmap_enabled = ( ( Boolean )new_view.getClientData( VIZMAP_ENABLED ) );

     if ( new_style == null ) 
       new_style = defaultVisualStyle;

     if ( vizmap_enabled == null )
       vizmap_enabled = new Boolean( true );

     vizMapper.setNetworkView( new_view );
     vizMapper.setVisualStyle( new_style );
     vizMapUI.getStyleSelector().getToolbarComboBox().setSelectedItem( new_style );

     cyMenus.setNodesRequiredItemsEnabled();
     cyMenus.setVisualMapperItemsEnabled( vizmap_enabled.booleanValue() );
     if ( vizmap_enabled.booleanValue() ) {
       new_view.redrawGraph( false, false );
     }
  }

  /**
   * TO keep things clearer there is one GraphView Controller 
   * per CytoscapeDesktop
   */
  public GraphViewController getGraphViewController () {
    if ( graphViewController == null )
      graphViewController = new GraphViewController();
    
    return graphViewController;
  }

  public SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
    return pcs;
  }

  public void propertyChange ( PropertyChangeEvent e ) {
    

   
      if ( e.getPropertyName() == NETWORK_VIEW_CREATED ) {
        // add the new view to the GraphViewController
        getGraphViewController().addGraphView( ( CyNetworkView )e.getNewValue() );
        // pass on the event 
        pcs.firePropertyChange( e );
      } 
  
      else if ( e.getPropertyName() == NETWORK_VIEW_FOCUSED ) {
        // get focus event from NetworkViewManager
        
        updateFocus( e.getNewValue().toString() );
        pcs.firePropertyChange( e );
      }

      else if ( e.getPropertyName() == NETWORK_VIEW_FOCUS ) {
        // get Focus from NetworkPanel
        
        updateFocus( e.getNewValue().toString() );
        pcs.firePropertyChange( e );
      }

      else if ( e.getPropertyName() == Cytoscape.NETWORK_CREATED ) {
        // fire the event so that the NetworkPanel can catch it
        pcs.firePropertyChange( e );
      }

   
  }
  
 



}
