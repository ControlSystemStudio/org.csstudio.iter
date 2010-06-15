package org.csstudio.opibuilder.converter.model;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.opibuilder.converter.parser.EdmColorsListParser;
import org.csstudio.opibuilder.converter.parser.EdmDisplayParser;


/**
 * Class containing altogether Edm data model: EdmColorsList and a HashMap of
 * EdmDisplay classes, mapped with appropriate file names.
 * EdmModel is a singleton class.
 *
 * @author Matevz
 *
 */
public class EdmModel {

	private static EdmEntity genColorsList;
	private static EdmColorsList colorsList;
	private static Map<String, EdmDisplay> displaysMap;	// fileName - EdmDisplay map

	private static EdmModel instance;

	/**
	 * Provides singleton functionality of EdmModel.
	 * Creates instance of EdmModel when needed.
	 * 
	 * @return EdmModel instance.
	 * @throws EdmException when EdmModel's colors list parsing returns an error.
	 */
	public synchronized static EdmModel getInstance() throws EdmException {
		if (instance == null)
			instance = new EdmModel();
		return instance;
	}

	/**
	 * Constructor.
	 * Parses Edm colors list file and stores it.
	 * 
	 * @throws EdmException
	 */
	private EdmModel() throws EdmException {

		// init EdmColorsList
		String colorsFile = System.getProperty("edm2xml.colorsFile");
		EdmColorsListParser colorsParser = new EdmColorsListParser(colorsFile);
		genColorsList = colorsParser.getRoot();
		colorsList = new EdmColorsList(genColorsList);

		displaysMap = new HashMap<String, EdmDisplay>();
	}

	/**
	 * Returns EdmColorsList of data model.
	 * @return EdmColorsList in current data model.
	 */
	public synchronized static EdmColorsList getColorsList() {
		return colorsList;
	}

	/**
	 * Parses given EDL file and stores it in fileName - EdmDisplay map.
	 * 
	 * @param fileName EDL file to parse.
	 * @return EdmDisplay object instance.
	 * @throws EdmException if there is a parsing error.
	 */
	public synchronized static EdmDisplay getDisplay(String fileName) throws EdmException {
		//it should not be buffered since the file could be updated outside
		//if (displaysMap.containsKey(fileName))
		//	return displaysMap.get(fileName);
		//else {
			EdmDisplayParser displayParser = new EdmDisplayParser(fileName);
			EdmDisplay display = new EdmDisplay(displayParser.getRoot());

			displaysMap.put(fileName, display);

			return display;
		//}
	}
}