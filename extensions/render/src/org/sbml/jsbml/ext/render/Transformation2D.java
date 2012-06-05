/*
 * $Id$
 * $URL$
 *
 * ---------------------------------------------------------------------------- 
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML> 
 * for the latest version of JSBML and more information about SBML. 
 * 
 * Copyright (C) 2009-2012 jointly by the following organizations: 
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
package org.sbml.jsbml.ext.render;

import java.util.Map;

import org.sbml.jsbml.PropertyUndefinedError;


/**
 * @author Eugen Netz
 * @author Alexander Diamantikos
 * @author Jakob Matthes
 * @author Jan Rudolph
 * @version $Rev$
 * @since 1.0
 * @date 08.05.2012
 */
public class Transformation2D extends Transformation {
	/**
   * 
   */
  private static final long serialVersionUID = -1737694519381619398L;


  protected Double[] transform = new Double[6];

  /**
   * Creates an Transformation2D instance 
   */
  public Transformation2D() {
    super();
    initDefaults();
  }
  
  /**
   * Clone constructor
   */
  public Transformation2D(Transformation2D obj) {
    super(obj);
    transform = obj.transform;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.ext.render.Transformation#clone()
   */
  @Override
  public Transformation2D clone() {
    return new Transformation2D(this);
  }

  /**
   * @return the value of transform
   */
  public Double[] getTransform() {
    if (isSetTransform()) {
      return transform;
    }
    // This is necessary if we cannot return null here.
    throw new PropertyUndefinedError(RenderConstants.transform, this);
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.ext.render.Transformation#initDefaults()
   */
  @Override
  public void initDefaults() {
    addNamespace(RenderConstants.namespaceURI);
  }

  /**
   * @return whether transform is set 
   */
  public boolean isSetTransform() {
    return this.transform != null;
  }

  /**
   * Set the value of transform
   */
  public void setTransform(Double[] transform) {
    Double[] oldTransform = this.transform;
    this.transform = transform;
    firePropertyChange(RenderConstants.transform, oldTransform, this.transform);
  }

  /**
   * Unsets the variable transform 
   * @return <code>true</code>, if transform was set before, 
   *         otherwise <code>false</code>
   */
  public boolean unsetTransform() {
    if (isSetTransform()) {
      Double[] oldTransform = this.transform;
      this.transform = null;
      firePropertyChange(RenderConstants.transform, oldTransform, this.transform);
      return true;
    }
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.AbstractSBase#writeXMLAttributes()
   */
  @Override
  public Map<String, String> writeXMLAttributes() {
    Map<String, String> attributes = super.writeXMLAttributes();
    if (isSetTransform()) {
    	
        attributes.remove(RenderConstants.transform);
        attributes.put(RenderConstants.shortLabel + ":" + RenderConstants.transform,
        	XMLTools.encodeArrayDoubleToString(transform)); 
    }
    return attributes;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.AbstractSBase#readAttribute(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public boolean readAttribute(String attributeName, String prefix, String value) {
    boolean isAttributeRead = super.readAttribute(attributeName, prefix, value);
    if (!isAttributeRead) {
      isAttributeRead = true;
      // TODO: catch Exception if Enum.valueOf fails, generate logger output
      if (attributeName.equals(RenderConstants.transform)) {
    	  setTransform(XMLTools.decodeStringToArrayDouble(value, 6));
      }
    }
    return isAttributeRead;
  }

}
