package cytoscape.visual.customgraphic.impl;

import java.util.HashMap;
import java.util.Map;

import cytoscape.visual.customgraphic.CyCustomGraphics;
import cytoscape.visual.customgraphic.CyCustomGraphicsParser;
import cytoscape.visual.customgraphic.CyCustomGraphicsParserFactory;

public class CyCustomGraphicsParserFactoryImpl implements
		CyCustomGraphicsParserFactory {

	private final Map<Class<? extends CyCustomGraphics>, CyCustomGraphicsParser> parserMap;
	
	public CyCustomGraphicsParserFactoryImpl() {
		parserMap = new HashMap<Class<? extends CyCustomGraphics>, CyCustomGraphicsParser>();
	}
	

	public CyCustomGraphicsParser getParser(String customGraphicsClassName) {
		if(customGraphicsClassName == null || customGraphicsClassName.trim().length() == 0)
			return null;
		
		Class<? extends CyCustomGraphics> cgClass;
		try {
			cgClass = (Class<? extends CyCustomGraphics>) Class.forName(customGraphicsClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (ClassCastException cce) {
			cce.printStackTrace();
			return null;
		}
		
		return parserMap.get(cgClass);
	}


	public void registerParser(Class<? extends CyCustomGraphics> cgClass,
			CyCustomGraphicsParser parser) {
		
		parserMap.put(cgClass, parser);
	}

}
