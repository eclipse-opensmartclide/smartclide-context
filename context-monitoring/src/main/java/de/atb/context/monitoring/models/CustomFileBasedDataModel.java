package de.atb.context.monitoring.models;

import com.hp.hpl.jena.rdf.model.Model;
import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Root;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.util.Date;
import java.util.UUID;

@RdfType("CustomFileBasedDataModel")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Root
@Getter
@Setter
public class CustomFileBasedDataModel implements IMonitoringDataModel<CustomFileBasedDataModel, FileSystemDataSource> {

    private Date monitoredAt;
    private String documentIndexId = "index/file";
    private String documentUri;
    private String identifier;
    private FileSystemDataSource dataSource;
    private String implementingClassName = CustomFileBasedDataModel.class.getName();
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private String source;
    private String message;
    private String userInfo;

    @Id
    @Override
    public String getIdentifier() {
        if (this.identifier == null) {
            this.identifier = UUID.randomUUID().toString();
        }
        return this.identifier;
    }

    @Override
    public String getContextIdentifierClassName() {
        return null;
    }

    @Override
    public BusinessCase getBusinessCase() {
        return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
    }

    @Override
    public ApplicationScenario getApplicationScenario() {
        return ApplicationScenario.getInstance(getBusinessCase());
    }

    @Override
    public boolean triggersContextChange() {
        return false;
    }

    @Override
    public void initialize() {

    }

    @Override
    public CustomFileBasedDataModel fromRdfModel(String rdfModel) {
        return RdfHelper.createMonitoringData(rdfModel, CustomFileBasedDataModel.class);
    }

    @Override
    public CustomFileBasedDataModel fromRdfModel(Model model) {
        return RdfHelper.createMonitoringData(model, CustomFileBasedDataModel.class);
    }

    @Override
    public String toRdfString() {
        return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
    }

    @Override
    public Model toRdfModel() {
        return RdfHelper.createRdfModel(this);
    }
}
