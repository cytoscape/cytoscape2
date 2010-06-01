package cytoscape.visual.properties;

import static cytoscape.visual.VisualPropertyDependency.Definition.NODE_CUSTOM_GRAPHICS_SIZE_SYNC;
import static cytoscape.visual.VisualPropertyDependency.Definition.NODE_SIZE_LOCKED;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cytoscape.Cytoscape;
import cytoscape.render.stateful.CustomGraphic;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.customgraphic.CustomGraphicsPool;
import cytoscape.visual.customgraphic.CyCustomGraphics;
import cytoscape.visual.customgraphic.NullCustomGraphics;
import cytoscape.visual.ui.icon.NodeIcon;
import cytoscape.visual.ui.icon.VisualPropertyIcon;
import ding.view.DNodeView;

public class NodeCustomGraphicsProp extends AbstractVisualProperty implements ChangeListener, PropertyChangeListener {

	private static final CyCustomGraphics<CustomGraphic> NULL = new NullCustomGraphics();

//	private final List<CustomGraphic> currentCG;
	
	private final int index;
	
	private Map<DNodeView, Set<CustomGraphic>> cgReferenceMap;

	public NodeCustomGraphicsProp(final Integer index) {
		super();
		this.index = index-1;
//		this.currentCG = new ArrayList<CustomGraphic>();
		
		cgReferenceMap = new HashMap<DNodeView, Set<CustomGraphic>>();
		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(Cytoscape.CYTOSCAPE_INITIALIZED, this);
	}

	@Override
	public Icon getIcon(final Object value) {
		final NodeIcon icon = new NodeIcon() {

			private static final long serialVersionUID = 403672612403499816L;
			private static final int ICON_SIZE = 128;

			@Override
			public String getName() {
				return Integer.toString(value.hashCode());
			}

			public void paintIcon(Component c, Graphics g, int x, int y) {

				super.setColor(new Color(10, 10, 10, 25));
				g2d = (Graphics2D) g;
				// AA on
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				this.setIconHeight(ICON_SIZE + 6);

				if (value == null
						|| value instanceof CyCustomGraphics<?> == false) {
					drawDefaultIcon(c);
				} else {
					final CyCustomGraphics<?> cg = (CyCustomGraphics<?>) value;

					Image originalImg = cg.getImage();
					if (originalImg == null)
						drawDefaultIcon(c);
					else {
						scaleImage(originalImg);
					}
				}

			}

			private void drawDefaultIcon(Component c) {
				g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
				g2d.setColor(Color.DARK_GRAY);
				g2d.drawString("?", c.getX() + 7,
						(int) ((c.getHeight() / 2) + 7));
				g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
			}

			private void scaleImage(final Image originalImg) {
				final float originalW = originalImg.getWidth(null);
				final float originalH = originalImg.getHeight(null);

				float ratio = 1;
				int shorterDim = 0;

				Image scaledImg;
				if (originalW > originalH) {
					ratio = originalH / originalW;
					shorterDim = (int) (ICON_SIZE * ratio);
					scaledImg = originalImg.getScaledInstance(ICON_SIZE,
							shorterDim, Image.SCALE_AREA_AVERAGING);
					g2d.drawImage(scaledImg, 5,
							3 + (ICON_SIZE - shorterDim) / 2, null);
				} else {
					ratio = originalW / originalH;
					shorterDim = (int) (ICON_SIZE * ratio);
					scaledImg = originalImg.getScaledInstance(shorterDim,
							ICON_SIZE, Image.SCALE_AREA_AVERAGING);
					g2d.drawImage(scaledImg, 5, 3, null);
				}

			}

		};

		return icon;
	}

	@Override
	public VisualPropertyType getType() {
		return VisualPropertyType.getCustomGraphicsType(index);
	}

	/**
	 * Apply ALL custom graphics 1 through 9.
	 */
	public void applyToNodeView(NodeView nv, Object o,
			VisualPropertyDependency dep) {

		// Ignore if view does not exist
		if (nv == null || nv instanceof DNodeView == false)
			return;

		final DNodeView dv = (DNodeView) nv;
				
		// If not necessary to update, ignore.
		if (o == null || o instanceof CyCustomGraphics<?> == false
				|| o instanceof NullCustomGraphics) {
			return;
		}
		
		final CyCustomGraphics<?> graphics = (CyCustomGraphics<?>) o;
		final Collection<CustomGraphic> layers = (Collection<CustomGraphic>) graphics
				.getCustomGraphics();
		
		Set<CustomGraphic> currentCG = this.cgReferenceMap.get(dv);
		if(currentCG != null && currentCG.size() != 0) {
			// Remove all of those
			for(CustomGraphic cg: currentCG)
				dv.removeCustomGraphic(cg);
		}
		currentCG = new HashSet<CustomGraphic>();

		// No need to update
		if (layers == null || layers.size() == 0)
			return;

		// Check dependency. Sync size or not.
		boolean sync = false;
		if (dep != null) {
			sync = dep.check(NODE_CUSTOM_GRAPHICS_SIZE_SYNC);
		}

		for (CustomGraphic cg : layers) {
			if (sync) {
				final CustomGraphic resized = syncSize(cg, dv,
						dep);
				dv.addCustomGraphic(resized);
				currentCG.add(resized);
			} else {
				dv.addCustomGraphic(cg);
				currentCG.add(cg);
			}
		}
		this.cgReferenceMap.put(dv, currentCG);
	}

	private CustomGraphic syncSize(final CustomGraphic cg, final DNodeView dv,
			final VisualPropertyDependency dep) {

		final double nodeW = dv.getWidth();
		final double nodeH = dv.getHeight();

		final Shape originalShape = cg.getShape();
		final Rectangle2D originalBounds = originalShape.getBounds2D();
		final double cgW = originalBounds.getWidth();
		final double cgH = originalBounds.getHeight();

		// In case size is same, return the original.
		if (nodeW == cgW && nodeH == cgH)
			return cg;

		// Check width/height lock status
		final boolean whLock = dep.check(NODE_SIZE_LOCKED);

		final AffineTransform scale;
		if (whLock) {
			scale = AffineTransform.getScaleInstance(nodeW / cgW, nodeH / cgH);
		} else {
			// Case 1: node height value is larger than width
			if (nodeW >= nodeH) {
				scale = AffineTransform.getScaleInstance((nodeW / cgW)
						* (nodeH / nodeW), nodeH / cgH);
				// scale = AffineTransform.getScaleInstance(nodeH/nodeW, 1);
			} else {
				scale = AffineTransform.getScaleInstance(nodeW / cgW,
						(nodeH / cgH) * (nodeW / nodeH));
				// scale = AffineTransform.getScaleInstance(1, nodeW/nodeH);
			}

		}
		return new CustomGraphic(scale.createTransformedShape(originalShape),
				cg.getPaintFactory());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getDefaultAppearanceObject() {
		return NULL;
	}

	public Map<Object, Icon> getIconSet() {
		final Map<Object, Icon> customGraphicsIcons = new HashMap<Object, Icon>();
		final CustomGraphicsPool pool = Cytoscape.getVisualMappingManager()
				.getCustomGraphicsPool();
		for (CyCustomGraphics<?> graphics : pool.getAll()) {
			VisualPropertyIcon icon = (VisualPropertyIcon) getIcon(graphics);
			icon.setName(graphics.getDisplayName());

			customGraphicsIcons.put(graphics, icon);
		}

		return customGraphicsIcons;
	}
	
	
	protected Set<CustomGraphic> getCurrentCustomGraphics(DNodeView dv) {
		return this.cgReferenceMap.get(dv);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		System.out.println("*******  Got VS Change! " + e.getSource());
		for(DNodeView dv: this.cgReferenceMap.keySet()) {
			dv.removeAllCustomGraphics();
		}
		this.cgReferenceMap.clear();
		this.cgReferenceMap = new HashMap<DNodeView, Set<CustomGraphic>>();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Add lister AFTER Cytoscape initialization.
		Cytoscape.getVisualMappingManager().addChangeListener(this);
	}
}
