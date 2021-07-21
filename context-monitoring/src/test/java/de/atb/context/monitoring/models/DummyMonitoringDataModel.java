package de.atb.context.monitoring.models;

import com.hp.hpl.jena.rdf.model.Model;
import de.atb.context.common.Version;
import de.atb.context.common.util.BusinessCase;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.rdf.RdfHelper;
import de.atb.context.persistence.ModelOutputLanguage;
import lombok.Getter;
import lombok.Setter;
import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * DummyMonitoringDataModel
 *
 * @author scholze
 * @version $LastChangedRevision: 692 $
 *
 */
@RdfType("DummyMonitoringDataModel")
@Namespace("http://www.atb-bremen.de/")
@Getter
@Setter
public class DummyMonitoringDataModel implements IMonitoringDataModel<DummyMonitoringDataModel, FileSystemDataSource> {

	private static final long serialVersionUID = -8744217754389596169L;

    private String documentIndexId = "index/dummy";
    private String documentUri = "/var/tmp/dummy.doc";
    private String implementingClassName = DummyMonitoringDataModel.class.getName();

    private Date monitoredAt = new Date();
    private String monitoringDataVersion = Version.MONITORING_DATA.getVersionString();
    private FileSystemDataSource dataSource;

    private String dummyName = "myDummyName";
    private String dummyValue = "myDummyVaLuE!";
    @Id
    private String identifier;

    public DummyMonitoringDataModel() {
        this.identifier = UUID.randomUUID().toString();
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * IMonitoringData#fromRdfModel(java.lang
	 * .String)
	 */
	@Override
	public final DummyMonitoringDataModel fromRdfModel(final String rdfModel) {
		return RdfHelper.createMonitoringData(rdfModel, DummyMonitoringDataModel.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * IMonitoringData#fromRdfModel(com.hp.hpl
	 * .jena.rdf.model.Model)
	 */
	@Override
	public final DummyMonitoringDataModel fromRdfModel(final Model model) {
		return RdfHelper.createMonitoringData(model, DummyMonitoringDataModel.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMonitoringData#toRdfString()
	 */
	@Override
	public final String toRdfString() {
		return ModelOutputLanguage.DEFAULT.getModelAsString(this.toRdfModel());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMonitoringData#toRdfModel()
	 */
	@Override
	public final Model toRdfModel() {
		return RdfHelper.createRdfModel(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * IMonitoringDataModel#getBusinessCase
	 * ()
	 */
	@Override
	public final BusinessCase getBusinessCase() {
		return BusinessCase.getInstance(BusinessCase.NS_DUMMY_ID, BusinessCase.NS_DUMMY_URL);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#
	 * triggersContextChange()
	 */
	@Override
	public final boolean triggersContextChange() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#
	 * getApplicationScenario()
	 */
	@Override
	public final ApplicationScenario getApplicationScenario() {
		return ApplicationScenario.getInstance();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IMonitoringDataModel#
	 * getContextIdentifierClassName()
	 */
	@Override
	public final String getContextIdentifierClassName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * IMonitoringDataModel#initialize()
	 */
	@Override
	public void initialize() {
	}
}
