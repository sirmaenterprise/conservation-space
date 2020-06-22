package com.sirma.itt.seip.annotations;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.annotations.model.Annotation;

/**
 * Service for managing of {@link Annotation} instances. The service supports single or batch operations of annotations.
 *
 * @author kirq4e
 * @author BBonev
 */
public interface AnnotationService {

	/**
	 * Saves or updates the given annotation. The persisted annotations will be only these that report
	 * {@link Annotation#isNew()} or {@link Annotation#isForEdit()} with <code>true</code>.
	 *
	 * @param annotation
	 *            The data of the annotation that will be persisted
	 * @return the updated annotation instance ready for response conversion if needed.
	 */
	Annotation saveAnnotation(Annotation annotation);

	/**
	 * Saves or updates collection of annotations. The persisted/updated annotations will be only these that report
	 * {@link Annotation#isNew()} or {@link Annotation#isForEdit()} with <code>true</code>.
	 *
	 * @param annotations
	 *            The data of the annotation that will be persisted
	 * @return the updated annotation instances ready for response conversion if needed.
	 * @see #saveAnnotation(Annotation)
	 */
	Collection<Annotation> saveAnnotation(Collection<Annotation> annotations);

	/**
	 * Deletes the annotation
	 *
	 * @param annotationId
	 *            ID of the annotation that will be deleted
	 */
	void deleteAnnotation(String annotationId);

	/**
	 * Deletes all annotations for the the given instance id and tab id.
	 *
	 * @param instanceId
	 *            the instance id to remove the annotations for
	 * @param tabId the tab id to remove the annotations for
	 */
	void deleteAllAnnotations(String instanceId, String tabId);

	
	/**
	 * Deletes all annotations for the the given instance id (target).
	 *
	 * @param targetId
	 *            the instance id to remove the annotations for instance id
	 */
	void deleteAllAnnotations(String targetId);
	
	/**
	 * Count the available annotations for the given instance tab
	 *
	 * @param targetId
	 *            the instance id
	 * @param tabId
	 *            the tab id from instance content
	 * @return the annotation count
	 */
	int countAnnotations(String targetId, String tabId);

	/**
	 * Count the available annotation replies for the given target. The returned mapping will contain the annotation ids
	 * as keys and the reply count will be it's value
	 *
	 * @param targetId
	 *            the target id
	 * @return the annotation reply count
	 */
	Map<String, Integer> countAnnotationReplies(String targetId);

	/**
	 * Loads an annotation and all of it's replies
	 *
	 * @param annotationId
	 *            the annotation id to look for
	 * @return the annotation if exists or not deleted
	 */
	Optional<Annotation> loadAnnotation(String annotationId);

	/**
	 * Load annotations for the given identifiers. The method does will not load annotation replies.
	 *
	 * @param annotationIds
	 *            the annotation ids
	 * @return the collection of found annotations
	 */
	Collection<Annotation> loadAnnotations(Collection<? extends Serializable> annotationIds);

	/**
	 * Searches for annotations that are created on a given image/instance
	 *
	 * @param targetId
	 *            ID of the target instance
	 * @param tabId ID of the tab (section) of target instance
	 * @param limit
	 *            Limit of the results
	 * @return the list of annotations found
	 */
	Collection<Annotation> searchAnnotation(String targetId, String tabId, Integer limit);

	/**
	 * Searches for annotations according to the passed parameters.
	 *
	 * @param searchRequest
	 *            the search request
	 * @return the loaded annotations
	 */
	Collection<Annotation> searchAnnotations(AnnotationSearchRequest searchRequest);

	/**
	 * Count annotations that matches the given search request.
	 *
	 * @param searchRequest
	 *            the search request
	 * @return the matching annotations count
	 */
	int searchAnnotationsCountOnly(AnnotationSearchRequest searchRequest);

	/**
	 * Loads all the annotations and their replies.
	 *
	 * @param targetId
	 *            the id of the target instance
	 * @param limit
	 *            the limit
	 * @return the collection with the annotations.
	 */
	Collection<Annotation> loadAnnotations(String targetId, Integer limit);

}
