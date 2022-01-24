package de.atb.context.extraction;

/*
 * #%L
 * ATB Context Extraction Core Service
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

import de.atb.context.extraction.util.IOntPropertyProvider;
import de.atb.context.extraction.util.base.BaseDatatypeProperties;
import de.atb.context.extraction.util.base.BaseObjectProperties;
import de.atb.context.extraction.util.base.BaseOntologyClasses;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.common.util.IApplicationScenarioProvider;
import de.atb.context.context.util.OntologyNamespace;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Context
 *
 * @author scholze
 * @version $LastChangedRevision: 647 $
 */
@Setter
@Getter
public class ContextContainer extends OntModelImpl implements
        IApplicationScenarioProvider {

    private final Logger logger = LoggerFactory
            .getLogger(ContextContainer.class);

    protected String identifier;
    protected String monitoringDataId;
    protected ApplicationScenario applicationScenario;

    protected Resource contextInstance;
    protected Date capturedAt;

    public ContextContainer() {
        super(OntModelSpec.OWL_MEM);
    }

    public ContextContainer(boolean useReasoner) {
        super(useReasoner ? OntModelSpec.OWL_MEM
                : OntModelSpec.OWL_DL_MEM);
    }

    public ContextContainer(OntModel base) {
        super(OntModelSpec.OWL_DL_MEM, base);
    }

    public ContextContainer(OntModel base, boolean useReasoner) {
        super(useReasoner ? OntModelSpec.OWL_MEM
                : OntModelSpec.OWL_DL_MEM, base);
    }

    public ContextContainer(ApplicationScenario scenario) {
        super(OntModelSpec.OWL_DL_MEM);
        applicationScenario = scenario;
    }

    public ContextContainer(ApplicationScenario scenario,
                            OntModel base) {
        super(OntModelSpec.OWL_DL_MEM, base);
        applicationScenario = scenario;
    }

    public ContextContainer(ApplicationScenario scenario,
                            boolean useReasoner) {
        super(useReasoner ? OntModelSpec.OWL_MEM
                : OntModelSpec.OWL_DL_MEM);
        applicationScenario = scenario;
    }

    public ContextContainer(ApplicationScenario scenario,
                            OntModel base, boolean useReasoner) {
        super(useReasoner ? OntModelSpec.OWL_MEM
                : OntModelSpec.OWL_DL_MEM, base);
        applicationScenario = scenario;
    }

    public final void addDefaultNamespaces() {
        for (OntologyNamespace ns : OntologyNamespace.values()) {
            setNsPrefix(ns.getNameSpacePrefix(), ns.getAbsoluteUri());
        }
    }

    public final void addDefaultModel(BusinessCase bc) {
        OntologyNamespace ns = null;
        switch (bc.toString()) {
            case "dummy":
                break;
            case "context":
                ns = OntologyNamespace.getInstance();
                break;
            default:
                break;
        }
        OntModel model = ModelFactory.createOntologyModel(getSpecification());
        if (ns != null) {
            model.read(ns.getAbsoluteUri());
        } else {
            model.read(OntologyNamespace.getInstance().getAbsoluteUri());
        }
        add(model);
    }

    public final BusinessCase getBusinessCase() {
        return applicationScenario.getBusinessCase();
    }

    public final void writeToFile(String file) throws IOException {
        ModelOutputLanguage.DEFAULT.writeModelToFile(this, new File(file));
        // RdfHelper.writeModelToFile(this, new File(file));
    }

    public final void writeToFile(String file,
                                  ModelOutputLanguage language) throws IOException {
        language.writeModelToFile(this, new File(file));
    }

    public static ContextContainer readFromFile(URI path) {
        ContextContainer context = new ContextContainer(false);
        context.read(String.valueOf(path));
        String identifier = context.inferContextIdentifier();
        context.setIdentifier(identifier);
        ApplicationScenario scenario = context.inferApplicationScenario();
        context.setApplicationScenario(scenario);
        Date capturedAt = context.inferCapturedAt();
        context.setCapturedAt(capturedAt);
        String monitoringId = context.inferMonitoringDataId();
        context.setMonitoringDataId(monitoringId);
        return context;

    }

    public static ContextContainer readFromFile(String filePath) {
        return ContextContainer.readFromFile(URI.create(filePath));
    }

    public final <T extends OntProperty> ResIterator listResourcesWithProperty(
            IOntPropertyProvider<T> p, Object o) {
        return listResourcesWithProperty(p.getProperty(this),
                createTypedLiteral(o));
    }

    public final ApplicationScenario inferApplicationScenario() {
        String query = String
                .format("SELECT ?applicationScenario WHERE {?c rdf:type :%1$s . ?c :%2$s \"%3$s\"^^xsd:string . ?c :%4$s ?applicationScenario}",
                        BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, getIdentifier(),
                        BaseDatatypeProperties.ApplicationScenarioIdentifier);
        ResultSet result = executeSelectSparqlQuery(query);
        while (result.hasNext()) {
            String applicationScenario = result.next()
                    .get("applicationScenario").asLiteral().getString();
            for (ApplicationScenario appScenario : ApplicationScenario.values()) {
                if (applicationScenario != null
                        && appScenario
                        .toString()
                        .toLowerCase(Locale.ENGLISH)
                        .equals(applicationScenario
                                .toLowerCase(Locale.ENGLISH))) {
                    return appScenario;
                }
            }
        }
        return null;
    }

    public final String inferContextIdentifier() {
        String query = String.format(
                "SELECT ?id WHERE {?c rdf:type :%1$s . ?c :%2$s ?id}", BaseOntologyClasses.Context,
                BaseDatatypeProperties.Identifier);
        ResultSet result = executeSelectSparqlQuery(query);
        if (result.hasNext()) {
            RDFNode node = result.next().get("id");
            if (node != null && node.asLiteral() != null) {
                return node.asLiteral().getString();
            }
        }
        return null;
    }

    public final String inferMonitoringDataId() {
        String query = String
                .format("SELECT ?monitoringId WHERE {?c rdf:type :%1$s . ?c :%2$s \"%3$s\"^^xsd:string . ?c :%4$s ?monitoringId}",
                        BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, getIdentifier(),
                        BaseDatatypeProperties.BasedOnMonitoringData);
        ResultSet result = executeSelectSparqlQuery(query);
        if (result.hasNext()) {
            RDFNode node = result.next().get("monitoringId");
            if (node != null && node.asLiteral() != null) {
                return node.asLiteral().getString();
            }
        }
        return null;
    }

    @Deprecated
    protected final BusinessCase inferBusinessCase() {
        String query = String
                .format("SELECT ?bc WHERE {?c rdf:type :%1$s . ?c :%2$s \"%3$s\"^^xsd:string . ?c :%4$s ?bc}",
                        BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, getIdentifier(),
                        BaseDatatypeProperties.BusinessCaseIdentifier);
        ResultSet result = executeSelectSparqlQuery(query);
        while (result.hasNext()) {
            String bcName = result.next().get("bc").asLiteral().getString();
            for (BusinessCase bc : BusinessCase.values()) {
                if (bcName != null
                        && bc.toString().toLowerCase(Locale.ENGLISH)
                        .equals(bcName.toLowerCase(Locale.ENGLISH))) {
                    return bc;
                }
            }
        }
        return null;
    }

    public final Date inferCapturedAt() {
        String query = String
                .format("SELECT ?cap WHERE {?c rdf:type :%1$s . ?c :%2$s \"%3$s\"^^xsd:string . ?c :%4$s ?cap}",
                        BaseOntologyClasses.Context, BaseDatatypeProperties.Identifier, getIdentifier(), BaseDatatypeProperties.CapturedAt);
        ResultSet result = executeSelectSparqlQuery(query);
        while (result.hasNext()) {
            RDFNode node = result.next().get("cap");
            if (node != null && node.isLiteral()) {
                Object value = node.asLiteral().getValue();
                return ((XSDDateTime) value).asCalendar().getTime();
            }
        }
        return null;
    }

    protected final ResultSet executeSelectSparqlQuery(String sparqlQuery) {
        Query query = QueryFactory.create(OntologyNamespace
                .prepareSparqlQuery(sparqlQuery));
        QueryExecution qexec = QueryExecutionFactory.create(query, this);
        logger.debug("Executing SparQL select query '" + query + "'");
        return qexec.execSelect();
    }

    public final String serializeToString() {
        return ModelOutputLanguage.DEFAULT.getModelAsString(this);
    }

    public static OntModel deserializeToModel(String serializedInstance) {
        OntModel model = ModelFactory.createOntologyModel();
        InputStream is = new ByteArrayInputStream(serializedInstance.getBytes());
        model.read(is, null);
        return model;
    }

    public static ContextContainer fromContextContainerWrapper(
            ContextContainerWrapper wrapper) {
        return ContextContainerWrapper.toContextContainer(wrapper);
    }

    public static ContextContainerWrapper toContextContainerWrapper(
            ContextContainer context) {
        return ContextContainerWrapper.fromContextContainer(context);
    }

    public final ContextContainerWrapper toContextContainerWrapper() {
        return ContextContainerWrapper.fromContextContainer(this);
    }

    public final Resource createNewContextInstance() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        UUID identifier = UUID.randomUUID();

        contextInstance = BaseOntologyClasses.Context.createIndividual(this);

        DatatypeProperty capturedAt = BaseDatatypeProperties.CapturedAt.getProperty(this);
        setCapturedAt(cal.getTime());
        add(contextInstance, capturedAt, createTypedLiteral(cal));

        DatatypeProperty bcIdentifier = BaseDatatypeProperties.BusinessCaseIdentifier
                .getProperty(this);
        add(contextInstance, bcIdentifier,
                createTypedLiteral(getBusinessCase().toString()));

        DatatypeProperty appScenarioIdentifier = BaseDatatypeProperties.ApplicationScenarioIdentifier
                .getProperty(this);
        add(contextInstance, appScenarioIdentifier, createTypedLiteral(getApplicationScenario().toString()));

        if (monitoringDataId != null
                && monitoringDataId.trim().length() > 0) {
            DatatypeProperty monitoringDataId = BaseDatatypeProperties.BasedOnMonitoringData
                    .getProperty(this);
            add(contextInstance, monitoringDataId,
                    createTypedLiteral(this.monitoringDataId));
        }

        setIdentifier(identifier.toString());
        addIdentifier(contextInstance, identifier.toString());

        return contextInstance;
    }

    public final void addBelonging(Resource instance) {
        add(instance, BaseObjectProperties.BelongsToContext,
                contextInstance);
    }

    public final void add(Resource instanceOne,
                          IOntPropertyProvider<ObjectProperty> property,
                          Resource instanceTwo) {
        add(instanceOne, property.getProperty(this), instanceTwo);
    }

    public final void add(Resource instanceOne,
                          IOntPropertyProvider<DatatypeProperty> property,
                          Literal literal) {
        add(instanceOne, property.getProperty(this), literal);
    }

    public final void add(Resource instance,
                          IOntPropertyProvider<ObjectProperty> property) {
        add(instance, property, contextInstance);
    }

    /**
     * Adds an identifier to the given resource. This will add the following
     * statement to the ontology model: {@code
     * model.add(resource, base_ns#identifier, id);
     * }
     *
     * @param resource the resource to be identified by the given identifier.
     * @param id       the identifier for the resource.
     */
    public final void addIdentifier(Resource resource, String id) {
        DatatypeProperty identifierProperty = BaseDatatypeProperties.Identifier.getProperty(this);
        add(resource, identifierProperty, createTypedLiteral(id));
    }
}
