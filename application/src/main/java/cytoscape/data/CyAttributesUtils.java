/*
  File: CyAttributesUtils.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package cytoscape.data;

import cytoscape.Cytoscape;

import cytoscape.data.attr.CountedIterator;
import cytoscape.data.attr.MultiHashMap;
import cytoscape.data.attr.MultiHashMapDefinition;

import giny.model.Edge;
import giny.model.Node;

import org.cytoscape.equations.Equation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class CyAttributesUtils {
	public static enum AttributeType {
		NODE,
		EDGE,
		NETWORK;
	}

	/**
	 * An AttributeFilter that produces all attributes--none are filtered out.
	 */
	public static final AttributeFilter ALL_ATTRIBUTES_FILTER = new AttributeFilter() {
		public boolean includeAttribute(CyAttributes attr, String objID, String attrName) {
			return true;
		}
	};

	/**
	 *  DOCUMENT ME!
	 *
	 * @param attributeName DOCUMENT ME!
	 * @param attrs DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static Map getAttribute(String attributeName, CyAttributes attrs) {
		Map<String, Object> attrMap;

		{
			final HashMap<String, Object> returnThis = new HashMap<String, Object>();
			final MultiHashMap mmap = attrs.getMultiHashMap();
			final MultiHashMapDefinition mmapDef = attrs.getMultiHashMapDefinition();

			if (mmapDef.getAttributeValueType(attributeName) != -1) {
				final Iterator objs = mmap.getObjectKeys(attributeName);

				while (objs.hasNext()) {
					final String obj = (String) objs.next();
					Object val;

					switch (attrs.getType(attributeName)) {
						case CyAttributes.TYPE_BOOLEAN:
							val = attrs.getBooleanAttribute(obj, attributeName);

							break;

						case CyAttributes.TYPE_INTEGER:
							val = attrs.getIntegerAttribute(obj, attributeName);

							break;

						case CyAttributes.TYPE_FLOATING:
							val = attrs.getDoubleAttribute(obj, attributeName);

							break;

						case CyAttributes.TYPE_STRING:
							val = attrs.getStringAttribute(obj, attributeName);

							break;

						case CyAttributes.TYPE_SIMPLE_LIST:

							List l = attrs.getListAttribute(obj, attributeName);

							if (l.size() > 0) {
								// val = l.get(0);
								val = l;
							} else {
								val = null;
							}

							break;

						case CyAttributes.TYPE_SIMPLE_MAP:
							val = attrs.getMapAttribute(obj, attributeName);

							break;

						default:
							val = null;
					}

					returnThis.put(obj, val);
				}
			}

			attrMap = (returnThis.size() == 0) ? null : returnThis;
		}

		return attrMap;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param canonicalName DOCUMENT ME!
	 * @param attrs DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static Map<String, Object> getAttributes(String canonicalName, CyAttributes attrs) {
		Map<String, Object> returnThis = new HashMap<String, Object>();
		final String[] attrNames = attrs.getAttributeNames();

		for (int i = 0; i < attrNames.length; i++) {
			final byte type = attrs.getType(attrNames[i]);

			if (attrs.hasAttribute(canonicalName, attrNames[i])) {
				if (type == CyAttributes.TYPE_SIMPLE_LIST) {
					List l = attrs.getListAttribute(canonicalName, attrNames[i]);

					if ((l != null) && (l.size() > 0)) {
						// returnThis.put(attrNames[i], l.get(0));
						returnThis.put(attrNames[i], l);
					}
				} else if (type == CyAttributes.TYPE_SIMPLE_MAP) {
					Map m = attrs.getMapAttribute(canonicalName, attrNames[i]);

					if ((m != null) && (m.size() > 0)) {
						returnThis.put(attrNames[i], m);
					}
				} else if (type == CyAttributes.TYPE_BOOLEAN) {
					returnThis.put(attrNames[i],
					               attrs.getBooleanAttribute(canonicalName, attrNames[i]));
				} else if (type == CyAttributes.TYPE_INTEGER) {
					returnThis.put(attrNames[i],
					               attrs.getIntegerAttribute(canonicalName, attrNames[i]));
				} else if (type == CyAttributes.TYPE_FLOATING) {
					returnThis.put(attrNames[i],
					               attrs.getDoubleAttribute(canonicalName, attrNames[i]));
				} else if (type == CyAttributes.TYPE_STRING) {
					returnThis.put(attrNames[i],
					               attrs.getStringAttribute(canonicalName, attrNames[i]));
				}
			}
		}

		return returnThis;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param attributeName DOCUMENT ME!
	 * @param attrs DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static Class getClass(String attributeName, CyAttributes attrs) {
		Class cl = null;

		switch (attrs.getType(attributeName)) {
			case CyAttributes.TYPE_BOOLEAN:
				cl = Boolean.class;

				break;

			case CyAttributes.TYPE_INTEGER:
				cl = Integer.class;

				break;

			case CyAttributes.TYPE_FLOATING:
				cl = Double.class;

				break;

			case CyAttributes.TYPE_STRING:
				cl = String.class;

				break;

			case CyAttributes.TYPE_SIMPLE_LIST:
				cl = List.class;

				break;

			case CyAttributes.TYPE_SIMPLE_MAP:
				cl = Map.class;

				break;

			case CyAttributes.TYPE_COMPLEX:
				cl = Object.class;

				break;

			default:
				cl = null;
		}

		return cl;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param attrs DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static List<String> getVisibleAttributeNames(CyAttributes attrs) {
		String[] allNames = attrs.getAttributeNames();
		List<String> visibleNames = new ArrayList<String>();

		for (String name : allNames)
			if (attrs.getUserVisible(name)) {
				visibleNames.add(name);
			}

		return visibleNames;
	}

	/**
	 *  @param copyEquation if true, and the source contains an equation, the equation and not the value will be copied
	 *  @param errorMessage will be set to an explanatory text should the copy operation fail
	 *  @return true if the copy operation succeeded, else false
	 */
	public static boolean copyAttribute(final CyAttributes attribs, final String sourceId, final String targetId,
	                                    final String attribName, final boolean copyEquation,
	                                    final StringBuilder errorMessage)
	{
		// Self-copy is a supported no-op!
		if (sourceId.equals(targetId))
			return true;

		errorMessage.setLength(0);

		if (copyEquation) {
			final Equation equation = attribs.getEquation(sourceId, attribName);
			if (equation == null)
				attribs.deleteAttribute(targetId, attribName);
			else
				attribs.setAttribute(targetId, attribName, equation);

			return true;
		}

		switch (attribs.getType(attribName)) {
		case CyAttributes.TYPE_BOOLEAN:
			final Boolean b = attribs.getBooleanAttribute(sourceId, attribName);
			if (b == null)
				attribs.deleteAttribute(targetId, attribName);
			else
				attribs.setAttribute(targetId, attribName, b);
			return true;
		case CyAttributes.TYPE_INTEGER:
			final Integer i = attribs.getIntegerAttribute(sourceId, attribName);
			if (i == null)
				attribs.deleteAttribute(targetId, attribName);
			else
				attribs.setAttribute(targetId, attribName, i);
			return true;
		case CyAttributes.TYPE_FLOATING:
			final Double d = attribs.getDoubleAttribute(sourceId, attribName);
			if (d == null)
				attribs.deleteAttribute(targetId, attribName);
			else
				attribs.setAttribute(targetId, attribName, d);
			return true;
		case CyAttributes.TYPE_STRING:
			final String s = attribs.getStringAttribute(sourceId, attribName);
			if (s == null)
				attribs.deleteAttribute(targetId, attribName);
			else
				attribs.setAttribute(targetId, attribName, s);
			return true;
		case CyAttributes.TYPE_SIMPLE_LIST:
			return copySimpleList(attribs, sourceId, targetId, attribName, errorMessage);
		case CyAttributes.TYPE_SIMPLE_MAP:
			return copySimpleMap(attribs, sourceId, targetId, attribName, errorMessage);
		default:
			final Object attribValue = attribs.getAttribute(sourceId, attribName);
			if (attribValue == null) {
				attribs.deleteAttribute(targetId, attribName);
				return true;
			}

			errorMessage.append("can't copy an attribute of this type ("
			                    + toString(attribs.getType(attribName)) + ")!  (Source ID: "
			                    + sourceId + ", Attribute name: " + attribName + ")");
			return false;
		}
	}

	/**
	 *  Helper method used by copyAttribute().
	 */
	private static boolean copySimpleList(final CyAttributes attribs, final String sourceId, final String targetId,
	                                      final String attribName, final StringBuilder errorMessage)
        {
		final List originalList = attribs.getListAttribute(sourceId, attribName);
		if (originalList == null) {
			attribs.deleteAttribute(targetId, attribName);
			return true;
		}

		if (originalList.isEmpty()) {
			attribs.setListAttribute(targetId, attribName, new ArrayList());
			return true;
		}

		final Class entryType = originalList.get(0).getClass();
		if (entryType != Boolean.class && entryType != Integer.class && entryType != Double.class
		    && entryType != String.class)
		{
			errorMessage.append("can't copy a list that has entries of a non-trivial type("
			                    + toString(attribs.getType(attribName)) + ")! (Source ID: "
                                            + sourceId + ", Attribute name: " + attribName + ")");
			return false;
		}

		final List newList = new ArrayList(originalList.size());
		for (final Object listEntry : originalList)
			newList.add(listEntry);
		attribs.setListAttribute(targetId, attribName, newList);
		return true;
	}

	/**
	 *  Helper method used by copyAttribute().
	 */
	private static boolean copySimpleMap(final CyAttributes attribs, final String sourceId, final String targetId,
	                                     final String attribName, final StringBuilder errorMessage)
        {
		final Map originalMap = attribs.getMapAttribute(sourceId, attribName);
		if (originalMap == null) {
			attribs.deleteAttribute(targetId, attribName);
			return true;
		}

		if (originalMap.isEmpty()) {
			attribs.setMapAttribute(targetId, attribName, new HashMap());
			return true;
		}

		final Map newMap = new HashMap();
		newMap.putAll(originalMap);
		attribs.setMapAttribute(targetId, attribName, newMap);
		return true;
	}

	/**
	 * Copy all the attributes of a given object to another object.
	 * Equivalent to:
	 * <PRE>
	 *    copyAttributes (originalID, copyID, attrs, ALL_ATTRIBUTES_FILTER, purge).
	 * </PRE>
	 * @see copyAttributes(String,String,CyAttributes,AttributeFilter,boolean)
	 */
	static public void copyAttributes(String originalID, String copyID, CyAttributes attrs,
	                                  boolean purge) {
		copyAttributes(originalID, copyID, attrs, ALL_ATTRIBUTES_FILTER, purge);
	}

	/**
	 * Copy all the attributes of a given object that pass a given filter to another
	 * object. This includes complex attributes.
	 * @param originalID the identifier of the object we are copying
	 *                   from (e.g, equivalent to CyNode.getIdentifier()).
	 * @param copyID the identifier of the object we are copying to.
	 * @param attrs the CyAttributes from which to copy and retrieve the attributes.
	 * @param filter an AttributeFilter that determines if a given attribute should
	 *        be copied. The filter is applied to all attributes stored un originalID.
	 *        An attribute will we copied iff filter.includeAttribute() returns true.
	 *        To copy all attributes, use {@link
	 *        CyAttributesUtils#ALL_ATTRIBUTES_FILTER ALL_ATTRIBUTES_FILTER}.
	 * @param purge true iff existing attribute values associated with
	 *              the object represented by copyID are to be removed
	 *              before copying the new attribute values from the
	 *              object represented by originalID.  This is useful
	 *              when copying to an existing object that contained
	 *              previous attribute values. This should be false
	 *              for newly created, objects that have no previous
	 *              attribute values.
	 * @throws IllegalArgumentException if originalID, copyID, attrs,
	 * or filter are null; or if originalID==copyID.
	 */
	static public void copyAttributes(String originalID, String copyID, CyAttributes attrs,
	                                  AttributeFilter filter, boolean purge) {
		// test filter separtely so that error message for copyAttributes() that doesn't
		// have 'filter' argument will not be confusing:
		if ((originalID == null) || (copyID == null) || (attrs == null)) {
			throwIllegalArgumentException("copyAttributes(): 'original', 'copy', or 'attrs' was null.");
		}

		if (filter == null) {
			throwIllegalArgumentException("copyAttributes(): 'filter' was null.");
		}

		if (originalID == copyID) {
			throwIllegalArgumentException("copyAttributes(): 'original' must not be the same object as 'copy'.");
		}

		// String originalID = original.getIdentifier();
		// String copyID = copy.getIdentifier();
		MultiHashMapDefinition mmapDef = attrs.getMultiHashMapDefinition();

		Iterator attIt = mmapDef.getDefinedAttributes();
		String attrName;

		AttributeValueVisitor copyVisitor = new CopyingAttributeValueVisitor(copyID);

		while (attIt.hasNext()) {
			attrName = (String) attIt.next();

			if (filter.includeAttribute(attrs, originalID, attrName)) {
				// primCopyAttribute(originalID, copyID, attrName, attrs, purge);
				traverseAttributeValues(originalID, attrName, attrs, copyVisitor);
			}
		}
	}

	/**
	 * Copy a specific attribute of a given object to another
	 * object. This includes complex attributes.
	 * @param originalID the identifier of the object we are copying
	 *                   from (e.g, equivalent to CyNode.getIdentifier()).
	 * @param copyID the identifier of the object we are copying to.
	 * @param attrName the name of the attribute we wish to copy.
	 * @param attrs the CyAttributes from which to copy and retrieve the attribute.
	 * @param purge true iff existing attribute values associated with
	 *              the object represented by copyID are to be removed
	 *              before copying the new attribute values from the
	 *              object represented by originalID.  This is useful
	 *              when copying to an existing object that contained
	 *              previous attribute values. This should be false
	 *              for newly created, objects that have no previous
	 *              attribute values.
	 * @throws IllegalArgumentException if originalID, copyID,
	 * attrName, or attrs are null; or if originalID==copyID.
	 */
	static public void copyAttribute(String originalID, String copyID, String attrName,
	                                 CyAttributes attrs, boolean purge) {
		if ((originalID == null) || (copyID == null) || (attrName == null) || (attrs == null)) {
			throwIllegalArgumentException("copyAttribute(): 'original', 'copy', 'attrName' or 'attrs' was null.");
		}

		if (originalID == copyID) {
			throwIllegalArgumentException("copyAttribute(): 'original' must not be the same object as 'copy'.");
		}

		AttributeValueVisitor copyVisitor = new CopyingAttributeValueVisitor(copyID);

		// String originalID = original.getIdentifier();
		// String copyID = copy.getIdentifier();
		traverseAttributeValues(originalID, attrName, attrs, copyVisitor);

		// return primCopyAttribute(originalID, copyID, attrName, attrs, purge);
	}

	/**
	 * Traverse all the values of a given attribute applying an
	 * AttributeValueVisitor to each value. For simple attributes,
	 * such as those of type CyAttributes.TYPE_INTEGER or
	 * CyAttributes.TYPE_STRING, a single value is traversed. For
	 * Lists, Maps, and complex attribute types, all attribute values
	 * are traversed--applying the given AttributeValueVisitor to each
	 * value.
	 * @param objToTraverseID the identifier of the object containing
	 * the attribute of interest.
	 * @param attrName the name of the attribute for which we are to
	 * traverse values.
	 * @param attrs the CyAttributes that contains the attribute of interest.

	 * @param visitor an AttributeValueVisitor to be applied to each
	 *                value of the attribute being traversed. For
	 *                example, to print out all attribute values, we
	 *                could perform:
	 * <PRE>
	 *     traverseAttributeValues ("testObject",
	 *                              "testAttributeName",
	 *                              Cytoscape.getNodeAttributes(),
	 *                              new AttributeValueVisitor () {
	 *              public void visitingAttributeValue (String objTraversedID,
	 *                                                  String attrName,
	 *                                                  CyAttributes attrs,
	 *                                                  Object[] keySpace,
	 *                                                  Object visitedValue) {
	 *                  CyLogger.getLogger().info ("traversing " + visitedValue);
	 *              }});
	 * </PRE>
	 * No traversal is performed if objToTraverseID or attrName are null,
	 * or if objToTraverseID has no associated attrName attribute.
	 */
	static public void traverseAttributeValues(String objToTraverseID, String attrName,
	                                           CyAttributes attrs, AttributeValueVisitor visitor) {
		if ((objToTraverseID == null) || (attrName == null)) {
			return;
		}

		// String visitID = objToTraverseID.getIdentifier();
		if (!attrs.hasAttribute(objToTraverseID, attrName)) {
			return;
		}

		// original has this attribute, now get it's value:
		MultiHashMap mmap = attrs.getMultiHashMap();
		MultiHashMapDefinition mmapDef = attrs.getMultiHashMapDefinition();
		byte attrType = attrs.getType(attrName);

		switch (attrType) {
			case CyAttributes.TYPE_BOOLEAN:
			case CyAttributes.TYPE_INTEGER:
			case CyAttributes.TYPE_FLOATING:
			case CyAttributes.TYPE_STRING:
				visitor.visitingAttributeValue(objToTraverseID, attrName, attrs, null,
				                               mmap.getAttributeValue(objToTraverseID, attrName,
				                                                      null));

				break;

			case CyAttributes.TYPE_SIMPLE_LIST:
			case CyAttributes.TYPE_SIMPLE_MAP:

				Object[] key = new Object[1];
				Iterator keyspan = mmap.getAttributeKeyspan(objToTraverseID, attrName, null);

				while (keyspan.hasNext()) {
					key[0] = keyspan.next();
					visitor.visitingAttributeValue(objToTraverseID, attrName, attrs, key,
					                               mmap.getAttributeValue(objToTraverseID,
					                                                      attrName, key));
				}

				break;

			case CyAttributes.TYPE_COMPLEX:

				byte[] keyTypes = mmapDef.getAttributeKeyspaceDimensionTypes(attrName);
				walkThruKeySpace(keyTypes.length, 0, new Object[0], objToTraverseID, attrName,
				                 attrs, visitor);

				break;
		}
	}

	/**
	 * A recursive method that iterates through each set of keys at a
	 * particular dimension in the keyspace and then continues down to the
	 * next dimension. Once the final dimension is reached, the visitor
	 * is run passing it the stored attribute value and related info.
	 */
	static private void walkThruKeySpace(int maxKeys, int keyTypesIndex,
	                                     Object[] currentDimensionKeys, String objToTraverseID,
	                                     String attrName, CyAttributes attrs,
	                                     AttributeValueVisitor visitor) {
		CountedIterator dimIt = attrs.getMultiHashMap()
		                             .getAttributeKeyspan(objToTraverseID, attrName,
		                                                  currentDimensionKeys);
		Object nextValue;
		keyTypesIndex++;

		Object[] nextDimensionKeys = null;

		if (keyTypesIndex <= maxKeys) {
			// we have another dimension to go down into (key to add).
			// copy all the previous keys and then we'll add
			// the last key during each iteration of dimIt:
			nextDimensionKeys = new Object[keyTypesIndex];

			for (int i = 0; i < (keyTypesIndex - 1); i++) {
				nextDimensionKeys[i] = currentDimensionKeys[i];
			}
		}

		while (dimIt.hasNext()) {
			nextValue = dimIt.next();
			// CyLogger.getLogger().info("dim " + keyTypesIndex + " value = " + nextValue);
			nextDimensionKeys[keyTypesIndex - 1] = nextValue;

			// have we reached the last dimension, or do we need to continue building
			// the keys?
			if (keyTypesIndex < maxKeys) {
				// do next dimension:
				walkThruKeySpace(maxKeys, keyTypesIndex, nextDimensionKeys, objToTraverseID,
				                 attrName, attrs, visitor);
			} else {
				// we reached the final set of keys, now deal with the real values:
				Object finalVal = attrs.getMultiHashMap()
				                       .getAttributeValue(objToTraverseID, attrName,
				                                          nextDimensionKeys);
				// CyLogger.getLogger().info("dim " + keyTypesIndex + " final value = " +
				//           finalVal);
				visitor.visitingAttributeValue(objToTraverseID, attrName, attrs, nextDimensionKeys,
				                               finalVal);
			}
		}
	}

	/**
	 * Return a non-null List of all the attribute names associated with
	 * a given identifier for a given CyAttributes. Will return
	 * an empty list if objID or attrs is null.
	 * @param objID the identifier of the object of interest.
	 * @param attrs the CyAttributes from which to check for attributes.
	 */
	static public List<String> getAttributeNamesForObj(String objID, CyAttributes attrs) {
		if ((objID == null) || (attrs == null)) {
			return null;
		}

		CountedIterator nameIt = attrs.getMultiHashMapDefinition().getDefinedAttributes();
		List<String> names = new ArrayList<String>();
		String attrName;

		while (nameIt.hasNext()) {
			attrName = (String) nameIt.next();

			if (attrs.hasAttribute(objID, attrName)) {
				names.add(attrName);
			}
		}

		return names;
	}

	/**
	 *  Get list of IDs associated with a specific attribute value.
	 *
	 * @param attr DOCUMENT ME!
	 * @param attrName DOCUMENT ME!
	 * @param attrValue DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static List<String> getIDListFromAttributeValue(AttributeType type, String attrName,
	                                                       Object attrValue) {
		final CyAttributes attr;
		final List<String> ids = new ArrayList<String>();
		final List<String> result = new ArrayList<String>();

		// Extract ids
		if (type == AttributeType.NODE) {
			attr = Cytoscape.getNodeAttributes();

			final List<Node> nodes = Cytoscape.getRootGraph().nodesList();

			for (Node node : nodes)
				ids.add(node.getIdentifier());
		} else if (type == AttributeType.EDGE) {
			attr = Cytoscape.getEdgeAttributes();

			final List<Edge> edges = Cytoscape.getRootGraph().edgesList();

			for (Edge edge : edges) {
				ids.add(edge.getIdentifier());
			}
		} else {
			attr = Cytoscape.getNetworkAttributes();
		}

		final byte attrDataType = attr.getType(attrName);

		String value;

		List<Object> l = null;
		if (attrDataType == CyAttributes.TYPE_SIMPLE_LIST) {
			for (String id : ids) {
				l = attr.getListAttribute(id, attrName);

				if ((l != null) && (l.size() > 0)) {
					for (Object obj : l) {
						if ((obj != null) && obj.equals(attrValue)) {
							result.add(id);

							break;
						}
					}
				}
			}
		} else if (attrDataType == CyAttributes.TYPE_STRING) {
			for (String id : ids) {
				// Extract attribute value from ID
				value = attr.getStringAttribute(id, attrName);

				if ((value != null) && value.equals(attrValue))
					result.add(id);
			}
		}

		return result;
	}

	/**
	 * Return a String representation of the various CyAttributes
	 * types returned by methods like CyAttributes.getType() and
	 * MultiHashMapDefinition.getAttributeKeysapceDimensionTypes().
	 * This is useful for "toString()" operations.
	 * @see cytoscape.data.CyAttributes#getType
	 * @see cytoscape.data.attr.MultiHashMapDefinition#getAttributeKeyspaceDimensionTypes
	 */
	static public String toString(byte atype) {
		switch (atype) {
			case CyAttributes.TYPE_COMPLEX:
				return "COMPLEX";

			case CyAttributes.TYPE_SIMPLE_MAP:
				return "SIMPLE_MAP";

			case CyAttributes.TYPE_SIMPLE_LIST:
				return "SIMPLE_LIST";

			case CyAttributes.TYPE_UNDEFINED:
				return "UNDEFINED";

			case CyAttributes.TYPE_BOOLEAN:
				return "BOOLEAN";

			case CyAttributes.TYPE_INTEGER:
				return "INTEGER";

			case CyAttributes.TYPE_FLOATING:
				return "FLOATING";

			case CyAttributes.TYPE_STRING:
				return "STRING";

			default:
				return "UNKNOWN:" + atype;
		}
	}

	/**
	 * Return a byte representation of the various CyAttributes
	 * types returned by methods like CyAttributes.getType() and
	 * MultiHashMapDefinition.getAttributeKeysapceDimensionTypes().
	 * This is useful to return the byte when you have an Attribute String.
	 * @see cytoscape.data.CyAttributes#getType
	 * @see cytoscape.data.attr.MultiHashMapDefinition#getAttributeKeyspaceDimensionTypes
	 */
	static public byte toByte(String attributeType) {
		if (attributeType.equals("COMPLEX"))
			return CyAttributes.TYPE_COMPLEX;

		if (attributeType.equals("SIMPLE_MAP"))
			return CyAttributes.TYPE_SIMPLE_MAP;

		if (attributeType.equals("SIMPLE_LIST"))
			return CyAttributes.TYPE_SIMPLE_LIST;

		if (attributeType.equals("UNDEFINED"))
			return CyAttributes.TYPE_UNDEFINED;

		if (attributeType.equals("BOOLEAN"))
			return CyAttributes.TYPE_BOOLEAN;

		if (attributeType.equals("INTEGER"))
			return CyAttributes.TYPE_INTEGER;

		if (attributeType.equals("FLOATING"))
			return CyAttributes.TYPE_FLOATING;

		if (attributeType.equals("STRING"))
			return CyAttributes.TYPE_STRING;

		return CyAttributes.TYPE_UNDEFINED;
	}

	/**
	 * Convenience method for throwing an IllegalArgumentException given
	 * a message.
	 */
	static private void throwIllegalArgumentException(String msg) throws IllegalArgumentException {
		IllegalArgumentException ex = new IllegalArgumentException(msg);
		ex.fillInStackTrace();

		// ex.printStackTrace();
		throw ex;
	}

	/**
	 * copy each attribute value from objTraversedID to an
	 * attribute value associated with objTraversedCopyID.
	 */
	private static class CopyingAttributeValueVisitor implements AttributeValueVisitor {
		private String copyID;

		public CopyingAttributeValueVisitor(String objTraversedCopyID) {
			copyID = objTraversedCopyID;
		}

		public void visitingAttributeValue(String objTraversedID, String attrName,
		                                   CyAttributes attrs, Object[] keySpace,
		                                   Object visitedValue) {
			attrs.getMultiHashMap().setAttributeValue(copyID, attrName, visitedValue, keySpace);
		}
	}
	
	
	/**
	 * Check if all the values for an attribute are NULL.
	 * @param attributeType     "node" or "edge"
	 * @param attributeName  	attribute name.
	 */
	public static boolean isNullAttribute(String attributeType, String attributeName){
		
		boolean retValue = true;
						
		CyAttributes attributes = null;
		if (attributeType.equalsIgnoreCase("node")){
			attributes = Cytoscape.getNodeAttributes();
			int[] nodeIndices = Cytoscape.getCurrentNetwork().getNodeIndicesArray();
			for (int i=0; i<nodeIndices.length; i++){
				String nodeID = Cytoscape.getRootGraph().getNode(nodeIndices[i]).getIdentifier();
				Object valueObj = attributes.getAttribute(nodeID, attributeName);
				if (valueObj != null){
					retValue = false;
					break;
				}
			}
		}
		else {// edge
			attributes = Cytoscape.getEdgeAttributes();			
			int[] edgeIndices = Cytoscape.getCurrentNetwork().getEdgeIndicesArray();
			for (int i=0; i<edgeIndices.length; i++){
				String edgeID = Cytoscape.getRootGraph().getEdge(edgeIndices[i]).getIdentifier();
				Object valueObj = attributes.getAttribute(edgeID, attributeName);
				if (valueObj != null){
					retValue = false;
					break;
				}
			}
		}

		return retValue;
	}
}
