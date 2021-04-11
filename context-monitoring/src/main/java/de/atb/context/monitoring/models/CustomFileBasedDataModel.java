package de.atb.context.monitoring.models;

import com.hp.hpl.jena.rdf.model.Model;
import de.atb.context.common.Version;
import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import org.simpleframework.xml.Root;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.util.Date;
import java.util.UUID;

@RdfType("CustomFileBasedDataModel")
@Namespace(BusinessCase.NS_DUMMY_URL)
@Root
public class CustomFileBasedDataModel implements IMonitoringDataModel<CustomFileBasedDataModel, FileSystemDataSource> {

    private Date monitoredAt;
    protected String documentIndexId = "index/file";
    protected String documentUri;
    protected String identifier;
    protected FileSystemDataSource dataSource;
    protected String implementingClassName = CustomFileBasedDataModel.class.getName();
    protected String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    protected String source;
    protected String message;
    protected String userInfo;

    @Id
    @Override
    public String getIdentifier() {
        if (this.identifier == null) {
            this.identifier = UUID.randomUUID().toString();
        }
        return this.identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getMonitoringDataVersion() {
        return this.monitoringDataVersion;
    }

    @Override
    public Date getMonitoredAt() {
        return monitoredAt;
    }

    public void setMonitoredAt(Date monitoredAt) {
        if (monitoredAt != null) {
            this.monitoredAt = (Date) monitoredAt.clone();
        } else {
            this.monitoredAt = null;
        }
    }

    @Override
    public String getDocumentIndexId() {
        return this.documentIndexId;
    }

    public void setDocumentIndexId(String documentIndexId) {
        this.documentIndexId = documentIndexId;
    }

    @Override
    public String getDocumentUri() {
        return this.documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    @Override
    public String getImplementingClassName() {
        return this.implementingClassName;
    }

    @Override
    public String getContextIdentifierClassName() {
        return null;
    }

    @Override
    public FileSystemDataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public void setDataSource(FileSystemDataSource ds) {
        this.dataSource = ds;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
