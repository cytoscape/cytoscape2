/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

import cytoscape.data.ImportHandler;

import cytoscape.data.readers.GraphReader;
import cytoscape.data.readers.InteractionsReader;

import cytoscape.logger.CyLogger;

import java.net.URL;
import java.net.URLConnection;

/**
 * FileFilter for Reading in Cytoscape SIF Files.
 *
 * @author Cytoscape Development Team.
 */
public class SIFFileFilter extends CyFileFilter {
	/**
	 * SIF Files are Graphs.
	 */
	private static String fileNature = ImportHandler.GRAPH_NATURE;

	/**
	 * File Extension.
	 */
	private static String fileExtension = "sif";

	/**
	 * Content Types
	 */
	private static String[] contentTypes = { "text/sif" };

	/**
	 * Filter Description.
	 */
	private static String description = "SIF files";

	/**
	 * Constructor.
	 */
	public SIFFileFilter() {
		super(fileExtension, description, fileNature);
	}

	/**
	 * Gets Graph Reader.
	 * @param fileName File name.
	 * @return GraphReader Object.
	 */
	public GraphReader getReader(String fileName) {
		reader = new InteractionsReader(fileName);

		return reader;
	}

	/**
	 * Gets Graph Reader.
	 * @param fileName File name.
	 * @return GraphReader Object.
	 */
	public GraphReader getReader(URL url, URLConnection conn) {
		try {
			// Get the input stream
			reader = new InteractionsReader(conn.getInputStream(), url.toString());
		} catch (Exception e) {
			CyLogger.getLogger(SIFFileFilter.class).error("Unable to get SIF reader: "+e.getMessage());
			reader = null;
		}
		return reader;
	}
}
