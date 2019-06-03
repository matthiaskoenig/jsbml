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

package org.sbml.jsbml.validator.offline.constraints;

import java.util.Set;

import org.sbml.jsbml.ext.spatial.SpatialConstants;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;
import org.sbml.jsbml.validator.offline.ValidationContext;
import org.sbml.jsbml.validator.offline.constraints.helper.DuplicatedElementValidationFunction;
import org.sbml.jsbml.validator.offline.constraints.helper.UnknownPackageElementValidationFunction;

/**
 * Defines validation rules (as {@link ValidationFunction} instances) for the {@link SpatialParameterPlugin} class.
 * 
 * @author Bhavye Jain
 * @since 1.5
 */
public class SpatialParameterPluginConstraints extends AbstractConstraintDeclaration {
	
	  /* (non-Javadoc)
	   * @see org.sbml.jsbml.validator.offline.constraints.ConstraintDeclaration#addErrorCodesForAttribute(java.util.Set, int, int, java.lang.String)
	   */
	  @Override
	  public void addErrorCodesForAttribute(Set<Integer> set, int level,
	    int version, String attributeName, ValidationContext context) 
	  {
	    // no attribute error code

	  }
	  
	  /* (non-Javadoc)
	   * @see org.sbml.jsbml.validator.offline.constraints.ConstraintDeclaration#addErrorCodesForCheck(java.util.Set, int, int, org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY)
	   */
	  @Override
	  public void addErrorCodesForCheck(Set<Integer> set, int level, int version,
	    CHECK_CATEGORY category, ValidationContext context) {
		  switch (category) {
		    case GENERAL_CONSISTENCY:
		    	if(level >= 3) {
		    		set.add(SPATIAL_20501);
		    	}
		      break;
		    case IDENTIFIER_CONSISTENCY:
		      break;
		    case MATHML_CONSISTENCY:
		      break;
		    case MODELING_PRACTICE:
		      break;
		    case OVERDETERMINED_MODEL:
		      break;
		    case SBO_CONSISTENCY:
		      break;
		    case UNITS_CONSISTENCY:
		      break;
		    }
	  }
	  
	  @Override
	  public ValidationFunction<?> getValidationFunction(int errorCode, ValidationContext context){
		  ValidationFunction<SpatialParameterPlugin> func = null;
		  
		  switch (errorCode) {
		  	case SPATIAL_20501:
		  	{
		  		// A Parameter object may contain one and only one instance of each of the  
		  		// SpatialSymbolReference, AdvectionCoefficient, BoundaryCondition and   
		  		// DiffusionCoefficient elements.No other elements from the SBML Level 3  
		  		// Spatial Processes namespaces are permitted on a Parameter object. 
		  		
		  		func = new ValidationFunction<SpatialParameterPlugin>() {
		  			@Override
		  			public boolean check(ValidationContext ctx, SpatialParameterPlugin spatialPM) {
		  				boolean onlyOneSSR = new DuplicatedElementValidationFunction<SpatialParameterPlugin>(SpatialConstants.spatialSymbolReference).check(ctx, spatialPM);
		  				boolean onlyOneAC = new DuplicatedElementValidationFunction<SpatialParameterPlugin>(SpatialConstants.advectionCoefficient).check(ctx, spatialPM);
		  				boolean onlyOneBC = new DuplicatedElementValidationFunction<SpatialParameterPlugin>(SpatialConstants.boundaryCondition).check(ctx, spatialPM);
		  				boolean onlyOneDC = new DuplicatedElementValidationFunction<SpatialParameterPlugin>(SpatialConstants.diffusionCoefficient).check(ctx, spatialPM);
		  				boolean noOtherElements = new UnknownPackageElementValidationFunction<SpatialParameterPlugin>(SpatialConstants.shortLabel).check(ctx, spatialPM);
		  				return (onlyOneSSR && onlyOneAC && onlyOneBC && onlyOneDC && noOtherElements);
		  			}
		  		};
		  		break;
		  	}
		  }
		  
		  return func;
	  }
	
}