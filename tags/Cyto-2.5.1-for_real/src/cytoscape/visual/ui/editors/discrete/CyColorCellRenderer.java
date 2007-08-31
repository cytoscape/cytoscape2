package cytoscape.visual.ui.editors.discrete;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Customized by kono
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.Icon;
import javax.swing.UIManager;


/**
 * CyColorCellRenderer.
 *
 * @since Cytoscape 2.5
 */
public class CyColorCellRenderer extends DefaultCellRenderer {
    /**
     * DOCUMENT ME!
     *
     * @param color
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String toHex(Color color) {
        String red = Integer.toHexString(color.getRed());
        String green = Integer.toHexString(color.getGreen());
        String blue = Integer.toHexString(color.getBlue());

        if (red.length() == 1)
            red = "0" + red;

        if (green.length() == 1)
            green = "0" + green;

        if (blue.length() == 1)
            blue = "0" + blue;

        return ("#" + red + green + blue).toUpperCase();
    }

    protected String convertToString(Object value) {
        // Do not return color as string.
        return null;
    }

    protected Icon convertToIcon(Object value) {
        if (value == null)
            return null;

        if (value instanceof Number)
            value = new Color(((Number) value).intValue());

        return new PaintIcon((Paint) value);
    }

    public static class PaintIcon
        implements Icon {
        private final Paint color;
        private int width;
        private int height;

        public PaintIcon(Paint color) {
            this(color, 70, 10);
        }

        public PaintIcon(Paint color, int width, int height) {
            this.color = color;
            this.width = width;
            this.height = height;
        }

        public int getIconHeight() {
            return height;
        }

        public int getIconWidth() {
            return width;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g;
            Paint oldPaint = g2d.getPaint();

            if (c != null)
                width = c.getWidth() - 6;

            if (color != null) {
                g2d.setPaint(color);
                g.fillRect(
                    3,
                    y,
                    getIconWidth(),
                    getIconHeight());
            }

            g.setColor(UIManager.getColor("controlDkShadow"));
            g.drawRect(
                3,
                y,
                getIconWidth(),
                getIconHeight());

            g2d.setPaint(oldPaint);
        }
    }
}
