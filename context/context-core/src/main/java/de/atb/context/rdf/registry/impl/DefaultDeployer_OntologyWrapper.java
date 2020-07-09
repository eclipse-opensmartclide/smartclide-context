package pt.uninova.context.rdf.registry.impl;

/*-
 * #%L
 * ATB Context Extraction Core Lib
 * %%
 * Copyright (C) 2016 - 2020 ATB
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.protege.owl.codegeneration.impl.WrappedIndividualImpl;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.IRI;
import pt.uninova.context.rdf.registry.Deployer_OntologyWrapper;
import pt.uninova.context.rdf.registry.SW_Service_Configuration_OntologyWrapper;
import pt.uninova.context.rdf.registry.SW_Service_OntologyWrapper;
import pt.uninova.context.rdf.registry.Vocabulary;

import java.util.Collection;


/**
 * Generated by Protege (http://protege.stanford.edu).<br>
 * Source Class: DefaultDeployer_OntologyWrapper <br>
 *
 * @version generated on Mon Oct 05 21:07:01 BST 2015 by Giovanni
 */
public class DefaultDeployer_OntologyWrapper extends WrappedIndividualImpl implements Deployer_OntologyWrapper {

    public DefaultDeployer_OntologyWrapper(CodeGenerationInference inference, IRI iri) {
        super(inference, iri);
    }





    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasDeployer_SWServices
     */

    public Collection<? extends SW_Service_OntologyWrapper> getHasDeployerSWServices() {
        return getDelegate().getPropertyValues(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASDEPLOYER_SWSERVICES,
                DefaultSW_Service_OntologyWrapper.class);
    }

    public boolean hasHasDeployerSWServices() {
        return !getHasDeployerSWServices().isEmpty();
    }

    public void addHasDeployerSWServices(SW_Service_OntologyWrapper newHasDeployerSWServices) {
        getDelegate().addPropertyValue(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASDEPLOYER_SWSERVICES,
                newHasDeployerSWServices);
    }

    public void removeHasDeployerSWServices(SW_Service_OntologyWrapper oldHasDeployerSWServices) {
        getDelegate().removePropertyValue(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASDEPLOYER_SWSERVICES,
                oldHasDeployerSWServices);
    }


    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasSWService_Configuration
     */

    public Collection<? extends SW_Service_Configuration_OntologyWrapper> getHasSWServiceConfiguration() {
        return getDelegate().getPropertyValues(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASSWSERVICE_CONFIGURATION,
                DefaultSW_Service_Configuration_OntologyWrapper.class);
    }

    public boolean hasHasSWServiceConfiguration() {
        return !getHasSWServiceConfiguration().isEmpty();
    }

    public void addHasSWServiceConfiguration(SW_Service_Configuration_OntologyWrapper newHasSWServiceConfiguration) {
        getDelegate().addPropertyValue(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASSWSERVICE_CONFIGURATION,
                newHasSWServiceConfiguration);
    }

    public void removeHasSWServiceConfiguration(SW_Service_Configuration_OntologyWrapper oldHasSWServiceConfiguration) {
        getDelegate().removePropertyValue(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASSWSERVICE_CONFIGURATION,
                oldHasSWServiceConfiguration);
    }


    /* ***************************************************
     * Object Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#hasSWService_Deployer
     */

    public Collection<? extends Deployer_OntologyWrapper> getHasSWServiceDeployer() {
        return getDelegate().getPropertyValues(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASSWSERVICE_DEPLOYER,
                DefaultDeployer_OntologyWrapper.class);
    }

    public boolean hasHasSWServiceDeployer() {
        return !getHasSWServiceDeployer().isEmpty();
    }

    public void addHasSWServiceDeployer(Deployer_OntologyWrapper newHasSWServiceDeployer) {
        getDelegate().addPropertyValue(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASSWSERVICE_DEPLOYER,
                newHasSWServiceDeployer);
    }

    public void removeHasSWServiceDeployer(Deployer_OntologyWrapper oldHasSWServiceDeployer) {
        getDelegate().removePropertyValue(getOwlIndividual(),
                Vocabulary.OBJECT_PROPERTY_HASSWSERVICE_DEPLOYER,
                oldHasSWServiceDeployer);
    }


    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#IdName
     */

    public Collection<? extends String> getIdName() {
        return getDelegate().getPropertyValues(getOwlIndividual(), Vocabulary.DATA_PROPERTY_IDNAME, String.class);
    }

    public boolean hasIdName() {
        return !getIdName().isEmpty();
    }

    public void addIdName(String newIdName) {
        getDelegate().addPropertyValue(getOwlIndividual(), Vocabulary.DATA_PROPERTY_IDNAME, newIdName);
    }

    public void removeIdName(String oldIdName) {
        getDelegate().removePropertyValue(getOwlIndividual(), Vocabulary.DATA_PROPERTY_IDNAME, oldIdName);
    }


    /* ***************************************************
     * Data Property http://www.semanticweb.org/giovanni/ontologies/2014/10/untitled-ontology-55#status
     */

    public Collection<? extends String> getStatus() {
        return getDelegate().getPropertyValues(getOwlIndividual(), Vocabulary.DATA_PROPERTY_STATUS, String.class);
    }

    public boolean hasStatus() {
        return !getStatus().isEmpty();
    }

    public void addStatus(String newStatus) {
        getDelegate().addPropertyValue(getOwlIndividual(), Vocabulary.DATA_PROPERTY_STATUS, newStatus);
    }

    public void removeStatus(String oldStatus) {
        getDelegate().removePropertyValue(getOwlIndividual(), Vocabulary.DATA_PROPERTY_STATUS, oldStatus);
    }


}
