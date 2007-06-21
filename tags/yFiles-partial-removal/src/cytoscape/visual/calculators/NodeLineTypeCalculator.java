//----------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//----------------------------------------------------------------------------
package cytoscape.visual.calculators;
//----------------------------------------------------------------------------
import java.util.Map;
import javax.swing.JPanel;

import giny.model.Node;
import cytoscape.visual.LineType;

import cytoscape.data.CyNetwork;
//----------------------------------------------------------------------------
public interface NodeLineTypeCalculator extends Calculator {
    
    LineType calculateNodeLineType(Node node, CyNetwork network);
}
