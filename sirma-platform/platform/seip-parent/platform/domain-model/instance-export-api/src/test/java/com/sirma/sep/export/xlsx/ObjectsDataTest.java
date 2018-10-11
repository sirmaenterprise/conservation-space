package com.sirma.sep.export.xlsx;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

/**
 * Test for {@link ObjectsData}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class ObjectsDataTest {

	@Test
	public void getManuallySelectedObjects() {
		ObjectsData data = new ObjectsData(new ArrayList<>(), "none", new HashMap<>(), new HashMap<>());
		assertNotNull(data.getManuallySelectedObjects());
	}

	@Test
	public void getInstanceHeaderType() {
		ObjectsData data = new ObjectsData(new ArrayList<>(), "none", new HashMap<>(), new HashMap<>());
		assertNotNull(data.getInstanceHeaderType());
	}

	@Test
	public void getSelectedProperties() {
		ObjectsData data = new ObjectsData(new ArrayList<>(), "none", new HashMap<>(), new HashMap<>());
		assertNotNull(data.getSelectedProperties());
	}

	@Test
	public void getSelectedSubProperties() {
		ObjectsData data = new ObjectsData(new ArrayList<>(), "none", new HashMap<>(), new HashMap<>());
		assertNotNull(data.getSelectedSubProperties());
	}

}
