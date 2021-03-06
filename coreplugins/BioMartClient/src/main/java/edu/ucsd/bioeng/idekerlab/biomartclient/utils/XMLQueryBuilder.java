
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

package edu.ucsd.bioeng.idekerlab.biomartclient.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 *
 */
public class XMLQueryBuilder {
	private static DocumentBuilderFactory factory;
	private static DocumentBuilder builder;

	static {
		factory = DocumentBuilderFactory.newInstance();

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param dataset DOCUMENT ME!
	 * @param attrs DOCUMENT ME!
	 * @param filters DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static String getQueryString(Dataset dataset, Attribute[] attrs, Filter[] filters) {
		final Document doc = builder.newDocument();
		Element query = doc.createElement("Query");
		query.setAttribute("virtualSchemaName", "default");
		query.setAttribute("header", "1");
		query.setAttribute("uniqueRows", "1");
		query.setAttribute("count", "");
		query.setAttribute("datasetConfigVersion", "0.6");
		query.setAttribute("formatter", "TSV");

		doc.appendChild(query);

		Element ds = doc.createElement("Dataset");
		ds.setAttribute("name", dataset.getName());
		query.appendChild(ds);

		for (Attribute attr : attrs) {
			Element at = doc.createElement("Attribute");
			at.setAttribute("name", attr.getName());
			ds.appendChild(at);
		}

		if ((filters != null) && (filters.length != 0)) {
			for (Filter filter : filters) {
				Element ft = doc.createElement("Filter");
				ft.setAttribute("name", filter.getName());
				if(filter.getValue() == null) {
					ft.setAttribute("excluded", "0");
				} else 
					ft.setAttribute("value", filter.getValue());
				ds.appendChild(ft);
			}
		}

		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf;
		String result = null;

		try {
			tf = tff.newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			StringWriter strWtr = new StringWriter();
			StreamResult strResult = new StreamResult(strWtr);

			tf.transform(new DOMSource(doc.getDocumentElement()), strResult);

			result = strResult.getWriter().toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}
