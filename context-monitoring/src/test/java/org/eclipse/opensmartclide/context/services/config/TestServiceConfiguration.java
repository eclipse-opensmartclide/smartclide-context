package org.eclipse.opensmartclide.context.services.config;

import static org.junit.Assert.assertTrue;

import org.eclipse.opensmartclide.context.common.ContextPathUtils;
import org.junit.Test;

import org.eclipse.opensmartclide.context.services.SWServiceContainer;
import org.eclipse.opensmartclide.context.services.config.models.ISWService;

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
        String configFilePath = ContextPathUtils.getConfigDirPath().resolve("services-config.xml").toString();
		SWServiceContainer serviceContainer = new SWServiceContainer(
				"AmI-repository", configFilePath);
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
