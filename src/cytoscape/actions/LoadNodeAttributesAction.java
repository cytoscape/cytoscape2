// $Revision$
// $Date$
// $Author$
package cytoscape.actions;

import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CytoscapeDataImpl;
import cytoscape.data.CytoscapeData;
import cytoscape.data.servers.BioDataServer;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.util.TaskManager;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.util.CyFileFilter;
import cytoscape.util.CytoscapeAction;
import cytoscape.util.FileUtil;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/* 
 * Added by T. Ideker April 16, 2003
 * to allow loading of node / edge attributes from the GUI
 */

public class LoadNodeAttributesAction extends CytoscapeAction {

    /**
     * Constructor.
     */
    public LoadNodeAttributesAction() {
        super("Node Attributes...");
        setPreferredMenu("File.Load");
    }

    /**
     * User Initiated Request.
     *
     * @param e Action Event.
     */
    public void actionPerformed(ActionEvent e) {

        //  Use a Default CyFileFilter:  enables user to select any file type.
        CyFileFilter nf = new CyFileFilter();

        // get the file name
        File file = FileUtil.getFile("Load Node Attributes",
                    FileUtil.LOAD, new CyFileFilter[]{nf});

        if (file != null) {

            //  Create Load Attributes Task
            LoadAttributesTask task = new LoadAttributesTask
                    (file, Cytoscape.getNodeNetworkData(),
                            LoadAttributesTask.NODE_ATTRIBUTES);

            //  Configure JTask Dialog Pop-Up Box
            JTaskConfig jTaskConfig = new JTaskConfig();
            jTaskConfig.setOwner(Cytoscape.getDesktop());
            jTaskConfig.displayCloseButton(true);
            jTaskConfig.displayStatus(true);
            jTaskConfig.setAutoDispose(false);

            //  Execute Task in New Thread;  pop open JTask Dialog Box.
            TaskManager.executeTask(task, jTaskConfig);

        }
    }
}

/**
 * Task to Load New Node/Edge Attributes Data.
 */
class LoadAttributesTask implements Task {
    private TaskMonitor taskMonitor;
    private File file;
    private CytoscapeData attributes;
    private int type;
    static final int NODE_ATTRIBUTES = 0;
    static final int EDGE_ATTRIBUTES = 1;

    /**
     * Constructor.
     * @param file File Object.
     * @param attributes Attributes Object.
     * @param type NODE_ATTRIBUTES or EDGE_ATTRIBUTES
     */
    LoadAttributesTask (File file, CytoscapeData attributes,
            int type) {
        this.file = file;
        this.attributes = attributes;
        this.type = type;
    }

    /**
     * Executes Task.
     */
    public void run() {
        try {
            //  Get Defaults.
            BioDataServer bioDataServer = Cytoscape.getBioDataServer();
            String speciesName = CytoscapeInit.getDefaultSpeciesName();
            boolean canonicalize = !CytoscapeInit.noCanonicalization();

            //  Read in Data
            attributes.setTaskMonitor(taskMonitor);
            //attributes.readAttributesFromFile(bioDataServer,
            //                                  speciesName, 
            //                                  file.getAbsolutePath(), 
            //                                  canonicalize);
            
            Cytoscape.loadAttributes( new String[] { file.getAbsolutePath() },
                                      new String[] {},
                                      canonicalize,
                                      bioDataServer,
                                      speciesName );

            //  Inform others via property change event.
            Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED,
                        null, null );
        } catch (IOException e) {
            taskMonitor.setException(e, "Unable to load attributes file.");
        }
    }

    /**
     * Halts the Task:  Not Currently Implemented.
     */
    public void halt() {
        //   Task can not currently be halted.
    }

    /**
     * Sets the Task Monitor Object.
     * @param taskMonitor
     * @throws IllegalThreadStateException
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
        if (type == NODE_ATTRIBUTES) {
            return new String ("Loading Node Attributes");
        } else {
            return new String ("Loading Edge Attributes");
        }
    }
}