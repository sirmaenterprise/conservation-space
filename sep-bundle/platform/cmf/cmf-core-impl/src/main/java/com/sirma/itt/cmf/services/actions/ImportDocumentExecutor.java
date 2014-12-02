package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.DISABLE_STALE_DATA_CHECKS;
import static com.sirma.itt.emf.configuration.RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Operation executor to handle the import of document instance.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ImportDocumentExecutor.TARGET_NAME, order = 135)
public class ImportDocumentExecutor extends BaseImportInstanceExecutor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "importDocumentInstance";
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected Class getDefinitionClass(Class<?> instanceClass) {
		return DocumentDefinitionRefProxy.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DefinitionModel getDefinition(SchedulerContext context, Class<?> javaClass) {
		DefinitionModel definition = super.getDefinition(context, javaClass);
		if (definition instanceof DocumentDefinitionTemplate) {
			return new DocumentDefinitionRefProxy((DocumentDefinitionTemplate) definition);
		}
		return definition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void beforeSave(Instance instance, SchedulerContext context) {
		if (instance instanceof DocumentInstance) {
			instance.getProperties().put(DocumentProperties.ATTACHMENT_LOCATION,
					((DocumentInstance) instance).getDmsId());
		}
	}

}
