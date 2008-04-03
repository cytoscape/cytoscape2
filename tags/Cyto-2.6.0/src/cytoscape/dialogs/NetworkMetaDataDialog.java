/*
 * MetadataDialog.java
 *
 * Created on 2006/03/22, 12:39
 */
package cytoscape.dialogs;

import cytoscape.CyNetwork;

import cytoscape.data.readers.MetadataEntries;
import cytoscape.data.readers.MetadataParser;

import java.awt.Color;
import java.awt.Font;

import java.net.URISyntaxException;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import javax.xml.bind.JAXBException;


/**
 * Dialog for editing network metadata in RDF.<br>
 *
 * @version 1.0
 * @since 2.3
 * @see cytoscape.dialogs.NetworkMetaDataTableModel
 * @author kono
 *
 */
public class NetworkMetaDataDialog extends JDialog implements TableModelListener {
	private NetworkMetaDataTableModel metaTM;
	private MetadataParser mdp;
	private CyNetwork network;
	private String description;

	/**
	 * Creates new form MetadataDialog
	 *
	 * @throws URISyntaxException
	 */
	public NetworkMetaDataDialog(java.awt.Frame parent, boolean modal, CyNetwork network) {
		super(parent, modal);
		this.network = network;
		this.mdp = new MetadataParser(network);
		metaTM = new NetworkMetaDataTableModel(network);

		try {
			metaTM.setTable();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		description = metaTM.getDescription();

		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		Font titleFont = new Font("SansSerif", Font.BOLD, 14);

		this.setTitle("Network Metadata Editor");

		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel1 = new javax.swing.JPanel();
		titleLabel = new javax.swing.JLabel();
		mainSplitPane = new javax.swing.JSplitPane();
		metadataTableScrollPane = new javax.swing.JScrollPane();
		descriptionScrollPane = new javax.swing.JScrollPane();
		descriptionTextArea = new javax.swing.JTextArea();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		okButton.setText("Update");
		okButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					okButtonMouseClicked(evt);
				}
			});

		cancelButton.setText("Cancel");
		cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					cancelButtonMouseClicked(evt);
				}
			});

		titleLabel.setText("Network Metadata for " + network.getTitle());
		titleLabel.setFont(titleFont);
		titleLabel.setForeground(Color.BLUE);

		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                                              .add(jPanel1Layout.createSequentialGroup()
		                                                                .addContainerGap()
		                                                                .add(titleLabel,
		                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
		                                                                     500,
		                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
		                                                                .addContainerGap(5,
		                                                                                 Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                                            .add(jPanel1Layout.createSequentialGroup()
		                                                              .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                                                               Short.MAX_VALUE)
		                                                              .add(titleLabel)));

		mainSplitPane.setDividerLocation(120);
		mainSplitPane.setDividerSize(5);
		mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		metadataTableScrollPane.setViewportView(getMetadataTable());

		mainSplitPane.setTopComponent(metadataTableScrollPane);

		descriptionScrollPane.setBorder(null);
		descriptionScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));

		descriptionTextArea.setText(description);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(false);
		descriptionTextArea.setTabSize(4);
		descriptionTextArea.setEditable(true);

		descriptionScrollPane.setViewportView(descriptionTextArea);
		descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		mainSplitPane.setRightComponent(descriptionScrollPane);

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                                .add(org.jdesktop.layout.GroupLayout.TRAILING,
		                                     layout.createSequentialGroup()
		                                           .addContainerGap(201, Short.MAX_VALUE)
		                                           .add(okButton)
		                                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
		                                           .add(cancelButton).addContainerGap())
		                                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                     Short.MAX_VALUE)
		                                .add(mainSplitPane,
		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 343,
		                                     Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                              .add(layout.createSequentialGroup()
		                                         .add(jPanel1,
		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
		                                         .add(mainSplitPane,
		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                              339, Short.MAX_VALUE)
		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
		                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
		                                                    .add(okButton).add(cancelButton))
		                                         .addContainerGap()));
		pack();
	} // </editor-fold>

	private void cancelButtonMouseClicked(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
		this.dispose();
	}

	private void okButtonMouseClicked(java.awt.event.MouseEvent evt) {
		// TODO add your handling code here:
		try {
			update();
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null,
		// null);
		this.dispose();
	}

	private void update() throws JAXBException {
		Vector dataVector = metaTM.getDataVector();
		Iterator it = dataVector.iterator();

		while (it.hasNext()) {
			Vector row = (Vector) it.next();
			String label = (String) row.get(0);
			Object value = row.get(1);

			if (label != null) {
				if (value == null) {
					mdp.setMetadata(MetadataEntries.valueOf(label.toUpperCase()), "N/A");
				} else {
					mdp.setMetadata(MetadataEntries.valueOf(label.toUpperCase()), value.toString());
				}
			}
		}

		mdp.setMetadata(MetadataEntries.DESCRIPTION, descriptionTextArea.getText());
	}

	/**
	 * @return Returns the metadataTable.
	 */
	private JTable getMetadataTable() {
		if (metadataTable == null) {
			metaTM.addTableModelListener(new metadataTableListener());

			metadataTable = new JTable(metaTM);
			metadataTable.setRowSelectionAllowed(true);
		}

		return metadataTable;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param arg0 DOCUMENT ME!
	 */
	public void tableChanged(TableModelEvent arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * table listener.
	 *
	 * @author kono
	 *
	 */
	class metadataTableListener implements TableModelListener {
		public void tableChanged(TableModelEvent arg0) {
		}
	}

	// Variables declaration - do not modify
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextArea descriptionTextArea;
	private javax.swing.JLabel titleLabel;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane metadataTableScrollPane;
	private javax.swing.JScrollPane descriptionScrollPane;
	private javax.swing.JSplitPane mainSplitPane;
	private JTable metadataTable;

	// End of variables declaration
}
