package cytoscape.data.readers;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import cytoscape.bookmarks.Bookmarks;

public class BookmarkReader {

	// Package name generated by JAXB based on XGMML schema file
	private static final String BOOKMARK_PACKAGE = "cytoscape.bookmarks";

	// Location of the default bookmark.
	private static final String BOOKMARK_RESOURCE_FILE = "/cytoscape/resources/bookmarks.xml";

	private Bookmarks bookmarks;

	public BookmarkReader() {
	}

	/**
	 * Read bookmark from resource.
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void readBookmarks() throws JAXBException, IOException {
		URL bookmarkSource = getClass().getResource(BOOKMARK_RESOURCE_FILE);
		readBookmarks(bookmarkSource);
	}

	/**
	 * Read bookmarks from the specified location.
	 * 
	 * @param bookmarkUrl location of bookmarks.xml as URL
	 * 
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void readBookmarks(URL bookmarkUrl) throws JAXBException,
			IOException {
		
		// Use JAXB-generated methods to create data structure
		final JAXBContext jaxbContext = JAXBContext.newInstance(
				BOOKMARK_PACKAGE, this.getClass().getClassLoader());
		// Unmarshall the XGMML file
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		bookmarks = (Bookmarks) unmarshaller.unmarshal(bookmarkUrl
				.openStream());
	}

	/**
	 * Get loaded bookmark.
	 * 
	 * @return
	 */
	public Bookmarks getBookmarks() {
		return bookmarks;
	}
}
