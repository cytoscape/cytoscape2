package cytoscape.dialogs;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

import cytoscape.Cytoscape;

/**
 * Options dialog for exporting to bitmap images.
 * @author Samad Lotia
 */
public class ExportBitmapOptionsDialog extends JDialog
{
	private final static long serialVersionUID = 1202339872514493L;
	private JFormattedTextField zoomField;
	private JFormattedTextField widthInPixelsField;
	private JFormattedTextField heightInPixelsField;
	private JFormattedTextField widthInInchesField;
	private JFormattedTextField heightInInchesField;
	private JComboBox resolutionComboBox;
	private JButton okButton;

	private int originalWidth;
	private int originalHeight;

	/**
	 * Creates the options dialog.
	 * This dialog disposes itself when it is closed.
	 * @param imageWidth The image width to be exported
	 * @param imageHeight The image height to be exported
	 * @param listener The action will be called when the "OK" button is clicked
	 */
	public ExportBitmapOptionsDialog(int imageWidth, int imageHeight)
	{
		super(Cytoscape.getDesktop(), "Export Bitmap Options");
		this.originalWidth = imageWidth;
		this.originalHeight = imageHeight;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Container content = getContentPane();

		JPanel sizePanel = new JPanel();
		sizePanel.setBorder(new TitledBorder(new EtchedBorder(), "Image Size"));
		sizePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5,5,5,5);

		JLabel zoomLabel = new JLabel("Zoom: ");
		c.gridx = 0;			c.gridy = 0;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(zoomLabel, c);
		
		zoomField = new JFormattedTextField(new DecimalFormat());
		zoomField.setColumns(3);
		ZoomListener zoomListener = new ZoomListener(zoomField);
		c.gridx = 1;			c.gridy = 0;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(zoomField, c);

		JLabel zoomPercentLabel = new JLabel("%");
		c.gridx = 2;			c.gridy = 0;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(zoomPercentLabel, c);

		JSeparator separator0 = new JSeparator();
		c.gridx = 0;			c.gridy = 1;
		c.gridwidth = 3;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(separator0, c);

		JLabel widthInPixelsLabel = new JLabel("Width: ");
		c.gridx = 0;			c.gridy = 2;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(widthInPixelsLabel, c);

		widthInPixelsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		widthInPixelsField.setColumns(4); 
		new WidthInPixelsListener(widthInPixelsField);
		c.gridx = 1;			c.gridy = 2;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(widthInPixelsField, c);

		JLabel widthPixelsLabel = new JLabel("pixels");
		c.gridx = 2;			c.gridy = 2;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(widthPixelsLabel, c);

		JLabel heightInPixelsLabel = new JLabel("Height:");
		c.gridx = 0;			c.gridy = 3;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(heightInPixelsLabel, c);

		heightInPixelsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		heightInPixelsField.setColumns(4);
		new HeightInPixelsListener(heightInPixelsField);
		c.gridx = 1;			c.gridy = 3;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(heightInPixelsField, c);

		JLabel heightPixelsLabel = new JLabel("pixels");
		c.gridx = 2;			c.gridy = 3;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(heightPixelsLabel, c);

		JSeparator separator1 = new JSeparator();
		c.gridx = 0;			c.gridy = 4;
		c.gridwidth = 3;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(separator1, c);

		JLabel widthInInchesLabel = new JLabel("Width: ");
		c.gridx = 0;			c.gridy = 5;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(widthInInchesLabel, c);

		widthInInchesField = new JFormattedTextField(new DecimalFormat());
		widthInInchesField.setColumns(4); 
		new WidthInInchesListener(widthInInchesField);
		c.gridx = 1;			c.gridy = 5;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(widthInInchesField, c);

		JLabel widthInchesLabel = new JLabel("inches");
		c.gridx = 2;			c.gridy = 5;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(widthInchesLabel, c);

		JLabel heightInInchesLabel = new JLabel("Height:");
		c.gridx = 0;			c.gridy = 6;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(heightInInchesLabel, c);

		heightInInchesField = new JFormattedTextField(new DecimalFormat());
		heightInInchesField.setColumns(4);
		new HeightInInchesListener(heightInInchesField);
		c.gridx = 1;			c.gridy = 6;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(heightInInchesField, c);
		
		JLabel heightInchesLabel = new JLabel("inches");
		c.gridx = 2;			c.gridy = 6;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(heightInchesLabel, c);

		JLabel resolutionLabel = new JLabel("Resolution:");
		c.gridx = 0;			c.gridy = 7;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(resolutionLabel, c);

		Integer[] resolutions = { new Integer(72), new Integer(100), new Integer(150), new Integer(300) };
		resolutionComboBox = new JComboBox(resolutions);
		resolutionComboBox.addActionListener(zoomListener);
		c.gridx = 1;			c.gridy = 7;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		sizePanel.add(resolutionComboBox, c);
		
		JLabel dpiLabel = new JLabel("DPI");
		c.gridx = 2;			c.gridy = 7;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		sizePanel.add(dpiLabel, c);

		okButton = new JButton("   OK   ");
		okButton.setDefaultCapable(true);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelListener());

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(okButton);

		content.setLayout(new GridBagLayout());
		c.gridx = 0;		c.gridy = 0;
		c.weightx = 1.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(sizePanel, c);
		c.gridx = 0;		c.gridy = 1;
		c.weightx = 1.0;	c.weighty = 1.0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		content.add(buttonsPanel, c);

		updateOnZoom(1.0);

		setLocationRelativeTo(Cytoscape.getDesktop());
		pack();
	}

	public double getZoom()
	{
		return ((Number) zoomField.getValue()).doubleValue() / 100.0;
	}

	/**
	 * Add an action for when the "OK" button is pressed.
	 */
	public void addActionListener(ActionListener l)
	{
		okButton.addActionListener(l);
	}

	private void updateOnZoom(double newZoom)
	{
		zoomField.setValue(new Double(newZoom * 100.0));
		int newWidth = (int) (newZoom * originalWidth);
		int newHeight = (int) (newZoom * originalHeight);
		widthInPixelsField.setValue(new Integer(newWidth));
		heightInPixelsField.setValue(new Integer(newHeight));
		double dpi = ((Number) resolutionComboBox.getSelectedItem()).doubleValue();
		double newWidthInches = newWidth / dpi;
		double newHeightInches = newHeight / dpi;
		widthInInchesField.setValue(new Double(newWidthInches));
		heightInInchesField.setValue(new Double(newHeightInches));
	}

	private void updateOnWidthPixels(int newWidthPixels)
	{
		double newZoom = ((double) newWidthPixels) / ((double) originalWidth);
		updateOnZoom(newZoom);
	}

	private void updateOnHeightPixels(int newHeightPixels)
	{
		double newZoom = ((double) newHeightPixels) / ((double) originalHeight);
		updateOnZoom(newZoom);
	}

	private void updateOnWidthInches(double newWidthInches)
	{
		double dpi = ((Number) resolutionComboBox.getSelectedItem()).doubleValue();
		updateOnWidthPixels((int) (newWidthInches * dpi));
	}

	private void updateOnHeightInches(double newHeightInches)
	{
		double dpi = ((Number) resolutionComboBox.getSelectedItem()).doubleValue();
		updateOnHeightPixels((int) (newHeightInches * dpi));
	}

	private abstract class FormattedFieldListener extends FocusAdapter implements ActionListener
	{
		public abstract void update();

		private JFormattedTextField field;
		public FormattedFieldListener(JFormattedTextField field)
		{
			this.field = field;
			field.addActionListener(this);
			field.addFocusListener(this);
		}

		public void actionPerformed(ActionEvent e)
		{
			update();
		}
		
		public void focusLost(FocusEvent l)
		{
			try
			{
				field.commitEdit();
			}
			catch (ParseException exp)
			{
				return;
			}
			update();
		}
	}

	private class ZoomListener extends FormattedFieldListener
	{
		public ZoomListener(JFormattedTextField field)
		{
			super(field);
		}

		public void update()
		{
			double zoom = ((Number) zoomField.getValue()).doubleValue();
			zoom /= 100.0;
			updateOnZoom(zoom);
		}
	}

	private class WidthInPixelsListener extends FormattedFieldListener
	{
		public WidthInPixelsListener(JFormattedTextField field)
		{
			super(field);
		}

		public void update()
		{
			int width = ((Number) widthInPixelsField.getValue()).intValue();
			updateOnWidthPixels(width);
		}
	}

	private class HeightInPixelsListener extends FormattedFieldListener
	{
		public HeightInPixelsListener(JFormattedTextField field)
		{
			super(field);
		}

		public void update()
		{
			int height = ((Number) heightInPixelsField.getValue()).intValue();
			updateOnHeightPixels(height);
		}
	}

	private class WidthInInchesListener extends FormattedFieldListener
	{
		public WidthInInchesListener(JFormattedTextField field)
		{
			super(field);
		}

		public void update()
		{
			double width = ((Number) widthInInchesField.getValue()).doubleValue();
			updateOnWidthInches(width);
		}
	}

	private class HeightInInchesListener extends FormattedFieldListener
	{
		public HeightInInchesListener(JFormattedTextField field)
		{
			super(field);
		}

		public void update()
		{
			double height = ((Number) heightInInchesField.getValue()).doubleValue();
			updateOnHeightInches(height);
		}
	}

	private class CancelListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			ExportBitmapOptionsDialog.this.dispose();
		}
	}
}
