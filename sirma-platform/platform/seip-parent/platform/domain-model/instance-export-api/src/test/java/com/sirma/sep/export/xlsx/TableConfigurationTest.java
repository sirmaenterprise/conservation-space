package com.sirma.sep.export.xlsx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

/**
 * Test for {@link TableConfiguration}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class TableConfigurationTest {

	@Test
	public void isManuallySelected() {
		TableConfiguration configuration = new TableConfiguration(true, new HashMap<>(), false);
		assertTrue(configuration.isManuallySelected());
	}

	@Test
	public void getHeadersInfo() {
		TableConfiguration configuration = new TableConfiguration(false, new HashMap<>(), false);
		assertNotNull(configuration.getHeadersInfo());
	}

	@Test
	public void showInstanceId() {
		TableConfiguration configuration = new TableConfiguration(false, new HashMap<>(), true);
		assertTrue(configuration.showInstanceId());
	}
}
