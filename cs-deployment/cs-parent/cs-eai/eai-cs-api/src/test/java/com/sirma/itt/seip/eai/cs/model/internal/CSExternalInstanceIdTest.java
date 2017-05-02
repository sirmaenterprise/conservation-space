package com.sirma.itt.seip.eai.cs.model.internal;

import org.junit.Assert;
import org.junit.Test;

public class CSExternalInstanceIdTest {

	@Test
	public void testHashCode() throws Exception {
	}

	@Test
	public void testEquals() throws Exception {

		CSExternalInstanceId obj1 = new CSExternalInstanceId("i0", "s");
		Assert.assertTrue(obj1.equals(obj1));

		CSExternalInstanceId obj2 = new CSExternalInstanceId("i0", "s");
		Assert.assertTrue(obj1.equals(obj2));

		CSExternalInstanceId obj3 = new CSExternalInstanceId("i3", "s3");
		Assert.assertFalse(obj3.equals(obj2));
		Assert.assertFalse(obj3.equals(obj1));

		CSExternalInstanceId obj4 = new CSExternalInstanceId("i3", null);
		Assert.assertFalse(obj4.equals(obj3));

		CSExternalInstanceId obj3U = new CSExternalInstanceId("I3", "S3");
		Assert.assertTrue(obj3.equals(obj3U));
	}

}
