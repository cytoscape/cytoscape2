/*
  File: CyNetworkEventTest.java

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

//-------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//-------------------------------------------------------------------------
package cytoscape.data;

import cytoscape.*;

//--------------------------------------------------------------------------------------
import junit.framework.*;

import java.io.*;

import java.util.*;


//-----------------------------------------------------------------------------------------
/**
 *
 */
public class CyNetworkEventTest extends TestCase {
	//------------------------------------------------------------------------------
	/**
	 * Creates a new CyNetworkEventTest object.
	 *
	 * @param name  DOCUMENT ME!
	 */
	public CyNetworkEventTest(String name) {
		super(name);
	}

	//------------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public void setUp() throws Exception {
	}

	//------------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public void tearDown() throws Exception {
	}

	//------------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public void testBasic() throws Exception {
		CyNetwork network = Cytoscape.createNetwork(null);

		//test begin event
		CyNetworkEvent e0 = new CyNetworkEvent(network, CyNetworkEvent.BEGIN);
		assertTrue(e0.getNetwork() == network);
		assertTrue(e0.getType() == CyNetworkEvent.BEGIN);

		//test end event
		CyNetworkEvent e1 = new CyNetworkEvent(network, CyNetworkEvent.END);
		assertTrue(e1.getNetwork() == network);
		assertTrue(e1.getType() == CyNetworkEvent.END);

		//test null network
		CyNetworkEvent eNull = new CyNetworkEvent(null, CyNetworkEvent.BEGIN);
		assertTrue(eNull.getNetwork() == null);
		assertTrue(eNull.getType() == CyNetworkEvent.BEGIN);

		//test graph replaced event
		CyNetworkEvent e2 = new CyNetworkEvent(network, CyNetworkEvent.GRAPH_REPLACED);
		assertTrue(e2.getNetwork() == network);
		assertTrue(e2.getType() == CyNetworkEvent.GRAPH_REPLACED);

		//test unknown event
		CyNetworkEvent e3 = new CyNetworkEvent(network, CyNetworkEvent.UNKNOWN);
		assertTrue(e3.getNetwork() == network);
		assertTrue(e3.getType() == CyNetworkEvent.UNKNOWN);

		//test invalid type
		CyNetworkEvent eBad = new CyNetworkEvent(network, -7);
		assertTrue(eBad.getNetwork() == network);
		assertTrue(eBad.getType() == CyNetworkEvent.UNKNOWN);
	}

	//-------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(new TestSuite(CyNetworkEventTest.class));
	}

	//-------------------------------------------------------------------------
}
