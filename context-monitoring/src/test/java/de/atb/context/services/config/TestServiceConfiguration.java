package de.atb.context.services.config;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.atb.context.services.SWServiceContainer;
import de.atb.context.services.config.models.ISWService;

/**
 * TestServiceConfiguration
 * 
 * @author scholze
 * @version $LastChangedRevision: 94 $
 * 
 */
public class TestServiceConfiguration {

	@Test
	public void testGetInstance() {
		String absolutefilePath = new File("").getAbsolutePath();
        File file = new File(
            absolutefilePath.concat(File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "services-config.xml"));
		String filepath = file.getPath();
		SWServiceContainer serviceContainer = new SWServiceContainer(
				"AmI-repository", filepath);
		String val = "";

		ISWService monitoringService = serviceContainer.getService();
		assertTrue(monitoringService != null);
		if (monitoringService != null) {
			val = String.valueOf(monitoringService.getLocation());
			assertTrue(!(val.equals("null") || (val.trim().length() == 0)));
			val = String.valueOf(monitoringService.getServerClass());
			assertTrue(!(val.equals("null") || (val.trim().length() == 0)));
			val = String.valueOf(monitoringService.getProxyClass());
			assertTrue(!(val.equals("null") || (val.trim().length() == 0)));
		}
	}

}
