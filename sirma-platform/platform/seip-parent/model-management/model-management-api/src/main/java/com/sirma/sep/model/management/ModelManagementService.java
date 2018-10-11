package com.sirma.sep.model.management;

import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.response.ModelResponse;

import java.util.List;

/**
 * Service for managing the models and their hierarchy in the system.
 *
 * @author Mihail Rakov
 */
public interface ModelManagementService {

	/**
	 * Retrieves the current models hierarchy between semantic class models and definition models.
	 *
	 * @return the hierarchy
	 */
	List<ModelHierarchyClass> getModelHierarchy();

	/**
	 * Retrieves the models meta information which is used to specify the supported model attributes, default values and their validation.
	 *
	 * @return models meta information
	 * @see com.sirma.sep.model.management.meta.ModelMetaInfo
	 * @see ModelsMetaInfo
	 */
	ModelsMetaInfo getMetaInfo();

	/**
	 * Retrieves all {@link ModelProperty} related to the available semantic and definition models.
	 *
	 * @return list of all {@link ModelProperty}
	 */
	List<ModelProperty> getProperties();

	/**
	 * Selects the model hierarchy for the provided identifier.
	 *
	 * @param id the model identifier for models selection. May be semantic or definition one.
	 * @return model response corresponding to the select identifier. If for the identifier no models are available then the response will
	 * be empty.
	 */
	ModelResponse getModel(String id);
}
