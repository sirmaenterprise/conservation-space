package com.sirma.itt.seip.template.observers;

import static com.sirma.itt.seip.domain.ObjectTypes.TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.IS_PRIMARY_TEMPLATE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_PURPOSE;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.BeforeInstancePersistEvent;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.db.TemplateDao;


/**
 * Observes template instances creation and auto-sets the appropriate primary flag to them, if needed.
 * 
 * @author Vilizar Tsonev
 */
@Singleton
public class TemplateCreateObserver {

	@Inject
	private TemplateDao templateDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Observes the {@link BeforeInstancePersistEvent} and if there is no existing primary template for the type and
	 * purpose of the new template, forcibly sets it to be primary.
	 * 
	 * @param event
	 *            is the {@link BeforeInstancePersistEvent}
	 */
	public <I extends Instance> void onBeforeTemplateCreated(@Observes BeforeInstancePersistEvent<I, ?> event) {
		I newTemplateinstance = event.getInstance();
		if (newTemplateinstance.type().is(TEMPLATE)) {
			String forType = newTemplateinstance.getAsString(FOR_OBJECT_TYPE);
			String templatePurpose = newTemplateinstance.getAsString(TEMPLATE_PURPOSE);
			Optional<Template> existingPrimaryTemplate = templateDao.findExistingPrimaryTemplate(forType,
					templatePurpose, null, null);
			if (!existingPrimaryTemplate.isPresent() && !newTemplateinstance.getBoolean(IS_PRIMARY_TEMPLATE)) {
				LOGGER.debug(
						"There is not yet primary template for type {} and purpose {} . Newly created [{}] will be forcibly set as primary",
						newTemplateinstance.getAsString(FOR_OBJECT_TYPE),
						newTemplateinstance.getAsString(TEMPLATE_PURPOSE), newTemplateinstance.getId());
				newTemplateinstance.add(IS_PRIMARY_TEMPLATE, Boolean.TRUE);
			}
		}
	}

}
