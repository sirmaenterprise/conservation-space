/**
 *
 */
package com.sirma.itt.seip.tenant.wizard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.util.file.ArchiveUtil;

/**
 * Builder class for {@link TenantInitializationModel}s.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TenantInitializationModelBuilder {

	@Inject
	private TempFileProvider fileProvider;

	/**
	 * Gets mew model builder.
	 *
	 * @return the builder
	 */
	public Builder getBuilder() {
		return new Builder();
	}

	/**
	 * Builder class that produces {@link TenantInitializationModel}
	 *
	 * @author BBonev
	 */
	public class Builder {
		private JSONObject model;
		private Map<String, List<Pair<String, InputStream>>> modelInput = new LinkedHashMap<>();

		/**
		 * Sets the model.
		 *
		 * @param model
		 *            the model
		 * @return the builder
		 */
		public Builder setModel(JSONObject model) {
			this.model = Objects.requireNonNull(model, "Could not build with null model");
			return this;
		}

		/**
		 * Sets model as string
		 *
		 * @param model
		 *            the model
		 * @return the builder
		 */
		public Builder setModel(String model) {
			String stringModel = Objects.requireNonNull(model, "Could not build with null model");
			this.model = Objects.requireNonNull(JsonUtil.createObjectFromString(stringModel), "Invalid input model");
			return this;
		}

		/**
		 * Append a file to the current properties model (should already exists). The key for added property is
		 * extracted from the pattern of provided key <code>stepId_attachment_propertyId</code>
		 *
		 * @param key
		 *            the key is the form key
		 * @param fileName
		 *            the file name
		 * @param body
		 *            the stream to read and store as a file
		 * @return the builder
		 */
		public Builder appendModelFile(String key, String fileName, InputStream body) {
			if (key != null && body != null) {
				List<Pair<String, InputStream>> models = modelInput.computeIfAbsent(key, k -> new ArrayList<>());
				models.add(new Pair<>(fileName, body));
			}
			return this;
		}

		/**
		 * Builds and returns {@link TenantInitializationModel} instance
		 *
		 * @return the tenant initialization model
		 */
		public TenantInitializationModel build() {
			TenantInitializationModel instance = new TenantInitializationModel();

			instance.fromJSONObject(model);

			if (!modelInput.isEmpty()) {
				for (Entry<String, List<Pair<String, InputStream>>> entry : modelInput.entrySet()) {
					appendModelFileToInstance(instance, entry.getKey(), entry.getValue());
				}
			}

			return instance;
		}

		private void appendModelFileToInstance(TenantInitializationModel instance, String key,
				List<Pair<String, InputStream>> bodies) {
			try {
				String fileName = key;
				String fileExtension = "tmp";
				String[] split = key.split("_");
				if (split.length < 3 || !"attachment".equals(split[1])) {
					throw new TenantCreationException(
							"Attachment id should be in format stepId_attachment_propertyId!");
				}
				String stepId = split[0];
				for (Pair<String, InputStream> body : bodies) {
					if (StringUtils.isNotBlank(body.getFirst())) {
						// save the file with the original name
						fileName = body.getFirst();
						fileExtension = fileName.substring(fileName.lastIndexOf("."));
					}
					File tempFile = copyToTempFile(body, fileName, fileExtension);
					if (tempFile != null && ".zip".equals(fileExtension)) {
						instance.get(stepId).addModel(unzipInTempDir(tempFile.getName(), tempFile));
					} else if (tempFile != null) {
						instance.get(stepId).addModel(tempFile);
					}
				}

			} catch (Exception e) {
				throw new TenantCreationException("Attachment could not be extracted!", e);
			}
		}

		private File unzipInTempDir(String fileName, File file) {
			File tempDir = fileProvider.createTempDir(fileName + "_" + Long.toString(System.currentTimeMillis()));
			ArchiveUtil.unZip(file, tempDir);
			return tempDir;
		}

		private File copyToTempFile(Pair<String, InputStream> body, String fileName, String fileExtension)
				throws IOException {
			File tempFile = fileProvider.createTempFile(fileName, fileExtension);
			try (InputStream input = body.getSecond(); OutputStream out = new FileOutputStream(tempFile)) {
				int copied = IOUtils.copy(input, out);
				if (copied < 1) {
					return null;
				}
			}
			return tempFile;
		}
	}

}
