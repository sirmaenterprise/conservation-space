package com.sirma.cmf.web.entity.dispatcher;

import java.util.List;

import javax.inject.Inject;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.EntityPreviewAction;
import com.sirma.itt.emf.converter.SerializableConverter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;

/**
 * Base class for instance landing page initializers.
 * 
 * @author svelikov
 */
public class PageContextInitializer extends Action {

	/** The entity preview action. */
	@Inject
	protected EntityPreviewAction entityPreviewAction;

	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The event service. */
	@Inject
	protected EventService eventService;

	/** The type converter. */
	@Inject
	@SerializableConverter
	protected TypeConverter typeConverter;

	/** The label provider. */
	@Inject
	protected LabelProvider labelProvider;

	/** The link service. */
	@Inject
	@RelationalDb
	private LinkService linkService;

	@Inject
	private LinkService baseService;

	/**
	 * Gets the section instance if the given instance is a part of any section at all. The returned
	 * instance is the first found from the returned links.
	 * 
	 * @param instance
	 *            the instance
	 * @return the section parent
	 */
	protected Instance getSectionParent(Instance instance) {
		if (instance == null) {
			return null;
		}
		return ((OwnedModel) instance).getOwningInstance();
	}

	/**
	 * Gets the any parent where the given instance is child. The first parent found will be
	 * returned.
	 * 
	 * @param instance
	 *            the instance
	 * @return the any parent
	 */
	protected Instance getAnyParent(Instance instance) {
		if (instance == null) {
			return null;
		}
		List<LinkReference> linksTo = baseService.getLinksTo(instance.toReference(),
				LinkConstants.PARENT_TO_CHILD);
		if (!linksTo.isEmpty()) {
			return getLinkedInstance(linksTo.get(0));
		}
		return null;
	}

	/**
	 * Gets the linked instance.
	 * 
	 * @param linkReference
	 *            the link reference
	 * @return the linked instance
	 */
	protected Instance getLinkedInstance(LinkReference linkReference) {
		return typeConverter.convert(InitializedInstance.class, linkReference.getFrom())
				.getInstance();
	}

}
