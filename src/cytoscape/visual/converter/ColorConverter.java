package cytoscape.visual.converter;

import java.awt.Color;

import cytoscape.util.ColorUtil;

public class ColorConverter implements ValueToStringConverter {

	@Override
	public String toString(Object value) {
		if(value instanceof Color)
			return  ColorUtil.getColorAsText((Color) value);
		else
			return "";
	}

	@Override
	public Class<?> getType() {
		return Color.class;
	}

}
