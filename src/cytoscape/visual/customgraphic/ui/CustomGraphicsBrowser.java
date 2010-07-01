package cytoscape.visual.customgraphic.ui;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;

import org.jdesktop.swingx.JXList;

import cytoscape.Cytoscape;
import cytoscape.render.stateful.CustomGraphic;
import cytoscape.visual.customgraphic.CustomGraphicsPool;
import cytoscape.visual.customgraphic.CyCustomGraphics;
import cytoscape.visual.customgraphic.NullCustomGraphics;
import cytoscape.visual.customgraphic.URLImageCustomGraphics;

/**
 * Display list of images available as custom graphics
 * 
 * @author kono
 */
public class CustomGraphicsBrowser extends JXList implements PropertyChangeListener {

	private static final long serialVersionUID = -8342056297304400824L;

	private DefaultListModel model;
	private final CustomGraphicsPool pool;
	
	// For drag and drop
	private static DataFlavor urlFlavor;
	
	static {
		try {
			urlFlavor = new DataFlavor(
					"application/x-java-url; class=java.net.URL");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	

	/**
	 * Creates new form CustomGraphicsBrowserPanel
	 * 
	 * @throws IOException
	 */
	public CustomGraphicsBrowser() throws IOException {
		pool = Cytoscape.getVisualMappingManager().getCustomGraphicsPool();

		initComponents();
		addAllImages();

		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(Cytoscape.SESSION_LOADED, this);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {
		this.setMaximumSize(new Dimension(300, 10000));
		this.setBackground(new java.awt.Color(255, 255, 255));
		model = new DefaultListModel();
		this.setModel(model);
		this.setCellRenderer(new CustomGraphicsCellRenderer());
		this.setDropTarget(new URLDropTarget());

	}// </editor-fold>
	
	
	public void removeCustomGraphics(final CyCustomGraphics<?> cg) {
		model.removeElement(cg);
	}

	/**
	 * Add on-memory images to Model.
	 */
	private void addAllImages() {
		final Collection<CyCustomGraphics<?>> graphics = pool.getAll();

		for (CyCustomGraphics<?> cg : graphics) {
			if (cg instanceof NullCustomGraphics == false)
				model.addElement(cg);
		}
	}

	private void addCustomGraphics(final String urlStr) {
		CyCustomGraphics<CustomGraphic> cg = null;
		try {
			cg = new URLImageCustomGraphics(urlStr);
			if (cg != null) {
				pool.addGraphics(cg.hashCode(), cg, new URL(urlStr));
				model.addElement(cg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * D & D
	 * 
	 * @author kono
	 * 
	 */
	private class URLDropTarget extends DropTarget {

		private static final long serialVersionUID = -7007999535331084109L;

		public void drop(DropTargetDropEvent dtde) {

			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			final Transferable trans = dtde.getTransferable();
			//dumpDataFlavors(trans);
			boolean gotData = false;
			try {
				if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					final List<File> fileList = (List<File>) trans
							.getTransferData(DataFlavor.javaFileListFlavor);

					for (File file : fileList) {
						addCustomGraphics(file.toURI().toURL().toString());
					}
					gotData = true;
				} else if (trans.isDataFlavorSupported(urlFlavor)) {
					URL url = (URL) trans.getTransferData(urlFlavor);
					// Add image
					addCustomGraphics(url.toString());
					gotData = true;
				} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String s = (String) trans
							.getTransferData(DataFlavor.stringFlavor);
					
					URL url = new URL(s);
					addCustomGraphics(url.toString());
					gotData = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dtde.dropComplete(gotData);
			}
		}

		// This is for debugging
		private void dumpDataFlavors(Transferable trans) {
			System.out.println("Flavors:");
			DataFlavor[] flavors = trans.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				System.out.println("*** " + i + ": " + flavors[i]);
			}
		}

	}

	public void propertyChange(PropertyChangeEvent e) {
		// Clear the model, and build new List from current pool of graphics
		model.removeAllElements();
		model.clear();
		
		addAllImages();
	}

}
