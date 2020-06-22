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
import com.sirma.itt.cmf.alfresco4.remote.ContentUploadContext;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.definition.DefintionAdapterServiceExtension;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;
import com.sirma.itt.seip.template.TemplateDefinition;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The definition adapter for cmf. It is facade for retrieving definitions from dms and working with them.
 *
 * @author borislav banchev
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionAlfresco4Service implements DefintionAdapterService, AlfrescoCommunicationConstants {

	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The extensions. */
	@Inject
	@ExtensionPoint(DefintionAdapterServiceExtension.TARGET_NAME)
	private Iterable<DefintionAdapterServiceExtension> extensions;

	/** The mapping. */
	private Map<Class, DefintionAdapterServiceExtension> mapping;

	/** The converter. */
	@Inject
	@Converter(name = ConverterConstants.GENERAL)
	private DMSTypeConverter converter;

	/** The uploader. */
	@Inject
	private AlfrescoUploader uploader;

	@Inject
	private AdaptersConfiguration adaptersConfiguration;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		mapping = PluginUtil.parseSupportedObjects(extensions, true);
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		DefintionAdapterServiceExtension extension = mapping.get(definitionClass);
		if (extension == null) {
			// we can throw an exception also
			return Collections.emptyList();
		}

		try {
			return searchDefinitionsInDms(extension.getSearchPath(definitionClass));
		} catch (DMSException e) {
			throw new RollbackedRuntimeException(e);
		}
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
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String callWebScript = restClient.request(uri, createMethod);
			if (callWebScript == null) {
				return Collections.emptyList();
			}
			JSONObject result = new JSONObject(callWebScript);
			JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
			// create proper size list when we know the size
			results = new ArrayList<>(nodes.length());

			String containerFilter = adaptersConfiguration.getDmsContainerId().get();

			for (int i = 0; i < nodes.length(); i++) {
				JSONObject nodeInfo = (JSONObject) nodes.get(i);
				String id = nodeInfo.getString(KEY_NODEREF);
				String containerId = null;
				if (nodeInfo.has(KEY_SITE_ID)) {
					containerId = nodeInfo.getString(KEY_SITE_ID);
				}
				// if the returned container does not match the requested we skip them
				if (containerId != null && !EqualsHelper.nullSafeEquals(containerId, containerFilter)) {
					continue;
				}

				String fileName = nodeInfo.getString(KEY_DMS_NAME);

				if (nodeInfo.has(KEY_PROPERTIES)) {
					JSONObject nodeProperties = nodeInfo.getJSONObject(KEY_PROPERTIES);
					Iterator<?> keys = nodeProperties.keys();
					Map<String, Serializable> converterdProps = new HashMap<>();
					while (keys.hasNext()) {
						String key = keys.next().toString();
						Pair<String, Serializable> toCMFProperty = converter.convertDMSToCMFProperty(key,
								nodeProperties.getString(key), DMSTypeConverter.PROPERTIES_MAPPING);
						if (toCMFProperty != null) {
							converterdProps.put(toCMFProperty.getFirst(), toCMFProperty.getSecond());
						}
					}
					results.add(new AlfrescoFileAndPropertiesDescriptor(id, containerId, fileName, converterdProps, restClient));
				} else {
					results.add(new AlfrescoFileDescriptor(id, containerId, fileName, restClient));
				}
			}
			return results;
		} catch (Exception e) {
			throw new DMSException("Definition retreivement failed @" + uri + " !", e);
		}
	}

	@Override
	public String uploadDefinition(Class<?> definitionClass, FileAndPropertiesDescriptor descriptor) {
		try {
			String parentNode = null;
			String folder = null;
			String aspect = null;
			Map<String, Serializable> props = null;
			if (TemplateDefinition.class == definitionClass) {
				props = converter.convertCMFtoDMSProperties(descriptor.getProperties(),
						DMSTypeConverter.PROPERTIES_MAPPING);
				// probably by old definition is better to find parent
				folder = "Template Definitions";
				aspect = "cmf:templateDefinition";
			}
			if (folder != null) {
				return uploadInternal(descriptor, props, folder, parentNode, aspect);
			}
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Definition upload failed for " + descriptor, e);
		}
		return null;
	}

	/**
	 * Upload internal a template to dms. The method relates either on folder and container id or parentNode to find
	 * where to upload the file
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
	private String uploadInternal(FileAndPropertiesDescriptor descriptor, Map<String, Serializable> props,
			String folder, String parentNode, String aspect)
					throws FileNotFoundException, DMSClientException, JSONException, DMSException {

		ContentUploadContext uploadContext = ContentUploadContext
				.create(ServiceURIRegistry.UPLOAD_SERVICE_URI, UploadMode.DIRECT)
					.setFilePart(uploader.getPartSource(descriptor))
					.setSiteId(adaptersConfiguration.getDmsContainerId().get())
					.setFolder(folder)
					.setParentNodeId(parentNode)
					.setContentType(KEY_DMS_CONTENT)
					.setProperties(props)
					.setAspectProperties(Collections.singleton(aspect))
					.setOverwrite(Boolean.TRUE)
					.setMajorVersion(Boolean.FALSE)
					.setVersionDescription("New definition uploaded by the system")
					.setThumbnailMode(ThumbnailGenerationMode.NONE.toString());

		String uploadFile = uploader.uploadFile(uploadContext);
		// upload the file now
		if (uploadFile != null) {
			JSONObject fileData = new JSONObject(uploadFile);
			return fileData.getString(KEY_NODEREF);
		}

		throw new DMSException("Template is not uploaded correctly!");
	}

}
