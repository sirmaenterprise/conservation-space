package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DISABLE_STALE_DATA_CHECKS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Executor operation to import folders.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ImportDocumentExecutor.TARGET_NAME, order = 132)
public class ImportFolderExecutor extends BaseImportInstanceExecutor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "importFolderInstance";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationResponse execute(SchedulerContext context) {
		RuntimeConfiguration.enable(DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS);
		RuntimeConfiguration.enable(DISABLE_STALE_DATA_CHECKS);
		RuntimeConfiguration.enable(OVERRIDE_MODIFIER_INFO);
		try {
			return super.execute(context);
		} finally {
			RuntimeConfiguration.disable(DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS);
			RuntimeConfiguration.disable(DISABLE_STALE_DATA_CHECKS);
			RuntimeConfiguration.disable(OVERRIDE_MODIFIER_INFO);
		}
	}

	@Override
	protected void beforeSave(Instance instance, SchedulerContext context) {
		if (instance instanceof SectionInstance) {
			((SectionInstance) instance).setPurpose(SectionProperties.PURPOSE_FOLDER);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected Class getDefinitionClass(Class<?> instanceClass) {
		return GenericDefinition.class;
	}

}
