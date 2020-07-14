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
package org.sbml.jsbml.ext.comp.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.ModelDefinition;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.Submodel;

/**
 * The {@link CompFlatteningConverter} object translates a hierarchical model defined with the SBML Level 3
 * Hierarchical Model Composition package into a 'flattened' version of the same model. This means the the hierarchical
 * structure is dissolved and all objects are built into a single model that does no longer require the comp package.
 *
 * @author Christoph Blessing
 * @since 1.0
 */
public class CompFlatteningConverter {

    private final static Logger LOGGER = Logger.getLogger(CompFlatteningConverter.class.getName());

    private List<String> previousModelIDs;
    private List<String> previousModelMetaIDs;
    private ListOf<ModelDefinition> modelDefinitionListOf;
    private ListOf<ExternalModelDefinition> externalModelDefinitionListOf;

    private List<Submodel> listOfSubmodelsToFlatten;

    private Model flattenedModel;

    public CompFlatteningConverter() {
        init();
    }

    /**
     * Initialize instance variables for flattening.
     * One CompFlatteningConverter could be used to flatten multiple
     * models, so at the beginning of the flattening the instance
     * variables must be reset.
     */
    private void init() {
        this.flattenedModel = new Model();

        this.modelDefinitionListOf = new ListOf<>();
        this.externalModelDefinitionListOf = new ListOf<>();

        this.listOfSubmodelsToFlatten = new ArrayList<>();

        this.previousModelIDs = new ArrayList<>();
        this.previousModelMetaIDs = new ArrayList<>();
    }

    /**
     * Public method to call on a CompflatteningConverter object.
     * Takes an SBML Document and flattens the models of the comp plugin.
     * Returns the SBML Document with a flattened model.
     *
     * @param document SBMLDocument to flatten
     * @return SBMLDocument with flattened model
     */
    public SBMLDocument flatten(SBMLDocument document) {
        init();

        SBMLDocument flatDocument = document.clone(); // no side-effects intended, flattening should not change original document
        if (flatDocument.isPackageEnabled(CompConstants.shortLabel)) {

            CompSBMLDocumentPlugin compSBMLDocumentPlugin = (CompSBMLDocumentPlugin) flatDocument.getExtension(CompConstants.shortLabel);

            this.modelDefinitionListOf = compSBMLDocumentPlugin.getListOfModelDefinitions();
            this.externalModelDefinitionListOf = compSBMLDocumentPlugin.getListOfExternalModelDefinitions();

            if (flatDocument.isSetModel()){
                CompModelPlugin compModelPlugin = (CompModelPlugin) flatDocument.getModel().getExtension(CompConstants.shortLabel);
                if (compModelPlugin != null){

                    // TODO: the model itself has to be flattened (can hold a list of replacements etc.)
                    handlePorts(compModelPlugin, compModelPlugin.getListOfPorts());
                    replaceElementsInModelDefinition(compModelPlugin, null); //TODO: why is null given here?
                    instantiateSubModels(compModelPlugin);

                    // remove comp extension from model
                    this.flattenedModel.unsetExtension(CompConstants.shortLabel);
                    this.flattenedModel.unsetPlugin(CompConstants.shortLabel);

                    flatDocument.setModel(this.flattenedModel);
                } else {
                    LOGGER.warning("No comp package set on Model. Cannot flatten.");
                }
            }

            // remove comp extension from document
            flatDocument.unsetExtension(CompConstants.shortLabel);
            flatDocument.disablePackage(CompConstants.shortLabel);
        } else {
            LOGGER.warning("No comp package set on SBMLDocument. Cannot flatten.");
        }
        return flatDocument;
    }


    /**
     * Initiates every Submodel in the CompModelPlugin recursively
     *
     * @param compModelPlugin
     */
    private void instantiateSubModels(CompModelPlugin compModelPlugin) {

        // TODO: the first model is not always flat.

        Model model = compModelPlugin.getExtendedSBase().getModel();

        handlePorts(compModelPlugin, compModelPlugin.getListOfPorts());
        replaceElementsInModelDefinition(compModelPlugin, null);
        this.flattenedModel = mergeModels(flattenModel(model), this.flattenedModel);

        if (compModelPlugin.getSubmodelCount() > 0) {
            // check if submodel has submodel
            this.flattenedModel = initSubModels(compModelPlugin);
        } else {
            LOGGER.info("No more submodels");
        }
    }

    /**
     * Actualizes replacement provided by comp extension: The "replaced elements" referenced by {@link ReplacedElement} 
     * instances are here actually removed, along with their respective {@link Port}, and thus replaced by the holder 
     * of the {@link ReplacedElement} 
     * @param compModelPlugin plugin holding the {@link ReplacedElement}s, may be null -- not used in that case
     * @param compSBasePlugin plugin holding {@link ReplacedElement}s, may be null - not used in that case
     */
    private void replaceElementsInModelDefinition(CompModelPlugin compModelPlugin, CompSBasePlugin compSBasePlugin) {

        if (compModelPlugin != null || compSBasePlugin != null) {

            ListOf<ReplacedElement> listOfReplacedElements = new ListOf<>();

            if (compModelPlugin != null) {
                listOfReplacedElements = compModelPlugin.getListOfReplacedElements();
            } else if (compSBasePlugin != null) {
                listOfReplacedElements = compSBasePlugin.getListOfReplacedElements();
            }

            for (ReplacedElement replacedElement : listOfReplacedElements) {

                for (ModelDefinition modelDefinition : this.modelDefinitionListOf) {
                    SBase sBase = modelDefinition.findNamedSBase(replacedElement.getIdRef());
                    if (sBase != null) {
                        sBase.removeFromParent();
                    }
                }

                if (compModelPlugin != null) {
                    for(Port port : compModelPlugin.getListOfPorts()){
                        if(port.getId().equals(replacedElement.getPortRef())){
                            port.removeFromParent();
                        }

                    }
                }
            }

        }
    }

    /**
     * Handle the ports on a model.
     * @param compModelPlugin
     * @param listOfPorts
     */
    private void handlePorts(CompModelPlugin compModelPlugin, ListOf<Port> listOfPorts){

        for (Port port : listOfPorts){
            // Port object instance defines a port for a component in a model.

            // A port could be created by using the metaIdRef attribute
            // to identify the object for which a given Port instance is the port;
            // The question 'what does this port correspond to?' would be answered by the value of the metaIdRef attribute.

            String idRef = port.getIdRef();
            String metaIDRef = port.getMetaIdRef();

            if(metaIDRef != null && !metaIDRef.isEmpty()){
                SBase parentOfPort = compModelPlugin.getParent();

                SBase sBase = compModelPlugin.getSBMLDocument().findSBase(idRef);
                addSBaseToModel(parentOfPort.getModel(), sBase);

            } else if(idRef != null && !idRef.isEmpty()){
                SBase parentOfPort = compModelPlugin.getParent();

                for (ModelDefinition modelDefinition : this.modelDefinitionListOf) {
                    SBase sBase = modelDefinition.findNamedSBase(idRef);

                    if(sBase != null){
                        addSBaseToModel(parentOfPort.getModel(), sBase);
                        break;
                    }
                }
            }
            // If a port references an object from a namespace that is not understood by the interpreter,
            // the interpreter must consider the port to be not understood as well.
            // If an interpreter cannot tell whether the referenced object does not
            // exist or if exists in an unparsed XML or SBML namespace, it may choose to display a warning to the user.

        }

        listOfPorts.removeFromParent();
    }

    private Model initSubModels(CompModelPlugin compModelPlugin) {

        ListOf<Submodel> subModelListOf = compModelPlugin.getListOfSubmodels().clone();

        // TODO: replace elements
        replaceElementsInModelDefinition(compModelPlugin, null);

        // TODO: ports
        ListOf<Port> listOfPorts = compModelPlugin.getListOfPorts();
        handlePorts(compModelPlugin, listOfPorts);

        for (Submodel submodel : subModelListOf) {

            this.listOfSubmodelsToFlatten.add(submodel);

            ModelDefinition modelDefinition = this.modelDefinitionListOf.get(submodel.getModelRef());

            // TODO: how to initialize external model definitions?
            if (modelDefinition == null) {
                ExternalModelDefinition externalModelDefinition = this.externalModelDefinitionListOf.get(submodel.getModelRef());
                try {
                    SBMLDocument externalDocument = SBMLReader.read(new File(externalModelDefinition.getSource()));
                    Model flattendExternalModel = flatten(externalDocument).getModel(); //external model can also contain submodels etc.
                    this.flattenedModel = mergeModels(this.flattenedModel, flattendExternalModel);
                } catch (XMLStreamException | IOException e) {
                    e.printStackTrace();
                }
            }

            if (modelDefinition != null && modelDefinition.getExtension(CompConstants.shortLabel) != null) {
                initSubModels((CompModelPlugin) modelDefinition.getExtension(CompConstants.shortLabel));
            } else {
                LOGGER.info("No model definition found in " + submodel.getId() + ".") ;
            }

        }

        return flattenAndMergeModels(this.listOfSubmodelsToFlatten);
    }

    private Model flattenAndMergeModels(List<Submodel> listOfSubmodelsToFlatten) {

        int sizeOfList = listOfSubmodelsToFlatten.size();

        if (sizeOfList >= 2) {
            Submodel last = listOfSubmodelsToFlatten.get(sizeOfList - 1);
            Submodel secondLast = listOfSubmodelsToFlatten.get(sizeOfList - 2);

            this.flattenedModel = mergeModels(this.flattenedModel, mergeModels(flattenSubModel(secondLast), flattenSubModel(last)));
            listOfSubmodelsToFlatten.remove(secondLast);
            listOfSubmodelsToFlatten.remove(last);

            flattenAndMergeModels(listOfSubmodelsToFlatten);

        } else if (sizeOfList == 1) {
            Submodel last = listOfSubmodelsToFlatten.get(sizeOfList - 1);

            this.flattenedModel = mergeModels(this.flattenedModel, flattenSubModel(last));
        }

        return this.flattenedModel;
    }

    /**
     * All remaining elements are placed in a single Model object
     * The original Model, ModelDefinition, and ExternalModelDefinition objects are all deleted
     *
     * @param model1
     * @param model2
     * @return mergedModel
     */
    private Model mergeModels(Model model1, Model model2) {

        Model mergedModel = new Model();

        // match versions and level
        if (model1 != null) {
            mergedModel.setLevel(model1.getLevel());
            mergedModel.setVersion(model1.getVersion());
        } else if (model2 != null) {
            mergedModel.setLevel(model2.getLevel());
            mergedModel.setVersion(model2.getVersion());
        }

        for (Model sourceModel : Arrays.asList(model1, model2)){
            if (sourceModel != null) {
                mergeListsOfInModels(sourceModel, mergedModel);
            }
        }
        // TODO: delete original model, ModelDefinition, and ExternalModelDefinition objects
        // QUESTION: can a model def be instantiated more than one time?
        return mergedModel;
    }


    /**
     *
     * Merging of SBML models should be done in the order
     *     Compartments -> Species -> Function Definitions -> Rules -> Events -> Units -> Reactions -> Parameters
     *
     *  If done in this order, potential conflicts are resolved incrementally along the way.
     *
     * @param sourceModel
     * @param targetModel
     */
    private void mergeListsOfInModels(Model sourceModel, Model targetModel) {

        // Compartments
        ListOf<Compartment> compartmentListOf = sourceModel.getListOfCompartments().clone();
        sourceModel.getListOfCompartments().removeFromParent();

        for (Compartment compartment : compartmentListOf) {
            targetModel.addCompartment(compartment.clone());
        }

        // Species
        ListOf<Species> speciesListOf = sourceModel.getListOfSpecies().clone();
        sourceModel.getListOfSpecies().removeFromParent();

        for (Species species : speciesListOf) {
            targetModel.addSpecies(species.clone());
        }

        // FunctionDefinitions
        ListOf<FunctionDefinition> functionalDefinitionsListOf = sourceModel.getListOfFunctionDefinitions().clone();
        sourceModel.getListOfFunctionDefinitions().removeFromParent();

        for (FunctionDefinition functionalDefinition : functionalDefinitionsListOf) {
            targetModel.addFunctionDefinition(functionalDefinition.clone());
        }

        // Rules
        ListOf<Rule> ruleListOf = sourceModel.getListOfRules().clone();
        sourceModel.getListOfRules().removeFromParent();

        for (Rule rule : ruleListOf) {
            targetModel.addRule(rule.clone());
        }

        // Events
        ListOf<Event> eventListOf = sourceModel.getListOfEvents().clone();
        sourceModel.getListOfEvents().removeFromParent();

        for (Event event : eventListOf) {
            targetModel.addEvent(event.clone());
        }

        // Units
        ListOf<UnitDefinition> unitDefinitionListOf = sourceModel.getListOfUnitDefinitions().clone();
        sourceModel.getListOfUnitDefinitions().removeFromParent();

        for (UnitDefinition unit : unitDefinitionListOf) {
            targetModel.addUnitDefinition(unit.clone());
        }

        // Reactions
        ListOf<Reaction> reactionListOf = sourceModel.getListOfReactions().clone();
        sourceModel.getListOfReactions().removeFromParent();

        for (Reaction reaction : reactionListOf) {
            targetModel.addReaction(reaction.clone());
        }

        // Parameters
        ListOf<Parameter> parameterListOf = sourceModel.getListOfParameters().clone();
        sourceModel.getListOfParameters().removeFromParent();

        for (Parameter parameter : parameterListOf) {
            if (parameter.isSetPlugin(CompConstants.shortLabel)){
                replaceElementsInModelDefinition(null, (CompSBasePlugin) parameter.getExtension(CompConstants.shortLabel));
                parameter.unsetPlugin(CompConstants.shortLabel);
            }
            targetModel.addParameter(parameter.clone());
        }

        // InitialAssignments
        ListOf<InitialAssignment> initialAssignmentListOf = sourceModel.getListOfInitialAssignments().clone();
        sourceModel.getListOfInitialAssignments().removeFromParent();

        for (InitialAssignment initialAssignment : initialAssignmentListOf) {
            targetModel.addInitialAssignment(initialAssignment.clone());
        }

        // Constraints
        ListOf<Constraint> constraintListOf = sourceModel.getListOfConstraints().clone();
        sourceModel.getListOfConstraints().removeFromParent();

        for (Constraint constraint : constraintListOf) {
            targetModel.addConstraint(constraint.clone());
        }

        // TODO: Following information not handled
        // no longer supported? there are no get methods for this
        // ListOf.Type.listOfCompartmentTypes
        // ListOf.Type.listOfEventAssignments
        // ListOf.Type.listOfLocalParameters: there is no getter method :(
        // ListOf.Type.listOfModifiers
        // ListOf.Type.listOfSpeciesTypes
        // ListOf.Type.listOfUnits

        // maybe they are already in listOfReactions?
        // ListOf.Type.listOfProducts
        // ListOf.Type.listOfReactants
    }

    /**
     * Performs the routine to flatten a submodel into a model
     *
     * @param subModel
     * @return
     */
    private Model flattenSubModel(Submodel subModel) {

        Model model = new Model();

        // 2
        // Let 'M' be the identifier of a given submodel.
        String subModelID = subModel.getId() + "_";
        String subModelMetaID = subModel.getMetaId() + "_";

        // Verify that no object identifier or meta identifier of objects in that submodel
        // (i.e., the id or metaid attribute values)
        // begin with the character sequence "M__";

        // if there is an existing identifier or meta identifier beginning with "M__",
        // add an underscore to "M__" (i.e., to produce "M___") and again check that the sequence is unique.
        // Continue adding underscores until you find a unique prefix. Let "P" stand for this final prefix.

        while (this.previousModelIDs.contains(subModelID)) {
            subModelID += "_";
        }

        while (this.previousModelMetaIDs.contains(subModelMetaID)) {
            subModelMetaID += "_";
        }

        if(!this.previousModelIDs.isEmpty()){ // because libSBML does it
            subModelID = this.previousModelIDs.get(this.previousModelIDs.size()-1) + subModelID;
        }

        this.previousModelIDs.add(subModelID);
        this.previousModelMetaIDs.add(subModelID);

        //subModel.setId(subModelID);
        //subModel.setMetaId(subModelMetaID);

        if (subModel.getModelRef() != null) {

            // initiate a clone of the referenced model
            Model modelOfSubmodel = this.modelDefinitionListOf.get(subModel.getModelRef()).clone();

            // 3
            // Remove all objects that have been replaced or deleted in the submodel.

            // TODO Delete Objects
            // each Deletion object identifies an object to "remove" from that Model instance
            for (Deletion deletion : subModel.getListOfDeletions()) {

                // TODO: search for element to remove in all model definitions?
                this.modelDefinitionListOf.remove(deletion.getMetaIdRef());
                subModel.removeDeletion(deletion);

                //deletion.removeFromParent();
                //for (ModelDefinition modelDefinition : this.modelDefinitionListOf){
                //    modelDefinition.remove(deletion.getMetaIdRef());
                //}
                // modelOfSubmodel.remove(deletion.getMetaIdRef());

            }
            subModel.getListOfDeletions().removeFromParent();

            // TODO: Replace Objects

            // 4
            // Transform the remaining objects in the submodel as follows:
            // a)
            // Change every identifier (id attribute)
            // to a new value obtained by prepending "P" to the original identifier.
            // b)
            // Change every meta identifier (metaid attribute)
            // to a new value obtained by prepending "P" to the original identifier.

            model = flattenModel(modelOfSubmodel);

            // 5
            // Transform every SIdRef and IDREF type value in the remaining objects of the submodel as follows:
            // a)
            // If the referenced object has been replaced by the application of a ReplacedBy or ReplacedElement construct,
            // change the SIdRef value (respectively, IDREF value) to the SId value (respectively, ID value)
            // of the object replacing it.
            // b)
            // If the referenced object has not been replaced, change the SIdRef and IDREF value by prepending "P"
            // to the original value.

            // 6
            // After performing the tasks above for all remaining objects, merge the objects of the remaining submodels
            // into a single model.
            // Merge the various lists (list of species, list of compartments, etc.)
            // in this step, and preserve notes and annotations as well as constructs from other SBML Level 3 packages.

            //model = modelOfSubmodel;
            //model = mergeModels(this.previousModel, modelOfSubmodel); // initiate model (?)

        }

        return model;
    }

    private Model flattenModel(Model modelOfSubmodel) {

        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfReactions());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfCompartments());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfConstraints());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfEvents());

        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfFunctionDefinitions());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfParameters());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfRules());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfSpecies());
        flattenSBaseList(modelOfSubmodel, modelOfSubmodel.getListOfUnitDefinitions());

        return modelOfSubmodel;
    }

    /**
     * Flatten ListOf<SBase>
     * @param modelOfSubmodel
     * @param listOfSBase
     */
    private void flattenSBaseList(Model modelOfSubmodel, ListOf listOfSBase){

        ListOf<SBase> list = (ListOf<SBase>) listOfSBase;

        for (SBase sBase : list){

            if (!sBase.getId().equals("")) {
                sBase.setId(modelOfSubmodel.getId() + sBase.getId());
            }

            if (!sBase.getMetaId().equals("")) {
                sBase.setMetaId(modelOfSubmodel.getId() + sBase.getMetaId());
            }

            if (sBase.isPackageEnabled(CompConstants.shortLabel)){
                CompSBasePlugin compSBasePlugin = (CompSBasePlugin) sBase.getExtension(CompConstants.shortLabel);
                CompModelPlugin compModelPlugin = (CompModelPlugin) modelOfSubmodel.getExtension(CompConstants.shortLabel);

                replaceElementsInModelDefinition(compModelPlugin, compSBasePlugin);
            }
            //sBase.unsetPlugin(CompConstants.shortLabel);
        }

    }

    /**
     * Adds SBase to given model.
     * Removes it from the parent.
     * @param model
     * @param sBase
     */
    private void addSBaseToModel(Model model, SBase sBase) {

        if (model != null && sBase != null) {
            sBase.removeFromParent();

            Class cls = sBase.getClass();
            if (cls == Reaction.class) {
                model.addReaction((Reaction) sBase);
            } else if (cls == Compartment.class) {
                model.addCompartment((Compartment) sBase);
            } else if (cls == Constraint.class) {
                model.addConstraint((Constraint) sBase);
            } else if (cls == Event.class) {
                model.addEvent((Event) sBase);
            } else if (cls == FunctionDefinition.class) {
                model.addFunctionDefinition((FunctionDefinition) sBase);
            } else if (cls == Parameter.class) {
                model.addParameter((Parameter) sBase);
            } else if (cls == Rule.class) {
                model.addRule((Rule) sBase);
            } else if (cls == Species.class) {
                model.addSpecies((Species) sBase);
            } else if (cls == UnitDefinition.class) {
                model.addUnitDefinition((UnitDefinition) sBase);
            }
        }
    }
    
    
  /**
   * Collects any {@link ExternalModelDefinition}s that might be contained in
   * the given {@link SBMLDocument} and transfers them into local
   * {@link ModelDefinition}s (recursively, if the external models themselves
   * include external models; in that case, renaming may occur).
   * <br>
   * The given {@link SBMLDocument} need have its locationURI set!
   * <br>
   * Opaque URIs (URNs) will not be dealt with in any defined way, resolve them
   * first (make sure all relevant externalModelDefinitions' source-attributes
   * are URLs or relative paths)
   * 
   * @param document
   *        an {@link SBMLDocument}, which might, but need not, contain
   *        {@link ExternalModelDefinition}s to be transferred into its local
   *        {@link ModelDefinition}s. The locationURI of the given document need
   *        be set ({@link SBMLDocument#isSetLocationURI})!
   * @return a new {@link SBMLDocument} without {@link
   *         ExternalModelDefinition}s, but containing the same information as
   *         the given one
   * @throws Exception
   *         if given document's locationURI is not set. Set it with
   *         {@link SBMLDocument#setLocationURI}
   */
  public static SBMLDocument internaliseExternalModelDefinitions(
    SBMLDocument document) throws Exception {

    if (!document.isSetLocationURI()) {
      LOGGER.warning("Location URI is not set: " + document);
      throw new Exception(
        "document's locationURI need be set. But it was not.");
    }
    SBMLDocument result = document.clone(); // no side-effects intended
    ArrayList<String> usedIds = new ArrayList<String>();
    if (result.isSetModel()) {
      usedIds.add(result.getModel().getId());
    }
    
    CompSBMLDocumentPlugin compSBMLDocumentPlugin =
      (CompSBMLDocumentPlugin) result.getExtension(CompConstants.shortLabel);
    
    // There is nothing to retrieve:
    if (compSBMLDocumentPlugin == null || !compSBMLDocumentPlugin.isSetListOfExternalModelDefinitions()) {
      return result;
    } else {
      /** For name-collision-avoidance */
      for (ExternalModelDefinition emd : compSBMLDocumentPlugin.getListOfExternalModelDefinitions()) {
        usedIds.add(emd.getId());
      }
      
      for (ExternalModelDefinition emd : compSBMLDocumentPlugin.getListOfExternalModelDefinitions()) {
        // general note: Be careful when using clone/cloning-constructors, they
        // do not preserve parent-child-relations
        Model referenced = emd.getReferencedModel();
        SBMLDocument referencedDocument = referenced.getSBMLDocument();  
        SBMLDocument flattened = internaliseExternalModelDefinitions(referencedDocument);
        // Guarantee: flattened does not contain any externalModelDefinitions, only local MDs 
        // (and main model)
        // use this, and migrate the MDs into the current compSBMLDocumentPlugin
        StringBuilder prefixBuilder = new StringBuilder(emd.getModelRef());
        /** For name-collision-avoidance */
        boolean contained = false;
        do {
          contained = false;
          prefixBuilder.append("_");
          for (String id : usedIds) {
            contained |= id.startsWith(prefixBuilder.toString());
            if(contained) {
              break;
            }
          }
        } while (contained);
        String newPrefix = prefixBuilder.toString();
        
        CompSBMLDocumentPlugin referencedDocumentPlugin =
          (CompSBMLDocumentPlugin) flattened.getExtension(
            CompConstants.shortLabel);
        
        ListOf<ModelDefinition> workingList;
        if(referencedDocumentPlugin == null) {
          // This may happen, if the main model of a non-comp-file is referenced
          workingList = new ListOf<ModelDefinition>();
          workingList.setLevel(referenced.getLevel());
          workingList.setVersion(referenced.getVersion());
        } else {
          workingList = referencedDocumentPlugin.getListOfModelDefinitions().clone();
        }
        
        // Check whether the main model is needed; Do not internalise it, if not necessary
        boolean isMainReferenced = flattened.getModel().getId().equals(emd.getModelRef());
        for ( ModelDefinition md : workingList) {
          if(isMainReferenced) {
            break;
          }
          CompModelPlugin cmp = (CompModelPlugin) md.getExtension(CompConstants.shortLabel);
          if (cmp != null) {
            for (Submodel sm : cmp.getListOfSubmodels()) {
              isMainReferenced |= flattened.getModel().getId().equals(sm.getModelRef());
            }
          }
        }
        
        if(isMainReferenced) {
          ModelDefinition localisedMain = new ModelDefinition(flattened.getModel());
          workingList.add(0, localisedMain);
        }

        for (ModelDefinition md : workingList) {
          ModelDefinition internalised = new ModelDefinition(md);
          // i.e. current one is the one directly referenced => take referent's place
          if(md.getId().equals(referenced.getId())) {
            internalised.setId(emd.getId());
          } else {
            internalised.setId(newPrefix + internalised.getId());
          }
          
          CompModelPlugin notYetInternalisedModelPlugin =
              (CompModelPlugin) internalised.getExtension(CompConstants.shortLabel);
          if(notYetInternalisedModelPlugin != null && notYetInternalisedModelPlugin.isSetListOfSubmodels()) {
            for (Submodel sm : notYetInternalisedModelPlugin.getListOfSubmodels()) {
              sm.setModelRef(newPrefix + sm.getModelRef());
            }
          }
          
          compSBMLDocumentPlugin.addModelDefinition(internalised);
        }
      }
      compSBMLDocumentPlugin.unsetListOfExternalModelDefinitions();
      return result;
    }
  }
}
