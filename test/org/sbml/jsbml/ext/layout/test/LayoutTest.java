/*
 * $Id:  LaoutTest.java 11:29:35 draeger $
 * $URL: LaoutTest.java $
 *
 * ---------------------------------------------------------------------------- 
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML> 
 * for the latest version of JSBML and more information about SBML. 
 * 
 * Copyright (C) 2009-2011 jointly by the following organizations: 
 * 1. The University of Tuebingen, Germany 
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK 
 * 3. The California Institute of Technology, Pasadena, CA, USA 
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation. A copy of the license agreement is provided 
 * in the file named "LICENSE.txt" included with this software distribution 
 * and also available online as <http://sbml.org/Software/JSBML/License>. 
 * ---------------------------------------------------------------------------- 
 */ 
package org.sbml.jsbml.ext.layout.test;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

import org.junit.Ignore;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.8
 * @date 26.05.2011
 */
@Ignore
public class LayoutTest {

	/**
	 * @param args
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException {
		SBMLDocument doc = SBMLReader.read(new File(args[0]));
		Model model = doc.getModel();
		ExtendedLayoutModel sbase = (ExtendedLayoutModel) model.getExtension("http://www.sbml.org/sbml/level3/version1/layout/version1");		
		Layout layout = sbase.getListOfLayouts().get(0);
		layout.getDimensions();
	}

}
