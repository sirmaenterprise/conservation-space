package com.sirma.itt.seip.instance.archive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link ArchivedEntity}.
 *
 * @author A. Kunchev
 */
public class ArchivedEntityTest {

	@Test
	public void setVersion() {
		ArchivedEntity entity = new ArchivedEntity();
		entity.setVersion("1.65");
		assertEquals(1, entity.getMajorVersion());
		assertEquals(65, entity.getMinorVersion());
	}

	@Test
	public void testGetVersion() {
		ArchivedEntity entity = new ArchivedEntity();
		entity.setMajorVersion(7);
		entity.setMinorVersion(5);
		assertEquals("7.5", entity.getVersion());
	}

}
