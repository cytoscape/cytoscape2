/*
  File: GraphSetUtils.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
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
package cytoscape.util;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.logger.CyLogger;

import cytoscape.plugin.CytoscapePlugin;

import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;

import giny.model.Edge;
import giny.model.Node;

import giny.view.EdgeView;
import giny.view.Label;
import giny.view.NodeView;

import java.util.*;


/**
 * Class contains various static methods to perform set-like operations on
 * graph.
 *
 * @author Ryan Kelley
 *
 */
public class GraphSetUtils {
	/**
	 * The different types of network graph operations
	 */
	protected static final int UNION        = 0;
	protected static final int INTERSECTION = 1;
	protected static final int DIFFERENCE   = 2;
	protected static final int DIFFERENCE2  = 3;

	/**
	 * Create a new graph which is the union of multiple graphs. The union graph
	 * is created by applying the set union on both the edges and the nodes.
	 *
	 * @param networkList
	 *            A list containing all of the networks.
	 * @param copyView
	 *            This argument is ignored.
	 * @param title
	 *            The title of the new network
	 * @return A cyNetwork which is the union of the input graphs
	 */
	public static CyNetwork createUnionGraph(List networkList, boolean copyView, String title) {
		return performNetworkOperation(networkList, UNION, copyView, title);
	}

	/**
	 * Create a new graph which is the intersection of multiple graphs. The
	 * intersection graph is created by applying the set intersection on both
	 * the edges and the nodes.
	 *
	 * @param networkList
	 *            A list containing all of the networks.
	 * @param copyView
	 *            This argument is ignored.
	 * @param title
	 *            The title of the new network
	 * @return A cyNetwork which is the intersection of the input graphs
	 */
	public static CyNetwork createIntersectionGraph(List networkList, boolean copyView, String title) {
		return performNetworkOperation(networkList, INTERSECTION, copyView, title);
	}

	/**
	 * Create a new graph which is the difference of multiple graphs. Note that
	 * this is not the symmetric difference. The second graph in the list (and
	 * the third and the fourth ... ) are subtracted from the first graph. Here,
	 * we can not directly apply the set difference on the nodes and edge sets
	 * and get a valid graph. Therefore, we apply the difference operation on
	 * the edge sets firsts and add those nodes in the node difference set which
	 * are required for those edges to exist.
	 *
	 * @param networkList
	 *            A list containing all of the networks.
	 * @param copyView
	 *            This argument is ignored.
	 * @param title
	 *            The title of the new network
	 * @return A cyNetwork which is the difference of the input graphs
	 */
	public static CyNetwork createDifferenceGraph(List networkList, boolean copyView, String title) {
		return performNetworkOperation(networkList, DIFFERENCE, copyView, title);
	}

	/**
	 * The way this works is that the 2nd and optional additional networks will be subtracted
	 * from the 1st network.  The way this works is that all the nodes and edges in the 2nd and
	 * consecutive networks will be removed from the 1st network.
	 *
	 * @param networkList
	 *            A list containing all of the networks.
	 * @param copyView
	 *            This argument is ignored.
	 * @param title
	 *            The title of the resulting, new network
	 * @return A cyNetwork which is the difference of the input graphs
	 */
	public static CyNetwork createDifferenceGraph2(List networkList, boolean copyView, String title) {
		return performNetworkOperation(networkList, DIFFERENCE2, copyView, title);
	}

	/**
	 * Protected helper function that actually does the heavy lifting to perform
	 * the set operations. For explantion of the input parameters, see any of
	 * the public methods.
	 *
	 * @return cyNetwork created from applying this set operation
	 */
	protected static CyNetwork performNetworkOperation(List networkList, int operation,
	                                                   boolean copyView, String title) {
		/*
		 * We require at least one network for this operation This should be
		 * enforced by hte GUI, bujt we will check it here as well
		 */
		if (networkList.size() == 0) {
			throw new IllegalArgumentException("Must have at least one network in the list");
		}

		/*
		 * Just handle each type of operation independently, this will cause
		 * some potential duplications of code, but it should be a little bit
		 * more readable
		 */
		int[] new_nodes = null;
		int[] new_edges = null;

		switch (operation) {
			case UNION:
				new_nodes = GraphSetUtils.unionizeNodes(networkList);
				new_edges = GraphSetUtils.unionizeEdges(networkList);
				break;
			case INTERSECTION:
				new_nodes = GraphSetUtils.intersectNodes(networkList);
				new_edges = GraphSetUtils.intersectEdges(networkList);
				CyLogger.getLogger().warn("number of intersecting nodes is " + new_nodes.length);
				break;
			case DIFFERENCE:
				new_edges = GraphSetUtils.differenceEdges(networkList);
				new_nodes = GraphSetUtils.differenceNodes(networkList, new_edges);
				break;
			case DIFFERENCE2:
				new_nodes = GraphSetUtils.differenceNodes2(networkList);
				new_edges = GraphSetUtils.differenceEdges2(networkList, new_nodes);
				break;
			default:
				throw new IllegalArgumentException("Specified invalid graph set operation");
		}

		// create the new network
		CyNetwork newNetwork = Cytoscape.createNetwork(new_nodes, new_edges, title);

		// get the visual style for the first network in the list and try to apply
		// it to the new network.
		CyNetwork firstNetwork = (CyNetwork)networkList.get(0);
		CyNetworkView firstView =  Cytoscape.getNetworkView( firstNetwork.getIdentifier() );
		if ( firstView != null && firstView != Cytoscape.getNullNetworkView() ) {
			VisualStyle firstVS = firstView.getVisualStyle();

			CyNetworkView newView = Cytoscape.getNetworkView( newNetwork.getIdentifier() );
			if ( newView != null && newView != Cytoscape.getNullNetworkView() && firstVS != null ) {
				newView.setVisualStyle(firstVS.getName()); 
				Cytoscape.getVisualMappingManager().setVisualStyle(firstVS);
				newView.redrawGraph(true,true);
			}
		}

		return newNetwork;
	}

	/**
	 * Determine the set of difference edges. This apply a straight set difference
	 * operation to the edge sets.
	 * @param networkList A lists containing cyNetworks
	 * @return an integer array containing the set of edges in the difference
	 */
	protected static int[] differenceEdges(List networkList) {
		final List<Edge> edges = new Vector<Edge>();

		/*
		 * For each node in the first network, chech to make sure that it is not
		 * present in all the other networks, add it to the list if this is the
		 * case
		 */
		CyNetwork firstNetwork = (CyNetwork) networkList.get(0);
EDGE_LOOP: 
		for (Iterator edgeIt = firstNetwork.edgesIterator(); edgeIt.hasNext();) {
			Edge currentEdge = (Edge) edgeIt.next();

			for (int idx = 1; idx < networkList.size(); idx++) {
				CyNetwork currentNetwork = (CyNetwork) networkList.get(idx);

				if (currentNetwork.containsEdge(currentEdge)) {
					continue EDGE_LOOP;
				}
			}

			edges.add(currentEdge);
		}

		int[] result = new int[edges.size()];
		int idx = 0;

		for (Iterator edgeIt = edges.iterator(); edgeIt.hasNext(); idx++) {
			Edge currentEdge = (Edge) edgeIt.next();
			result[idx] = currentEdge.getRootGraphIndex();
		}

		return result;
	}

	/**
	 * Returns the nodes of the 1st network that are not contained in any of the other networks.
	 * @param networkList A lists containing cyNetworks
	 * @return an integer array containing the set of nodes in the difference
	 */
	protected static int[] differenceNodes2(List networkList) {
		final List<Node> nodes = new Vector<Node>();
		final CyNetwork firstNetwork = (CyNetwork)networkList.get(0);
NODE_LOOP:
		for (Iterator nodeIt = firstNetwork.nodesIterator(); nodeIt.hasNext(); /* Empty! */)
		{
			Node currentNode = (Node) nodeIt.next();

			for (int idx = 1; idx < networkList.size(); idx++) {
				CyNetwork currentNetwork = (CyNetwork) networkList.get(idx);

				if (currentNetwork.containsNode(currentNode))
					continue NODE_LOOP;
			}

			nodes.add(currentNode);
		}

		final int[] result = new int[nodes.size()];
		int idx = 0;
		for (Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); idx++) {
			Node currentNode = (Node) nodeIt.next();
			result[idx] = currentNode.getRootGraphIndex();
		}

		return result;
	}

	/**
	 * Determine the set of difference nodes. In order to perform this operation
	 * we also have to know the set of edges in the edge difference, so that we
	 * can make sure that any nodes are present that are required by those
	 * edges
	 * @param networkList A lists containing cyNetworks
	 * @param edges The difference set of edges
	 * @return an integer array containing the set of edges in the difference
	 */
	protected static int[] differenceNodes(List networkList, int[] edges) {
		final HashSet<Node> nodes = new HashSet<Node>();

		/*
		 * For each node in the first network, check to see if it is not present
		 * in any of the other networks, add it to the list if this is the case
		 */
		CyNetwork firstNetwork = (CyNetwork) networkList.get(0);
NODE_LOOP: 
		for (Iterator nodeIt = firstNetwork.nodesIterator(); nodeIt.hasNext();) {
			Node currentNode = (Node) nodeIt.next();

			for (int idx = 1; idx < networkList.size(); idx++) {
				CyNetwork currentNetwork = (CyNetwork) networkList.get(idx);

				if (currentNetwork.containsNode(currentNode)) {
					continue NODE_LOOP;
				}
			}

			nodes.add(currentNode);
		}

		/*
		 * Now we need to make sure that any nodes required to be present are
		 * included (if these nodes connect an edge in the difference set)
		 */
		for (int idx = 0; idx < edges.length; idx++) {
			nodes.add(firstNetwork.getNode(firstNetwork.getEdgeSourceIndex(edges[idx])));
			nodes.add(firstNetwork.getNode(firstNetwork.getEdgeTargetIndex(edges[idx])));
		}

		int[] result = new int[nodes.size()];
		int idx = 0;

		for (Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); idx++) {
			Node currentNode = (Node) nodeIt.next();
			result[idx] = currentNode.getRootGraphIndex();
		}

		return result;
	}

	/**
	 * Determine the set of difference edges. In order to perform this operation
	 * we also have to know the set of nodes in the node difference, so that we
	 * can make sure that any edges are present that are required by those
	 * nodes
	 * @param networkList A lists containing cyNetworks
	 * @param nodes The difference set of nodes
	 * @return an integer array containing the set of nodes in the difference
	 */
	protected static int[] differenceEdges2(List networkList, int[] nodes) {
		final HashSet<Edge> edges = new HashSet<Edge>();

		/*
		 * For each edge in the first network, check to see if it is not present
		 * in any of the other networks, add it to the list if this is the case
		 */
		CyNetwork firstNetwork = (CyNetwork) networkList.get(0);
EDGE_LOOP: 
		for (Iterator edgeIt = firstNetwork.edgesIterator(); edgeIt.hasNext(); /* Empty! */)
		{
			Edge currentEdge = (Edge) edgeIt.next();

			for (int idx = 1; idx < networkList.size(); idx++) {
				final CyNetwork currentNetwork = (CyNetwork) networkList.get(idx);
				if (currentNetwork.containsEdge(currentEdge))
					continue EDGE_LOOP;
			}

			edges.add(currentEdge);
		}

		/*
		 * Now we need to make sure that we do not keep edges if either source or target
		 * nodes are not in "nodes."
		 */

		final Set<Integer> knownNodes = new HashSet<Integer>(nodes.length);
		for (final int nodeIndex : nodes)
			knownNodes.add(nodeIndex);

		final ArrayList<Integer> acceptableEdges = new ArrayList<Integer>(edges.size());
		for (final Edge edgeCandidate : edges) {
			if (knownNodes.contains(edgeCandidate.getSource())
			    && knownNodes.contains(edgeCandidate.getTarget()))
				acceptableEdges.add(edgeCandidate.getRootGraphIndex());
		}

		final int[] result = new int[acceptableEdges.size()];
		int index = 0;
		for (final int edgeIndex : acceptableEdges)
			result[index++] = edgeIndex;
		return result;
	}

	/**
	 * Apply a simple intersection operation to the node sets
	 * @param networkList A list of cyNetworks
	 * @return an integer array which contains the indices of nodes in the intersection
	 */
	protected static int[] intersectNodes(List networkList) {
		final List<Node> nodes = new Vector<Node>();

		/*
		 * For each node in the first network, check to see if it is present in
		 * all of the other networks, add it to the list if this is the case
		 */
		CyNetwork firstNetwork = (CyNetwork) networkList.get(0);
NODE_LOOP: 
		for (Iterator nodeIt = firstNetwork.nodesIterator(); nodeIt.hasNext();) {
			Node currentNode = (Node) nodeIt.next();

			for (int idx = 1; idx < networkList.size(); idx++) {
				CyNetwork currentNetwork = (CyNetwork) networkList.get(idx);

				if (!currentNetwork.containsNode(currentNode)) {
					continue NODE_LOOP;
				}
			}

			nodes.add(currentNode);
		}

		int[] result = new int[nodes.size()];
		int idx = 0;

		for (Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); idx++) {
			Node currentNode = (Node) nodeIt.next();
			result[idx] = currentNode.getRootGraphIndex();
		}

		return result;
	}

	/**
	 * Apply a simple intersection operation to the edge sets
	 * @param networkList A list of cyNetworks
	 * @return an integer array which contains the indices of edges in the intersection
	 */
	protected static int[] intersectEdges(List networkList) {
		final List<Edge> edges = new Vector<Edge>();

		/*
		 * For each node in the first network, check to see if it is present in
		 * all of the other networks, add it to the list if this is the case
		 */
		CyNetwork firstNetwork = (CyNetwork) networkList.get(0);
EDGE_LOOP: 
		for (Iterator edgeIt = firstNetwork.edgesIterator(); edgeIt.hasNext();) {
			Edge currentEdge = (Edge) edgeIt.next();

			for (int idx = 1; idx < networkList.size(); idx++) {
				CyNetwork currentNetwork = (CyNetwork) networkList.get(idx);

				if (!currentNetwork.containsEdge(currentEdge)) {
					continue EDGE_LOOP;
				}
			}

			edges.add(currentEdge);
		}

		int[] result = new int[edges.size()];
		int idx = 0;

		for (Iterator edgeIt = edges.iterator(); edgeIt.hasNext(); idx++) {
			Edge currentEdge = (Edge) edgeIt.next();
			result[idx] = currentEdge.getRootGraphIndex();
		}

		return result;
	}

	/**
	 * Makes nodes request overtime pay.
	 * @param networkList a list of cyNetworks
	 * @return an integer array containing the indices of nodes in the union
	 */
	protected static int[] unionizeNodes(List networkList) {
		/*
		 * This is the set of nodes that will be in the final merged network
		 */
		Set<Integer> nodes = new HashSet<Integer>();

		for (Iterator it = networkList.iterator(); it.hasNext();) {
			CyNetwork currentNetwork = (CyNetwork) it.next();

			for (Iterator nodeIt = currentNetwork.nodesIterator(); nodeIt.hasNext();) {
				nodes.add(new Integer(((Node) nodeIt.next()).getRootGraphIndex()));
			}
		}

		int[] result = new int[nodes.size()];
		int idx = 0;

		for (Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); idx++) {
			result[idx] = ((Integer) nodeIt.next()).intValue();
		}

		return result;
	}

	/**
	 * Perform a simple set union on the sets of nodes
	 * @param networkList a list of cyNetworks
	 * @return an integer array containing the indices of edges in the union
	 */
	protected static int[] unionizeEdges(List networkList) {
		/*
		 * This is the set of edges that will be in the final network
		 */
		Set<Integer> edges = new HashSet<Integer>();

		for (Iterator it = networkList.iterator(); it.hasNext();) {
			CyNetwork currentNetwork = (CyNetwork) it.next();

			for (Iterator edgeIt = currentNetwork.edgesIterator(); edgeIt.hasNext();) {
				edges.add(new Integer(((Edge) edgeIt.next()).getRootGraphIndex()));
			}
		}

		int[] result = new int[edges.size()];
		int idx = 0;

		for (Iterator edgeIt = edges.iterator(); edgeIt.hasNext(); idx++) {
			result[idx] = ((Integer) edgeIt.next()).intValue();
		}

		return result;
	}
}
