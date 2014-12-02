package com.sirma.itt.idoc.web.events.observer;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.idoc.web.document.IntelligentDocumentEditor;
import com.sirma.itt.idoc.web.document.IntelligentDocumentService;
import com.sirma.itt.idoc.web.events.CreateRelationshipFromLinkEvent;
import com.sirma.itt.idoc.web.events.CreateRelationshipFromWidgetEvent;
import com.sirma.itt.idoc.web.events.WidgetBinding;

/**
 * Base class for parsing the document content and firing events for widgets.
 *
 * @author yasko
 */
public abstract class AbstractDocumentLinkHandler {

	/** The Constant EMPTY_PAIR. */
	private static final Pair<Set<String>, Set<String>> EMPTY_PAIR = new Pair<Set<String>, Set<String>>(
			Collections.<String> emptySet(), Collections.<String> emptySet());

	/** The editor. */
	@Any
	@Inject
	private IntelligentDocumentEditor editor;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The idoc service. */
	@Inject
	private IntelligentDocumentService idocService;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The create relationships event. */
	@Inject
	private Event<CreateRelationshipFromWidgetEvent> createRelationshipsEvent;

	/** The create link relationships event. */
	@Inject
	private Event<CreateRelationshipFromLinkEvent> createLinkRelationshipsEvent;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(AbstractDocumentLinkHandler.class);

	/**
	 * Parses the document and fires events for widgets.
	 *
	 * @param instance
	 *            {@link Instance} to parse.
	 * @param linkFrom
	 *            The 'real' instance to be used in for example in creating relationships, either
	 *            {@link DocumentInstance} or ObjectInstance.
	 */
	public void handle(Instance instance, Instance linkFrom) {

		if (instance instanceof DocumentInstance) {
			if (!editor.canHandle((DocumentInstance) instance)) {
				return;
			}

			Version version = new Version();
			Pair<Set<String>, Set<String>> links = removeOldDocumentLinks(version,
					(DocumentInstance) instance, linkFrom.toReference());
			if (links.getFirst().isEmpty() && links.getSecond().isEmpty()) {
				// no new links found no need to continue
				return;
			}

			try {
				Elements widgets = version.content.select(".widget");
				Iterator<Element> iterator = widgets.iterator();
				while (iterator.hasNext()) {
					Element widget = iterator.next();
					String widgetName = widget.attr("data-name");

					JSONObject configAsJson = null;
					JSONObject valueAsJson = null;
					String config = widget.attr("data-config");
					String value = widget.attr("data-value");
					if (StringUtils.isNotBlank(config) && StringUtils.isNotBlank(value)) {
						/*
						 * FIXME: When we save the widget config we html escape it, so it can be
						 * saved as an attribute, but when we save the document the sanitizer kicks
						 * in and it escapes it again. Maybe we should save the attribute as a
						 * base64 string.
						 */
						config = StringEscapeUtils.unescapeHtml(StringEscapeUtils
								.unescapeHtml(config));
						configAsJson = new JSONObject(config);
						value = StringEscapeUtils.unescapeHtml(StringEscapeUtils
								.unescapeHtml(value));
						valueAsJson = new JSONObject(value);
						WidgetBinding binding = new WidgetBinding(widgetName);
						createRelationshipsEvent.select(binding).fire(
								new CreateRelationshipFromWidgetEvent(widgetName, configAsJson,
										valueAsJson, linkFrom.toReference(), links.getFirst()));
					}
				}

				createRelationsForNewLinks(linkFrom.toReference(), version.content,
						links.getSecond());

			} catch (JSONException e) {
				LOGGER.error("Can not parse given strng to JSON", e);
			}

			// If event is fired by comment extract all links to documents/objects
			// and make relations between comment and corresponding document/object
		} else if (instance instanceof CommentInstance) {
			handleCommentInstanceInternal((CommentInstance) instance, linkFrom);
		}
	}

	/**
	 * Handle comment instance internal.
	 * 
	 * @param commentInstance
	 *            the comment instance
	 * @param linkFrom
	 *            the link from
	 */
	private void handleCommentInstanceInternal(CommentInstance commentInstance, Instance linkFrom) {
		// if no comment nothing to process
		if (StringUtils.isBlank(commentInstance.getComment())) {
			return;
		}
		Document doc = Jsoup.parse(commentInstance.getComment());

		// will hold all instance ids that are referenced in the old comment
		Set<String> oldLinkedInstances = Collections.emptySet();

		Set<String> newLinks = new HashSet<String>();
		Set<Pair<String, String>> newLinkAndTypes = new HashSet<Pair<String, String>>();
		// collect all links for the current/new comment instance
		collectExternalLinkIds(doc, newLinks, newLinkAndTypes);

		InstanceReference reference;
		// handle the case when topic is passed
		// after redesigning the comments module the topic will have the first content
		if (commentInstance instanceof TopicInstance) {
			reference = ((TopicInstance) commentInstance).getTopicAbout();
		} else {
			reference = commentInstance.getTopic().getTopicAbout();
		}

		// if the other element is also a comment instance we will collect it's links
		if ((linkFrom instanceof CommentInstance)
				&& (((CommentInstance) linkFrom).getComment() != null)) {

			oldLinkedInstances = getCommentLinks(
					Jsoup.parse(((CommentInstance) linkFrom).getComment()), reference);
		}

		// create diffs to determine the removed and added links
		Set<String> removed = new HashSet<String>(oldLinkedInstances);
		removed.removeAll(newLinks);
		Set<String> added = new HashSet<String>(newLinks);
		added.removeAll(oldLinkedInstances);

		// no need to check if there are no removed links on the first place
		if (!removed.isEmpty()) {
			// collect links for instance that have been removed
			Set<Serializable> removedLinks = collectIncomingLinks(reference, removed);
			removeLinkdsById(removedLinks);
		}

		// create new relations
		for (Pair<String, String> pair : newLinkAndTypes) {
			createLinkRelationshipsEvent.fire(new CreateRelationshipFromLinkEvent(pair.getFirst(),
					pair.getSecond(), reference, added));
		}
	}

	/**
	 * Removes the linkds by id.
	 * 
	 * @param removedLinks
	 *            the removed links
	 */
	private void removeLinkdsById(Set<Serializable> removedLinks) {
		for (Serializable linkId : removedLinks) {
			linkService.removeLinkById(linkId);
		}
	}

	/**
	 * Creates the relations for new links.
	 * 
	 * @param linkFrom
	 *            the link from
	 * @param doc
	 *            the doc
	 * @param set
	 *            the set
	 */
	private void createRelationsForNewLinks(InstanceReference linkFrom, Document doc,
			Set<String> set) {
		Elements links = doc.getElementsByClass("instance-link");
		Iterator<Element> iterator = links.iterator();
		while (iterator.hasNext()) {
			Element link = iterator.next();
			createLinkRelationshipsEvent.fire(new CreateRelationshipFromLinkEvent(link
					.attr("data-instance-id"), link.attr("data-instance-type"), linkFrom, set));
		}
	}

	/**
	 * Parses the document. Compare old and new version and deactivate all removed links.
	 *
	 * @param currentData
	 *            the current data
	 * @param instance
	 *            {@link Instance} to parse.
	 * @param linkFrom
	 *            The 'real' instance to be used in for example in creating relationships, either
	 *            {@link DocumentInstance} or ObjectInstance.
	 * @return the pair of new links first element are the widgets instances and the second are the
	 *         internal links
	 */
	public Pair<Set<String>, Set<String>> removeOldDocumentLinks(Version currentData,
			DocumentInstance instance,
			InstanceReference linkFrom) {

		// Get previous version of document
		List<VersionInfo> versions = idocService.getVersions(instance);

		// If there's no previous version can not exist deleted links so just return
		if (versions.size() < 2) {
			return EMPTY_PAIR;
		}
		// 0 -> current version, 1-> previous version

		// collect data for the current version
		currentData.version = versions.get(0);
		currentData.docInstance = instance;
		currentData.content = getDocumentContent(currentData.docInstance);
		collectVersionData(currentData);

		// collect needed information from the old document content
		Version oldData = new Version();
		oldData.version = versions.get(1);
		oldData.docInstance = idocService.loadVersion(instance.getId(),
				oldData.version.getVersionLabel());
		oldData.content = getDocumentContent(oldData.docInstance);
		collectVersionData(oldData);

		// create links diff to determine the removed links
		// remove old widgets
		Set<String> widgets = new HashSet<String>(oldData.widgets);
		widgets.removeAll(currentData.widgets);
		// remove old internal links
		Set<String> internalLinks = new HashSet<String>(oldData.internalLinks);
		internalLinks.removeAll(currentData.internalLinks);

		Set<Serializable> linksToRemove = new HashSet<Serializable>(32);

		// no need to check anything if there are no removed links at all
		if (!widgets.isEmpty() || !internalLinks.isEmpty()) {
			List<LinkReference> oldLinks = linkService.getLinksTo(linkFrom);
			for (LinkReference link : oldLinks) {

				if (widgets.contains(link.getFrom().getIdentifier())
						|| internalLinks.contains(link.getFrom().getIdentifier())) {
					linksToRemove.add(link.getId());
				}
			}

			oldLinks = linkService.getLinks(linkFrom);
			for (LinkReference link : oldLinks) {

				if (widgets.contains(link.getTo().getIdentifier())
						|| internalLinks.contains(link.getTo().getIdentifier())) {
					linksToRemove.add(link.getId());
				}
			}

			removeLinkdsById(linksToRemove);
		}

		// create links diff to determine the added links
		widgets = new HashSet<String>(currentData.widgets);
		widgets.removeAll(oldData.widgets);
		internalLinks = new HashSet<String>(currentData.internalLinks);
		internalLinks.removeAll(oldData.internalLinks);

		return new Pair<Set<String>, Set<String>>(widgets, internalLinks);
	}

	/**
	 * Collects the version data like widget and instance links.
	 *
	 * @param data
	 *            the data
	 */
	private void collectVersionData(Version data) {
		try {
			Elements docWidgets = data.content.select(".widget");
			Iterator<Element> iterator = docWidgets.iterator();
			while (iterator.hasNext()) {
				Element widget = iterator.next();
				String name = widget.attr("data-name");

				if ("datatable".equals(name) || "objectData".equals(name)
						|| "imageWidget".equals(name)) {
					String value = widget.attr("data-value");
					if (StringUtils.isNotBlank(value)) {

						value = StringEscapeUtils.unescapeHtml(StringEscapeUtils
								.unescapeHtml(value));
						JSONObject valueAsJson = new JSONObject(value);

						try {
							if (valueAsJson.has("manuallySelectedObjects")) {

								JSONArray manuallySelected = valueAsJson
										.getJSONArray("manuallySelectedObjects");
								int length = manuallySelected.length();

								for (int a = 0; a < length; a++) {
									JSONObject item = manuallySelected.getJSONObject(a);
									String id = JsonUtil.getStringValue(item, "dbId");
									data.widgets.add(id);
								}
							}
						} catch (Exception e) {
							LOGGER.error("", e);
						}

						JSONObject selectedObject = JsonUtil.getJsonObject(valueAsJson,
								"selectedObject");
						if (selectedObject != null) {
							String id = JsonUtil.getStringValue(selectedObject, "dbId");
							data.widgets.add(id);
						}

						try {
							value = widget.attr("data-config");
							value = StringEscapeUtils.unescapeHtml(StringEscapeUtils
									.unescapeHtml(value));
							valueAsJson = new JSONObject(value);

							String id = JsonUtil.getStringValue(valueAsJson, "imageId");

							if (id != null) {
								data.widgets.add(id);
							}
						} catch (Exception e) {
							LOGGER.error("", e);
						}
					}
				}
			}

		} catch (JSONException e) {
			LOGGER.error("Can not parse given strng to JSON", e);
		}

		collectExternalLinkIds(data.content, data.internalLinks, null);
	}

	/**
	 * Collect external link ids and id-type pairs.
	 *
	 * @param content
	 *            the content
	 * @param data
	 *            pass non <code>null</code> set to collect all external ids
	 * @param pairs
	 *            pass non <code>null</code> set to collect all external ids and their instance
	 *            types
	 */
	private void collectExternalLinkIds(Document content, Set<String> data,
			Set<Pair<String, String>> pairs) {
		if ((data == null) && (pairs == null)) {
			return;
		}
		Elements links = content.getElementsByClass("instance-link");
		Iterator<Element> iterator = links.iterator();
		while (iterator.hasNext()) {
			Element link = iterator.next();
			String id = link.attr("data-instance-id");
			if (data != null) {
				data.add(id);
			}
			if (pairs != null) {
				pairs.add(new Pair<String, String>(id, link.attr("data-instance-type")));
			}
		}
	}

	/**
	 * Gets the document content.
	 *
	 * @param docInstance
	 *            the doc instance
	 * @return the document content
	 */
	private Document getDocumentContent(DocumentInstance docInstance) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		documentService.getContent(docInstance, baos);
		String content;
		try {
			content = new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Can not convert document content to UTF-8 ", e);
			return null;
		}

		Document doc = Jsoup.parse(content);
		return doc;
	}

	/**
	 * Parses the old comment and remove links.
	 *
	 * @param jsoupDoc
	 *            {@link Instance} to parse.
	 * @param reference
	 *            The 'real' instance to be used in for example in creating relationships, either
	 *            {@link DocumentInstance} or ObjectInstance.
	 * @return the sets the
	 */
	public Set<String> getCommentLinks(Document jsoupDoc,
			InstanceReference reference) {

		Set<String> internalLinks = new HashSet<>();
		collectExternalLinkIds(jsoupDoc, internalLinks, null);
		return internalLinks;
	}

	/**
	 * Collect incoming links for the given instance that match the given set of links
	 *
	 * @param reference
	 *            the reference
	 * @param internalLinks
	 *            the internal links
	 * @return the sets db ids of links that match the given instance ids
	 */
	private Set<Serializable> collectIncomingLinks(InstanceReference reference,
			Set<String> internalLinks) {
		Set<Serializable> currentLinks = CollectionUtils.createHashSet(internalLinks.size());
		List<LinkReference> oldLinks = linkService.getLinksTo(reference);
		for (LinkReference link : oldLinks) {
			if (internalLinks.contains(link.getFrom().getIdentifier())) {
				currentLinks.add(link.getId());
			}
		}
		return currentLinks;
	}

	/**
	 * Checks if is operation auditable and changes data.
	 *
	 * @param event
	 *            the operation to check
	 * @return true, if is operation auditable
	 */
	protected boolean isOperationChanging(OperationEvent event) {
		if (event.getOperationId() == null) {
			return true;
		}
		return !(ActionTypeConstants.LOCK.equals(event.getOperationId()) || ActionTypeConstants.UNLOCK
				.equals(event.getOperationId()));
	}

	/**
	 * The Class Version.
	 */
	private static class Version {

		/** The version. */
		VersionInfo version;

		/** The doc instance. */
		DocumentInstance docInstance;

		/** The widgets. */
		Set<String> widgets = new HashSet<String>();

		/** The internal links. */
		Set<String> internalLinks = new HashSet<String>();

		/** The content. */
		Document content;
	}

}
