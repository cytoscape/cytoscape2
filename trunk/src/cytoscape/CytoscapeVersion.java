// CytoscapeVersion: identify (and describe) successive versions of cytoscape

/** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 ** 
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and the
 ** Institute of Systems Biology and the Whitehead Institute 
 ** have no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall the
 ** Institute of Systems Biology and the Whitehead Institute 
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if the
 ** Institute of Systems Biology and the Whitehead Institute 
 ** have been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 ** 
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

//-----------------------------------------------------------------------------------------
// $Revision$   
// $Date$ 
// $Author$
//-----------------------------------------------------------------------------------
package cytoscape;
//-----------------------------------------------------------------------------------
public class CytoscapeVersion {
  private String versionString = "0.9911";
  private String dateString = "2002/02/18";

  private String [] briefHistory = {
      "0.1   (2001/12/12) initial version",
      "0.2   (2001/12/18) sped up node selection by name",
      "0.3   (2001/12/20) node synonyms displayed in NodeProps dialog",
      "0.4   (2001/12/21) edge attribute files supported",
      "0.5   (2001/12/28) edge attributes now can control edge color",
      "0.6   (2002/01/01) popup dialog 'relocation flash' now fixed",
      "0.7   (2002/01/04) checkEnviroment centralized, now checks for java version",
      "0.8   (2002/01/07) active paths dialog bounds checking fixed",
      "0.9   (2002/01/07) IPBiodataServer.getGoTermName exception fixed",
      "0.10  (2002/01/22) selected nodes make new window; active paths bug fixed",
      "0.11  (2002/02/04) automatic running of active paths from command line\n" +
       "                 data passed to ActivePathsFinder via arrays",
      "0.12  (2002/02/19) reorganized directories; gene common names supported",
      "0.20  (2002/03/28) now uses plugin architecture; redesign of VizMapping underway",
      "0.8   (2002/06/17) first alpha release",
      "0.9   (2002/11/01) first beta release",
      "0.95  (2002/11/04) added generic annotation",
      "0.97  (2002/12/05) added LGPL to all source",
      "0.9911 (2003/02/26) added visual properties UI, almost released",
      };

//-----------------------------------------------------------------------------------
public String getVersion ()
{
  return "cytoscape version " + versionString + ", " + dateString;
}
//------------------------------------------------------------------------------
public String toString ()
{
  return getVersion ();
}
//-----------------------------------------------------------------------------------
public String getBriefHistory ()
{
  StringBuffer sb = new StringBuffer ();
  for (int i=0; i < briefHistory.length; i++) {
    sb.append (briefHistory [i]);
    sb.append ("\n");
    }

  return sb.toString ();

} // getBriefHistory
//-----------------------------------------------------------------------------------
public static void main (String [] args)
{
  CytoscapeVersion app = new CytoscapeVersion ();
  System.out.println (app.getVersion ());
  // System.out.println (app.getBriefHistory ());
}
//-----------------------------------------------------------------------------------
} // class 


