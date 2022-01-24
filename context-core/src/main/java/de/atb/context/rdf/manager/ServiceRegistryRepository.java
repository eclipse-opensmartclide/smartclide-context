package de.atb.context.rdf.manager;

/*
 * #%L
 * ATB Context Extraction Core Lib
 * %%
 * Copyright (C) 2021 ATB – Institut für angewandte Systemtechnik Bremen GmbH
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */


import org.apache.commons.io.IOUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.atb.context.common.io.FileUtils;
import de.atb.context.infrastructure.ConnectedDeployer;
import de.atb.context.infrastructure.ConnectedServices;
import de.atb.context.infrastructure.Node;
import de.atb.context.infrastructure.ServiceInfo;
import de.atb.context.modules.broker.status.ontology.StatusVocabulary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni
 */
public final class ServiceRegistryRepository implements
        IServiceRegistryRepository {

    private static final Logger logger = LoggerFactory
            .getLogger(ServiceRegistryRepository.class);

    private static final String TEMPLATE_ONTOLOGY_PATH = "/resources/rdfs/SoftwareServiceOntology_v0.7.owl";
    private static final String WORKING_ONTOLOGY_PATH = "/resources/rdfs/SoftwareServiceOntology_v0.7_working.owl";
    private static final String HOME_CONFIG_PATH = System.getProperty("user.home") + File.separator + ".context";

    private boolean initialized = false;

    public ServiceRegistryRepository() {
        this.initializeRepository();
    }

    @Override
    public void initializeRepository() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            //Create a local working copy of the ontology;
            InputStream streamTemplate = getClass().getResourceAsStream(TEMPLATE_ONTOLOGY_PATH);
            InputStream streamWorking = getClass().getResourceAsStream(WORKING_ONTOLOGY_PATH);
            String templateOntology = "";
            String workingOntology = "";
            if (streamTemplate != null) {
                String templateFileString = IOUtils.toString(streamTemplate, Charset.defaultCharset());
                if (FileUtils.ensureDirectoryExists(HOME_CONFIG_PATH + TEMPLATE_ONTOLOGY_PATH.replace("/", File.separator))) {
                    FileUtils.writeStringToFile(templateFileString, HOME_CONFIG_PATH + TEMPLATE_ONTOLOGY_PATH.replace("/", File.separator));
                    templateOntology = HOME_CONFIG_PATH + TEMPLATE_ONTOLOGY_PATH.replace("/", File.separator);
                    logger.info("filepath created by Jar internal configuration Template Ontology Registry Filepath: {}", templateOntology);
                    if (streamWorking != null) {
                        String workingFileString = IOUtils.toString(streamWorking, Charset.defaultCharset());

                        FileUtils.writeStringToFile(workingFileString, HOME_CONFIG_PATH + WORKING_ONTOLOGY_PATH.replace("/", File.separator));
                        workingOntology = HOME_CONFIG_PATH + WORKING_ONTOLOGY_PATH.replace("/", File.separator);
                        logger.info("filepath created by Jar internal configuration Working Ontology Registry Filepath: {}", workingOntology);
                    } else {
                        logger.info("File does not exists: {}", WORKING_ONTOLOGY_PATH);
                        initialized = false;
                    }
                }
            } else {
                logger.info("File does not exists: {}", TEMPLATE_ONTOLOGY_PATH);
                initialized = false;
            }
            if (!templateOntology.equals("") && !workingOntology.equals("")) {
                File templateOntoFile = new File(templateOntology);
                File workingOntoFile = new File(workingOntology);
                manager.saveOntology(manager.loadOntologyFromOntologyDocument(templateOntoFile), IRI.create(workingOntoFile));
                OWLManager.createOWLOntologyManager();
                initialized = true;
            } else {
                logger.info("Problem in creating the file paths");
                initialized = false;
            }

        } catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex) {
            logger.error(ex.getMessage());
            initialized = false;
        }
    }

    @Override
    public boolean insert(final Node node) {
        ConnectedDeployer deployer = node.getDeployer();
        return initialized && (deployer != null && deployer.getConfig() != null
            && deployer.getServices().getConfig() != null
            && !deployer.getServices().getConfig().isEmpty());
    }

    @Override
    public boolean delete(final Node node) {
        ConnectedDeployer deployer = node.getDeployer();
        return initialized && (deployer != null && deployer.getConfig() != null
            && !deployer.getServices().getConfig().isEmpty());
    }

    @Override
    public Node selectForId(final String id) {
        if (initialized) {
            ConnectedDeployer connectedDeployer = new ConnectedDeployer();
            // TODO: there is a lot of duplicated code `serviceInfo.set...` in this class
            // this should be refactored into a separate method
            ServiceInfo deployerConfig = new ServiceInfo();
            connectedDeployer.setConfig(deployerConfig);
            ConnectedServices connectedServices = new ConnectedServices();
            List<ServiceInfo> servicesConfigurations = new ArrayList<>();
            connectedServices.setConfig(servicesConfigurations);
            connectedServices.setDeployer(deployerConfig);
            connectedDeployer.setServices(connectedServices);
            return new Node(connectedDeployer);
        }
        return null;
    }

    @Override
    public List<ServiceInfo> selectForServiceType(final String typeID) {
        List<ServiceInfo> result = new ArrayList<>();
        if (initialized) {
            ServiceInfo info = new ServiceInfo();
            result.add(info);
        }
        return result;
    }

    @Override
    public List<Node> selectAllConnectedDeployers() {
        if (initialized) {
            List<Node> nodes = new ArrayList<>();
            ConnectedDeployer connectedDeployer = new ConnectedDeployer();
            ServiceInfo deployerConfig = new ServiceInfo();
            connectedDeployer.setConfig(deployerConfig);
            ConnectedServices connectedServices = new ConnectedServices();
            List<ServiceInfo> servicesConfigurations = new ArrayList<>();
            ServiceInfo serviceConfig = new ServiceInfo();
            servicesConfigurations.add(serviceConfig);
            connectedServices.setConfig(servicesConfigurations);
            connectedServices.setDeployer(deployerConfig);
            connectedDeployer.setServices(connectedServices);
            nodes.add(new Node(connectedDeployer));

            return nodes;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean updateStatus(final List<String> idList, final StatusVocabulary status) {
        return initialized;
    }

    @Override
    public List<ServiceInfo> selectForFreeServiceByType(String typeID) {
        List<ServiceInfo> result = new ArrayList<>();
        if (initialized) {
            ServiceInfo info = new ServiceInfo();
            result.add(info);
        }
        return result;
    }

    @Override
    public boolean updateSingleStatusById(String id, StatusVocabulary status) {
        return initialized;
    }

    @Override
    public boolean setStatusByIds(List<String> listId, StatusVocabulary status) {
        boolean res = true;

        for (String id : listId) {
            try {
                if (!updateSingleStatusById(id, status)) {
                    res = false;
                }
            } catch (Exception e) {
                res = false;
            }
        }
        return res;
    }

    @Override
    public boolean updateSingleStatusByLocation(String location, StatusVocabulary status) {
        return initialized;
    }
}
