package de.atb.context.kmb;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author scholze
 * @version $LastChangedRevision: 176 $
 */
public class KMBApiTest {
	private static KMBApi kmb;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		kmb = new KMBApi();
	}

	/**
	 * Test method for {@link de.atb.context.kmb.KMBApi#readElementConfiguration(java.lang.String)}.
	 */
	@Test
	public void testReadElementConfiguration() {
		Assert.assertTrue(kmb.writeElementConfiguration("PES_example_12345", "{\"\"PES_example_12345\"\":{},\"\"Machine_example_234\"\":{},\\\\n\"\"selected_Sensors\"\":[\\\\n\\\\t{ \"\"name\"\":\"\"Actor Position\"\",\"\"description\"\":\"\"Actor Position Sensor example\"\",\"\"id\"\":\"\"1f2153b2-50c2-4d1e-a23d-f533a2ece76e\"\",\"\"output_value\"\":{\"\"NativeType\"\":\"\"float\"\",\"\"unit\"\":\"\"meter\"\",\"\"Description\"\":\"\"Actor Position Sensor value Example\"\"},\"\"type\"\":\"\"simpleValue\"\"}]}"));
		Assert.assertTrue(kmb.readElementConfiguration("PES_example_12345").equals("{\"PES_example_12345\":{},\"Machine_example_234\":{},\n\"selected_Sensors\":[\n\t{ \"name\":\"Actor Position\",\"description\":\"Actor Position Sensor example\",\"id\":\"1f2153b2-50c2-4d1e-a23d-f533a2ece76e\",\"output_value\":{\"NativeType\":\"float\",\"unit\":\"meter\",\"Description\":\"Actor Position Sensor value Example\"},\"type\":\"simpleValue\"}]}"));
	}

	/**
	 * Test method for {@link de.atb.context.kmb.KMBApi#readAllElementConfigurations(java.lang.String)}.
	 */
	//@Test
	public void testReadAllElementConfigurations() {
		String allConfigs = kmb.readAllElementConfigurations("PES_example_12345");
	}

	/**
	 * Test method for {@link de.atb.context.kmb.KMBApi#readElementPropertyValue(java.lang.String, java.lang.String)}.
	 */
	//@Test
	public void testReadElementPropertyValue() {
		Assert.assertTrue(kmb.writeElementInformation("ID_PES_1234567778", "{\"\"CreatedBy\"\":\"\"FranÇois Cerdeña\"\",\"\"Status\"\":\"\"Under development\"\"}"));
		Assert.assertTrue(kmb.readElementPropertyValue("ID_PES_1234567778", "\"\"Status\"\"").equals("{\"Status\":\"Under development\"}"));
	}

	/**
	 * Test method for {@link de.atb.context.kmb.KMBApi#readElementInformation(java.lang.String)}.
	 */
	@Test
	public void testReadElementInformation() {
		Assert.assertTrue(kmb.writeElementInformation("ID_PES_12345677788", "{\"\"CreatedBy\"\":\"\"FranÇois Cerdeña\"\",\"\"Status\"\":\"\"Under development\"\"}"));
		Assert.assertTrue(kmb.readElementInformation("ID_PES_12345677788").equals("{\"CreatedBy\":\"FranÇois Cerdeña\",\"Status\":\"Under development\"}")); //FIXME temp fix until clarified with sem
	}

}
