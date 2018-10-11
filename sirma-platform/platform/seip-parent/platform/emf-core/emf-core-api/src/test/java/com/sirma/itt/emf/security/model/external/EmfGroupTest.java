package com.sirma.itt.emf.security.model.external;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.resources.EmfGroup;

public class EmfGroupTest {

	private EmfGroup group;

  @BeforeTest
  public void beforeTest() {
	  group = new EmfGroup();
  }

  @Test
  public void testClone(){
	  EmfGroup theClone = group.createCopy();
	  Assert.assertTrue(group.equals(theClone));
  }

  @Test
  public void testSealFalse(){
	  Assert.assertFalse(group.isSealed());
  }

  @Test
  public void testSealTrue(){
	  group.seal();
	  Assert.assertTrue(group.isSealed());
  }
}
