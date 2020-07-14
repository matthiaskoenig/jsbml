package org.sbml.jsbml.ext.comp.util;

import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.SBMLDocument;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class CompFlatteningExample {

    public static void main(String[] args) throws IOException, XMLStreamException {

        SBMLDocument doc = JSBML.readSBMLFromFile(
                "/home/mkoenig/git/jsbml/extensions/comp/src/org/sbml/jsbml/ext/comp/util/submodels_example.xml"
        );

        CompFlatteningConverter converter = new CompFlatteningConverter();
        SBMLDocument flatDoc = converter.flatten(doc);

        String sbml = flatDoc.toString();
        System.out.println(sbml);
    }

}
