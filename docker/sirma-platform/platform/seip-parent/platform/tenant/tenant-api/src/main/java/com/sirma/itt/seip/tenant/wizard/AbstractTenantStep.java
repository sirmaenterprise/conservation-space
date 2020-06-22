package com.sirma.itt.seip.tenant.wizard;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The AbstractTenantCreationStep is the base step for tenant creation. Added support for external managed model
 *
 * @author bbanchev
 */
public abstract class AbstractTenantStep implements TenantStep {

	@Override
	@SuppressWarnings("resource")
	public TenantStepData provide() {
		InputStream model = provideExternalModel();
		try {
			if (model == null) {
				model = provideInternalModel();
			}
			if (model == null) {
				return TenantStepData.createEmpty(getIdentifier());
			}
			return TenantStepData.create(getIdentifier(), readStream(model));
		} catch (JSONException | IOException e) {
			throw new TenantCreationException("Failed to obtain model for: " + getIdentifier(), e);
		}
	}

	/**
	 * Provide external model - by default return null. Should be override by the specific step impl
	 *
	 * @return the input stream of external model
	 */
	@SuppressWarnings("static-method")
	protected InputStream provideExternalModel() {
		return null;
	}

	/**
	 * Provide internal model is the model contained in current classpath. File is looked up by the
	 * {@link #getIdentifier()} in the current class package.
	 *
	 * @return the input stream
	 */
	protected InputStream provideInternalModel() {
		return getClass().getResourceAsStream(getIdentifier() + ".json");
	}

	/**
	 * Reads a stream as json string and initialize new intsance.
	 *
	 * @param data
	 *            is stream with data
	 * @return the new jsonObject
	 * @throws JSONException
	 *             on json parse error
	 * @throws IOException
	 *             on io error
	 */
	@SuppressWarnings("static-method")
	protected JSONObject readStream(InputStream data) throws JSONException, IOException {
		if (data == null) {
			return null;
		}
		try (InputStream input = data) {
			return new JSONObject(IOUtils.toString(input, StandardCharsets.UTF_8));
		}
	}
}