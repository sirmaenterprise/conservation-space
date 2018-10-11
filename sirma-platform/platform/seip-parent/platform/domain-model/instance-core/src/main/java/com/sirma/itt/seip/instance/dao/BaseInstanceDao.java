package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.util.PropertiesEvaluationHelper;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Class with common methods for working with different instances.
 *
 * @author BBonev
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 */
public abstract class BaseInstanceDao<P extends Serializable, K extends Serializable> {

	/**
	 * The query limit for most relational databases is 2^16, but to be safe we will do only 2^15.
	 */
	public static final int MAX_QUERY_ELEMENT_COUNT = 2 << 15;

	/** The properties dao. */
	@Inject
	protected PropertiesService propertiesService;

	@Inject
	protected ExpressionsManager evaluatorManager;

	@Inject
	protected ObjectMapper dozerMapper;

	@Inject
	protected DatabaseIdManager idManager;

	@Inject
	protected SecurityContext securityContext;

	/**
	 * Sets the current user to.
	 *
	 * @param model
	 *            the model
	 * @param key
	 *            the key
	 */
	public void setCurrentUserTo(PropertyModel model, String key) {
		model.add(key, securityContext.getAuthenticated().getSystemId());
	}

	/**
	 * Populate properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields
	 */
	public <E extends PropertyDefinition> void populateProperties(PropertyModel model, List<E> fields) {
		PropertiesEvaluationHelper.populateProperties(model, fields, evaluatorManager, false, idManager);
	}
}
