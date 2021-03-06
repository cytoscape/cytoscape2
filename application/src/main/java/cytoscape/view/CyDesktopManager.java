/*
  File: CyNodeView.java

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
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package cytoscape.view;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import cytoscape.Cytoscape;
import java.awt.Dimension;

/**
 *
  */
public class CyDesktopManager  {
	
	public static enum Arrange {
		GRID,CASCADE,HORIZONTAL,VERTICAL,DEFAULT
	}
	public static int MINIMUM_WIN_WIDTH = 200;
	public static int MINIMUM_WIN_HEIGHT = 200;
	
	public static int DEFAULT_WIN_WIDTH = 400;
	public static int DEFAULT_WIN_HEIGHT = 400;
		
	protected static JDesktopPane desktop;
	private CyDesktopManager() {
		desktop = Cytoscape.getDesktop().getNetworkViewManager().getDesktopPane();
	}
			
	//Closes all open windows
	public  void closeAllWindows() {
		JInternalFrame[] allFrames = desktop.getAllFrames();
		for (int i= allFrames.length -1; i>=0; i--) {
			allFrames[i].dispose();			
		}
	}
		
	// Implementation of grid layout algorithm
	// gridLayout -- an int array-- int[i] holds the number of row for column i 
	private static void getGridLayout(final int pTotal, final int pCol, final int pRow, int[] gridLayout) {
		if (pTotal > pRow) {
			int row = -1;
			if (pTotal%pCol == 0) {
				row = pTotal/pCol;
				gridLayout[pCol-1] = row;
			}
			else {
				row = pRow;				
				gridLayout[pCol-1] = pRow;
			}
			getGridLayout(pTotal-row, pCol-1,row, gridLayout);
		}
		else {
			gridLayout[0] = pTotal;
		}		
	}
	
	
	// Arrange all windows in the desktop according to the given style
	public static void arrangeFrames(Arrange pStyle) {
		if (desktop == null)
			new CyDesktopManager();
		
		final Dimension desktopSize = desktop.getSize();
		
		final JInternalFrame[] allFrames = desktop.getAllFrames();
		
		int frameCount = allFrames.length; 
		if ( frameCount == 0)
			return;

		if (pStyle == Arrange.CASCADE) {
			int delta_x = 20;
			int delta_y = 20;
			int delta_block = 50;
						
			int[] x = new int[frameCount];
			int[] y = new int[frameCount];
			int[] w = new int[frameCount];
			int[] h = new int[frameCount];
			x[0] = 0; y[0] = 0; w[0] = 0; h[0] =0;
			int width = 0;
			int height = 0;

			boolean multiBlock = false;
			int blockSize =0;
			for (int i=1; i<frameCount; i++) {
				blockSize++;
				x[i] = x[i-1] + delta_x;
				y[i] = y[i-1] + delta_y;

				if (desktopSize.height - y[i]<MINIMUM_WIN_HEIGHT) {
					y[i] =0;
					multiBlock = true;
				}
				if (desktopSize.width - x[i]<MINIMUM_WIN_WIDTH) {
					x[i] = x[i-1];
				}
				
				// Determine the w,h for the previous block and start of another block 
				if (y[i]==0 && multiBlock) {
					// The first block
					if (i == blockSize) {
						width = desktopSize.width - x[i-1];
						height = desktopSize.height - y[i-1];
					}
					
					// The blocks other than the first (i.e. 2nd, 3rd, 4th, ...)
					if (i > blockSize) { 					
						//try to use the same (w, h) as previous block
						width = w[i-blockSize -1];
						height = h[i-blockSize -1];
						
						if (desktopSize.width - x[i-1] < width) {
							width = desktopSize.width - x[i-1];
							if (width < MINIMUM_WIN_WIDTH) {
								width = MINIMUM_WIN_WIDTH;								
							}
						}
						if (desktopSize.height - y[i-1]<MINIMUM_WIN_HEIGHT) {
							height = MINIMUM_WIN_HEIGHT;	
						}						
					}									

					// Set width the same for the whole block
					for (int j=0; j< blockSize; j++) {
						w[i-j-1] = width;
						h[i-j-1] = height;	
					}
					
					//start of another block
					x[i] = x[i-blockSize] + delta_block; 
					if (x[i] > (desktopSize.width - delta_x * blockSize - MINIMUM_WIN_WIDTH)) {						
						x[i] = x[i-blockSize];
					}
					blockSize =0;	
				}
			}

			// Handle the last block
			if (!multiBlock) { // single block
				for (int i = 0; i < frameCount; i++) {
					w[frameCount-1-i] = desktopSize.width - x[frameCount - 1];
					h[frameCount-1-i] = desktopSize.height - y[frameCount - 1];					

					if (desktopSize.width - x[frameCount-1-i]<MINIMUM_WIN_WIDTH) {
						w[frameCount-1-i] = MINIMUM_WIN_WIDTH;	
					}
					if (desktopSize.height - y[frameCount-1-i]<MINIMUM_WIN_HEIGHT) {
						h[frameCount-1-i] = MINIMUM_WIN_HEIGHT;	
					}
				}
			}
			else { //case for multiBlock
				//try to use the same (w, h) as previous block
				width = w[frameCount-blockSize -1];
				height = h[frameCount-blockSize -1];
								
				if (desktopSize.width - x[frameCount-1] < width) {
					width = desktopSize.width - x[frameCount-1];
					if (width < MINIMUM_WIN_WIDTH) {
						width = MINIMUM_WIN_WIDTH;								
					}
				}
				if (desktopSize.height - y[frameCount-1]<MINIMUM_WIN_HEIGHT) {
					height = MINIMUM_WIN_HEIGHT;	
				}						
				
				for (int i = 0; i < blockSize; i++) {
					w[frameCount-1-i] = width;
					h[frameCount-1-i] = height;
				}				
			}
			
			if (desktopSize.height - MINIMUM_WIN_HEIGHT < delta_y ) { // WinHeight is too small, This is a special case
				double delta_x1 = ((double)(desktopSize.width - MINIMUM_WIN_WIDTH))/(frameCount-1);
				for (int i = 0; i < frameCount; i++) {
					x[i] = (int) Math.ceil( i * delta_x1);
					y[i] =0;
					w[i] = MINIMUM_WIN_WIDTH;
					h[i] = MINIMUM_WIN_HEIGHT;
				}
			}
			
			//Arrange all frames on the screen
			for (int i=0; i<frameCount; i++) {
				allFrames[frameCount-1-i].setBounds(x[i], y[i], w[i], h[i]);
			}
		}
		else if (pStyle == Arrange.GRID) {
			// Determine the max_col and max_row for grid layout 
			int maxCol = (new Double(Math.ceil(Math.sqrt(frameCount)))).intValue();
			int maxRow = maxCol;
			while (true) {
				if (frameCount <= maxCol*(maxRow -1)) {
					maxRow--;
					continue;
				}
				break;
			}

			// Calculate frame layout on the screen, i.e. the number of frames for each column 
			int[] gridLayout = new int[maxCol];
			getGridLayout(frameCount, maxCol, maxRow, gridLayout);
			
			// Apply the layout on screen
			int w = desktopSize.width/maxCol;
			int curFrame = frameCount -1;
			for (int col=maxCol-1; col>=0; col--) {
				int h = desktopSize.height/gridLayout[col];
				
				for (int i=0; i< gridLayout[col]; i++) {
					int x = col * w;
					int y = (gridLayout[col]-i-1)* h;					
					allFrames[curFrame--].setBounds(x, y, w, h);
				}				
			}
		}
		else if (pStyle == Arrange.HORIZONTAL) {
			int x = 0;
			int y = 0;
			int w = desktopSize.width;
			int h = desktopSize.height/frameCount;
			if (h < MINIMUM_WIN_HEIGHT ) {
				h = MINIMUM_WIN_HEIGHT;
			}
			
			double delta_y = 0;
			if (frameCount > 1) {
				if (h < MINIMUM_WIN_HEIGHT) {
					delta_y = ((double)(desktopSize.height - MINIMUM_WIN_HEIGHT))/(frameCount-1);						
				}
				else {
					delta_y = ((double)(desktopSize.height))/(frameCount);
				}
			}
			
			for (int i=0; i< frameCount; i++) {
				y = (int)(delta_y * i);
				if (y> desktopSize.height - MINIMUM_WIN_HEIGHT) {
					y = desktopSize.height - MINIMUM_WIN_HEIGHT;
				}
				allFrames[frameCount-i-1].setBounds(x, y, w, h);
			}
		}
		else if (pStyle == Arrange.VERTICAL) {
			int x = 0;
			int y = 0;
			int w = desktopSize.width/frameCount;
			int h = desktopSize.height;
			
			if (w < MINIMUM_WIN_WIDTH) {
				w = MINIMUM_WIN_WIDTH;
			}

			double delta_x = 0;
			if (frameCount > 1) {
				if (w < MINIMUM_WIN_WIDTH) {
					delta_x = ((double)(desktopSize.width - MINIMUM_WIN_WIDTH))/(frameCount-1);	
				}
				else {
					delta_x = ((double)desktopSize.width)/frameCount;
				}
			}
			
			for (int i=0; i< frameCount; i++) {
				x = (int)(delta_x * i);
				if (x > desktopSize.width - MINIMUM_WIN_WIDTH) {
					x = desktopSize.width - MINIMUM_WIN_WIDTH;
				}
				allFrames[frameCount-i-1].setBounds(x, y, w, h);
			}
		}
		else if (pStyle == Arrange.DEFAULT) {
			int x = 0;
			int y = 0;
			int w = DEFAULT_WIN_WIDTH;
			int h = DEFAULT_WIN_HEIGHT;
						
			for (int i=0; i< frameCount; i++) {
				allFrames[frameCount-i-1].setBounds(x, y, w, h);
			}
		}
		
		// Clean up.
		System.gc();
	}
	
}

