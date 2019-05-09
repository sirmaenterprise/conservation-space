package com.sirma.sep.model.management;

import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.response.ModelUpdateResponse;

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

	/**
	 * Returns the actual runtime model instance. Any changes to this instance will affect the system model
	 *
	 * @return the actual model
	 */
	Models getModels();

	/**
	 * Issue a request to update the model with the given model changes. The request should include the version on which
	 * the changes are based upon. <br>
	 *     The method will perform the following steps:
	 *     <ol>
	 *         <li>Validate the input changes</li>
	 *         <li>Apply the changes to a copy of the runtime model</li>
	 *         <li>Validate the model after applying the changes. No business validation will be done as the model state
	 *         is not expected to be valid</li>
	 *         <li>Store changes is persistent storage</li>
	 *         <li>Apply the changes to the common runtime model</li>
	 *         <li>Increase the model version</li>
	 *         <li>Return all changes that occurred after the model version passed in the request. This should include
	 *         the changes in the current request as well</li>
	 *     </ol>
	 * <br>
	 * The method does not deploy any of the changes. In order to deploy the model changes call
	 * {@link #validateDeploymentCandidates()} and {@link #deployChanges(ModelDeploymentRequest)}
	 *
	 * @param updateRequest the request object that carry the changes to be saved and applied
	 * @throws StaleModelException if the user model state is not compatible with the current model state. The user
	 * should fetch it's model again fix his/her collisions and try again.
	 * @throws com.sirma.sep.model.management.operation.ChangeSetValidationFailed if any of the changes fails its validation
	 */
	ModelUpdateResponse updateModel(ModelUpdateRequest updateRequest);

	/**
	 * Get changes stored and successfully applied to the runtime model after the given model version
	 * @param modelVersion the last known model version
	 * @return a response containing the applied changes and the current version
	 */
	ModelUpdateResponse getChangesSince(Long modelVersion);

	/**
	 * Perform a validation for all deployment candidates and return a report with the result. Deployment candidates
	 * are modified root level nodes with non deployed changes such as {@link ModelClass ModelClasses},
	 * {@link ModelDefinition ModelDefinitions} and {@link ModelProperty ModelProperties}.
	 *
	 * @return report from the deployment validation
	 */
	DeploymentValidationReport validateDeploymentCandidates();

	/**
	 * Perform a validation for the deployment candidates just like {@link #validateDeploymentCandidates()} and if everything is valid then
	 * deploys all changes for the given list of root model nodes (classes and definitions)
	 *
	 * @param deploymentRequest request containing the desired nodes to deploy
	 * @return report from the deployment validation
	 */
	DeploymentValidationReport deployChanges(ModelDeploymentRequest deploymentRequest);
}
