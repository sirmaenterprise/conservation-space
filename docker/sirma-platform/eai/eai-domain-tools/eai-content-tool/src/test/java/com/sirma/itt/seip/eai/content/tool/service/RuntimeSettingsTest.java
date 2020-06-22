package com.sirma.itt.seip.eai.content.tool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.eai.content.tool.BaseTest;
import com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService;

public class RuntimeSettingsTest extends BaseTest {
	private File settingsFile;

	@Test
	public void testLoadModel() throws Exception {
		settingsFile.delete();
		settingsFile.getParentFile().mkdirs();
		try (FileOutputStream out = new FileOutputStream(settingsFile)) {
			IOUtils.copy(RuntimeSettingsTest.class.getResourceAsStream("runtime.config"), out);
		}
		RuntimeSettings runtimeSettings = newRuntimeSettings();
		assertEquals("value", runtimeSettings.get("key_stored"));
	}

	@Test
	public void testLoadModelCannotRead() throws Exception {
		settingsFile.delete();
		assertNull(newRuntimeSettings().get("key_stored"));
	}

	@Test
	public void testLoadModelInvalid() throws Exception {
		settingsFile.delete();
		boolean createNewFile = settingsFile.createNewFile();
		assertTrue(createNewFile);
		assertNull(newRuntimeSettings().get("key_stored"));
	}

	@Test
	public void testStoreModel() throws Exception {
		setRuntimeSettingInstance(newRuntimeSettings());
		assertFalse(settingsFile.exists());
		newRuntimeSettings().storeModel();
		assertTrue(settingsFile.exists());
	}

	@Before
	public void setUp() throws Exception {
		settingsFile = new File(new File(LocalFileService.INSTANCE.getInstanceDirectory(), ".settings"),
				"runtime.config");
		setRuntimeSettingInstance(null);
	}

	@Test(expected = NullPointerException.class)
	public void testPutNullKeyValue() throws Exception {
		newRuntimeSettings().put(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void testPutNullKey() throws Exception {
		newRuntimeSettings().put(null, "val");
	}

	@Test
	public void testPutAndReadValues() throws Exception {
		RuntimeSettings runtimeSettings = newRuntimeSettings();
		runtimeSettings.put("key", null);
		assertNull(runtimeSettings.get("key"));
		assertEquals("default", runtimeSettings.get("key", "default"));
		runtimeSettings.put("key", "val");
		assertEquals("val", runtimeSettings.get("key"));
	}

	@After
	public void clean() throws Exception {
		setRuntimeSettingInstance(null);
		clearDirectory(LocalFileService.INSTANCE.getInstanceDirectory());
	}

	private RuntimeSettings newRuntimeSettings() throws Exception {
		Constructor<RuntimeSettings> constructor = RuntimeSettings.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		RuntimeSettings runtimeSettings = constructor.newInstance();
		setRuntimeSettingInstance(runtimeSettings);
		return runtimeSettings;
	}

	private static void setRuntimeSettingInstance(RuntimeSettings instance) throws Exception {
		Field field = RuntimeSettings.class.getDeclaredField("INSTANCE");
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, instance);
	}

}
