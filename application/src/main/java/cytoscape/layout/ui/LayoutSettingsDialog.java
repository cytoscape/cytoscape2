/* vim: set ts=2:

  File: LayoutSettingsDialog.java

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
  Dout of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package cytoscape.layout.ui;

import cytoscape.Cytoscape;

import cytoscape.layout.CyLayouts;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.LayoutProperties;
import cytoscape.layout.LayoutTask;

import cytoscape.task.util.TaskManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants.*;
import javax.swing.border.*;
import javax.swing.text.Position;


/**
 *
 * The LayoutSettingsDialog is a dialog that provides an interface into all of the
 * various settings for layout algorithms.  Each CyLayoutAlgorithm must return a single
 * JPanel that provides all of its settings.
 */
public class LayoutSettingsDialog extends JDialog implements ActionListener {
	private CyLayoutAlgorithm currentLayout = null;

	private static Object defaultLayout = null;

	// Dialog components
	private JLabel titleLabel; // Our title
	private JPanel mainPanel; // The main content pane
	private JPanel buttonBox; // Our action buttons (Save Settings, Cancel, Execute, Done)
	private JComboBox algorithmSelector; // Which algorithm we're using
	private JPanel algorithmPanel; // The panel this algorithm uses

	/**
	 * Creates a new LayoutSettingsDialog object.
	 */
	public LayoutSettingsDialog() {
		super(Cytoscape.getDesktop(), "Layout Settings", false);
		initializeOnce(); // Initialize the components we only do once
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// Are we the source of the event?
		String command = e.getActionCommand();

		if (command.equals("done")) {
			updateAllSettings();
			setVisible(false);
		} else if (command.equals("save")) {
			updateAllSettings();
			saveAllProperties();
		} else if (command.equals("execute")) {
			// Layout using the current layout
			updateAllSettings();
			TaskManager.executeTask( new LayoutTask(currentLayout,Cytoscape.getCurrentNetworkView()),
			                         LayoutTask.getDefaultTaskConfig() );
		} else if (command.equals("cancel")) {
			// Call revertSettings for each layout
			revertAllSettings();
			setVisible(false);
		} else {
			// OK, initialize and display
			initialize();
			pack();
			setLocationRelativeTo(Cytoscape.getDesktop());
			setVisible(true);
		}
	}

	private void initializeOnce() {
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		// Create our main panel
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// Create a panel for the list of algorithms
		JPanel algorithmSelectorPanel = new JPanel();
		algorithmSelector = new JComboBox();
		algorithmSelectorPanel.add(algorithmSelector);

		Border selBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(selBorder, "Layout Algorithm");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		algorithmSelectorPanel.setBorder(titleBorder);
		mainPanel.add(algorithmSelectorPanel);

		// Create a panel for algorithm's content
		this.algorithmPanel = new JPanel();
		mainPanel.add(algorithmPanel);

		// Create a panel for our button box
		this.buttonBox = new JPanel();

		JButton doneButton = new JButton("Done");
		doneButton.setActionCommand("done");
		doneButton.addActionListener(this);

		JButton saveButton = new JButton("Save Settings");
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);

		JButton executeButton = new JButton("Execute Layout");
		executeButton.setActionCommand("execute");
		executeButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		buttonBox.add(executeButton);
		buttonBox.add(saveButton);
		buttonBox.add(cancelButton);
		buttonBox.add(doneButton);
		buttonBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		mainPanel.add(buttonBox);
		setContentPane(mainPanel);
	}

	private void initialize() {
		// Populate the algorithm selector
		algorithmSelector.removeAllItems();

		// Add the "instructions"
		algorithmSelector.setRenderer(new MyItemRenderer());
		algorithmSelector.addItem("Select algorithm to view settings");

		// Get the list of known layout menus
		Set<String> menus = LayoutMenuManager.getLayoutMenuNames();
	
		for (String menu : menus) {

			if (menus.size() > 1) {
				algorithmSelector.addItem(menu);
			}

			for (CyLayoutAlgorithm algo : LayoutMenuManager.getLayoutsInMenu(menu)) {
				if (algo.getSettingsPanel() != null) {
					algorithmSelector.addItem(algo);
				}
			}
		}

		algorithmSelector.addItemListener(new AlgorithmItemListener());

		// Do we already have a defaultLayout?
		if (LayoutSettingsDialog.defaultLayout != null) {
			algorithmSelector.setSelectedItem(defaultLayout);
		}
	}

	private void updateAllSettings() {
		for ( CyLayoutAlgorithm algo : CyLayouts.getAllLayouts() ) { 
			algo.updateSettings();
		}
	}

	private void saveAllProperties() {
		for ( CyLayoutAlgorithm algo : CyLayouts.getAllLayouts() ) { 
			LayoutProperties props = algo.getSettings();
			if (props != null)
				props.saveProperties();
		}
	}

	private void revertAllSettings() {
		for ( CyLayoutAlgorithm algo : CyLayouts.getAllLayouts() ) { 
			algo.revertSettings();
		}
	}

	private class AlgorithmItemListener implements ItemListener, ComponentListener {
		public AlgorithmItemListener() {
		}

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				algorithmPanel.removeAll();

				if (e.getItem().getClass() == String.class) {
					currentLayout = null;
					LayoutSettingsDialog.defaultLayout = null;
					algorithmPanel.setBorder(null);
				} else {
					CyLayoutAlgorithm newLayout = (CyLayoutAlgorithm) e.getItem();
					LayoutSettingsDialog.defaultLayout = newLayout;

					// Replace the previous settings panel with a new one
					JPanel panel = newLayout.getSettingsPanel();
					algorithmPanel.removeAll();
					algorithmPanel.add(panel);

					// Add a component listener to the panel in case it gets dynamically updated
					panel.addComponentListener(this);

					Border selBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
					TitledBorder titleBorder = BorderFactory.createTitledBorder(selBorder,
					                                                            newLayout.toString()
					                                                            + " Settings");
					titleBorder.setTitlePosition(TitledBorder.LEFT);
					titleBorder.setTitlePosition(TitledBorder.TOP);
					algorithmPanel.setBorder(titleBorder);
					currentLayout = newLayout; // Remember which one is set
				}

				validate();
				pack();
			}
		}

		public void componentHidden(ComponentEvent e) { }

		public void componentMoved(ComponentEvent e) { }

		// This gets called by the Tunable mechanism is one of our
		// Tunable GUI's changes size.  This allows us to dynamically
		// repaint our layout in response
		public void componentResized(ComponentEvent e) {
			validate();
			doLayout();
			pack();
		}

		public void componentShown(ComponentEvent e) { }

	}

	private class MyItemRenderer extends JLabel implements ListCellRenderer {
		public MyItemRenderer() {
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			// If this is a String, we don't want to allow selection.  If this is
			// index 0, we want to set the font 
			Font f = getFont();

			if (value.getClass() == String.class) {
				setFont(f.deriveFont(Font.PLAIN));
				setText((String) value);
				setHorizontalAlignment(CENTER);
				setForeground(Color.GRAY);
				setEnabled(false);
			} else {
				setForeground(list.getForeground());
				setHorizontalAlignment(LEFT);
				setEnabled(true);

				if (isSelected) {
					setFont(f.deriveFont(Font.BOLD));
				} else {
					setFont(f.deriveFont(Font.PLAIN));
				}

				setText(value.toString());
			}

			return this;
		}
	}
}
