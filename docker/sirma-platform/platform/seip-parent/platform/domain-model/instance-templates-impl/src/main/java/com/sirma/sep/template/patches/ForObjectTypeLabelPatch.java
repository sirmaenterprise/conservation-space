package com.sirma.sep.template.patches;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.observers.TemplateInstanceSaveObserver;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Finds all template instances and initializes their 'forObjectTypeProperty' by reusing the same login implemented in
 * {@link TemplateInstanceSaveObserver}.
 *
 * @author Adrian Mitev
 */
public class ForObjectTypeLabelPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DomainInstanceService domainInstanceService;
	private TransactionSupport transactionSupport;
	private SearchService searchService;
	private TemplateInstanceSaveObserver instanceSaveObserver;

	@Override
	public void setUp() throws SetupException {
		domainInstanceService = CDI.instantiateBean(DomainInstanceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		instanceSaveObserver = CDI.instantiateBean(TemplateInstanceSaveObserver.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		searchService = CDI.instantiateBean(SearchService.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.getArguments().put(DefaultProperties.SEMANTIC_TYPE, TemplateProperties.TEMPLATE_CLASS_ID);
		searchArguments.setMaxSize(-1);

		searchService.searchAndLoad(Instance.class, searchArguments);

		searchArguments.getResult().stream()
			.filter(templateInstance -> templateInstance.get(TemplateProperties.FOR_OBJECT_TYPE_LABEL) == null)
			.peek(instanceSaveObserver::setForObjectTypeLabel)
			.map(templateInstance -> InstanceSaveContext.create(templateInstance, Operation.NO_OPERATION))
			.peek(context -> context.disableValidation("Validation should not block the current patch"))
			.forEach(this::saveInstance);

		LOGGER.info("Finished processing in {} ms", tracker.stop());
	}

	private void saveInstance(InstanceSaveContext saveContext) {
		transactionSupport.invokeInNewTx(() -> domainInstanceService.save(saveContext));
	}

	@Override
	public String getConfirmationMessage() {
		return "Initializes 'forObjectTypeLabel' property of template instances";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not used
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

}
