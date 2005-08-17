//-------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//-------------------------------------------------------------------------
package cytoscape.actions;
//-------------------------------------------------------------------------
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import cytoscape.util.CytoscapeAction;
import cytoscape.Cytoscape;
//-------------------------------------------------------------------------
public class UnHideSelectedEdgesAction extends CytoscapeAction  {

    public UnHideSelectedEdgesAction () {
        super ("Show All");
        setPreferredMenu( "Select.Edges" );
        setAcceleratorCombo( java.awt.event.KeyEvent.VK_H, ActionEvent.ALT_MASK| ActionEvent.SHIFT_MASK) ;
    }

    public void actionPerformed (ActionEvent e) {
      //GinyUtils.unHideSelectedEdges( Cytoscape.getCurrentNetworkView() );
      GinyUtils.unHideAll( cytoscape.Cytoscape.getCurrentNetworkView() );
    }//action performed
}

