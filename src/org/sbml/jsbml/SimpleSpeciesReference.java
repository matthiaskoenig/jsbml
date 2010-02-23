/*
 * $Id$
 * $URL$
 *
 * 
 *==================================================================================
 * Copyright (c) 2009 The jsbml team.
 *
 * This file is part of jsbml, the pure java SBML library. Please visit
 * http://sbml.org for more information about SBML, and http://jsbml.sourceforge.net/
 * to get the latest version of jsbml.
 *
 * jsbml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jsbml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jsbml.  If not, see <http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>.
 *
 *===================================================================================
 *
 */

package org.sbml.jsbml;

import java.util.HashMap;

/**
 * The base class of {@link SpeciesReference} and
 * {@link ModifierSpeciesReference}.
 * 
 * @author Simon Sch&auml;fer
 * @author marine
 * @author Andreas Dr&auml;ger
 * 
 * @has 1 reference 1..* Species
 * @opt attributes
 * @opt types
 * @opt visibility
 */
public abstract class SimpleSpeciesReference extends Symbol {

	/**
	 * Represents the 'species' XML attribute.
	 */
	private String speciesID;

	/**
	 * Creates a SimpleSpeciesReference instance. By default, the speciesId is
	 * null.
	 */
	public SimpleSpeciesReference() {
		super();
		this.speciesID = null;

	}

	/**
	 * Creates a SimpleSpeciesReference instance from a given
	 * SimpleSpeciesReference.
	 * 
	 * @param ssr
	 */
	public SimpleSpeciesReference(SimpleSpeciesReference ssr) {
		super(ssr);
		if (ssr.isSetSpeciesInstance()) {
			this.speciesID = new String(ssr.getSpecies());
		} else {
			this.speciesID = null;
		}
	}

	/**
	 * Creates a SimpleSpeciesReference instance from a given Species.
	 * 
	 * @param ssr
	 */
	public SimpleSpeciesReference(Species s) {
		super(s);
		if (s.isSetId()) {
			this.speciesID = new String(s.getId());
		} else {
			this.speciesID = null;
		}
	}

	public SimpleSpeciesReference(int level, int version) {
		super(level, version);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.NamedSBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);

		// System.out.println("SimplespeciesReference : equals : super.equals = "
		// + equal);

		if (o.getClass().getName().equals(getClass().getName())) {
			SimpleSpeciesReference ssr = (SimpleSpeciesReference) o;

			if ((!isSetSpecies() && ssr.isSetSpecies())
					|| (isSetSpecies() && !ssr.isSetSpecies())) {
				return false;

			} else if (isSetSpecies() && ssr.isSetSpecies()) {
				equal &= ssr.getSpecies().equals(speciesID);
				// System.out.println("SimplespeciesReference : speciesRef =  "
				// + ssr.getSpecies() + ", " + speciesID);
			}

		} else {
			equal = false;
		}

		return equal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.SBase#getParentSBMLObject()
	 */
	// @Override
	public Reaction getParentSBMLObject() {
		return (Reaction) super.getParentSBMLObject();
	}

	/**
	 * 
	 * @return the speciesID of this Species. The empty String if it is not set.
	 */
	public String getSpecies() {
		return isSetSpecies() ? speciesID : "";
	}

	/**
	 * 
	 * @return The Species instance which has the speciesID of this
	 *         SimpleSpeciesReference as id. Can be null if it doesn't exist.
	 */
	public Species getSpeciesInstance() {
		Model m = getModel();
		return m != null ? m.getSpecies(this.speciesID) : null;
	}

	/**
	 * 
	 * @return true if the Species instance which has the speciesID of this
	 *         SimpleSpeciesReference as id is not null.
	 */
	public boolean isSetSpeciesInstance() {
		Model m = getModel();
		return m != null ? m.getSpecies(this.speciesID) != null : false;
	}

	/**
	 * 
	 * @return true if ths speciesID of this SimpleSpeciesReference is not null.
	 */
	public boolean isSetSpecies() {
		return speciesID != null;
	}

	/**
	 * Sets the speciesID to the id of the Species 'species'.
	 * 
	 * @param spec
	 */
	public void setSpecies(Species spec) {
		this.speciesID = spec != null ? spec.getId() : null;
		stateChanged();
	}

	/**
	 * Sets the speciesID to 'spec'.
	 * 
	 * @param spec
	 */
	public void setSpecies(String spec) {
		if (spec != null && spec.trim().length() == 0) {
			speciesID = null;
		} else {
			speciesID = spec;
		}

		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.NamedSBase#toString()
	 */
	public String toString() {
		if (isSetName() && getName().length() > 0)
			return getName();
		if (isSetId())
			return getId();
		if (isSetSpeciesInstance()) {
			return getSpeciesInstance().toString();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.SBase#readAttribute(String attributeName,
	 * String prefix, String value)
	 */
	@Override
	public boolean readAttribute(String attributeName, String prefix,
			String value) {
		boolean isAttributeRead = super.readAttribute(attributeName, prefix,
				value);

		if (!isAttributeRead) {
			if (attributeName.equals("species")
					&& ((getLevel() == 1 && getVersion() == 2) || getLevel() > 1)) {
				this.setSpecies(value);
			} else if (attributeName.equals("specie") && getLevel() == 1
					&& getVersion() == 1) {
				this.setSpecies(value);
			}
		}
		return isAttributeRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.SBase#writeXMLAttributes()
	 */
	@Override
	public HashMap<String, String> writeXMLAttributes() {
		HashMap<String, String> attributes = super.writeXMLAttributes();

		if (isSetSpecies()) {
			if ((getLevel() == 1 && getVersion() == 2) || getLevel() > 1) {
				attributes.put("species", getSpecies());
			} else if (getLevel() == 1 && getVersion() == 1) {
				attributes.put("specie", getSpecies());
			}
		}

		return attributes;
	}
}
