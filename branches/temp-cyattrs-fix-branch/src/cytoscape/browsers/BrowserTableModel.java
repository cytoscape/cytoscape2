package cytoscape.browsers;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.lang.reflect.Array;

import cytoscape.data.CyAttributes;

public class BrowserTableModel extends AbstractTableModel {

  String [] columnNames;
  Object [][] data;
  int [] columnWidths = {40};  // missing values, which are possible only at
                               // the end of array, mean that default column widths
                               // will be used

  protected final int defaultColumnWidth = 100;
  protected int preferredTableWidth = defaultColumnWidth; // incremented below


  public BrowserTableModel (Object [] graphObjects,
                            CyAttributes cyAttributes,
                            String [] attributeNames) 
  {
    int graphObjectCount = graphObjects.length;
    columnNames = new String [attributeNames.length + 1];
    columnNames [0] = "Name";
    for (int i=0; i < attributeNames.length; i++)
      columnNames [i+1] = attributeNames [i];

    int numberOfColumns = columnNames.length;
    int numberOfRows = calculateMaxRowsNeeded (graphObjects, cyAttributes, attributeNames);
    data = new Object [numberOfRows][numberOfColumns];

    if (cyAttributes != null) {
      for (int i=0; i < columnNames.length; i++) {
        preferredTableWidth += defaultColumnWidth;
      } // for i
    } // if cyAttributes


    //-----------------------------------------------------------------
    // attributes are retrived by canonicalName; collect those first
    //-----------------------------------------------------------------
    String [] canonicalNames = new String [graphObjectCount];
    for (int i=0; i < graphObjectCount; i++) {
      canonicalNames[i] =
        ((giny.model.GraphObject) graphObjects[i]).getIdentifier();
    }

    //-----------------------------------------------------------------
    // now fill the data
    // todo (pshannon, 25 oct 2002): nasty special case below for when the
    // todo: 'attribute' canonicalName is requested.  this is not reliably
    // todo: an attribute (though perhaps we should institute that policy).
    // todo: so we look for that 'attribute' and assign the table cell outside
    // todo: of the normal table flow
    //-----------------------------------------------------------------
    if (cyAttributes != null) {
      int currentRowBase = 0;
      for (int graphObject=0; graphObject < graphObjects.length; graphObject++) {
        int maxRowsUsedThisObject = 1;
        String canonicalName =
          ((giny.model.GraphObject) graphObjects[graphObject]).getIdentifier();
        String commonName = cyAttributes.getStringAttribute(canonicalName, "commonName");
        if (commonName == null || commonName.length () == 0)
          commonName = canonicalName;
        data [currentRowBase][0] = commonName;
        for (int i=1; i < columnNames.length; i++) {
          if (columnNames [i].equals ("canonicalName"))
            data [currentRowBase][i] = canonicalName;
          else {
            List l = cyAttributes.getAttributeList(canonicalName, columnNames[i]);
            Object[] attributeValuesThisObject = l.toArray();
            int attributeCount = attributeValuesThisObject.length;
            for (int a=0; a < attributeCount; a++)
              if (attributeValuesThisObject [a] != null) {
                data [currentRowBase + a][i] = attributeValuesThisObject [a];
              }
            if (attributeCount > maxRowsUsedThisObject)
              maxRowsUsedThisObject = attributeCount;
          } // else: not the special case of 'canonicalName'
        } // for i
        currentRowBase += maxRowsUsedThisObject;
      } // for object:  each attribute name
    } // if cyAttributes


  } // ObjectBrowserTableModel
 
  protected int calculateMaxRowsNeeded (Object [] graphObjects,
                                        CyAttributes cyAttributes,
                                        String [] attributeNames) 
  {
    int max = 0;
    for (int graphObject=0; graphObject < graphObjects.length; graphObject++) {
      int maxRowsUsedThisObject = 1;
      String canonicalName = ((giny.model.GraphObject) graphObjects[graphObject]).getIdentifier();
      for (int i=0; i < attributeNames.length; i++) {
        String attributeName = attributeNames [i];
        int attributeCount = cyAttributes.getAttributeList(canonicalName, attributeName).size();
        if (attributeCount > maxRowsUsedThisObject)
          maxRowsUsedThisObject = attributeCount;
      } // for i
      max += maxRowsUsedThisObject;
    } // for object:  each attribute name

    return max;

  } // calculateMaxRowsNeeded
 
  public String getColumnName (int col) { return columnNames[col];}
  public int getColumnCount () { return columnNames.length;}
  public int getRowCount () { return data.length; }
  public boolean isCellEditable (int row, int col) {return false;}
  public Object getValueAt (int row, int col) {
    Object cellData = data [row][col];
    if (cellData != null && cellData.getClass().isArray () && Array.getLength (cellData) > 0) {
      StringBuffer sb = new StringBuffer ();
      Object element0 = Array.get (cellData, 0);
      int max = Array.getLength (cellData);
      for (int i=0; i < max; i++) {
        sb.append ((Array.get (cellData, i)).toString ());
        if (i < max - 1) sb.append (" | ");
      } // for i
      return sb.toString ();
    }
    return data [row][col];
  }
 
  public int getPreferredColumnWidth (int col) 
    // '0' means: there is no preferred width, use the default
    //  the columnWidths array can be incomplete. so if, for example,
    //  only the first column has a specified width, then the array
    //  need only contain one value.
  { 
    if (col >= columnWidths.length)
      return 0;
    else
      return columnWidths [col];
  }
 
  public Class getColumnClass (int column) 
    // though i do not understand the circumstances in which this method
    // is called, trial and error has led me to see that -some- class
    // must be returned, and that if the 0th row in the specified column
    // is null, then returning the String Class seems to work okay.
  {
    Object cellValue = getValueAt (0, column);
    if (cellValue == null) { 
      String s = new String ();
      return s.getClass ();
    }
    else
      return getValueAt (0, column).getClass();

  } // getColumnClass
 
} // class browserTableModel


