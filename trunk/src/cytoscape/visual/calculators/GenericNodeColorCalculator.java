/*
  File: GenericNodeColorCalculator.java

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
package cytoscape.visual.calculators;

import cytoscape.CyNetwork;

import cytoscape.visual.NodeAppearance;

import cytoscape.visual.mappings.ObjectMapping;

import cytoscape.visual.parsers.DoubleParser;

import cytoscape.visual.ui.VizMapUI;

import giny.model.Node;

import java.awt.Color;

import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;


/** @deprecated Use NodeFillColor or NodeBorderColor instead.
    will be removed 10/2007 */
public class GenericNodeColorCalculator extends AbstractNodeColorCalculator
    implements NodeColorCalculator {
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public byte getType() {
		return colType;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getPropertyLabel() {
		return propertyLabel;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getTypeName() {
		return typename;
	}

	protected String getClassName() {
		if (getType() == VizMapUI.NODE_COLOR)
			return "cytoscape.visual.calculators.GenericNodeFillColorCalculator";

		if (getType() == VizMapUI.NODE_BORDER_COLOR)
			return "cytoscape.visual.calculators.GenericNodeBorderColorCalculator";

		System.out.println("GenericNodeColorCalculator unknown type:  " + getType() + " " + colType);

		return getClass().getName();
	}

	GenericNodeColorCalculator() {
		super();
		// do this as a better default than 0,null,null - still not good though	
		set(VizMapUI.NODE_COLOR, "nodeFillColorCalculator", "Node Color");
	}

	/**
	 * Creates a new GenericNodeColorCalculator object.
	 *
	 * @param name  DOCUMENT ME!
	 * @param m  DOCUMENT ME!
	 */
	public GenericNodeColorCalculator(String name, ObjectMapping m) {
		super(name, m);
		// do this as a better default than 0,null,null - still not good though	
		set(VizMapUI.NODE_COLOR, "nodeFillColorCalculator", "Node Color");
	}

	/**
	 * Creates a new GenericNodeColorCalculator object.
	 *
	 * @param name  DOCUMENT ME!
	 * @param props  DOCUMENT ME!
	 * @param baseKey  DOCUMENT ME!
	 */
	public GenericNodeColorCalculator(String name, Properties props, String baseKey) {
		super(name, props, baseKey);
		// do this as a better default than 0,null,null - still not good though	
		set(VizMapUI.NODE_COLOR, "nodeFillColorCalculator", "Node Color");
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param appr DOCUMENT ME!
	 * @param node DOCUMENT ME!
	 * @param network DOCUMENT ME!
	 */
	public void apply(NodeAppearance appr, Node node, CyNetwork network) {
		if (getType() == VizMapUI.NODE_COLOR)
			apply(appr, node, network, FILL);
		else if (getType() == VizMapUI.NODE_BORDER_COLOR)
			apply(appr, node, network, BORDER);
		else
			System.err.println("don't know what kind of calculator this is!");
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 * @param n DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Color calculateNodeColor(Node e, CyNetwork n) {
		NodeAppearance ea = new NodeAppearance();
		apply(ea, e, n);

		if (getType() == VizMapUI.NODE_COLOR)
			return ea.getFillColor();
		else if (getType() == VizMapUI.NODE_BORDER_COLOR)
			return ea.getBorderColor();
		else

			return Color.white;
	}
}
