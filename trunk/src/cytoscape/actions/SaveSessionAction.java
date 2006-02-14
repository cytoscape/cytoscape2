package cytoscape.actions;

import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBException;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.readers.GMLParser;
import cytoscape.data.readers.GMLReader2;
import cytoscape.data.readers.GMLWriter;
//import cytoscape.data.writers.CytoscapeSessionWriter;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CyFileFilter;
import cytoscape.util.CytoscapeAction;
import cytoscape.util.FileUtil;
import cytoscape.util.ZipMultipleFiles;
import cytoscape.view.CyNetworkView;

//
//	"Save Project" Action
//		1. Extract current status
//		2. Generate file based on the extracted information
//		3. Write as XML.
//		4.
//
//
public class SaveSessionAction extends CytoscapeAction {

	// Extension for the new cytoscape session file
	public static String SESSION_EXT = ".cys";

	
	/**
	 * Constructor.
	 *
	 */
	public SaveSessionAction() {
		super("Save Session");
		setPreferredMenu("File");
		setAcceleratorCombo(java.awt.event.KeyEvent.VK_S, ActionEvent.CTRL_MASK);
	}


	// If no current session file exists, open dialog box to save new session,
	// and if it exists, overwrite the file.
	public void actionPerformed(ActionEvent e) {

		String name; // file name

		// Open Dialog to ask user the file name.
		try {
			name = FileUtil.getFile("Save Current Session as Project File",
					FileUtil.SAVE, new CyFileFilter[] {}).toString();
		} catch (Exception exp) {
			// this is because the selection was canceled
			return;
		}

		if (!name.endsWith(SESSION_EXT))
			name = name + SESSION_EXT;
		
		// Create Task
		SaveSessionTask task = new SaveSessionTask(name);

		// Configure JTask Dialog Pop-Up Box
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(false);

		// Execute Task in New Thread; pop open JTask Dialog Box.
		TaskManager.executeTask(task, jTaskConfig);
	}
} // SaveAsGMLAction

/**
 * Task to save current session as a project file.
 * 
 * To save the current status (call this 'session'), we need to create the
 * following structure: 0. Networks (includes the following) 1. Nodes with
 * attributes 2. Edges with attributes 3. Tree structure of the networks 4.
 * Vizmap 5. GraphML style properties (tentative) 6. Properties 7. Hidden node
 * info.
 * 
 * everything will be stored in XML. Schema will be available soon.
 */
class SaveSessionTask implements Task {

	private String fileName;
	private CyNetwork network;
	
	private TaskMonitor taskMonitor;

	//private CytoscapeSessionWriter sw;

	/**
	 * Constructor.
	 * 
	 * @param network
	 *            Network Object.
	 * @param view
	 *            Network View Object.
	 * @throws JAXBException
	 */
	SaveSessionTask(String fileName) {
		this.fileName = fileName;
		
		// Create session writer object
		//sw = new CytoscapeSessionWriter( fileName );
		
	}
	
	

	/**
	 * Executes Task
	 */
	public void run() {
		taskMonitor.setStatus("Saving Session...");
		taskMonitor.setPercentCompleted(-1);
		
		//sw.write();
		
		taskMonitor.setPercentCompleted(100);
		taskMonitor
				.setStatus("Session successfully saved to:  " + fileName);

	}

	/**
	 * Halts the Task: Not Currently Implemented.
	 */
	public void halt() {
		// Task can not currently be halted.
	}

	/**
	 * Sets the Task Monitor.
	 * 
	 * @param taskMonitor
	 *            TaskMonitor Object.
	 */
	public void setTaskMonitor(TaskMonitor taskMonitor)
			throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Gets the Task Title.
	 * 
	 * @return Task Title.
	 */
	public String getTitle() {
		return new String("Saving Project");
	}


} // End of SaveSessionAction
