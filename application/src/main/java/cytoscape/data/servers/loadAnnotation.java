/*
  File: loadAnnotation.java

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

// loadAnnotation

//------------------------------------------------------------------------------
// $Revision$   
// $Date$ 
// $Author$
//-----------------------------------------------------------------------------------
package cytoscape.data.servers;

import cytoscape.data.annotation.*;
import cytoscape.data.annotation.readers.*;
import cytoscape.logger.CyLogger;

import java.io.*;

//-----------------------------------------------------------------------------------
import java.rmi.*;

import java.util.Vector;


//------------------------------------------------------------------------------
/**
 *  load an annotation into an rmi biodata server.  an annotation -- necessarily
 *  accompanied by an ontology, whose location is specified in the annotation's xml
 *  file -- specifies the relationship of an entity (i.e., a gene) to one or more nodes
 *  (integers) in the ontology.  from this assignment, the full ontological hierarchy
 *  can be deduced.
 *
 *  @see cytoscape.data.annotation.Annotation
 *  @see cytoscape.data.annotation.Ontology
 */
public class loadAnnotation {
	//------------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			CyLogger.getLogger().warn("usage:  loadAnnotation <server name> <annotation.xml>");
			System.exit(1);
		}

		String serverName = args[0];
		BioDataServer server = new BioDataServer(serverName);

		String filename = args[1];

		File xmlFile = new File(filename);

		if (!xmlFile.canRead()) {
			CyLogger.getLogger().warn("--- cytoscape.data.servers.loadAnnotation error:  cannot read");
			CyLogger.getLogger().warn("        " + filename);
			System.exit(1);
		}

		AnnotationXmlReader reader = new AnnotationXmlReader(xmlFile);
		server.addAnnotation(reader.getAnnotation());

		CyLogger.getLogger().info(server.describe());
	} // main
	  //------------------------------------------------------------------------------
} // loadAnnotation
