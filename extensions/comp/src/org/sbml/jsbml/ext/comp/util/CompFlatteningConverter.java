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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
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
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.comp.*;

/**
 * The {@link CompFlatteningConverter} object translates a hierarchical model defined with the SBML Level 3
 * Hierarchical Model Composition package into a 'flattened' version of the same model. This means the the hierarchical
 * structure is dissolved and all objects are built into a single model that does no longer require the comp package.
 * <p>
 * The model flattening follows the proposed algorithm in the Hierarchical Model Composition Specification
 * <p>
 * 1. Examine all submodels of the model being validated. For any submodel that itself contains submodels,
 * perform this algorithm in its entirety on each of those inner submodels before proceeding to the next step.
 * 2. Let “M” be the identifier of a given submodel. Verify that no object identifier or meta identifier of
 * objects in that submodel (i.e., the id or metaid attribute values) begin with the character sequence “M__”;
 * if there is an existing identifier or meta identifier beginning with “M__”, add an underscore to “M__”
 * (i.e., to produce “M___”) and again check that the sequence is unique. Continue adding underscores until you
 * find a unique prefix. Let “P” stand for this final prefix.
 * 3. Remove all objects that have been replaced or deleted in the submodel.
 * 4. Transform the remaining objects in the submodel as follows:
 * a) Change every identifier (id attribute) to a new value obtained by prepending “P” to the original identifier.
 * b) Change every meta identifier (metaid attribute) to a new value obtained by prepending “P” to the original identifier.
 * 5. Transform every SIdRef and IDREF type value in the remaining objects of the submodel as follows:
 * a) If the referenced object has been replaced by the application of a ReplacedBy or ReplacedElement construct,
 * change the SIdRef value (respectively, IDREF value) to the SId value (respectively, ID value) of the object
 * replacing it.
 * b) If the referenced object has not been replaced, change the SIdRef and IDREF value by prepending “P”
 * to the original value.
 * 6. After performing the tasks above for all remaining objects, merge the objects of the remaining submodels
 * into a single model. Merge the various lists (list of species, list of compartments, etc.) in this step,
 * and preserve notes and annotations as well as constructs from other SBML Level 3 packages.
 *
 * Open issues:
 * - FIXME: conversionFactors are not handled (e.g. on ReplacedElement)
 * - FIXME: replacedBy not handled
 *
 * @author Christoph Blessing, Matthias König
 * @since 1.0
 */
public class CompFlatteningConverter {

    private final static Logger LOGGER = Logger.getLogger(CompFlatteningConverter.class.getName());

    private Model flatModel;

    private Set<String> submodelIdPrefixes;
    private Set<String> submodelMetaIdPrefixes;

    private ListOf<ModelDefinition> modelDefinitions;
    private List<Submodel> submodelsToFlatten;


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
        this.flatModel = new Model();

        this.submodelIdPrefixes = new HashSet<>();
        this.submodelMetaIdPrefixes = new HashSet<>();

        this.modelDefinitions = new ListOf<>();
        this.submodelsToFlatten = new ArrayList<>();
    }

    /**
     * Public method to call on a CompflatteningConverter object.
     * Takes an SBML Document and flattens the models of the comp plugin.
     * Returns the SBML Document with a flattened model.
     *
     * @param document SBMLDocument to flatten
     * @return SBMLDocument with flattened model
     */
    public SBMLDocument flatten(SBMLDocument document) throws XMLStreamException, IOException, URISyntaxException {
        init();

        SBMLDocument flatDocument = document.clone(); // no side-effects intended, flattening should not change original document
        flatDocument = internaliseExternalModelDefinitions(flatDocument);

        if (flatDocument.isPackageEnabled(CompConstants.shortLabel)) {

            CompSBMLDocumentPlugin compSBMLDocumentPlugin = (CompSBMLDocumentPlugin) flatDocument.getExtension(CompConstants.shortLabel);

            this.modelDefinitions = compSBMLDocumentPlugin.getListOfModelDefinitions();

            if (flatDocument.isSetModel()) {
                CompModelPlugin compModelPlugin = (CompModelPlugin) flatDocument.getModel().getExtension(CompConstants.shortLabel);
                if (compModelPlugin != null) {

                    // handle the top model
                    Model model = compModelPlugin.getExtendedSBase().getModel();
                    System.out.println("Flatten top model: " + compModelPlugin);

                    resolvePorts(compModelPlugin);

                    // no prefix for top model
                    this.flatModel = mergeModels(flattenModel(model, "", ""), this.flatModel);
                    System.out.println(this.flatModel);


                    // recursively handling of submodels
                    this.flatModel = flattenSubmodels(compModelPlugin);

                    // remove comp extension from model
                    this.flatModel.unsetExtension(CompConstants.shortLabel);
                    this.flatModel.unsetPlugin(CompConstants.shortLabel);

                    flatDocument.setModel(this.flatModel);
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
     * Recursive handling of submodels.
     * @param compModelPlugin
     * @return
     */
    private Model flattenSubmodels(CompModelPlugin compModelPlugin) {

        System.out.println("Flattening submodels for: " + compModelPlugin);

        resolvePorts(compModelPlugin);
        // replaceElementsInModelDefinition(compModelPlugin, null);

        ListOf<Submodel> subModelListOf = compModelPlugin.getListOfSubmodels().clone();

        if (subModelListOf.size() == 0) {
            LOGGER.info("No submodels for comp model:" + compModelPlugin);
        } else {
            for (Submodel submodel : subModelListOf) {
                System.out.println("Flatten submodel: " + submodel.getId());
                this.submodelsToFlatten.add(submodel);

                ModelDefinition modelDefinition = this.modelDefinitions.get(submodel.getModelRef());
                if (modelDefinition != null) {
                    if (modelDefinition.getExtension(CompConstants.shortLabel) != null) {
                        flattenSubmodels((CompModelPlugin) modelDefinition.getExtension(CompConstants.shortLabel));
                    }
                } else {
                    LOGGER.info("No modelDefinition found in " + submodel.getId() + ".");
                }
            }
        }

        // merge flattened submodels
        int size = this.submodelsToFlatten.size();
        while (size > 0) {
            Submodel lastSubmodel = this.submodelsToFlatten.get(size - 1);
            this.flatModel = mergeModels(this.flatModel, flattenSubmodel(lastSubmodel));
            this.submodelsToFlatten.remove(size - 1);
            size--;
        }

        return this.flatModel;
    }

    /**
     * Resolves the ports on a comp model.
     *
     * Ports are removed and the SBase referenced by the SBaseRef are added to the
     * model.
     *
     * @param compModelPlugin
     */
    private void resolvePorts(CompModelPlugin compModelPlugin) {

        ListOf<Port> listOfPorts = compModelPlugin.getListOfPorts();
        for (Port port : listOfPorts) {
            Model model = port.getModel();

            // Port object instance defines a port for a component in a model.
            SBase sBase = getSBaseFromSBaseRef(port, null);
            // The reference object is added to the model
            moveSBaseToModel(model, sBase);
        }

        // FIXME: necessary to resolve the links,
//        for (ModelDefinition modelDefinition : this.modelDefinitionListOf) {
//            SBase sBase = modelDefinition.findNamedSBase(idRef);
//
//            if(sBase != null){
//                addSBaseToModel(parentOfPort.getModel(), sBase);
//                break;
//            }
//        }

        listOfPorts.removeFromParent();
    }

    /**
     * Get SBase from SBaseRef.
     *
     * @param sBaseRef
     * @return
     */
    private SBase getSBaseFromSBaseRef(SBaseRef sBaseRef, Model model){
        SBase sBase = null;
        if (model == null) {
            // model is model of the sBaseRef
            model = sBaseRef.getParent().getModel();
        }
        if (sBaseRef.isSetMetaIdRef()){
            sBase = model.getElementByMetaId(sBaseRef.getMetaIdRef());
        } else if (sBaseRef.isSetIdRef()){
            sBase = model.getElementBySId(sBaseRef.getIdRef());
        } else if (sBaseRef.isSetUnitRef()){
            sBase = model.getUnitDefinition(sBaseRef.getUnitRef());
        } else if (sBaseRef.isSetPortRef()){
            String portRef = sBaseRef.getPortRef();
            CompModelPlugin compModelPlugin = (CompModelPlugin) model.getExtension(CompConstants.shortLabel);
            Port port = compModelPlugin.getPort(portRef);
            sBase = getSBaseFromSBaseRef(port, model);
        }


        if (sBase == null){
            LOGGER.warning("SBaseRef could not be resolved:" + sBaseRef);
        }
        // Not sure if necessary:
        // String unitRef = sBaseRef.getUnitRef();
        // SBaseRef sbaseRef = sBaseRef.getSBaseRef();
        return sBase;
    }

    /**
     * Adds SBase to given model.
     * Removes it from the parent.
     *
     * @param model
     * @param sBase
     */
    private void moveSBaseToModel(Model model, SBase sBase) {

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
     * Actualizes replacement provided by comp extension: The "replaced elements" referenced by {@link ReplacedElement}
     * instances are here actually removed, along with their respective {@link Port}, and thus replaced by the holder
     * of the {@link ReplacedElement}
     *
     * @param compSBasePlugin plugin holding the {@link ReplacedElement}s
     */
    private void deleteReplacedElements(CompSBasePlugin compSBasePlugin, CompModelPlugin compModelPlugin) {
        if (compSBasePlugin != null) {
            for (ReplacedElement replacedElement : compSBasePlugin.getListOfReplacedElements()) {

                String submodelRef = replacedElement.getSubmodelRef();
                Submodel submodel = compModelPlugin.getSubmodel(submodelRef);
                ModelDefinition modelDefinition = this.modelDefinitions.get(submodel.getModelRef());
                SBase sBase = getSBaseFromSBaseRef(replacedElement, modelDefinition);
                sBase.removeFromParent();
            }
        }

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

        for (Model sourceModel : Arrays.asList(model1, model2)) {
            if (sourceModel != null) {
                mergeListsOfInModels(sourceModel, mergedModel);
            }
        }
        // TODO: delete original model, ModelDefinition, and ExternalModelDefinition objects
        // QUESTION: can a model def be instantiated more than one time?
        return mergedModel;
    }


    /**
     * Merging of SBML models should be done in the order
     * Compartments -> Species -> Function Definitions -> Rules -> Events -> Units -> Reactions -> Parameters
     * <p>
     * If done in this order, potential conflicts are resolved incrementally along the way.
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
            if (parameter.isSetPlugin(CompConstants.shortLabel)) {
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
    private Model flattenSubmodel(Submodel subModel) {

        // 2.
        // Let 'M' be the identifier of a given submodel.
        //
        // Verify that no object identifier or meta identifier of objects in that submodel
        // (i.e., the id or metaid attribute values)
        // begin with the character sequence "M__";
        //
        // if there is an existing identifier or meta identifier beginning with "M__",
        // add an underscore to "M__" (i.e., to produce "M___") and again check that the sequence is unique.
        // Continue adding underscores until you find a unique prefix. Let "P" stand for this final prefix.

        String submodelIdPrefix = subModel.getId() + "__";
        String submodelMetaIdPrefix = subModel.getMetaId() + "__";

        while (this.submodelIdPrefixes.contains(submodelIdPrefix)) {
            submodelIdPrefix += "_";
        }
        this.submodelIdPrefixes.add(submodelIdPrefix);

        while (this.submodelMetaIdPrefixes.contains(submodelMetaIdPrefix)) {
            submodelMetaIdPrefix += "_";
        }
        this.submodelMetaIdPrefixes.add(submodelIdPrefix);


        //subModel.setId(subModelID);
        //subModel.setMetaId(subModelMetaID);

        // 3
        // Remove all objects that have been replaced or deleted in the submodel.

        // initiate a clone of the referenced model
        Model model = this.modelDefinitions.get(subModel.getModelRef()).clone();

        // Remove all objects that have been deleted in the submodel
        for (Deletion deletion : subModel.getListOfDeletions()) {
            SBase sbase = getSBaseFromSBaseRef(deletion, null);
            removeSBaseFromModel(sbase, model);
            subModel.removeDeletion(deletion);
        }
        subModel.getListOfDeletions().removeFromParent();

        // Remove all objects that have been replaced

        // 4
        // Transform the remaining objects in the submodel as follows:
        // a)
        // Change every identifier (id attribute)
        // to a new value obtained by prepending "P" to the original identifier.
        // b)
        // Change every meta identifier (metaid attribute)
        // to a new value obtained by prepending "P" to the original identifier.

        model = flattenModel(model, submodelIdPrefix, submodelMetaIdPrefix);

        // 5
        // Transform every SIdRef and IDREF type value in the remaining objects of the submodel as follows:
        // a)
        // If the referenced object has been replaced by the application of a ReplacedBy or ReplacedElement construct,
        // change the SIdRef value (respectively, IDREF value) to the SId value (respectively, ID value)
        // of the object replacing it.
        // b)
        // If the referenced object has not been replaced, change the SIdRef and IDREF value by prepending "P"
        // to the original value.

        // TODO: change all the remaining idrefs or SIdrefs

        // 6
        // After performing the tasks above for all remaining objects, merge the objects of the remaining submodels
        // into a single model.
        // Merge the various lists (list of species, list of compartments, etc.)
        // in this step, and preserve notes and annotations as well as constructs from other SBML Level 3 packages.


        return model;
    }

    private void removeSBaseFromModel(SBase sbase, Model model){
        Class cls = sbase.getClass();
        if (cls == Species.class){
            model.removeSpecies((Species) sbase);
        } else if (cls == Parameter.class){
            model.removeParameter((Parameter) sbase);
        } else if (cls == Reaction.class){
            model.removeReaction((Reaction) sbase);
        } else if (cls == Rule.class){
            model.removeRule((Rule) sbase);
        }
        // FIXME: complete list of possible deletions
    }

    private Model flattenModel(Model model, String submodelIdPrefix, String submodelMetaIdPrefix) {

        flattenSBaseList(model, model.getListOfReactions(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfCompartments(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfConstraints(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfEvents(), submodelIdPrefix, submodelMetaIdPrefix);

        flattenSBaseList(model, model.getListOfFunctionDefinitions(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfParameters(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfRules(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfSpecies(), submodelIdPrefix, submodelMetaIdPrefix);
        flattenSBaseList(model, model.getListOfUnitDefinitions(), submodelIdPrefix, submodelMetaIdPrefix);

        return model;
    }

    /**
     * Flatten ListOf<SBase>
     *
     * @param modelOfSubmodel
     * @param listOfSBase
     */
    private void flattenSBaseList(Model modelOfSubmodel, ListOf listOfSBase, String submodelIdPrefix, String submodelMetaIdPrefix) {

        ListOf<SBase> list = (ListOf<SBase>) listOfSBase;

        for (SBase sBase : list) {

            if (sBase.isSetId()) {
                sBase.setId(submodelIdPrefix + sBase.getId());
            }

            if (sBase.isSetMetaId()) {
                sBase.setMetaId(submodelIdPrefix + sBase.getMetaId());
            }

            if (sBase.isPackageEnabled(CompConstants.shortLabel)) {
                CompSBasePlugin compSBasePlugin = (CompSBasePlugin) sBase.getExtension(CompConstants.shortLabel);
                CompModelPlugin compModelPlugin = (CompModelPlugin) modelOfSubmodel.getExtension(CompConstants.shortLabel);

                // resolve replacedElements
                deleteReplacedElements(compSBasePlugin, compModelPlugin);
            }
            //sBase.unsetPlugin(CompConstants.shortLabel);
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
     * @param document an {@link SBMLDocument}, which might, but need not, contain
     *                 {@link ExternalModelDefinition}s to be transferred into its local
     *                 {@link ModelDefinition}s. The locationURI of the given document need
     *                 be set ({@link SBMLDocument#isSetLocationURI})!
     * @return a new {@link SBMLDocument} without {@link
     * ExternalModelDefinition}s, but containing the same information as
     * the given one
     * @throws Exception if given document's locationURI is not set. Set it with
     *                   {@link SBMLDocument#setLocationURI}
     */
    public static SBMLDocument internaliseExternalModelDefinitions(
            SBMLDocument document) throws IOException, XMLStreamException, URISyntaxException {

        if (!document.isSetLocationURI()) {
            LOGGER.warning("Location URI is not set: " + document);
            throw new IOException("document's locationURI need be set. But it was not.");
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
                        if (contained) {
                            break;
                        }
                    }
                } while (contained);
                String newPrefix = prefixBuilder.toString();

                CompSBMLDocumentPlugin referencedDocumentPlugin =
                        (CompSBMLDocumentPlugin) flattened.getExtension(
                                CompConstants.shortLabel);

                ListOf<ModelDefinition> workingList;
                if (referencedDocumentPlugin == null) {
                    // This may happen, if the main model of a non-comp-file is referenced
                    workingList = new ListOf<ModelDefinition>();
                    workingList.setLevel(referenced.getLevel());
                    workingList.setVersion(referenced.getVersion());
                } else {
                    workingList = referencedDocumentPlugin.getListOfModelDefinitions().clone();
                }

                // Check whether the main model is needed; Do not internalise it, if not necessary
                boolean isMainReferenced = flattened.getModel().getId().equals(emd.getModelRef());
                for (ModelDefinition md : workingList) {
                    if (isMainReferenced) {
                        break;
                    }
                    CompModelPlugin cmp = (CompModelPlugin) md.getExtension(CompConstants.shortLabel);
                    if (cmp != null) {
                        for (Submodel sm : cmp.getListOfSubmodels()) {
                            isMainReferenced |= flattened.getModel().getId().equals(sm.getModelRef());
                        }
                    }
                }

                if (isMainReferenced) {
                    ModelDefinition localisedMain = new ModelDefinition(flattened.getModel());
                    workingList.add(0, localisedMain);
                }

                for (ModelDefinition md : workingList) {
                    ModelDefinition internalised = new ModelDefinition(md);
                    // i.e. current one is the one directly referenced => take referent's place
                    if (md.getId().equals(referenced.getId())) {
                        internalised.setId(emd.getId());
                    } else {
                        internalised.setId(newPrefix + internalised.getId());
                    }

                    CompModelPlugin notYetInternalisedModelPlugin =
                            (CompModelPlugin) internalised.getExtension(CompConstants.shortLabel);
                    if (notYetInternalisedModelPlugin != null && notYetInternalisedModelPlugin.isSetListOfSubmodels()) {
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
