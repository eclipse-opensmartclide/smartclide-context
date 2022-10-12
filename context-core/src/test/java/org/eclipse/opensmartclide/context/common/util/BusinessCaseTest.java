package org.eclipse.opensmartclide.context.common.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BusinessCaseTest {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      BusinessCase[] businessCaseArray0 = BusinessCase.values();
      assertNotNull(businessCaseArray0);
  }
}
