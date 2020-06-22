package com.sirma.itt.seip.instance.content.share;

import java.util.Collection;
import java.util.Map;

/**
 * Allows sharing of instance content so that it is public visible and anyone can access it. The service does not work
 * directly with the already existing content but instead create new records in the content table.
 *
 * @author A. Kunchev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
public interface ShareInstanceContentService {

	/**
	 * Generates and returns URI for an instance content that will be publicly visible(accessible). If the content of
	 * the original instance is a binary file then it is re-imported. If the content is creatable (iDoc) then first
	 * export is run to the given as an input argument format. Because the re-import and the export processes
	 * are relatively slow we run this using an async task from the schedule API so the shared content might not be
	 * available instantaneously. Here the content handling task is executed immediately.
	 *
	 * @param id
	 * 		the identifier of an already existing instance.
	 * @param contentFormat
	 * 		content format is used when the instance is iDoc / created. Then we need to export it first and afterwards
	 * 		share it. This parameter is used to specify which export format we should use when the content is iDoc.
	 * @return the URI from which the instance will be accessible
	 */
	String getSharedContentURI(String id, String contentFormat);

	/**
	 * Generates and returns URI for an instance content that will be publicly visible(accessible). This method works
	 * analogical to {@link ShareInstanceContentService#getSharedContentURI(String, String)} the only difference is that
	 * here the asynchronous task is not started immediately but instead is only scheduled for execution.
	 *
	 * @param id
	 * 		the identifier of an already existing instance.
	 * @param contentFormat
	 * 		content format is used when the instance is iDoc / created. Then we need to export it first and afterwards
	 * 		share it. This parameter is used to specify which export format we should use when the content is iDoc.
	 * @return a single string, containing the URL with its ShareCode.
	 */
	String createContentShareTask(String id, String contentFormat);

	/**
	 * Triggers the execution of an already scheduled task for sharing instance's content. Such task is created using
	 * {@link ShareInstanceContentService#createContentShareTask(String, String)}.
	 *
	 * @param taskIdentifier
	 * 		the id of the already created task. It is returned from
	 * 		{@link ShareInstanceContentService#createContentShareTask(String, String)}
	 */
	void triggerContentShareTask(String taskIdentifier);

	/**
	 * Generates and returns URIs for an instances' content that will be publicly visible(accessible). If the content of
	 * the original instance is binary file then it is re-imported. If the content is creatable (iDoc) then first
	 * export is run to the given as an input argument format. Because the re-import and export process are relatively
	 * slow we run this using an async task. Here the content handling task is executed immediately.
	 *
	 * @param id
	 * 		the identifier of an already existing instance.
	 * @param contentFormat
	 * 		content format is used when the instance is iDoc / created. Then we need to export it first and afterwards
	 * 		share it. This parameter is used to specify which export format we should use when the content is iDoc.
	 * @return a single string, containing the URL with its ShareCode.
	 */
	@SuppressWarnings("unused")
	Map<String, String> getSharedContentURIs(Collection<String> id, String contentFormat);

	/**
	 * Generates and returns URI for an instances contents that will be publicly visible(accessible). This method works
	 * analogical to {@link ShareInstanceContentService#getSharedContentURIs(Collection, String)} the only difference
	 * is that here the asynchronous task is not started immediately but instead is only scheduled for execution.
	 *
	 * @param ids
	 * 		collection of identifiers of an already existing instances.
	 * @param contentFormat
	 * 		content format is used when the instances is iDoc / created. Then we need to export it first and afterwards
	 * 		share it. This parameter is used to specify which export format we should use when the content is iDoc.
	 * @return a single string, containing the URL with its ShareCode.
	 */
	Map<String, String> createContentShareTasks(Collection<String> ids, String contentFormat);

	/**
	 * Triggers the execution of an already scheduled task for sharing instance's content. Such task is created using
	 * {@link ShareInstanceContentService#createContentShareTasks(Collection, String)}.
	 *
	 * @param tasksIdentifiers
	 * 		list of identifiers of already created tasks.
	 */
	void triggerContentShareTasks(Collection<String> tasksIdentifiers);

}
