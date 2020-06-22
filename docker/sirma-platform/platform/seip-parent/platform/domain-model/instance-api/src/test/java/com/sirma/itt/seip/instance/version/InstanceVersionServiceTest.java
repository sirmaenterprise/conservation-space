package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link InstanceVersionService}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class InstanceVersionServiceTest {

	@Test(expected = IllegalArgumentException.class)
	public void isVersion_nullId() {
		InstanceVersionService.isVersion(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void isVersion_emptyId() {
		InstanceVersionService.isVersion("");
	}

	@Test
	public void isVersion_false() {
		InstanceVersionService.isVersion("emf:instance-id");
	}

	@Test
	public void isVersion_true() {
		InstanceVersionService.isVersion("emf:instance-id-v1.3");
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildVersionId_nullInstance() {
		InstanceVersionService.buildVersionId(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildVersionId_nullInstanceId() {
		InstanceVersionService.buildVersionId(new EmfInstance());
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildVersionId_nullVersion() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		InstanceVersionService.buildVersionId(instance);
	}

	@Test
	public void buildVersionId_versionInstancePassed_instanceIdReturned() {
		Instance version = new EmfInstance();
		version.setId("instance-id-v1.2");
		// the difference is only for test purpose, this case is generally invalid
		version.add(VERSION, "1.6");
		Serializable id = InstanceVersionService.buildVersionId(version);
		assertEquals("instance-id-v1.2", id);
	}

	@Test
	public void buildVersionId_instancePassed_buildSuccessful() {
		Instance version = new EmfInstance();
		version.setId("instance-id");
		version.add(VERSION, "1.8");
		Serializable id = InstanceVersionService.buildVersionId(version);
		assertEquals("instance-id-v1.8", id);
	}

	@Test
	public void getIdFromVersionId_notVersionIdPassed_inputReturned() {
		Serializable id = InstanceVersionService.getIdFromVersionId("instance-id");
		assertEquals("instance-id", id);
	}

	@Test
	public void getIdFromVersionId_versionIdPassed_idWithoutSuffix() {
		Serializable id = InstanceVersionService.getIdFromVersionId("instance-id-v1.101");
		assertEquals("instance-id", id);
	}

}
