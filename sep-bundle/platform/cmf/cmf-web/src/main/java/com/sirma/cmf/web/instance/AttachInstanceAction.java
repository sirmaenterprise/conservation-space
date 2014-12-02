package com.sirma.cmf.web.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Base implementation of attach operation.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class AttachInstanceAction {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String SELECTED_ITEMS = "selectedItems";
	private static final String CURRENT_INSTANCE_TYPE = "currentInstanceType";
	private static final String CURRENT_INSTANCE_ID = "currentInstanceId";

	/**
	 * Attach documents.
	 * 
	 * @param data
	 *            the data
	 * @param service
	 *            base rest service
	 * @param instanceService
	 *            the instance service
	 * @param operation
	 *            the operation
	 * @return the response
	 */
	public Response attachDocuments(String data, EmfRestService service,
			InstanceService<Instance, DefinitionModel> instanceService, String operation) {
		return attach(data, service, instanceService, operation, true);
	}

	/**
	 * Attach objects.
	 * 
	 * @param data
	 *            the data
	 * @param service
	 *            base rest service
	 * @param instanceService
	 *            the instance service
	 * @param operation
	 *            the operation
	 * @return the response
	 */
	public Response attachObjects(String data, EmfRestService service,
			InstanceService<Instance, DefinitionModel> instanceService, String operation) {
		return attach(data, service, instanceService, operation, false);
	}

	/**
	 * Attach operation.
	 * 
	 * @param data
	 *            Request data in format <code>
	 * 	{
	 * 		'currentInstanceId' : 'instanceid',
	 * 		'currentInstanceType': 'instancetype',
	 * 		'selectedItems' : {
	 * 			dbId: 'instanceid',
	 * 			type: 'instancetype'
	 * 		}
	 * 	}</code>
	 * @param service
	 *            Base rest service
	 * @param instanceService
	 *            The instance service
	 * @param operationType
	 *            The operation type
	 * @param saveItems
	 *            If passed items should be saved one by one after attach
	 * @return the response
	 */
	public Response attach(String data, EmfRestService service,
			InstanceService<Instance, DefinitionModel> instanceService, String operationType,
			boolean saveItems) {
		if (StringUtils.isNullOrEmpty(data)) {
			log.debug("CMFWeb: Request is missing required data for attach operation!");
			return service.buildResponse(Response.Status.BAD_REQUEST,
					"Request is missing required data for attach operation!");
		}
		try {
			JSONObject request = new JSONObject(data);
			String currentInstanceId = request.getString(CURRENT_INSTANCE_ID);
			String currentInstanceType = request.getString(CURRENT_INSTANCE_TYPE);
			JSONObject selectedItems = new JSONObject(request.getString(SELECTED_ITEMS));
			List<InstanceReference> items = new ArrayList<>();

			for (Iterator<String> iterator = selectedItems.keys(); iterator.hasNext();) {
				String identifier = iterator.next();
				JSONObject document = (JSONObject) selectedItems.get(identifier);
				String dbId = JsonUtil.getStringValue(document, "dbId");
				String instancetype = JsonUtil.getStringValue(document, "type");
				InstanceReference reference = service.getTypeConverter().convert(
						InstanceReference.class, instancetype);
				if (reference != null) {
					reference.setIdentifier(dbId);
					items.add(reference);
				}
			}

			if (!items.isEmpty()) {
				Instance sectionInstance = service.fetchInstance(currentInstanceId,
						currentInstanceType);
				if (sectionInstance != null) {
					// perform attach
					Collection<Instance> values = service.loadInstances(items);
					Operation operation = new Operation(operationType);
					instanceService.attach(sectionInstance, operation,
							values.toArray(new Instance[values.size()]));
					// save parent (case) to update timestamp
					RuntimeConfiguration.setConfiguration(
							RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
					Instance parentInstance = InstanceUtil.getParent(CaseInstance.class,
							sectionInstance);
					if (parentInstance != null) {
						instanceService.save(parentInstance, operation);
					}

					if (saveItems) {
						for (Instance instance : values) {
							instanceService.save(instance, operation);
						}
					}
				} else {
					log.debug("CMFWeb: Can't load target section to attach instance.");
					return service.buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
							"Can't load target section to attach instance.");
				}
			}
		} catch (JSONException jsone) {
			log.warn("CMFWeb: An error happen during parsing request data.", jsone);
			return service.buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
					"An error happen during parsing request data.");
		} catch (Exception e) {
			log.warn("CMFWeb: Failed attach operation", e);
			return service.buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
					"Failed attach operation.");
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}

		return Response.ok().build();
	}

	/**
	 * Reference to instance is separated for testability.
	 * 
	 * @param instanceReference
	 *            the instance reference
	 * @return the instance
	 */
	protected Instance referenceToInstance(InstanceReference instanceReference) {
		return instanceReference.toInstance();
	}

}
