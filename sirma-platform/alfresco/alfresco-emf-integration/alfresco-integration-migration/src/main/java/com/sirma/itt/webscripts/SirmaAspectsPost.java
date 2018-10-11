package com.sirma.itt.webscripts;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Class implementing web script qviaspects.post for adding and removing
 * aspects. When adding aspects a user custom properties can be specified.
 * <p>
 * Example request:<br>
 *
 * <pre>
 * <code>
 * {
 * "added": ["imap:flaggable"],
 * "removed": [],
 * "properties" : {
 * 		"imap_flaggable": {
 * 			"prop_imap_flagSeen" : true
 * 		}
 * }
 * </code>
 * </pre>
 *
 * @author BBonev
 */
public class SirmaAspectsPost extends DeclarativeWebScript {

	/** The node service. */
	private NodeService nodeService;

	/** The namespace service. */
	private NamespaceService namespaceService;

	/** The transaction service. */
	private TransactionService transactionService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {

		Map<String, String> templateVars = req.getServiceMatch()
				.getTemplateVars();
		final NodeRef nodeRef = new NodeRef(templateVars.get("store_type"),
				templateVars.get("store_id"), templateVars.get("id"));
		final boolean forced = templateVars.get("forced") != null;

		Map<String, Object> model = new HashMap<String, Object>();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(
				1);
		model.put("results", results);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("action", "manageAspects");
		result.put("success", false);
		model.put("overallSuccess", false);
		model.put("successCount", 0);
		model.put("failureCount", 0);
		results.add(result);

		if (!nodeService.exists(nodeRef)) {
			return model;
		}

		String content = null;
		try {
			content = req.getContent().getContent();
			JSONObject jsonObject = new JSONObject(content);
			final Set<String> aspectsToAdd = new LinkedHashSet<String>();
			if (jsonObject.has("added")) {
				JSONArray jsonArray = jsonObject.getJSONArray("added");
				for (int i = 0; i < jsonArray.length(); i++) {
					String item = jsonArray.getString(i);
					aspectsToAdd.add(item);
				}
			}

			final Set<String> aspectsToRemove = new LinkedHashSet<String>();
			if (jsonObject.has("removed")) {
				JSONArray jsonArray = jsonObject.getJSONArray("removed");
				for (int i = 0; i < jsonArray.length(); i++) {
					String item = jsonArray.getString(i);
					aspectsToRemove.add(item);
				}
			}
			final Map<String, Map<QName, Serializable>> aspectProperties = new HashMap<String, Map<QName, Serializable>>();
			if (jsonObject.has("properties")) {
				JSONObject props = jsonObject.getJSONObject("properties");
				for (Iterator<?> iterator = props.keys(); iterator.hasNext();) {
					String key = (String) iterator.next();
					String aspect = parseName(key);
					Map<QName, Serializable> map = aspectProperties.get(aspect);
					if (map == null) {
						map = new HashMap<QName, Serializable>();
						aspectProperties.put(aspect, map);
					}

					JSONObject aspectProps = props.getJSONObject(key);
					for (Iterator<?> iterator2 = aspectProps.keys(); iterator2
							.hasNext();) {
						String string = (String) iterator2.next();
						Serializable value = (Serializable) aspectProps
								.get(string);
						map.put(QName.createQName(parseName(string),
								namespaceService), value);
					}
				}
			}
			int errorCount = 0;
			int successfulCount = 0;
			Pair<Boolean, Boolean> oldAutoVersion = null;
			if (!aspectsToAdd.isEmpty() || !aspectsToRemove.isEmpty()) {
				oldAutoVersion = setAutoVersion(nodeRef, new Pair<Boolean, Boolean>(Boolean.FALSE, Boolean.FALSE));
			}

			RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
			Pair<Integer, Integer> pair = helper.doInTransaction(new RetryingTransactionCallback<Pair<Integer, Integer>>() {

				@Override
				public Pair<Integer, Integer> execute() throws Throwable {
							return addOrRemoveAspects(nodeRef, aspectsToAdd,
									aspectsToRemove, aspectProperties, forced);
						}
			}, false, true);

			successfulCount = pair.getFirst();
			errorCount = pair.getSecond();

			setAutoVersion(nodeRef, oldAutoVersion);
			model.put("overallSuccess", errorCount == 0);
			result.put("success", errorCount == 0);
			model.put("successCount", successfulCount);
			model.put("failureCount", errorCount);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}

	/**
	 * Adds or removes aspects.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param aspectsToAdd
	 *            the aspects to add
	 * @param aspectsToRemove
	 *            the aspects to remove
	 * @param aspectProperties
	 *            the aspect properties
	 * @param forced
	 *            forced properties update
	 * @return the pair
	 */
	private Pair<Integer, Integer> addOrRemoveAspects(NodeRef nodeRef,
			Set<String> aspectsToAdd, Set<String> aspectsToRemove,
			Map<String, Map<QName, Serializable>> aspectProperties,
			boolean forced) {
		int errorCount = 0;
		int successfulCount = 0;
		for (String aspect : aspectsToAdd) {
			QName aspectQName = QName.createQName(aspect, namespaceService);
			try {
				if (!nodeService.hasAspect(nodeRef, aspectQName)) {
					Map<QName, Serializable> props = aspectProperties
							.get(aspect);
					nodeService.addAspect(nodeRef, aspectQName, props);
					successfulCount++;
				} else if (forced) {
					Map<QName, Serializable> properties = nodeService
							.getProperties(nodeRef);
					Map<QName, Serializable> props = aspectProperties
							.get(aspect);
					properties.putAll(props);
					nodeService.setProperties(nodeRef, properties);
					successfulCount++;
				}
			} catch (Exception e) {
				errorCount++;
				e.printStackTrace();
			}
		}
		for (String aspect : aspectsToRemove) {
			QName aspectQName = QName.createQName(aspect, namespaceService);
			try {
				if (nodeService.hasAspect(nodeRef, aspectQName)) {
					nodeService.removeAspect(nodeRef, aspectQName);
					successfulCount++;
				}
			} catch (Exception e) {
				errorCount++;
				e.printStackTrace();
			}
		}
		return new Pair<Integer, Integer>(successfulCount, errorCount);
	}

	/**
	 * Sets the auto version.
	 *
	 * @param nodeRef
	 *            the node ref
	 * @param newValue
	 *            the new value
	 * @return the pair
	 */
	private Pair<Boolean, Boolean> setAutoVersion(final NodeRef nodeRef, final Pair<Boolean, Boolean> newValue) {
		RetryingTransactionHelper transactionHelper = transactionService
				.getRetryingTransactionHelper();
		return transactionHelper.doInTransaction(
				new RetryingTransactionCallback<Pair<Boolean, Boolean>>() {

					@Override
					public Pair<Boolean, Boolean> execute() throws Throwable {
						Boolean oldValue = (Boolean) nodeService.getProperty(
								nodeRef, ContentModel.PROP_AUTO_VERSION);
						if (oldValue != null) {
							nodeService.setProperty(nodeRef,
									ContentModel.PROP_AUTO_VERSION, newValue.getFirst());
						}
						Boolean oldValueProps = (Boolean) nodeService.getProperty(
								nodeRef, ContentModel.PROP_AUTO_VERSION);
						if (oldValueProps != null) {
							nodeService.setProperty(nodeRef,
									ContentModel.PROP_AUTO_VERSION, newValue.getSecond());
						}
						return new Pair<Boolean, Boolean>(oldValue, oldValueProps);
					}
				}, false, true);
	}

	/**
	 * Parses the name.
	 *
	 * @param key
	 *            the key
	 * @return the string
	 */
	private String parseName(String key) {
		String k = key;
		if (k.startsWith("prop_")) {
			k = key.substring(5);
		}
		return k.replace("_", ":");
	}

	/**
	 * Getter method for nodeService.
	 *
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Setter method for nodeService.
	 *
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Getter method for namespaceService.
	 *
	 * @return the namespaceService
	 */
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	/**
	 * Setter method for namespaceService.
	 *
	 * @param namespaceService
	 *            the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * Getter method for transactionService.
	 *
	 * @return the transactionService
	 */
	public TransactionService getTransactionService() {
		return transactionService;
	}

	/**
	 * Setter method for transactionService.
	 *
	 * @param transactionService
	 *            the transactionService to set
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

}
