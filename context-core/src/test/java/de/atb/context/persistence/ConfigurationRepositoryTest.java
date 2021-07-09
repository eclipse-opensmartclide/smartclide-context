package de.atb.context.persistence;

import de.atb.context.common.util.ApplicationScenario;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ConfigurationRepositoryTest {

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      ConfigurationRepository configurationRepository0 = new ConfigurationRepository();
      configurationRepository0.basicLocation = null;
      configurationRepository0.getBasicLocation();
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      ConfigurationRepository configurationRepository0 = ConfigurationRepository.getInstance();
      configurationRepository0.basicLocation = "; ";
      configurationRepository0.basicLocation = "";
      configurationRepository0.getBasicLocation();
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      ConfigurationRepository configurationRepository0 = new ConfigurationRepository();
      // Undeclared exception!
      try { 
        configurationRepository0.deleteApplicationScenarioConfiguration((ApplicationScenario) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
    	  assertFalse(false);
      }
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      // Undeclared exception!
      try { 
        ConfigurationRepository.clearDirectory((File) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
    	  assertFalse(false);
      }
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      ConfigurationRepository configurationRepository0 = new ConfigurationRepository();
      String string0 = configurationRepository0.getBasicLocation();
      assertEquals("configurations", string0);
  }
}
