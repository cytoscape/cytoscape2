/*
 Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)

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
package cytoscape.visual.properties;

import cytoscape.Cytoscape;

import cytoscape.visual.*;
import cytoscape.visual.VisualPropertyType;

import cytoscape.visual.ui.icon.LineTypeIcon;

import cytoscape.visual.VisualPropertyDependency;

import giny.view.EdgeView;
import giny.view.Label;

import java.awt.Color;
import java.awt.Font;

import java.util.Properties;

import javax.swing.Icon;


/**
 *
 */
public class EdgeLabelColorProp extends AbstractVisualProperty {
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public VisualPropertyType getType() {
		return VisualPropertyType.EDGE_LABEL_COLOR;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Icon getIcon(final Object value) {
		final LineTypeIcon icon = new LineTypeIcon();
		icon.setColor(new Color(10, 10, 10, 0));
		icon.setText("Font");

		final Color fontColor = (Color) value;
		final Font defFont = (Font) VisualPropertyType.EDGE_FONT_FACE.getDefault(Cytoscape.getVisualMappingManager()
		                                                                                  .getVisualStyle());
		icon.setTextFont(new Font(defFont.getFontName(), defFont.getStyle(), 24));
		icon.setBottomPadding(-7);
		icon.setTextColor(fontColor);

		return icon;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param ev DOCUMENT ME!
	 * @param o DOCUMENT ME!
	 */
	public void applyToEdgeView(EdgeView ev, Object o, VisualPropertyDependency dep) {
		if ((o == null) || (ev == null))
			return;

		Label edgelabel = ev.getLabel();

		if (!((Color) o).equals(edgelabel.getTextPaint()))
			edgelabel.setTextPaint((Color) o);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Object getDefaultAppearanceObject() {
		return Color.black;
	}
}
