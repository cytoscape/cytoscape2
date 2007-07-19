//----------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//----------------------------------------------------------------------------
package cytoscape.visual.calculators;
//----------------------------------------------------------------------------
import java.util.Map;
import javax.swing.JPanel;

import y.base.Node;

import cytoscape.data.CyNetwork;
//----------------------------------------------------------------------------
public interface NodeToolTipCalculator extends Calculator {
    
    String calculateNodeToolTip(Node node, CyNetwork network);
}
