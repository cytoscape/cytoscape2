/*
 File: LabelPosition.java

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
package cytoscape.visual;

import giny.view.Label;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cytoscape.logger.CyLogger;

/**
 * Deprecated. Use ObjectPosition instead. Will be removed 03/2011
 * 
 * @deprecated
 */
@Deprecated
public class LabelPosition {
	/**
	 *
	 */
	public static final String northWestName = "Northwest";

	/**
	 *
	 */
	public static final String northName = "North";

	/**
	 *
	 */
	public static final String northEastName = "Northeast";

	/**
	 *
	 */
	public static final String westName = "West";

	/**
	 *
	 */
	public static final String centerName = "Center";

	/**
	 *
	 */
	public static final String eastName = "East";

	/**
	 *
	 */
	public static final String southWestName = "Southwest";

	/**
	 *
	 */
	public static final String southName = "South";

	/**
	 *
	 */
	public static final String southEastName = "Southeast";
	protected static final String nwName = "NW";
	protected static final String nName = "N";
	protected static final String neName = "NE";
	protected static final String wName = "W";
	protected static final String cName = "C";
	protected static final String eName = "E";
	protected static final String swName = "SW";
	protected static final String sName = "S";
	protected static final String seName = "SE";

	/**
	 *
	 */
	public static final String noName = "none";

	/**
	 *
	 */
	public static final String justifyCenterName = "Center Justified";

	/**
	 *
	 */
	public static final String justifyLeftName = "Left Justified";

	/**
	 *
	 */
	public static final String justifyRightName = "Right Justified";
	protected static final String justifyCName = "c";
	protected static final String justifyLName = "l";
	protected static final String justifyRName = "r";
	protected int labelAnchor;
	protected int targetAnchor;
	protected int justify;
	protected double xOffset;
	protected double yOffset;

	/**
	 * Creates a new LabelPosition object.
	 */
	public LabelPosition() {
		this(Label.CENTER, Label.CENTER, Label.JUSTIFY_CENTER, 0.0, 0.0);
	}

	/**
	 * Creates a new LabelPosition object.
	 * 
	 * @param lp
	 *            DOCUMENT ME!
	 */
	public LabelPosition(LabelPosition lp) {
		targetAnchor = lp.getTargetAnchor();
		labelAnchor = lp.getLabelAnchor();
		xOffset = lp.getOffsetX();
		yOffset = lp.getOffsetY();
		justify = lp.getJustify();
	}

	/**
	 * Creates a new LabelPosition object.
	 * 
	 * @param targ
	 *            DOCUMENT ME!
	 * @param lab
	 *            DOCUMENT ME!
	 * @param just
	 *            DOCUMENT ME!
	 * @param x
	 *            DOCUMENT ME!
	 * @param y
	 *            DOCUMENT ME!
	 */
	public LabelPosition(int targ, int lab, int just, double x, double y) {
		targetAnchor = targ;
		labelAnchor = lab;
		justify = just;
		xOffset = x;
		yOffset = y;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getLabelAnchor() {
		return labelAnchor;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getTargetAnchor() {
		return targetAnchor;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getJustify() {
		return justify;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getOffsetX() {
		return xOffset;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public double getOffsetY() {
		return yOffset;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param b
	 *            DOCUMENT ME!
	 */
	public void setLabelAnchor(int b) {
		labelAnchor = b;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param b
	 *            DOCUMENT ME!
	 */
	public void setTargetAnchor(int b) {
		targetAnchor = b;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param b
	 *            DOCUMENT ME!
	 */
	public void setJustify(int b) {
		justify = b;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param d
	 *            DOCUMENT ME!
	 */
	public void setOffsetX(double d) {
		xOffset = d;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param d
	 *            DOCUMENT ME!
	 */
	public void setOffsetY(double d) {
		yOffset = d;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param lp
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean equals(Object lp) {
		if (lp == null)
			return false;

		if (lp instanceof LabelPosition) {
			LabelPosition LP = (LabelPosition) lp;

			if (Math.abs(LP.getOffsetX() - xOffset) > 0.0000001) {
				CyLogger.getLogger().info("xoff");

				return false;
			}

			if (Math.abs(LP.getOffsetY() - yOffset) > 0.0000001) {
				CyLogger.getLogger().info("yoff");

				return false;
			}

			if (LP.getLabelAnchor() != labelAnchor) {
				CyLogger.getLogger().info("label");

				return false;
			}

			if (LP.getTargetAnchor() != targetAnchor) {
				CyLogger.getLogger().info("taret");

				return false;
			}

			if (LP.getJustify() != justify) {
				CyLogger.getLogger().info("justify");

				return false;
			}

			return true;
		} else {
			return false;
		}
	}

	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param b
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String convert(int b) {
		switch (b) {
		case (Label.NORTH):
			return northName;

		case (Label.SOUTH):
			return southName;

		case (Label.EAST):
			return eastName;

		case (Label.WEST):
			return westName;

		case (Label.NORTHWEST):
			return northWestName;

		case (Label.NORTHEAST):
			return northEastName;

		case (Label.SOUTHWEST):
			return southWestName;

		case (Label.SOUTHEAST):
			return southEastName;

		case (Label.CENTER):
			return centerName;

		case (Label.NONE):
			return noName;

		case (Label.JUSTIFY_CENTER):
			return justifyCenterName;

		case (Label.JUSTIFY_LEFT):
			return justifyLeftName;

		case (Label.JUSTIFY_RIGHT):
			return justifyRightName;

		default:
			return null;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param s
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static int convert(String s) {
		if (northName.equals(s) || nName.equals(s))
			return Label.NORTH;
		else if (southName.equals(s) || sName.equals(s))
			return Label.SOUTH;
		else if (eastName.equals(s) || eName.equals(s))
			return Label.EAST;
		else if (westName.equals(s) || wName.equals(s))
			return Label.WEST;
		else if (northWestName.equals(s) || nwName.equals(s))
			return Label.NORTHWEST;
		else if (northEastName.equals(s) || neName.equals(s))
			return Label.NORTHEAST;
		else if (southWestName.equals(s) || swName.equals(s))
			return Label.SOUTHWEST;
		else if (southEastName.equals(s) || seName.equals(s))
			return Label.SOUTHEAST;
		else if (centerName.equals(s) || cName.equals(s))
			return Label.CENTER;
		else if (justifyCenterName.equals(s) || justifyCName.equals(s))
			return Label.JUSTIFY_CENTER;
		else if (justifyLeftName.equals(s) || justifyLName.equals(s))
			return Label.JUSTIFY_LEFT;
		else if (justifyRightName.equals(s) || justifyRName.equals(s))
			return Label.JUSTIFY_RIGHT;
		else

			return -1;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String[] getJustifyNames() {
		String[] s = { justifyLeftName, justifyCenterName, justifyRightName };

		return s;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String[] getAnchorNames() {
		String[] s = { northWestName, northName, northEastName, westName,
				centerName, eastName, southWestName, southName, southEastName };

		return s;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("target: ").append(convert(targetAnchor));
		sb.append("  label: ").append(convert(labelAnchor));
		sb.append("  justify: ").append(convert(justify));
		sb.append("  X offset: ").append(Double.toString(xOffset));
		sb.append("  Y offset: ").append(Double.toString(yOffset));

		return sb.toString();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String shortString() {
		// force the locale to US so that we consistently serialize
		DecimalFormat df = new DecimalFormat("#0.00;-#0.00", new DecimalFormatSymbols(Locale.US));

		StringBuffer sb = new StringBuffer();
		sb.append(getShortName(targetAnchor));
		sb.append(",");
		sb.append(getShortName(labelAnchor));
		sb.append(",");
		sb.append(getShortName(justify));
		sb.append(",");
		sb.append(df.format(xOffset));
		sb.append(",");
		sb.append(df.format(yOffset));

		return sb.toString();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param value
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static LabelPosition parse(String value) {
		Pattern p = Pattern
				.compile("^([NSEWC]{1,2}+),([NSEWC]{1,2}+),([clr]{1}+),(-?\\d+(.\\d+)?),(-?\\d+(.\\d+)?)$");
		Matcher m = p.matcher(value);

		if (m.matches()) {
			LabelPosition lp = new LabelPosition();
			lp.setTargetAnchor(convert(m.group(1)));
			lp.setLabelAnchor(convert(m.group(2)));
			lp.setJustify(convert(m.group(3)));
			lp.setOffsetX(Double.parseDouble(m.group(4)));
			lp.setOffsetY(Double.parseDouble(m.group(6)));

			return lp;
		}

		return null;
	}

	protected static String getShortName(int x) {
		switch (x) {
		case (Label.NORTH):
			return nName;

		case (Label.SOUTH):
			return sName;

		case (Label.EAST):
			return eName;

		case (Label.WEST):
			return wName;

		case (Label.NORTHWEST):
			return nwName;

		case (Label.NORTHEAST):
			return neName;

		case (Label.SOUTHWEST):
			return swName;

		case (Label.SOUTHEAST):
			return seName;

		case (Label.CENTER):
			return cName;

		case (Label.JUSTIFY_CENTER):
			return justifyCName;

		case (Label.JUSTIFY_LEFT):
			return justifyLName;

		case (Label.JUSTIFY_RIGHT):
			return justifyRName;

		default:
			CyLogger.getLogger().info("don't recognize type: " + x);

			return "x";
		}
	}

	/**
	 *
	 */
	public static final LabelPosition DEFAULT = new LabelPosition();
}
