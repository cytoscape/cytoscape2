// Ontology.java
//------------------------------------------------------------------------------
// $Revision$   
// $Date$ 
// $Author$
//-----------------------------------------------------------------------------------
package cytoscape.data.annotation;
//-----------------------------------------------------------------------------------
import java.util.*;
import java.io.*;
//------------------------------------------------------------------------------
/**
 *  Contains a collection of OntologyTerms, each of which may have pointers
 *  to other terms, creating a hierarchical controlled vocabulary.  The pointers
 *  may express either "parent/child" relationships, or "container/contained"
 *  relationships.  Objects of this class each aggregate a full set of 
 *  related terms,  creating a distinct ontology of a certain type, like KEGG's metabolic
 *  pathways, or GeneOntology's biological processes.  Extensive navigational
 *  methods are provided so that, for instance, the full leaf-to-root biological
 *  process hierarchies which may exist for a given gene are conveniently
 *  returned.
 */
public class Ontology implements Serializable {
  protected String curator;       // KEGG, GO, ...
  protected String ontologyType;  // biological process, metabolic pathway, ...
  protected HashMap termHash;
//------------------------------------------------------------------------------
/**
 *  @param curator        The institute or group which maintains this ontology
 *  @param ontolotyType   The nature of this ontology, eg, "metabolic pathway",
 *                        "molecular function", or "cellular component"
 */
public Ontology (String curator, String ontologyType)
{
  this.curator = curator;
  this.ontologyType = ontologyType;
  termHash = new HashMap ();

} // ctor
//------------------------------------------------------------------------------
public String getCurator ()
{
  return curator;
}
//------------------------------------------------------------------------------
public String getType ()
{
  return ontologyType;
}
//------------------------------------------------------------------------------
public void add (OntologyTerm newTerm)
{
  termHash.put (new Integer (newTerm.getId ()), newTerm);

} // add
//------------------------------------------------------------------------------
public int size ()
{
  return termHash.size ();

} // size
//------------------------------------------------------------------------------
public HashMap getTerms ()
{
  return termHash;
}
//------------------------------------------------------------------------------
public boolean containsTerm (int id)
{
  return termHash.containsKey (new Integer (id));
}
//------------------------------------------------------------------------------
public OntologyTerm getTerm (int id)
{
  return (OntologyTerm) termHash.get (new Integer (id));
}
//------------------------------------------------------------------------------
/**
 *  get all unique paths from the termID to the root of the ontology, in reverse
 *  order, with the most general classification first, and most specific last.  for some
 *  terms, this will be a path, possibly with only one member.  for other terms,
 *  which have be assigned to multiple categories, there will be multiple paths.
 *  furthermore, some categories belong to multiple pathways, further increasing
 *  the number of paths implicit in the termID.
 *   
 */
public int [][] getAllHierarchyPaths (int termID)
{
  Vector flattenedList = getAllHierarchyPathsAsVector (termID);
  int size = flattenedList.size ();
  int [][] result = new int [size] [];

  for (int i=0; i < size; i++) {
    Vector integerList = (Vector) flattenedList.get (i);
    int max = integerList.size ();
    result [i] = new int [max];
    for (int j=0; j < max; j++) {
      int indexForReversingOrder = max - j - 1;
      result [i][j] = ((Integer) integerList.get (indexForReversingOrder)).intValue ();
      }
    } // for i

  return result;

} // getAllHierarchyPaths
//------------------------------------------------------------------------------
/**
 *  get all unique paths as a set of names, from termID to the root of the ontology,
 *  in reverse order, from most general to most specific.  for some
 *  terms, this will be a path, possibly with only one member.  for other terms,
 *  which have be assigned to multiple categories, there will be multiple paths.
 *  furthermore, some categories belong to multiple pathways, further increasing
 *  the number of paths implicit in the termID.
 */
public String [][] getAllHierarchyPathsAsNames (int termID)
{

  int [][] hierarchy = getAllHierarchyPaths (termID);
  String [][] result = new String [hierarchy.length][];

  for (int i=0; i < hierarchy.length; i++) {
    int [] path = hierarchy [i];
    int max = path.length;
    result [i] = new String [max];
    for (int j=0; j < path.length; j++)
      result [i][j] = getTerm (path [j]).getName ();
      //result [i][j] = getTerm (path [max-j-1]).getName ();
    } // for i

  return result;

} // getAllHierarchyPathsAsNames
//------------------------------------------------------------------------------
/**
 *  assemble a Vector of Vectors of Integers, using a recursive traversal
 *  of the ontology hierarchy.  the recursive step often creates a 
 *  multi-level list which is untangled into the desired simple list of lists
 *  by the FlattenIntVectors class.
 */
protected Vector getAllHierarchyPathsAsVector (int termID)
{
  Vector nestedLists = recursiveGetPath (termID, new Vector ());
  if (nestedLists.size () == 0)
    return nestedLists;

  FlattenIntVectors flattener = new FlattenIntVectors (nestedLists);
  Vector flattenedList = flattener.getResult ();
  return flattenedList;

} // getAllHierarchyPathsAsVector
//------------------------------------------------------------------------------
/**
 *  traverse the ontology hierachy from leaf to root, adding a new vector
 *  with each recursive step.
 */
protected Vector recursiveGetPath (int termID, Vector path)
{
  Integer ID = new Integer (termID);

  if (termHash != null && termHash.containsKey (ID)) {
    OntologyTerm term = (OntologyTerm) termHash.get (ID);
    int parentCount = term.numberOfParentsAndContainers ();
    if (parentCount == 0) {
      path.addElement (ID);
      return path;
      }
    else if (parentCount == 1) {
      path.addElement (ID);
      int parentID = term.getParentsAndContainers () [0];
      return (recursiveGetPath (parentID, path));
      }        
    else { // assume for now:  (parentCunt == 2) 
      path.addElement (ID);
      Vector newPath = new Vector ();
      Vector path1 = (Vector) path.clone ();
      Vector path2 = (Vector) path.clone ();
      int parent1 = term.getParentsAndContainers () [0];
      int parent2 = term.getParentsAndContainers () [1];
      newPath.addElement (recursiveGetPath (parent1, path1));
      newPath.addElement (recursiveGetPath (parent2, path2));
      return newPath;
      }
   }
  return path;

} // recursiveGetPath
//------------------------------------------------------------------------------
public OntologyDescription getDescription ()
{
  return new OntologyDescription (curator, ontologyType);
}
//------------------------------------------------------------------------------
public String toString ()
{
  StringBuffer sb = new StringBuffer ();
  sb.append ("ontology: " + ontologyType + "\n");
  sb.append ("curator:  " + curator + "\n");

  return sb.toString ();

} // toString
//------------------------------------------------------------------------------
} // class Ontology
