package org.sbml.jsbml.ext.comp.util;

import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.SBMLDocument;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URISyntaxException;

public class CompFlatteningExample {

    public static void main(String[] args) throws IOException, XMLStreamException, URISyntaxException {

        int test_case = 5;

        SBMLDocument doc = JSBML.readSBMLFromFile(
                "/home/mkoenig/git/jsbml/extensions/comp/resources/testFlattening/test" + test_case + ".xml"
        );
        SBMLDocument refDoc = JSBML.readSBMLFromFile(
                "/home/mkoenig/git/jsbml/extensions/comp/resources/testFlattening/test" + test_case + "_flat.xml"
        );

        System.out.println("---------------------------------------------------");
        System.out.println("Comp model");
        System.out.println("---------------------------------------------------");
        System.out.println(JSBML.writeSBMLToString(doc));
        System.out.println("---------------------------------------------------");
        System.out.println(JSBML.writeSBMLToString(refDoc));
        System.out.println("---------------------------------------------------");
        System.out.println();

        CompFlatteningConverter converter = new CompFlatteningConverter();
        SBMLDocument flatDoc = converter.flatten(doc);

        System.out.println("---------------------------------------------------");
        System.out.println("Flat model");
        System.out.println("---------------------------------------------------");
        System.out.println(JSBML.writeSBMLToString(flatDoc));
        System.out.println("---------------------------------------------------");

    }

}
