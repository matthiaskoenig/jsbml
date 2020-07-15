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


@RunWith(Parameterized.class)
public class CompFlattenExamplesTest {
    private final static Logger LOGGER = Logger.getLogger(CompFlattenExamplesTest.class.getName());

    private int flattenTest;

    public CompFlattenExamplesTest(int flattenTest) {
        this.flattenTest = flattenTest;
    }

    /**
     * List of all comp flattening test files
     */
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
        runTestOnFiles(urlFile, urlExpected, String.valueOf(flattenTest));
    }

    private static void runTestOnFiles(URL urlFile, URL urlExpected, String name) {
        try {
            File file = new File(urlFile.toURI());
            File expectedFile = new File(urlExpected.toURI());
            SBMLReader reader = new SBMLReader();
            SBMLDocument document = reader.readSBML(file);
            SBMLDocument expectedDocument = reader.readSBML(expectedFile);
            CompFlatteningConverter compFlatteningConverter =
                    new CompFlatteningConverter();
            SBMLDocument flatDocument =
                    compFlatteningConverter.flatten(document);
            LOGGER.info("Testing Model " + name + ": ");
            System.out.println("\n-------");
            SBMLWriter.write(document, System.out, ' ', (short) 2);
            System.out.println("\n-------");
            SBMLWriter.write(flatDocument, System.out, ' ', (short) 2);
            System.out.println("\n-------");
            SBMLWriter.write(expectedDocument, System.out, ' ', (short) 2);
            System.out.println("\n-------");
            Assert.assertTrue("Success Testing Model",
                    expectedDocument.equals(flatDocument));
        } catch (XMLStreamException | IOException e) {
            LOGGER.warning("Failed testing Model " + name + ": ");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
