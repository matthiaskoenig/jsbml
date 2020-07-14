/*
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2018 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 * 4. The University of California, San Diego, La Jolla, CA, USA
 * 5. The Babraham Institute, Cambridge, UK
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.ext.comp.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.util.CompFlatteningConverter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class CompFlatten2Test {

    private final static Logger LOGGER = Logger.getLogger(CompFlatteningConverter.class.getName());

    private int flattenTest;

    public CompFlatten2Test(int flattenTest) {
        this.flattenTest = flattenTest;
    }

    @Parameterized.Parameters // (name = "testFlattening/test{0}.xml")
    public static Collection<Object[]> flatIntegers() {
        List<Object[]> args = new ArrayList<>();
        for (int i = 1; i < 62; i++) {
            args.add(new Object[]{i});
        }
        return args;
    }

    @Test
    public void testFlatteningExample() {
        ClassLoader cl = this.getClass().getClassLoader();

        URL urlFile = cl.getResource("testFlattening/" + "test" + flattenTest + ".xml");
        URL urlExpected = cl.getResource("testFlattening/" + "test" + flattenTest + "_flat.xml");
        assert urlFile != null;
        assert urlExpected != null;
        CompFlattenTest.runTestOnFiles(urlFile, urlExpected, String.valueOf(flattenTest));
    }
}
