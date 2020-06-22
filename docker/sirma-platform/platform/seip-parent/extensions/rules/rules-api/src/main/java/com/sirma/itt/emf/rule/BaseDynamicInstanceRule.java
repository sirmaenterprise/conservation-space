package com.sirma.itt.emf.rule;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.Applicable;
import com.sirma.itt.seip.AsyncSupportable;
import com.sirma.itt.seip.DefinitionSupportable;
import com.sirma.itt.seip.OperationSupportable;
import com.sirma.itt.seip.context.Configurable;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Base abstract implementation of {@link DynamicInstanceRule}. Note the class itself does not implement that particular
 * interface.
 *
 * @author BBonev
 */
public abstract class BaseDynamicInstanceRule implements Configurable, DynamicSupportable, Applicable, AsyncSupportable,
		OperationSupportable<String>, DefinitionSupportable<String>, SupportablePlugin<String> {

	private Set<String> operations;
	private List<String> supportedTypes;
	private Context<String, Object> config;
	private boolean asyncMode;
	private Collection<String> supportedDefinitions;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (configuration == null) {
			return false;
		}
		config = configuration;
		return true;
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		Instance currentInstance = getProcessedInstance(context);
		if (currentInstance == null) {
			return false;
		}

		boolean objects = isEmpty(getSupportedObjects()) || getSupportedObjects().contains(currentInstance.type().getCategory());
		boolean isOperations = isEmpty(getSupportedOperations())
				|| getSupportedOperations().contains(context.getIfSameType(RuleContext.OPERATION, String.class));
		boolean definitions = isEmpty(getSupportedDefinitions())
				|| getSupportedDefinitions().contains(currentInstance.getIdentifier());
		return objects && isOperations && definitions;
	}

	/**
	 * Gets the processed instance.
	 *
	 * @param context
	 *            the context
	 * @return the processed instance
	 */
	protected Instance getProcessedInstance(Context<String, Object> context) {
		return context.getIfSameType(RuleContext.PROCESSING_INSTANCE, Instance.class);
	}

	/**
	 * Gets the previous version.
	 *
	 * @param context
	 *            the context
	 * @return the processed instance
	 */
	protected Instance getPreviousVersion(Context<String, Object> context) {
		return context.getIfSameType(RuleContext.PREVIOUS_VERSION, Instance.class);
	}

	@Override
	public boolean isAsyncSupported() {
		return asyncMode;
	}

	@Override
	public void setIsAsyncSupported(boolean mode) {
		asyncMode = mode;
	}

	@Override
	public List<String> getSupportedObjects() {
		return supportedTypes;
	}

	@Override
	public Set<String> getSupportedOperations() {
		return operations;
	}

	@Override
	public void setSupportedOperations(Collection<String> operations) {
		if (operations == null) {
			this.operations = Collections.emptySet();
		} else {
			this.operations = new LinkedHashSet<>(operations);
		}
	}

	@Override
	public void setSupportedTypes(Collection<String> supportedTypes) {
		if (supportedTypes == null) {
			this.supportedTypes = Collections.emptyList();
		} else {
			this.supportedTypes = new ArrayList<>(supportedTypes);
		}
	}

	@Override
	public void setSupportedDefinitions(Collection<String> definitions) {
		supportedDefinitions = definitions;
		if (definitions == null) {
			supportedDefinitions = Collections.emptyList();
		} else {
			supportedDefinitions = new LinkedHashSet<>(definitions);
		}
	}

	/**
	 * Getter method for supportedDefinitions.
	 *
	 * @return the supportedDefinitions
	 */
	@Override
	public Collection<String> getSupportedDefinitions() {
		return supportedDefinitions;
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	protected Context<String, Object> getConfiguration() {
		return config;
	}

}
