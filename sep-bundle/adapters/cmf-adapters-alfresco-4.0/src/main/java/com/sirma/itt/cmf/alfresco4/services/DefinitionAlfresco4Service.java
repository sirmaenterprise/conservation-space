/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileAndPropertiesDescriptor;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;

/**
 * The definition adapter for cmf. It is facade for retrieving definitions from dms and working with
 * them.
 *
 * @author borislav banchev
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionAlfresco4Service implements DMSDefintionAdapterService,
		AlfrescoCommunicationConstants {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4550889011642948909L;

	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The extensions. */
	@Inject
	@ExtensionPoint(DMSDefintionAdapterServiceExtension.TARGET_NAME)
	private Iterable<DMSDefintionAdapterServiceExtension> extensions;

	/** The mapping. */
	private Map<Class<?>, DMSDefintionAdapterServiceExtension> mapping;

	/** The converter. */
	@Inject
	@Converter(name = ConverterConstants.GENERAL)
	private DMSTypeConverter converter;

	/** The uploader. */
	@Inject
	private AlfrescoUploader uploader;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		mapping = PluginUtil.parseSupportedObjects(extensions, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) throws DMSException {
		DMSDefintionAdapterServiceExtension extension = mapping.get(definitionClass);
		if (extension == null) {
			// we can throw an exception also
			return Collections.emptyList();
		}

		return searchDefinitionsInDms(extension.getSearchPath(definitionClass));
	}

	/**
	 * Search in dms for definitions based on the provided url and returns list of dms descriptors.
	 *
	 * @param uri
	 *            the uri to use. see dms for more informations about available locations
	 * @return the list of defintions
	 * @throws DMSException
	 *             the dMS exception
	 */
	protected List<FileDescriptor> searchDefinitionsInDms(String uri) throws DMSException {
		JSONObject request = new JSONObject();
		List<FileDescriptor> results = Collections.emptyList();
		try {
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String callWebScript = restClient.request(uri, createMethod);
			if (callWebScript == null) {
				return Collections.emptyList();
			}
			JSONObject result = new JSONObject(callWebScript);
			JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
			// create proper size list when we know the size
			results = new ArrayList<FileDescriptor>(nodes.length());

			for (int i = 0; i < nodes.length(); i++) {
				JSONObject nodeInfo = (JSONObject) nodes.get(i);
				String id = nodeInfo.getString(KEY_NODEREF);
				String containerId = null;
				if (nodeInfo.has(KEY_SITE_ID)) {
					containerId = nodeInfo.getString(KEY_SITE_ID);
				}
				if (nodeInfo.has(KEY_PROPERTIES)) {
					JSONObject nodeProperties = nodeInfo.getJSONObject(KEY_PROPERTIES);
					Iterator<?> keys = nodeProperties.keys();
					Map<String, Serializable> converterdProps = new HashMap<String, Serializable>();
					while (keys.hasNext()) {
						String key = keys.next().toString();
						Pair<String, Serializable> toCMFProperty = converter
								.convertDMSToCMFProperty(key, nodeProperties.getString(key),
										DMSTypeConverter.PROPERTIES_MAPPING);
						if (toCMFProperty != null) {
							converterdProps
									.put(toCMFProperty.getFirst(), toCMFProperty.getSecond());
						}
					}
					results.add(new AlfrescoFileAndPropertiesDescriptor(id, containerId,
							converterdProps, restClient));
				} else {
					results.add(new AlfrescoFileDescriptor(id, containerId, restClient));
				}
			}
			return results;
		} catch (Exception e) {
			throw new DMSException("Definition retreivement failed @" + uri + " !", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String uploadDefinition(Class<?> definitionClass,
			FileAndPropertiesDescriptor descriptor) throws DMSException {
		try {
			String parentNode = null;
			String folder = null;
			String aspect = null;
			Map<String, Serializable> props = null;
			if (TemplateDefinition.class == definitionClass) {
				props = converter.convertCMFtoDMSProperties(descriptor.getProperties(),
						DMSTypeConverter.PROPERTIES_MAPPING);
				// TODO probably by old definition is better to find parent
				// List<DMSFileDescriptor> definitions = getDefinitions(definitionClass);
				// if (definitions != null && definitions.size() > 0) {
				// parentNode = null;
				// } else {
				folder = "Template Definitions";
				aspect = "cmf:templateDefinition";
				// }
			}
			if ((parentNode != null) || (folder != null)) {
				return uploadInternal(descriptor, props, folder, parentNode, aspect);
			}
		} catch (DMSException e) {
			throw e;
		} catch (Exception e) {
			throw new DMSException("Definition upload failed for " + descriptor, e);
		}
		return null;
	}

	/**
	 * Upload internal a template to dms. The method relates either on folder and container id or
	 * parentNode to find where to upload the file
	 *
	 * @param descriptor
	 *            the descriptor to use for the file
	 * @param props
	 *            the props to set on upload
	 * @param folder
	 *            the folder is the parent
	 * @param parentNode
	 *            the parent node
	 * @param aspect
	 *            the aspect for the specified definition
	 * @return the dms id of uploaded definition
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws DMSException
	 *             the dMS exception
	 */
	private String uploadInternal(FileAndPropertiesDescriptor descriptor,
			Map<String, Serializable> props, String folder, String parentNode, String aspect)
			throws FileNotFoundException, DMSClientException, JSONException, DMSException {
		String uploadFile = uploader.uploadFile(ServiceURIRegistry.UPLOAD_SERVICE_URI,
				uploader.getPartSource(descriptor), descriptor.getContainerId(), folder,
				parentNode, "cm:content", props, Collections.singleton(aspect), Boolean.TRUE,
				Boolean.FALSE, "New definition uploaded by the system",
				ThumbnailGenerationMode.ASYNCH.toString());
		// upload the file now
		if (uploadFile != null) {
			JSONObject fileData = new JSONObject(uploadFile);
			return fileData.getString(KEY_NODEREF);
		}
		throw new DMSException("Template is not uploaded correctly!");
	}
}
