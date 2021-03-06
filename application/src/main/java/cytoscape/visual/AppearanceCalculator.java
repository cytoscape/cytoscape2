/*
 File: AppearanceCalculator.java

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

import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.logger.CyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * This class calculates the appearance of a Node. It holds a default value and
 * a (possibly null) calculator for each visual attribute.
 */
abstract class AppearanceCalculator implements Cloneable {
	
	protected static final String CLONE_SUFFIX = "-clone-";
	
	protected final List<Calculator> calcs = new ArrayList<Calculator>();
	
	protected Appearance tmpDefaultAppearance;
	
	protected VisualPropertyDependency deps;

	/**
	 * Creates a new AppearanceCalculator object.
	 */
	public AppearanceCalculator(VisualPropertyDependency deps) {
		this.deps = deps; 
	}

	/**
	 * Creates a new AppearanceCalculator and immediately customizes it by
	 * calling applyProperties with the supplied arguments.
	 */
	public AppearanceCalculator(String name, Properties nacProps, String baseKey,
	                            CalculatorCatalog catalog, Appearance appr, VisualPropertyDependency deps) {
		this(deps);	
		tmpDefaultAppearance = appr;
		applyProperties(appr, name, nacProps, baseKey, catalog);
	}

	/**
	 * Creates a new AppearanceCalculator object.
	 *
	 * @param toCopy DOCUMENT ME!
	 */
	public AppearanceCalculator(AppearanceCalculator toCopy) {
		if (toCopy == null)
			return;

		for (Calculator c : toCopy.getCalculators())
			setCalculator(c);

		if (deps != null)
			deps.copy( toCopy.deps );

		copyDefaultAppearance(toCopy);
	}

	/**
	 * Make shallow copy of this object
	 */
	public Object clone() {
		Object copy = null;

		try {
			copy = super.clone();
		} catch (CloneNotSupportedException e) {
			CyLogger.getLogger().warn("Error cloning!");
		}

		return copy;
	}
	
	/**
	 * Make deep copy of this object
	 */
	public Object clone(String vsName) {
		Object copy = null;

		CyLogger.getLogger().info("====Cloning: " + this.toString());
		
		try {
			copy = super.clone();
		} catch (CloneNotSupportedException e) {
			CyLogger.getLogger().warn("Error cloning!");
		}

		return copy;
	}


	/**
	 * DOCUMENT ME!
	 *
	 * @param type DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Calculator getCalculator(final VisualPropertyType type) {
		for (Calculator nc : calcs) {
			if (nc.getVisualPropertyType() == type)
				return nc;
		}

		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public List<Calculator> getCalculators() {
		return calcs;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param type DOCUMENT ME!
	 */
	public void removeCalculator(final VisualPropertyType type) {
		Calculator toBeRemoved = null;

		for (Calculator c : calcs) {
			if (c.getVisualPropertyType() == type) {
				toBeRemoved = c;

				break;
			}
		}

		calcs.remove(toBeRemoved);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param c DOCUMENT ME!
	 */
	public void setCalculator(Calculator c) {
		if (c == null)
			return;

		Calculator toReplace = null;

		for (Calculator nc : calcs)
			if (nc.getVisualPropertyType() == c.getVisualPropertyType()) {
				toReplace = nc;

				break;
			}

		if (toReplace != null)
			calcs.remove(toReplace);

		calcs.add(c);
	}

	protected String getDescription(String name, Appearance defaultAppr) {
		final String lineSep = System.getProperty("line.separator");
		final StringBuilder sb = new StringBuilder();

		sb.append(name + ":" + lineSep);
		sb.append(defaultAppr.getDescription("default")).append(lineSep);

		for (Calculator c : calcs)
			sb.append(c.toString()).append(lineSep);

		return sb.toString();
	}

	protected void applyProperties(Appearance appr, String name, Properties nacProps,
	                               String baseKey, CalculatorCatalog catalog) {
		String value = null;

		appr.applyDefaultProperties(nacProps, baseKey);
		deps.applyDefaultProperties(nacProps, baseKey);

		Calculator newCalc;

		for (VisualPropertyType type : catalog.getCalculatorTypes()) {
			for (Calculator c : catalog.getCalculators(type)) {
				value = nacProps.getProperty(baseKey + "."
				                             + c.getVisualPropertyType().getPropertyLabel());
				newCalc = catalog.getCalculator(c.getVisualPropertyType(), value);
				setCalculator(newCalc);
			}
		}
	}

	protected Properties getProperties(Appearance appr, String baseKey) {
		String key = null;
		String value = null;
		Properties newProps = appr.getDefaultProperties(baseKey);
		Properties depProps = deps.getDefaultProperties(baseKey);
		newProps.putAll(depProps);

		for (Calculator c : calcs) {
			// do actual
			key = baseKey + "." + c.getVisualPropertyType().getPropertyLabel();
			value = c.toString();
			newProps.setProperty(key, value);
		}

		return newProps;
	}

	protected abstract void copyDefaultAppearance(AppearanceCalculator toCopy);
	
	protected void copyCalculators(AppearanceCalculator copy) {
		// Copy individual calculators
    	for(Calculator cal  : this.calcs) {
    		final ObjectMapping mCopy = (ObjectMapping) cal.getMapping(0).clone();
    		final String originalName = cal.toString();
    		String copyName;
    		if(originalName.contains(CLONE_SUFFIX))
    			copyName = originalName.split(CLONE_SUFFIX)[0] + CLONE_SUFFIX + System.currentTimeMillis();
    		else
    			copyName = originalName + CLONE_SUFFIX + System.currentTimeMillis();
    		
    		final Calculator bCalc = new BasicCalculator(copyName, mCopy, cal.getVisualPropertyType());
    		copy.setCalculator(bCalc);
    	}
	}
}
