/*
  File: InDegree.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.EquationFunctions.functions;


import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.CyNode;

import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.AbstractFunction;


public class InDegree extends AbstractFunction {
	public InDegree() {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.STRICT_STRING, "edge_ID", "An ID identifying a node.") });
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "INDEGREE"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns indegree of a node."; }

	public Class getReturnType() { return Long.class; }

	/**
	 *  @param args the function arguments which must be either one object of type Double or Long
	 *  @return the result of the function evaluation which is the natural logarithm of the first argument
	 */
	public Object evaluateFunction(final Object[] args) {
		final String nodeID = (String)args[0];

		final CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
		if (currentNetwork == null)
			return (Long)(-1L);

		final CyNode node = Cytoscape.getCyNode(nodeID);
		if (node == null)
			throw new IllegalArgumentException("\"" + nodeID + "\" is not a valid node identifier!");
		
		return (Long)(long)currentNetwork.getInDegree(node, /* count_undirected_edges = */ false);
	}
}
