/*
  File: Misc.java

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

// Misc.java:  miscellaneous static utilities
package cytoscape.util;

import java.awt.Color;
import java.util.Properties;
import java.util.Vector;

/**
 *
 */
public class Misc {

	/**
	 * Convert string to color object.
	 * 
	 * @param text
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @deprecated will be removed in April, 2011. use ColorUtil.parseColorText
	 *             instead.
	 */
	@Deprecated
	public static Color parseRGBText(final String colorAsText) {
		return ColorUtil.parseColorText(colorAsText);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param color
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @deprecated Will be removed April, 2011. Use ColorUtil.getColorAsText
	 *             instead.
	 * 
	 */
	@Deprecated
	public static String getRGBText(Color color) {
		return ColorUtil.getColorAsText(color);
	}

	/**
	 * return the (possibly multiple) value of the specified property as a
	 * vector. property values (which typically come from cytoscape.prop files)
	 * are usually scalar strings, but may be a list of such strings, surrounded
	 * by parentheses, and delimited by the value of a property called
	 * 'property.delimiter' (whose value is usually "::") get the property
	 * value; check to see if it is a list; parse it if necessary
	 */
	static public Vector getPropertyValues(Properties props, String propName) {
		String propertyDelimiterName = "property.token.delimiter";
		String delimiter = props.getProperty(propertyDelimiterName, "::");

		String listStartTokenName = "list.startToken";
		String listStartToken = props.getProperty(listStartTokenName, "(");

		String listEndTokenName = "list.endToken";
		String listEndToken = props.getProperty(listEndTokenName, ")");

		Vector result = new Vector();
		String propString = props.getProperty(propName);

		if (propString == null)
			return result;

		String propStringTrimmed = propString.trim();
		String[] tokens = Misc.parseList(propStringTrimmed, listStartToken,
				listEndToken, delimiter);

		for (int i = 0; i < tokens.length; i++)
			result.add(tokens[i]);

		return result;
	} // getPropertyValues

	/**
	 * DOCUMENT ME!
	 * 
	 * @param listString
	 *            DOCUMENT ME!
	 * @param startToken
	 *            DOCUMENT ME!
	 * @param endToken
	 *            DOCUMENT ME!
	 * @param delimiter
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	static public boolean isList(String listString, String startToken,
			String endToken, String delimiter) {
		String s = listString.trim();
		Vector list = new Vector();

		if (s.startsWith(startToken) && s.endsWith(endToken))
			return true;
		else

			return false;
	} // isList

	/**
	 * DOCUMENT ME!
	 * 
	 * @param listString
	 *            DOCUMENT ME!
	 * @param startToken
	 *            DOCUMENT ME!
	 * @param endToken
	 *            DOCUMENT ME!
	 * @param delimiter
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	static public String[] parseList(String listString, String startToken,
			String endToken, String delimiter) {
		String s = listString.trim();

		if (s.startsWith(startToken) && s.endsWith(endToken)) {
			s = s.substring(1, s.length() - 1);

			return s.split(delimiter);
		} else {
			String[] unparseableResult = new String[1];
			unparseableResult[0] = listString;

			return unparseableResult;
		}

		/*********************
		 * StringTokenizer strtok = new StringTokenizer (deparenthesizedString,
		 * delimiter); int count = strtok.countTokens (); for (int i=0; i <
		 * count; i++) list.add (strtok.nextToken ()); } else list.add
		 * (listString);
		 * 
		 * return (String []) list.toArray (new String [0]);
		 **********************/
	} // parseList
} // class Misc
