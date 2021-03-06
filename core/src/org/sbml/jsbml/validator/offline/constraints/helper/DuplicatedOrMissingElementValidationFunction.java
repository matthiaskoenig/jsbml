/*
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2020 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 *
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */
package org.sbml.jsbml.validator.offline.constraints.helper;

import java.util.List;

import org.apache.log4j.Logger;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.util.TreeNodeWithChangeSupport;
import org.sbml.jsbml.validator.offline.ValidationContext;
import org.sbml.jsbml.validator.offline.constraints.ValidationFunction;


/**
 * Class used to check that a child XML element is not missing or present more than once.
 * 
 * @author rodrigue
 */
public class DuplicatedOrMissingElementValidationFunction<T extends TreeNodeWithChangeSupport> implements ValidationFunction<T> {

  /**
   * Log4j logger
   */
  private static final transient Logger logger = Logger.getLogger(DuplicatedOrMissingElementValidationFunction.class);

  /**
   * an (XML) element name that should not be present more than once
   */
  private String elementName;

  /**
   * 
   */
  public DuplicatedOrMissingElementValidationFunction(String elementName) {
    this.elementName = elementName;
  }

  @Override
  public boolean check(ValidationContext ctx, T t) {

    if (elementName == null) {
      return true;
    }

    if (t.isSetUserObjects() && t.getUserObject(JSBML.CHILD_ELEMENT_NAMES) != null)
    {
      @SuppressWarnings("unchecked")
      List<String> childElementNames = (List<String>) t.getUserObject(JSBML.CHILD_ELEMENT_NAMES);

      if (childElementNames == null || childElementNames.size() == 0) {
        logger.warn(elementName + "' is missing.");
        return false;
      }

      int elementCount = 0;

      for (String currentElementName : childElementNames) {

        if (currentElementName.equals(elementName)) {
          elementCount++;
        }
      }

      if (elementCount > 1) {
        logger.warn(elementName + "' was encountered " + elementCount + " times.");
        return false;
      } else if (elementCount == 0) {
        logger.warn(elementName + "' is missing.");
        return false;
      }

    }
    else 
    {
      logger.warn(elementName + "' is missing.");
      return false;    
    }

    return true;
  } 
}
