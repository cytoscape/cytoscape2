package cytoscape.visual.customgraphic.impl;

import giny.view.ObjectPosition;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import cytoscape.visual.customgraphic.CyCustomGraphics;
import cytoscape.visual.customgraphic.CyCustomGraphicsParser;
import cytoscape.visual.customgraphic.IDGenerator;
import cytoscape.visual.customgraphic.Layer;
import cytoscape.visual.customgraphic.Taggable;
import ding.view.ObjectPositionImpl;

public abstract class AbstractDCustomGraphics implements
		CyCustomGraphics, Taggable {

	protected static final String DELIMITER = ",";
	public static final String LIST_DELIMITER = "|";
	
	protected float fitRatio = 0.9f;

	// Unique ID
	protected final Long id;
	
	// Layers of Ding Custom Graphic objects.
	protected List<Layer> layers;
	
	// Human readable name
	protected String displayName;
	
	protected int width = 50;
	protected int height = 50;
	
	protected CyCustomGraphicsParser parser;

	protected ObjectPosition position;

	// For tags
	protected final SortedSet<String> tags;


	public AbstractDCustomGraphics(final String displayName) {
		this(IDGenerator.getIDGenerator().getNextId(), displayName);
	}
	
	
	/**
	 * Create new object for a given ID.
	 * Used when restoring session.
	 * 
	 * @param id
	 * @param displayName
	 */
	public AbstractDCustomGraphics(final Long id, final String displayName) {
		this.id = id;
		
		this.layers = new ArrayList<Layer>();
		this.displayName = displayName;

		this.tags = new TreeSet<String>();
		this.position = new ObjectPositionImpl();
	}
	
	
	public Long getIdentifier() {
		return id;
	}
	
	public void setWidth(final int width) {
		this.width = width;
	}
	
	public void setHeight(final int height) {
		this.height = height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}

	
	public List<Layer> getLayers() {
		return layers;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public Image getRenderedImage() {
		return null;
	}


	public Collection<String> getTags() {
		return tags;
	}


	public ObjectPosition getPosition() {
		return position;
	}

	public void setPosition(final ObjectPosition position) {
		this.position = position;
	}
	

	// This will be used prop file.
	public String toString() {
		String tagStr = "";
		// Build tags as a string
		if (tags.size() != 0) {
			final StringBuilder builder = new StringBuilder();
			for (String tag : tags)
				builder.append(tag + LIST_DELIMITER);
			String temp = builder.toString();
			tagStr = temp.substring(0, temp.length() - 1);
		}

		String name = displayName;
		if (displayName.contains(",")) {
			// Replace delimiter
			name = displayName.replace(",", "___");
		}

		return this.getClass().getName() + DELIMITER + this.getIdentifier()
				+ DELIMITER + name + DELIMITER + tagStr;
	}
	
	public void setFitRatio(float fitRatio) {
		this.fitRatio = fitRatio;
	}
	
	public float getFitRatio() {
		return fitRatio;
	}

}
