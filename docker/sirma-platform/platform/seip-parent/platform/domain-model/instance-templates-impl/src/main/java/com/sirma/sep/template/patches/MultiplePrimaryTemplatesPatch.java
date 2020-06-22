package com.sirma.sep.template.patches;

import static com.sirma.itt.seip.template.TemplateProperties.IS_PRIMARY_TEMPLATE;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.db.TemplateDao;
import com.sirma.itt.seip.template.exceptions.MissingTemplateException;
import com.sirma.itt.seip.template.utils.TemplateUtils;
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
 * If more than one primary templates for the same group are present in the relational database, repairs their primary
 * flags by synchronizing them with the corresponding instance.
 * 
 * @author Viliar Tsonev
 */
public class MultiplePrimaryTemplatesPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private TransactionSupport transactionSupport;
	private InstanceTypeResolver instanceTypeResolver;
	private TemplateDao templateDao;

	@Override
	public void setUp() throws SetupException {
		instanceTypeResolver = CDI.instantiateBean(InstanceTypeResolver.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		templateDao = CDI.instantiateBean(TemplateDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<Template> templates = templateDao.getAllTemplates();
		Map<String, List<Template>> groupedTemplates = TemplateUtils.groupNonMailTemplatesByHash(templates);

		transactionSupport.invokeInNewTx(() -> repairAll(groupedTemplates));

		LOGGER.info("Repairing of templates' primary flags took {} ms.", tracker.stop());
	}

	private void repairAll(Map<String, List<Template>> groupedTemplates) {
		for (Map.Entry<String, List<Template>> entry : groupedTemplates.entrySet()) {
			List<Template> primaries = getPrimaries(entry.getValue());
			if (primaries.size() > 1) {
				syncTemplatesWithCorrespondingInstance(primaries);
			}
		}
	}

	private void syncTemplatesWithCorrespondingInstance(List<Template> templates) {
		for (Template template : templates) {
			InstanceReference reference = instanceTypeResolver
					.resolveReference(template.getCorrespondingInstance())
					.orElseThrow(() -> new MissingTemplateException(
								"Failed to sync primary flag of template [" + template.getId()
										+ "], because its instance [" + template.getCorrespondingInstance()
										+ "] was not found in the system"));
			Instance instance = reference.toInstance();
			if (!instance.getBoolean(IS_PRIMARY_TEMPLATE) == template.getPrimary()) {
				template.setPrimary(instance.getBoolean(IS_PRIMARY_TEMPLATE));
				LOGGER.debug(
						"Template {} was detected as a duplicate primary. Synchronizing it in Relational DB to be primary={}",
						template.getId(), template.getPrimary());
				templateDao.saveOrUpdate(template);
			}
		}
	}
	
	private static List<Template> getPrimaries(List<Template> templates) {
		return templates.stream()
				.filter(Template::getPrimary)
				.collect(Collectors.toList());
	}

	@Override
	public String getConfirmationMessage() {
		return "Repairs duplicate primary templates per group";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
