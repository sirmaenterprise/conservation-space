package com.sirma.itt.seip.instance.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Extension to provide instance functions to JS. The instance will be accessible via <code>instance</code> binding.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 10)
public class InstanceScriptsProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceScriptsProvider.class);

	/** Default script name */
	private static final String INSTANCE_ACTIONS_JS = "instance-actions.js";
	@Inject
	private TypeConverter typeConverter;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceService instanceService;

	@Inject
	private HeadersService headersService;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("instance", this);
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), INSTANCE_ACTIONS_JS);
	}

	/**
	 * Gets the definition for the given id
	 *
	 * @param definitionId
	 *            the definition id
	 * @return the definition for the given id
	 */
	public DefinitionModel getDefinition(String definitionId) {
		if (StringUtils.isBlank(definitionId)) {
			LOGGER.warn("Missing required arguments: definitionId[{}]", definitionId);
			return null;
		}
		return definitionService.find(definitionId);
	}

	/**
	 * Gets the definition for type.
	 *
	 * @param type
	 *            the type
	 * @param definitionId
	 *            the definition id
	 * @return the definition for type
	 */
	public DefinitionModel getDefinitionForType(String type, String definitionId) {
		// this method is left for backward compatibility in old scripts
		return getDefinition(definitionId);
	}

	/**
	 * Creates instance of the provided type using the definition identified by the definition id. The instance is
	 * created using the given operation.
	 *
	 * @param type
	 *            the type of instance to create
	 * @param definitionId
	 *            the definition id to use for the instance initialization
	 * @param operation
	 *            the operation used to log the creation
	 * @return the script node representing the created instance or <code>null</code> if something was missing or the
	 *         instance could not be created
	 */
	public ScriptNode create(String type, String definitionId, String operation) {
		return createWithParent(type, definitionId, operation, null);
	}

	/**
	 * Creates instance of the provided type using the definition identified by the definition id. The instance is
	 * created using the given operation. The created instance is created as a parent to the given parent script node.
	 *
	 * @param type
	 *            the type of instance to create
	 * @param definitionId
	 *            the definition id to use for the instance initialization
	 * @param operation
	 *            the operation used to log the creation
	 * @param parentNode
	 *            the parent node to use
	 * @return the script node representing the created instance or <code>null</code> if something was missing or the
	 *         instance could not be created
	 */
	public ScriptNode createWithParent(String type, String definitionId, String operation, ScriptNode parentNode) {
		DefinitionModel model = getDefinitionForType(type, definitionId);
		if (model == null) {
			LOGGER.warn("Could not find definition [{}] for type [{}]", definitionId, type);
			return null;
		}
		Instance parent = null;
		if (parentNode != null) {
			parent = parentNode.getTarget();
		}

		Instance instance = instanceService.createInstance(model, parent, new Operation(operation));
		return typeConverter.convert(ScriptNode.class, instance);
	}

	/**
	 * Generate instance headers for the given node if non null.
	 *
	 * @param node
	 *            the node
	 */
	public void generateHeaders(ScriptNode node) {
		if (node == null) {
			return;
		}
		headersService.generateInstanceHeaders(node.getTarget(), false);
	}

	/**
	 * Gets the instance header for the given node.
	 *
	 * @param node
	 *            the node
	 * @param header
	 *            the header
	 * @return the header or <code>null</code>
	 */
	public String getHeader(ScriptNode node, String header) {
		if (node == null || StringUtils.isBlank(header)) {
			return null;
		}
		return headersService.generateInstanceHeader(node.getTarget(), header);
	}

	/**
	 * Gets the instance compact header.
	 *
	 * @param node
	 *            the node
	 * @return the compact header
	 */
	public String getCompactHeader(ScriptNode node) {
		return getHeader(node, DefaultProperties.HEADER_COMPACT);
	}

	/**
	 * Gets the instance bread crumb header.
	 *
	 * @param node
	 *            the node
	 * @return the breadcrumb header
	 */
	public String getBreadcrumbHeader(ScriptNode node) {
		return getHeader(node, DefaultProperties.HEADER_BREADCRUMB);
	}

	/**
	 * Gets the instance default header.
	 *
	 * @param node
	 *            the node
	 * @return the header
	 */
	public String getHeader(ScriptNode node) {
		return getHeader(node, DefaultProperties.HEADER_DEFAULT);
	}

	/**
	 * Gets the instance tooltip.
	 *
	 * @param node
	 *            the node
	 * @return the tooltip
	 */
	public String getTooltip(ScriptNode node) {
		return getHeader(node, DefaultProperties.HEADER_TOOLTIP);
	}

	/**
	 * Loads a specific instance given the ID.
	 *
	 * @param id
	 *            the id of the instance we want to load.
	 *
	 * @return the script node of the instance or null if nothing was found.
	 */
	public ScriptNode load(String id) {
		return typeConverter.convert(ScriptNode.class, instanceService.loadByDbId(id));
	}

	/**
	 * Check if the instance has the same type as provided
	 *
	 * @param node
	 *            the node to check
	 * @param type
	 *            the type to check
	 * @return true if type is the same, false otherwise or if information is missing.
	 */
	public boolean isType(ScriptNode node, String type) {
		InstanceType instanceType = node.getTarget().type();
		return instanceType != null ? instanceType.is(type) : false;
	}

}
