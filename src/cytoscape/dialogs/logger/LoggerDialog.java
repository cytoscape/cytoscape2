package cytoscape.dialogs.logger;

import cytoscape.logger.LogLevel;
import cytoscape.logger.CyLogHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Singleton dialog */
public class LoggerDialog extends javax.swing.JFrame implements CyLogHandler
  {

  private static LoggerDialog dialog;
  private Map<LogLevel, List<String>> messageMap;
  private Map<LogLevel, JScrollPane> logTabMap;


  public static LoggerDialog getLoggerDialog()
    {
    if (dialog == null)
      {
      dialog = new LoggerDialog();
      }
    return dialog;
    }

  protected LoggerDialog()
    {
    messageMap = new HashMap<LogLevel, List<String>>();
    logTabMap = new HashMap<LogLevel, JScrollPane>();
    initComponents();
    }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
  private void initComponents()
    {
    closeButton = new javax.swing.JButton();
    clearButton = new javax.swing.JButton();
    logTabs = new javax.swing.JTabbedPane();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    closeButton.setText("Close");
    closeButton.addActionListener(new java.awt.event.ActionListener()
    {
    public void actionPerformed(java.awt.event.ActionEvent evt)
      {
      closeButtonActionPerformed(evt);
      }
    });

    clearButton.setText("Clear Messages");
    clearButton.addActionListener(new java.awt.event.ActionListener()
    {
    public void actionPerformed(java.awt.event.ActionEvent evt)
      {
      clearButtonActionPerformed(evt);
      }
    });

    logTabs.setAutoscrolls(true);


    org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(logTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(clearButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeButton)))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .add(logTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(closeButton)
                .add(clearButton))
            .addContainerGap())
    );
    pack();
    } // </editor-fold>

  private void clearButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    for (LogLevel key: this.messageMap.keySet())
      {
      this.messageMap.get(key).clear();
      ((JEditorPane)this.logTabMap.get(key).getViewport().getView()).setText("");
      }
    }

  private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    this.dispose();
    }


  public void handleLog(LogLevel level, String msg)
    {
    if (level.getLevel() > 3) return; // not adding messages for "fatal" errors
    
    if (level.equals(LogLevel.LOG_DEBUG))
      {
      // check for a "debugging" mode, if false do not add a tab
      }

    // get or create list of messages
    List<String> Messages = (this.messageMap.get(level) != null) ?
        this.messageMap.get(level) : new ArrayList<String>();
    Messages.add(msg);
    // Make sure it gets added back in
    this.messageMap.put(level, Messages);

    // obviously not the way it *will* be done in the end...
    JEditorPane MessagePane = addTab(level);
    StringBuffer sb = new StringBuffer();
    sb.append("<html><style type='text/css'>");
    sb.append("body,th,td,div,p,h1,h2,li,dt,dd ");
    sb.append("{ font-family: Tahoma, \"Gill Sans\", Arial, sans-serif; }");
    sb.append("body { margin: 0px; color: #333333; background-color: #ffffff; }");
    sb.append("#indent { padding-left: 30px; }");
    sb.append("ul {list-style-type: none}");
    sb.append("</style><body>");

    sb.append("<table width='100%' cellspacing='5'>");

    int line = 1;
    for (int i=messageMap.get(level).size()-1; i >= 0; i--) {
      sb.append("<tr><td width='5%'>" + line + "</td><td width='95%'>");
      sb.append(messageMap.get(level).get(i));
      sb.append("</td></tr>");
      if (i > 0) { sb.append("<tr><td colspan='2'><hr></td></tr>"); }
      line++;
    }
    sb.append("</table></body></html>");

    MessagePane.setContentType("text/html");
    MessagePane.setText(sb.toString());


		// We want to pop the dialog up if we get an error or warning
    if (level.equals(LogLevel.LOG_ERROR) || level.equals(LogLevel.LOG_WARN))
			{ setVisible(true); }
    }

  private JEditorPane addTab(LogLevel level)
    {
    JScrollPane ScrollPane = new JScrollPane();
    if (this.logTabMap.get(level) != null)
      { ScrollPane = this.logTabMap.get(level); }
    else
      {
      this.logTabMap.put(level, ScrollPane);
      logTabs.addTab(level.getPrettyName(), ScrollPane);
      }

    JEditorPane MessagesPane = (ScrollPane.getViewport().getView() != null) ?
        (JEditorPane) ScrollPane.getViewport().getView() : new JEditorPane();
    ScrollPane.setViewportView(MessagesPane);
    MessagesPane.setEditable(false);

    return MessagesPane;
    }


  public static void main(String args[]) {
    LoggerDialog dialog = LoggerDialog.getLoggerDialog();
    dialog.handleLog(LogLevel.LOG_ERROR, "Error, error!");
    dialog.handleLog(LogLevel.LOG_ERROR, "It's gonna blow!!!");

    dialog.handleLog(LogLevel.LOG_WARN, "Canna take much more Cap'n!");
    dialog.handleLog(LogLevel.LOG_WARN, "Foobared");
  }

  // Variables declaration - do not modify
  private javax.swing.JButton clearButton;
  private javax.swing.JButton closeButton;
  private javax.swing.JTabbedPane logTabs;
  // End of variables declaration
  }
