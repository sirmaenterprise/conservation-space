package com.sirma.itt.seip.eai.content.tool;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import com.sirma.itt.seip.eai.content.tool.params.ParametersProvider;
import com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService;

import javafx.application.Application.Parameters;

public abstract class BaseTest {
	protected static File storage;

	@BeforeClass
	public static void setUpClass() throws Exception {
		storage = File.createTempFile("xlsx", "");
		storage.delete();
		new File(storage, "/").getParentFile().mkdirs();
		// init param for instance dir
		initParameters(Collections.emptyMap());
		LocalFileService.init(storage);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		clearDirectory(storage);
	}

	protected static void clearDirectory(File root) {
		File[] listFiles = root.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				LocalFileService.deleteFile(file);
			}
		}
		LocalFileService.deleteFile(root);
	}

	protected static void initParameters(Map<String, String> provided) {
		Parameters parameters = Mockito.mock(Parameters.class);
		Map<String, String> paramsNamed = new HashMap<>();
		paramsNamed.put(ParametersProvider.PARAM_CONTENT_URI, "emf:uri");
		paramsNamed.put(ParametersProvider.PARAM_API_URL, "http");
		paramsNamed.put(ParametersProvider.PARAM_AUTHORIZATION, "key");
		paramsNamed.putAll(provided);
		Mockito.when(parameters.getNamed()).thenReturn(paramsNamed);
		ParametersProvider.setParameters(parameters);
	}
}
