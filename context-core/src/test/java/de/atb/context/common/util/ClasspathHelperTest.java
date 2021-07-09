package de.atb.context.common.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ClasspathHelperTest {

  @Test
  public void test5()  throws Throwable  {
      Class<Integer> class0 = Integer.class;
      List<Class<?>> list0 = ClasspathHelper.getMatchingClasses(class0, false);
      assertEquals(0, list0.size());
  }

  @Test
  public void test6()  throws Throwable  {
      Class<String> class0 = String.class;
      List<Class<?>> list0 = ClasspathHelper.getMatchingClasses("", class0);
      assertEquals(0, list0.size());
  }

  @Test
  public void test7()  throws Throwable  {
      Class<String> class0 = String.class;
      List<Class<?>> list0 = ClasspathHelper.getMatchingClasses(class0);
      assertEquals(0, list0.size());
  }

//  @Test
//  public void test8()  throws Throwable  {
//      List<Class<?>> list0 = ClasspathHelper.getMatchingClasses("", true);
//      assertEquals(0, list0.size());
//  }

//  @Test
//  public void test9()  throws Throwable  {
//      List<Class<?>> list0 = ClasspathHelper.getAllClasses();
//      //assertEquals(385, list0.size());
//  }
}
