
package cytoscape.data;

import cytoscape.data.attr.CountedIterator;
import cytoscape.data.attr.MultiHashMap;
import cytoscape.data.attr.MultiHashMapDefinition;

import java.util.List;
import java.util.Map;

public class CyAttributesImpl implements CyAttributes
{

  private MultiHashMap mmap;
  private MultiHashMapDefinition mmapDef;

  public String[] getAttributeNames()
  {
    final CountedIterator citer = mmapDef.getDefinedAttributes();
    final String[] names = new String[citer.numRemaining()];
    int inx = 0;
    while (citer.hasNext()) {
      names[inx++] = (String) citer.next(); }
    return names;
  }

  public boolean hasAttribute(String id, String attributeName)
  {
    final byte valType = mmapDef.getAttributeValueType(attributeName);
    if (valType < 0) return false;
    final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes
      (attributeName);
    if (dimTypes.length == 0) {
      return mmap.getAttributeValue(id, attributeName, null) != null; }
    else {
      return mmap.getAttributeKeyspan
        (id, attributeName, null).numRemaining() > 0; }
  }

  public void setAttribute(String id, String attributeName, Boolean value)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) {
      mmapDef.defineAttribute(attributeName,
                              MultiHashMapDefinition.TYPE_BOOLEAN,
                              null); }
    else {
      if (type != MultiHashMapDefinition.TYPE_BOOLEAN) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_BOOLEAN"); }
      final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes
        (attributeName);
      if (dimTypes.length != 0) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_BOOLEAN"); } }
    mmap.setAttributeValue(id, attributeName, value, null);
  }

  public void setAttribute(String id, String attributeName, Integer value)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) {
      mmapDef.defineAttribute(attributeName,
                              MultiHashMapDefinition.TYPE_INTEGER,
                              null); }
    else {
      if (type != MultiHashMapDefinition.TYPE_INTEGER) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_INTEGER"); }
      final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes
        (attributeName);
      if (dimTypes.length != 0) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_INTEGER"); } }
    mmap.setAttributeValue(id, attributeName, value, null);
  }

  public void setAttribute(String id, String attributeName, Double value)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) {
      mmapDef.defineAttribute(attributeName,
                              MultiHashMapDefinition.TYPE_FLOATING_POINT,
                              null); }
    else {
      if (type != MultiHashMapDefinition.TYPE_FLOATING_POINT) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_FLOATING"); }
      final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes
        (attributeName);
      if (dimTypes.length != 0) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_FLOATING"); } }
    mmap.setAttributeValue(id, attributeName, value, null);
  }

  public void setAttribute(String id, String attributeName, String value)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) {
      mmapDef.defineAttribute(attributeName,
                              MultiHashMapDefinition.TYPE_STRING,
                              null); }
    else {
      if (type != MultiHashMapDefinition.TYPE_STRING) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_STRING"); }
      final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes
        (attributeName);
      if (dimTypes.length != 0) {
        throw new IllegalArgumentException
          ("definition for attributeName '" + attributeName +
           "' already exists and it is not of TYPE_STRING"); } }
    mmap.setAttributeValue(id, attributeName, value, null);
  }

  public Boolean getBooleanAttribute(String id, String attributeName)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) { return null; }
    if (type != MultiHashMapDefinition.TYPE_BOOLEAN) {
      throw new ClassCastException
        ("definition for attributeName '" + attributeName +
         "' is not of TYPE_BOOLEAN"); }
    return (Boolean) mmap.getAttributeValue(id, attributeName, null);
  }

  public Integer getIntegerAttribute(String id, String attributeName)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) { return null; }
    if (type != MultiHashMapDefinition.TYPE_INTEGER) {
      throw new ClassCastException
        ("definition for attributeName '" + attributeName +
         "' is not of TYPE_INTEGER"); }
    return (Integer) mmap.getAttributeValue(id, attributeName, null);
  }

  public Double getDoubleAttribute(String id, String attributeName)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) { return null; }
    if (type != MultiHashMapDefinition.TYPE_FLOATING_POINT) {
      throw new ClassCastException
        ("definition for attributeName '" + attributeName +
         "' is not of TYPE_FLOATING"); }
    return (Double) mmap.getAttributeValue(id, attributeName, null);
  }

  public String getStringAttribute(String id, String attributeName)
  {
    final byte type = mmapDef.getAttributeValueType(attributeName);
    if (type < 0) { return null; }
    if (type != MultiHashMapDefinition.TYPE_STRING) {
      throw new ClassCastException
        ("definition for attributeName '" + attributeName +
         "' is not of TYPE_STRING"); }
    return (String) mmap.getAttributeValue(id, attributeName, null);
  }

  public byte getType(String attributeName)
  {
    final byte valType = mmapDef.getAttributeValueType(attributeName);
    if (valType < 0) { return TYPE_UNDEFINED; }
    final byte[] dimTypes = mmapDef.getAttributeKeyspaceDimensionTypes
      (attributeName);
    if (dimTypes.length == 0) { return valType; }
    // fixme.
    return -99;
  }

  public boolean deleteAttribute(String id, String attributeName)
  {
    return mmap.removeAllAttributeValues(id, attributeName);
  }

  public boolean deleteAttribute(String attributeName)
  {
    return mmapDef.undefineAttribute(attributeName);
  }

  public void setAttributeList(String id, String attributeName, List list)
  {
  }

  public List getAttributeList(String id, String attributeName)
  {
    return null;
  }

  public void setAttributeMap(String id, String attributeName, Map map)
  {
  }

  public Map getAttributeMap(String id, String attributeName)
  {
    return null;
  }

  public MultiHashMap getMultiHashMap()
  {
    return null;
  }

  public MultiHashMapDefinition getMultiHashMapDefinition()
  {
    return null;
  }

}
