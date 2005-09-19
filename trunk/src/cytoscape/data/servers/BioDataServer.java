// BioDataServer

/**
 * Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute * *
 * This library is free software; you can redistribute it and/or modify it *
 * under the terms of the GNU Lesser General Public License as published * by
 * the Free Software Foundation; either version 2.1 of the License, or * any
 * later version. * * This library is distributed in the hope that it will be
 * useful, but * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF *
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. The software and *
 * documentation provided hereunder is on an "as is" basis, and the * Institute
 * for Systems Biology and the Whitehead Institute * have no obligations to
 * provide maintenance, support, * updates, enhancements or modifications. In no
 * event shall the * Institute for Systems Biology and the Whitehead Institute *
 * be liable to any party for direct, indirect, special, * incidental or
 * consequential damages, including lost profits, arising * out of the use of
 * this software and its documentation, even if the * Institute for Systems
 * Biology and the Whitehead Institute * have been advised of the possibility of
 * such damage. See * the GNU Lesser General Public License for more details. * *
 * You should have received a copy of the GNU Lesser General Public License *
 * along with this library; if not, write to the Free Software Foundation, *
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

// -----------------------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
// -----------------------------------------------------------------------------------------
package cytoscape.data.servers;

// -----------------------------------------------------------------------------------------
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import cytoscape.CytoscapeInit;
import cytoscape.cruft.obo.BiologicalProcessAnnotationReader;
import cytoscape.cruft.obo.CellularComponentAnnotationReader;
import cytoscape.cruft.obo.MolecularFunctionAnnotationReader;
import cytoscape.cruft.obo.OboOntologyReader;
import cytoscape.cruft.obo.SynonymReader;
import cytoscape.data.annotation.Annotation;
import cytoscape.data.annotation.AnnotationDescription;
import cytoscape.data.annotation.Ontology;
import cytoscape.data.annotation.readers.AnnotationFlatFileReader;
import cytoscape.data.annotation.readers.AnnotationXmlReader;
import cytoscape.data.annotation.readers.OntologyFlatFileReader;
import cytoscape.data.readers.TextFileReader;
import cytoscape.data.readers.TextHttpReader;
import cytoscape.data.readers.TextJarReader;
import cytoscape.data.synonyms.Thesaurus;
import cytoscape.data.synonyms.readers.ThesaurusFlatFileReader;

// ----------------------------------------------------------------------------------------
public class BioDataServer {

	private static String GENE_ASSOCIATION_FILE = "gene_association";
	private static String OBO_FILE = "obo";

	protected BioDataServerInterface server;
	
	// Flip the file content (names) or not.
	private boolean flip;
	
	// This is for taxon name-number conversion over the net
	private static final String NCBI_TAXON_SERVER = 
		"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=";
	private static final String TAXON_FILE = "tax_report.txt"; 

	String taxonName;
	String taxonNumber;
	
	String absPath;
	String taxonFileName; //Filename of taxonomy table
	File taxonFile; // Table for the NCBI Taxonomy number <-> Taxonomy Name
	File start;  //Start dir of the Cytoscape
	
	// ----------------------------------------------------------------------------------------
	/**
	 * serverName is either an RMI URI, or a manifest file which says what files
	 * to load into an in-process server; the manifest, the annotations, and the
	 * ontologies, may be files on a filesystem, files in a jar, or files
	 * retrieved by HTTP
	 * 
	 * Mod. by Kei (9/10/2005): 1. Read new obo file and gene_association file
	 * format. 2. Wrote a taxon number <-> taxon name converter. This function
	 * is based on the list in the file, users need to put the file in the dir.
	 */
	public BioDataServer(String serverName) throws Exception {

		// Flip the names or not.  Will be given from the Wizard.
		flip = false;
		
		
		taxonName = null;
		taxonNumber = null;
		start = CytoscapeInit.getMRUD();
		
		if (serverName.indexOf("rmi://") >= 0)
			server = (BioDataServerInterface) Naming.lookup(serverName);
		else {
			// look for a readable file
			server = new BioDataServerRmi(); // actually runs in process
			File fileTester = new File(serverName);
			final String separator = fileTester.separator;
			absPath = start.getPath() + separator;

			if ((serverName.startsWith("jar://"))
					|| (serverName.startsWith("http://"))
					|| (!fileTester.isDirectory() && fileTester.canRead())) {

				boolean fileFlag = false;

				// Read manifest file. First, try to read new format manifest
				// file which includes ".obo" and gene_association file.
				final BufferedReader manFileIn = new BufferedReader(
						new FileReader(serverName));

				// Check file type.
				// If the manifest contains obo or gene_association entry,
				// handle it as a new manifest file format.
				fileFlag = checkFileType(manFileIn);

				if (fileFlag == true) {
					// System.out.println("New format manifest file found...");

					// Get default sp. name
					String defSpName = CytoscapeInit.getDefaultSpeciesName();
					
					taxonFileName = absPath + TAXON_FILE;
					taxonFile = new File(taxonFileName);
					System.out.println( "Taxon File Name is " + taxonFileName);

					// Extract file names from the manifest
					String[] flags = parseLoadFile(serverName, "flip");
					String[] tempStrs = flags[0].split( separator );
				    
					
					//System.out.println( "Flip State is " + tempStrs[ tempStrs.length - 1] );
					if( tempStrs[ tempStrs.length - 1].equals( "true" ) ){
						flip = true;
					} else {
						flip = false;
					}
					System.out.println( "Flip State is " + flip );
					
					String[] oboFile = parseLoadFile(serverName, OBO_FILE );
					String[] geneAssociationFile = parseLoadFile(serverName,
							GENE_ASSOCIATION_FILE );

					// Read files
					try {
						loadObo(geneAssociationFile, oboFile );
					} catch (Exception e) {
						e.printStackTrace(System.err);
						throw e;
					}
					try {
						loadThesaurusFiles2(geneAssociationFile);
					} catch (Exception e) {
						e.printStackTrace(System.err);
						throw e;
					}
					

				} else {
					// Old format manifest found.
					//System.out.println("Old style manifest file is: " + serverName );
					String[] ontologyFiles = parseLoadFile(serverName,
							"ontology");
					String[] annotationFilenames = parseLoadFile(serverName,
							"annotation");
					loadAnnotationFiles(annotationFilenames, ontologyFiles);
					String[] thesaurusFilenames = parseLoadFile(serverName,
							"synonyms");
					loadThesaurusFiles(thesaurusFilenames);
				}
			} // if a plausible candidate load file
			else {
				System.err.println("could not read BioDataServer load file '"
						+ serverName + "'");
			}
		} // else: look for a readable file
	} // ctor

	//
	// Determine whether given file is new format or old.
	//
	protected boolean checkFileType(final BufferedReader br) throws IOException {
		String curLine = null;
		//System.out.println("Manifest: " + curLine);
		while (null != (curLine = br.readLine())) {
			if (curLine.startsWith(OBO_FILE)
					|| curLine.startsWith(GENE_ASSOCIATION_FILE)) {
				br.close();
				return true;
			}
		}
		br.close();
		return false;
	}

	
	/*
	 * For a given taxon ID, returns species name.
	 * This method connects to NCBI's Taxonomy server, so
	 * Internet connection is required.
	 * 
	 * Kei
	 */
	protected String getTaxonFromNCBI ( String id ) throws MalformedURLException {
		String txName = null;
		URL taxonURL = null;
		BufferedReader htmlPageReader = null;
		String curLine = null;
		
		String targetId = id + "&lvl=0";
		
		taxonURL = new URL(NCBI_TAXON_SERVER + targetId );
		try {
			htmlPageReader = new BufferedReader
				( new InputStreamReader(taxonURL.openStream()));
			while((curLine = htmlPageReader.readLine().trim()) != null ) {
				//System.out.println("HTML:" +  curLine);
				if( curLine.startsWith("<title>Taxonomy") ) {
					System.out.println("HTML:" +  curLine);
					StringTokenizer st = new StringTokenizer(curLine, "(");
					st.nextToken();
					curLine = st.nextToken();
					st = new StringTokenizer(curLine, ")");
					txName = st.nextToken().trim();
					System.out.println("Fetch result: NCBI code " + id + " is " +  txName);
					return txName;
				}
				
			}
			htmlPageReader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return txName;
	}
	// ----------------------------------------------------------------------------------------

	// Mod by Keiichiro Ono (09/09/2005)
	// 1. support for the new manifest file
	// 2. support for the internet files
	//
	// TODO:
	// get rid of manifest file idea...
	//
	protected String[] parseLoadFile(String filename, String key)
	// todo (pshannon 2003/07/01): there is some ugly special casing here, as we
	// figure
	// todo: out if the manifest file path is jar, http, or a regular file
	// system
	// todo: file. this should be refactored.
	{
		String rawText;
		// when annotation files are loaded from a filesystem, the manifest will
		// probably
		// use names which are relative to the manifest. so (for this case, but
		// not when
		// reading from jar files) get the absolute path to the manifest, and
		// prepend
		// that path to the names found in the manifest. somewhat clunkily,
		// track that
		// information with these next two variables
		File absoluteDirectory = null;
		String httpUrlPrefix = null;
		boolean readingFromFileSystem = false;
		boolean readingFromWeb = false;

		try {
			if (filename.trim().startsWith("jar://")) {
				TextJarReader reader = new TextJarReader(filename);
				reader.read();
				rawText = reader.getText();
			} // if
			else if (filename.trim().startsWith("http://")) {
				TextHttpReader reader = new TextHttpReader(filename);
				reader.read();
				rawText = reader.getText();
				readingFromWeb = true;
				try {
					URL url = new URL(filename);
					String fullUrlString = url.toString();
					httpUrlPrefix = fullUrlString.substring(0, fullUrlString
							.lastIndexOf("/"));
				} catch (Exception e) {
					httpUrlPrefix = "url parsing error!";
				}
			} // else: http
			else {
				File file = new File(filename);
				readingFromFileSystem = true;
				absoluteDirectory = file.getAbsoluteFile().getParentFile();
				TextFileReader reader = new TextFileReader(filename);
				reader.read();
				rawText = reader.getText();

			} // else: regular filesystem file
		} catch (Exception e0) {
			System.err
					.println("-- Exception while reading annotation server load file "
							+ filename);
			System.err.println(e0.getMessage());
			return new String[0];
		}

		String[] lines = rawText.split("\n");

		Vector list = new Vector();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.trim().startsWith("#"))
				continue;
			if (line.startsWith(key)) {
				String fileToRead = line.substring(line.indexOf("=") + 1);
				if (readingFromFileSystem)
					fileToRead = (new File(absoluteDirectory, fileToRead))
							.getPath();
				else if (readingFromWeb)
					fileToRead = httpUrlPrefix + "/" + fileToRead;
				list.add(fileToRead);
			} // if
		} // for i

		if (list.size() == 0) {
			return null;
		} else
			return (String[]) list.toArray(new String[0]);

	} // parseLoadFile

	// ----------------------------------------------------------------------------------------

	public BioDataServer() throws Exception {
		server = new BioDataServerRmi();
	} // ctor

	// ----------------------------------------------------------------------------------------

	public Ontology[] readOntologyFlatFiles(String[] ontologyFilenames)
			throws Exception
	// a quick hack. this is called only if annotation & ontology are each flat
	// files,
	// which means they must be read separately. and xml annotation file names
	// its own
	// ontology file, and the annotationXmlReader is responsible for loading its
	// ontology.
	{

		Vector list = new Vector();

		for (int i = 0; i < ontologyFilenames.length; i++) {
			String filename = ontologyFilenames[i];
			if (!filename.endsWith(".xml")) {
				OntologyFlatFileReader reader = new OntologyFlatFileReader(
						filename);
				list.add(reader.getOntology());
			}
		} // for i

		return (Ontology[]) list.toArray(new Ontology[0]);

	} // loadOntologyFiles

	// Read new obo file format.
	public Ontology[] readOntologyFlatFiles2(String[] ontologyFilenames)
			throws Exception {
		//System.out.println("Reading Ontology flat file...");
		Vector list = new Vector();

		for (int i = 0; i < ontologyFilenames.length; i++) {
			String filename = ontologyFilenames[i];

			// Reader for the ".obo" file
			BufferedReader oboReader = new BufferedReader(
					new OboOntologyReader(new FileReader(filename)));

			OntologyFlatFileReader reader = new OntologyFlatFileReader(
					oboReader );
			list.add(reader.getOntology());
			oboReader.close();
			
		}
		return (Ontology[]) list.toArray(new Ontology[0]);

	} // loadOntologyFiles

	// ----------------------------------------------------------------------------------------

	protected Ontology pickOntology(Ontology[] ontologies, Annotation annotation) {
		for (int i = 0; i < ontologies.length; i++)
			if (ontologies[i].getCurator().equalsIgnoreCase(
					annotation.getCurator()))
				return ontologies[i];

		return null;

	} // pickOntology

	// ----------------------------------------------------------------------------------------

	public void loadObo(String[] annotationFilenames, String[] ontologyFilenames)
			throws Exception {

		
		//System.out.println("Loading OBO file...");
		BufferedReader [] oboReaders = new BufferedReader[ontologyFilenames.length];
		for( int i = 0; i< ontologyFilenames.length; i++ ) {
			oboReaders[i] = new BufferedReader(
					new OboOntologyReader( new FileReader( ontologyFilenames[i] )));
		}
		Ontology[] ontologies = readOntologyFlatFiles2(ontologyFilenames);

		/*
		 * Since one Gene Association file is equal to the follwing:
		 * 
		 * Biological Process Annotation Cellar Component Annotation Molecular
		 * Function Annotation
		 * 
		 * we need 3 separate readers.
		 */
		for (int i = 0; i < annotationFilenames.length; i++) {
			Annotation bpAnnotation, ccAnnotation, mfAnnotation;
			String filename = annotationFilenames[i];

			// Extract taxon name.
			BufferedReader gaFileReader = new BufferedReader(
					new FileReader( filename ));
			
			taxonName = checkSpecies( gaFileReader );
			System.out.println("Taxonomy Name for this GA file is: " + taxonName);
			
			// Reader for the "gene_association" file
			BufferedReader bpRd = new BufferedReader(
					new BiologicalProcessAnnotationReader(taxonName,
							new FileReader( filename )));
			
			BufferedReader ccRd = new BufferedReader(
					new CellularComponentAnnotationReader(taxonName,
							new FileReader( filename )));

			BufferedReader mfRd = new BufferedReader(
					new MolecularFunctionAnnotationReader(taxonName,
							new FileReader( filename )));
			
			AnnotationFlatFileReader bpReader = new AnnotationFlatFileReader(
					bpRd);
			AnnotationFlatFileReader ccReader = new AnnotationFlatFileReader(
					ccRd);
			AnnotationFlatFileReader mfReader = new AnnotationFlatFileReader(
					mfRd);

			bpAnnotation = bpReader.getAnnotation();
			ccAnnotation = ccReader.getAnnotation();
			mfAnnotation = mfReader.getAnnotation();

			bpAnnotation.setOntology(pickOntology(ontologies, bpAnnotation));
			ccAnnotation.setOntology(pickOntology(ontologies, ccAnnotation));
			mfAnnotation.setOntology(pickOntology(ontologies, mfAnnotation));

			server.addAnnotation(bpAnnotation);
			server.addAnnotation(ccAnnotation);
			server.addAnnotation(mfAnnotation);
			
			bpRd.close();
			ccRd.close();
			mfRd.close();
		}
	} // loadAnnotationFiles2

	// Read the content of the annotation and ontology file
	public void loadAnnotationFiles(String[] annotationFilenames,
			String[] ontologyFilenames) throws Exception {

		Ontology[] ontologies = readOntologyFlatFiles(ontologyFilenames);

		for (int i = 0; i < annotationFilenames.length; i++) {
			Annotation annotation;
			String filename = annotationFilenames[i];
			if (!filename.endsWith(".xml")) {
				AnnotationFlatFileReader reader = new AnnotationFlatFileReader(
						filename);
				annotation = reader.getAnnotation();
				annotation.setOntology(pickOntology(ontologies, annotation));
			} else {
				File xmlFile = new File(annotationFilenames[i]);
				AnnotationXmlReader reader = new AnnotationXmlReader(xmlFile);
				annotation = reader.getAnnotation();
			}
			server.addAnnotation(annotation);
		} // for i

	} // loadAnnotationFiles

	// ----------------------------------------------------------------------------------------

	public void loadThesaurusFiles(String[] thesaurusFilenames)
			throws Exception {
		for (int i = 0; i < thesaurusFilenames.length; i++) {
			String filename = thesaurusFilenames[i];
			// /Thread.currentThread().dumpStack();
			// System.out.println( "Load Thesaurus: "+filename );
			ThesaurusFlatFileReader reader = new ThesaurusFlatFileReader(
					filename);
			Thesaurus thesaurus = reader.getThesaurus();
			server.addThesaurus(thesaurus.getSpecies(), thesaurus);
		}

	} // loadThesaurusFiles
	
	
	/*
	 * Accept new gene association file.
	 */
	public void loadThesaurusFiles2( String[] thesaurusFilenames )
		throws Exception {
		for (int i = 0; i < thesaurusFilenames.length; i++) {
			String filename = thesaurusFilenames[i];
			// /Thread.currentThread().dumpStack();
			// System.out.println( "Load Thesaurus: "+filename );
			BufferedReader thRd = new BufferedReader(
					new SynonymReader(taxonName,
							new FileReader( filename )));
			
			ThesaurusFlatFileReader reader = new ThesaurusFlatFileReader( thRd );
			Thesaurus thesaurus = reader.getThesaurus();
	
			server.addThesaurus(thesaurus.getSpecies(), thesaurus);
			
			thRd.close();
		}

} // loadThesaurusFiles

	// ----------------------------------------------------------------------------------------

	public void clear() {
		try {
			server.clear();
		} catch (Exception e) {
			System.err.println("Error!  failed to clear");
			e.printStackTrace();
		}

	}

	// ----------------------------------------------------------------------------------------
	public void addAnnotation(Annotation annotation) {
		try {
			server.addAnnotation(annotation);
		} catch (Exception e) {
			System.err
					.println("Error!  failed to add annotation " + annotation);
			e.printStackTrace();
		}

	}

	// ----------------------------------------------------------------------------------------
	public int getAnnotationCount() {
		try {
			int count = server.getAnnotationCount();
			return count;
		} catch (Exception e) {
			return 0;
		}

	}

	// ----------------------------------------------------------------------------------------
	public AnnotationDescription[] getAnnotationDescriptions() {
		try {
			return server.getAnnotationDescriptions();
		} catch (Exception e) {
			return null;
		}
	}

	// ----------------------------------------------------------------------------------------
	public Annotation getAnnotation(String species, String curator, String type) {
		try {
			return server.getAnnotation(species, curator, type);
		} catch (Exception e) {
			return null;
		}

	}

	// ----------------------------------------------------------------------------------------
	public Annotation getAnnotation(AnnotationDescription description) {
		try {
			return server.getAnnotation(description);
		} catch (Exception e) {
			return null;
		}

	}

	// ----------------------------------------------------------------------------------------
	public int[] getClassifications(String species, String curator,
			String type, String entity) {
		try {
			return server.getClassifications(species, curator, type, entity);
		} catch (Exception e) {
			return null;
		}
	}

	// ----------------------------------------------------------------------------------------
	public int[] getClassifications(AnnotationDescription description,
			String entity) {
		try {
			return server.getClassifications(description, entity);
		} catch (Exception e) {
			return null;
		}
	}

	// ----------------------------------------------------------------------------------------
	public String[][] getAllAnnotations(AnnotationDescription description,
			String entity) {
		try {
			return server.getAllAnnotations(description, entity);
		} catch (Exception e) {
			return null;
		}
	}

	// ----------------------------------------------------------------------------------------
	public String describe() {
		try {
			return server.describe();
		} catch (Exception e) {
			return "error connecting to data server";
		}
	}

	// ----------------------------------------------------------------------------------------
	public void addThesaurus(String species, Thesaurus thesaurus) {
		try {
			server.addThesaurus(species, thesaurus);
		} catch (Exception e) {
			return;
		}

	}

	// ----------------------------------------------------------------------------------------
	public String getCanonicalName(String species, String commonName) {
		try {
			return server.getCanonicalName(species, commonName);
		} catch (Exception e) {
			return null;
		}

	}

	// ----------------------------------------------------------------------------------------
	public String[] getAllCommonNames(String species, String commonName) {
		try {
			return server.getAllCommonNames(species, commonName);
		} catch (Exception e) {
			return null;
		}

	}

	// ----------------------------------------------------------------------------------------
	public String getCommonName(String species, String canonicalName) {
		try {
			return server.getCommonName(species, canonicalName);
		} catch (Exception e) {
			return null;
		}

	}

	// ----------------------------------------------------------------------------------------

	public String getSpecies(final BufferedReader taxRd,
			final BufferedReader gaRd) throws IOException {
		String sp = null;
		String curLine = null;

		while (null != (curLine = gaRd.readLine().trim())) {
			// Skip comment
			if (curLine.startsWith("!")) {
				// do nothing
				//System.out.println("Comment: " + curLine);
			} else {
				StringTokenizer st = new StringTokenizer(curLine, "\t");
				while (st.hasMoreTokens()) {
					String curToken = st.nextToken();
					if (curToken.startsWith("taxon")) {
						st = new StringTokenizer(curToken, ":");
						st.nextToken();
						curToken = st.nextToken();
						st = new StringTokenizer(curToken, "|");
						curToken = st.nextToken();
						//System.out.println("Taxon ID found: " + curToken);
						sp = curToken;
						sp = taxIdToName(sp, taxRd);
						taxRd.close();
						gaRd.close();
						return sp;
					}
				}
			}

		}

		taxRd.close();
		gaRd.close();
		return sp;
	}

	// Taxonomy to name.
	// taxId is an NCBI taxon ID
	// All info is availabe at:
	// http://www.ncbi.nlm.nih.gov/Taxonomy/TaxIdentifier/tax_identifier.cgi
	//
	public String taxIdToName(String taxId, final BufferedReader taxRd)
			throws IOException {
		String name = null;
		String curLine = null;

		taxRd.readLine();

		while (null != (curLine = taxRd.readLine().trim())) {
			StringTokenizer st = new StringTokenizer(curLine, "|");
			String[] oneEntry = new String[st.countTokens()];
			int counter = 0;

			while (st.hasMoreTokens()) {
				String curToken = st.nextToken().trim();
				oneEntry[counter] = curToken;
				counter++;
				if (curToken.equals(taxId)) {
					name = oneEntry[1];
					return name;
				}
			}
		}
		return name;
	}
	
	public String checkSpecies( final BufferedReader gaReader ) throws IOException {
		
		String txName = null;
		// Get taxon name
		if (taxonFile.canRead() == true) {
			final BufferedReader taxonFileReader = new BufferedReader(
					new FileReader(taxonFile));

			txName = getSpecies(taxonFileReader, gaReader);
			if (txName == null) {
				System.out.println("Warning: Cannot recognized speices.  Speices field is set to \"unknown.\"");
				System.out.println("Warning: Please check your tax_report.txt file.");
				txName = "unknown";
			}
		} else {
			System.out.println("Warning: Cannot read taxon file.");
			System.out.println("Warning: Speices field is set to \"unknown.\"");
			System.out.println("Warning: Please check your tax_report.txt file.");
			txName = "unknown";
		}
		
		return txName;
	}

} // BioDataServer

