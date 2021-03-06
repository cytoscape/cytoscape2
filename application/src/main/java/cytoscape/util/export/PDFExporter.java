package cytoscape.util.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Graphics2D;

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.view.InternalFrameComponent;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.DefaultFontMapper;
import cytoscape.ding.DingNetworkView;

/**
 * PDF exporter by the iText library.
 * 
 * @author Samad Lotia
 */
public class PDFExporter implements Exporter {
	private boolean exportTextAsFont = true;

	public void export(final CyNetworkView view, final FileOutputStream stream)
			throws IOException {

		final DingNetworkView dView = (DingNetworkView) view;
		dView.setPrintingTextAsShape(!exportTextAsFont);

		final InternalFrameComponent ifc = Cytoscape.getDesktop()
				.getNetworkViewManager().getInternalFrameComponent(view);
		Rectangle pageSize = PageSize.LETTER;
		Document document = new Document(pageSize);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, stream);
			try {
				document.open();
				PdfContentByte cb = writer.getDirectContent();
				Graphics2D g = null;
				if (exportTextAsFont) {
					g = cb.createGraphics(pageSize.getWidth(), pageSize
							.getHeight(), new DefaultFontMapper());
				} else {
					g = cb.createGraphicsShapes(pageSize.getWidth(), pageSize
							.getHeight());
				}

				double imageScale = Math.min(pageSize.getWidth()
						/ ((double) ifc.getWidth()), pageSize.getHeight()
						/ ((double) ifc.getHeight()));
				g.scale(imageScale, imageScale);

				ifc.print(g);
				g.dispose();
			} finally {
				if (document != null) {
					document.close();
				}
				if (writer != null) {
					writer.close();
				}
			}
		} catch (DocumentException exp) {
			throw new IOException(exp.getMessage());
		}
	}

	public void setExportTextAsFont(boolean pExportTextAsFont) {
		exportTextAsFont = pExportTextAsFont;
	}
}
