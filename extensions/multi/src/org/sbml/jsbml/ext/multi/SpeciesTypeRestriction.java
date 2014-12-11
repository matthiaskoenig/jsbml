/*
 * $Id$
 * $URL$
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2014 jointly by the following organizations:
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
package org.sbml.jsbml.ext.multi;

import org.sbml.jsbml.AbstractNamedSBase;

/**
 * 
 * @author Nicolas Rodriguez
 * @version $Rev$
 * @since 1.0
 * @date 16.10.2013
 */
public class SpeciesTypeRestriction extends AbstractNamedSBase {

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -6703552149441215128L;
  /**
   * 
   */
  private String speciesTypeInstance;

  /**
   * @param speciesTypeRestriction
   */
  public SpeciesTypeRestriction(SpeciesTypeRestriction speciesTypeRestriction) {
    this();
    // TODO
  }

  /**
   * 
   */
  public SpeciesTypeRestriction() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Returns the speciesTypeInstance.
   * 
   * @return the speciesTypeInstance
   */
  public String getSpeciesTypeInstance() {
    return speciesTypeInstance;
  }

  /**
   * Sets the speciesTypeInstance.
   * 
   * @param speciesTypeInstance the speciesTypeInstance to set
   */
  public void setSpeciesTypeInstance(String speciesTypeInstance) {
    this.speciesTypeInstance = speciesTypeInstance;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.NamedSBase#isIdMandatory()
   */
  @Override
  public boolean isIdMandatory() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.AbstractSBase#clone()
   */
  @Override
  public SpeciesTypeRestriction clone() {
    return new SpeciesTypeRestriction(this);
  }

}
