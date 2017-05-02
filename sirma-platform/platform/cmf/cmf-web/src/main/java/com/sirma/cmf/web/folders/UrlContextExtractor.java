package com.sirma.cmf.web.folders;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;

/**
 * This class will extract parameters from link location and based on them, populate require context instances.
 *
 * @author cdimitrov
 */
@ViewAccessScoped
public class UrlContextExtractor extends EntityAction implements Serializable {

	private static final long serialVersionUID = 7554368924598491365L;
	private static final String INSTANCE_ID = "instanceId";
	private static final String INSTANCE_TYPE = "type";

	/**
	 * Main point for populating the required context.
	 */
	public void extractAndPopulateFromUrl() {
		Map<String, String> requestedMap = getRequestedMap();
		String id = requestedMap.get(INSTANCE_ID);
		String type = requestedMap.get(INSTANCE_TYPE);
		if (StringUtils.isNotNullOrEmpty(id) && StringUtils.isNotNullOrEmpty(type)) {
			Instance instance = fetchInstance(id, type);
			storeRootInstanceInTheContext(instance);
		}
	}

	/**
	 * Populate the root and current instances into {@link DocumentContext}.
	 *
	 * @param instance
	 *            current instance
	 */
	private void storeRootInstanceInTheContext(Instance instance) {
		if (instance != null) {
			// retrieve root instance based on the given child, root instance
			// will be used for project initialization
			Instance rootInstance = InstanceUtil.getRootInstance(instance);
			if (rootInstance == null) {
				// assume that this instance is root
				rootInstance = instance;
			}
			getDocumentContext().setRootInstance(rootInstance);
			// current instance will be used for browser tree initialization
			getDocumentContext().setCurrentInstance(instance);
		}
	}

	/**
	 * Getter for current request map.
	 *
	 * @return current request map elements
	 */
	protected Map<String, String> getRequestedMap() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Map<String, String> requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
		return requestParameterMap;
	}

}
