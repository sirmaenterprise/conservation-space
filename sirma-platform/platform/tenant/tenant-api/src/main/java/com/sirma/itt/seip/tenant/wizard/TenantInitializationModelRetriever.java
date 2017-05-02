package com.sirma.itt.seip.tenant.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Implementation responsible for parsing the manifest file and construction {@link TenantInitializationExternalModel}
 * objects from it.
 *
 * @author nvelkov
 */
public class TenantInitializationModelRetriever {

	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "tenant.management.models.manifest", type = String.class, defaultValue = "manifest.json", sensitive = true, label = "Path to the models manifest json file.", system = true)
	private ConfigurationProperty<String> manifestPath;

	/**
	 * Parse the json file and convert it's entries to {@link TenantInitializationExternalModel} objects.
	 *
	 * @throws TenantCreationException
	 *             if the manifest file is not found or is malformed
	 * @return a list of the {@link TenantInitializationExternalModel} objects
	 * @throws IOException
	 */
	public List<TenantInitializationExternalModel> getModels() {
		try (JsonReader reader = Json.createReader(getPathInputStream(manifestPath.get()))) {
			JsonArray array = reader.readArray();
			List<TenantInitializationExternalModel> models = new ArrayList<>();
			for (JsonValue element : array) {
				TenantInitializationExternalModel model = new TenantInitializationExternalModel();
				JsonObject elementObject = (JsonObject) element;
				model.setId(elementObject.getString("id"));
				if (elementObject.containsKey("definitions_path")) {
					model.setDefinitionsPath(elementObject.getString("definitions_path"));
				}
				if (elementObject.containsKey("semantic_path")) {
					model.setSemanticPath(elementObject.getString("semantic_path"));
				}
				model.setLabel(elementObject.getString("label"));

				models.add(model);
			}
			return models;
		} catch (JsonException e) {
			throw new TenantCreationException("Malformed manifest file", e);
		}
	}

	/**
	 * Try to get the input stream from the given path. The path may point to a local file, external file (e.g. via the
	 * http protocol) or a file in the deployed application.
	 *
	 * @param path
	 *            path to the file.
	 * @return the input stream
	 */
	public InputStream getPathInputStream(String path) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
		if (inputStream != null) {
			return inputStream;
		}
		try {
			File file = new File(path);
			if (file.exists() && !file.isDirectory()) {
				return new FileInputStream(file);
			}
			inputStream = new URL(path).openStream();
			if (inputStream != null) {
				return inputStream;
			}
		} catch (IOException e) {
			throw new TenantCreationException("File couldn't be loaded", e);
		}
		return inputStream;
	}

	/**
	 * Retrieve a {@link TenantInitializationExternalModel} based on the provided modelId.
	 *
	 * @param modelId
	 *            the model id
	 * @return the {@link TenantInitializationExternalModel}
	 */
	public TenantInitializationExternalModel getModel(String modelId) {
		return getModels().stream().filter(model -> modelId.equals(model.getId())).findFirst().get();
	}
}
