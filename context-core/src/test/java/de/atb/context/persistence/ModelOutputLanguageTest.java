package de.atb.context.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelOutputLanguageTest {

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      ModelOutputLanguage modelOutputLanguage0 = ModelOutputLanguage.RDFXML;
      // Undeclared exception!
      try { 
        modelOutputLanguage0.getModelAsString((Model) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
    	  assertFalse(false);
      }
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      ModelOutputLanguage modelOutputLanguage0 = ModelOutputLanguage.DEFAULT;
      modelOutputLanguage0.toString();
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      ModelOutputLanguage modelOutputLanguage0 = ModelOutputLanguage.RDFXML_ABBREV;
      // Undeclared exception!
      try { 
        modelOutputLanguage0.writeModelToFile((Model) null, (File) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
    	  assertFalse(false);
      }
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      ModelOutputLanguage modelOutputLanguage0 = ModelOutputLanguage.DEFAULT;
      modelOutputLanguage0.getLanguage();
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      // Undeclared exception!
      try { 
        ModelOutputLanguage.valueOf(".4");
        fail("Expecting exception: IllegalArgumentException");
      
      } catch(IllegalArgumentException e) {
         //
         // No enum constant de.atb.context.persistence.ModelOutputLanguage..4
         //
    	  assertFalse(false);
      }
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      // Undeclared exception!
      try { 
        ModelOutputLanguage.valueOf((String) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // Name is null
         //
    	  assertFalse(false);
      }
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      ModelOutputLanguage modelOutputLanguage0 = ModelOutputLanguage.N3;
      modelOutputLanguage0.getLanguage();
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      ModelOutputLanguage modelOutputLanguage0 = ModelOutputLanguage.RDFXML;
      modelOutputLanguage0.toString();
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
      ModelOutputLanguage.valueOf("RDFXML");
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
      ModelOutputLanguage.valueOf("DEFAULT");
  }

  @Test(timeout = 4000)
  public void test15()  throws Throwable  {
      ModelOutputLanguage.values();
  }
}
