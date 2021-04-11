package de.atb.context;

import de.atb.context.common.util.ApplicationScenario;
import de.atb.context.monitoring.models.CustomFileBasedDataModel;
import de.atb.context.persistence.monitoring.MonitoringDataRepository;
import de.atb.context.services.faults.ContextFault;
import org.junit.Test;

/**
 * 
 * 
 * @author pvyas
 * 
 */
public class TestCustomJsonFile {

	@Test
	public void doNothing() {
		
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ContextFault {
//		ServiceManager.registerWebservice(SWService.AdaptationRepositoryService);
//		ServiceManager.registerWebservice(SWService.ContextExtractionService);

		MonitoringDataRepository<CustomFileBasedDataModel> repos = (MonitoringDataRepository<CustomFileBasedDataModel>)  MonitoringDataRepository
				.getInstance();
		final CustomFileBasedDataModel analysed = repos.getMonitoringData(ApplicationScenario.getInstance(),
            CustomFileBasedDataModel.class, "ContextId_TankRefilling");
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
//				adapterService.informAboutContextChange(analysed.getApplicationScenario(), analysed.getIdentifier());
			}
		});
		t.start();
	}
}
