package com.sirma.itt.seip.annotations.parser;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.RDFHandlerWrapper;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationBody;
import com.sirma.itt.seip.annotations.model.SpecificResource;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.semantic.model.vocabulary.CNT;
import com.sirma.itt.semantic.model.vocabulary.DCMI;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * {@link RDFHandler} that collects {@link Annotation}s when parsing Open annotation format.
 *
 * @author BBonev
 */
public class AnnotationCollectorHandler extends AbstractRDFHandler {

	private static final String HAS_TARGET = OA.HAS_TARGET.toString();
	private static final String HAS_SOURCE = OA.HAS_SOURCE.toString();
	private static final String CHARS = CNT.CHARS.toString();
	private static final String HAS_BODY = OA.HAS_BODY.toString();
	private Map<Value, Instance> annotations = new LinkedHashMap<>();
	private boolean isResolved = false;

	/**
	 * Returns a consumer that sets the current handler as handler to the provided parser.
	 *
	 * @return the consumer
	 */
	public Consumer<RDFParser> addHandlerToParser() {
		return parser -> {
			if (!(parser instanceof JSONLDParser)) {
				return;
			}
			RDFHandler currentHandler = ((JSONLDParser) parser).getRDFHandler();
			if (currentHandler != null) {
				// wrap handlers
				parser.setRDFHandler(new RDFHandlerWrapper(currentHandler, this));
			} else {
				parser.setRDFHandler(this);
			}
		};
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		if (st.getObject().equals(OA.ANNOTATION)) {
			annotations
					.compute(st.getSubject(), (subject, annotation) -> getOrCreateAnnotation(subject, annotation))
						.add(st.getPredicate().toString(), st.getObject());
		} else if (st.getObject().equals(DCMI.TEXT)) {
			annotations.compute(st.getSubject(), (subject, annotation) -> createBody(subject, annotation));
		} else if (st.getObject().equals(OA.SPECIFIC_RESOURCE)) {
			annotations.compute(st.getSubject(), (subject, annotation) -> createResource(subject, annotation));
		} else {
			annotations.computeIfPresent(st.getSubject(),
					(subject, annotation) -> mergeValue(annotation, st.getPredicate().toString(), st.getObject()));
		}
	}

	@SuppressWarnings("unchecked")
	private static Instance mergeValue(Instance instance, String key, Serializable newValue) {
		Serializable oldValue = instance.get(key);
		if (oldValue == null) {
			instance.add(key, newValue);
		} else if (oldValue instanceof Collection<?>) {
			((Collection<Serializable>) oldValue).add(newValue);
		} else if (!isBlankNode(newValue)) {
			Set<Object> set = new HashSet<>();
			set.add(oldValue);
			set.add(newValue);
			instance.add(key, (Serializable) set);
		}
		return instance;
	}

	/**
	 * Gets the collected annotations annotations or empty collection if nothing is processed, yet.
	 *
	 * @return the annotations
	 */
	public Collection<Annotation> getAnnotations() {
		resolveDependencies();

		Set<Annotation> result = new LinkedHashSet<>();
		for (Instance annotation : annotations.values()) {
			if (annotation instanceof Annotation) {
				result.add(normalizeAnnotation((Annotation) annotation));
			}
		}
		return result;
	}

	private static Annotation normalizeAnnotation(Annotation annotation) {
		cleanInstanceId(annotation);
		annotation.addIfNotNull(HAS_BODY, getValueFrom(annotation.remove(HAS_BODY), CHARS));
		annotation.addIfNotNull(HAS_TARGET, getValueFrom(annotation.remove(HAS_TARGET), HAS_SOURCE));
		return annotation;
	}

	private static Serializable getValueFrom(Serializable value, String subKey) {
		if (value instanceof Collection<?>) {
			return getValueFromCollection((Collection<?>) value, subKey);
		} else if (value instanceof Instance) {
			return ((Instance) value).get(subKey);
		}
		return null;
	}

	private static Serializable getValueFromCollection(Collection<?> value, String subKey) {
		Set<Serializable> converted = CollectionUtils.createLinkedHashSet(value.size());
		for (Object item : value) {
			if (item instanceof Instance) {
				CollectionUtils.addNonNullValue(converted, ((Instance) item).get(subKey));
			}
		}
		if (converted.isEmpty()) {
			return null;
		}
		if (converted.size() == 1) {
			return converted.iterator().next();
		}
		return (Serializable) converted;
	}

	private static void cleanInstanceId(Instance annotation) {
		if (isBlankNode(annotation.getId())) {
			annotation.setId(null);
		}
	}

	/**
	 * Reset the handler so it could be used again for parsing.
	 */
	public void reset() {
		annotations.clear();
		isResolved = false;
	}

	private void resolveDependencies() {
		if (isResolved) {
			return;
		}
		// resolved lazily only once.
		isResolved = true;
		for (Instance instance : annotations.values()) {
			Map<String, Serializable> propertiesCopy = new HashMap<>(instance.getProperties());
			for (Entry<String, Serializable> entry : propertiesCopy.entrySet()) {
				if (!isBlankNode(entry.getValue())) {
					continue;
				}
				String property = entry.getKey();
				annotations.computeIfPresent((Value) entry.getValue(),
						(key, reference) -> mergeInstanceProperty(instance, property, reference));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Instance mergeInstanceProperty(Instance instance, String property, Instance reference) {
		if (!instance.addIfNotPresent(property, reference)) {
			Serializable serializable = instance.get(property);
			if (serializable instanceof Collection<?>) {
				((Collection<Serializable>) serializable).add(reference);
			} else {
				Set<Serializable> set = new HashSet<>();
				set.add(serializable);
				set.add(reference);
				instance.add(property, (Serializable) set);
			}
		}
		return reference;
	}

	private static boolean isBlankNode(Serializable value) {
		return value instanceof BNode;
	}

	private static Instance getOrCreateAnnotation(Value id, Instance current) {
		Instance annotation = current;
		if (annotation == null) {
			annotation = new Annotation();
			annotation.setId(id);
		}
		return annotation;
	}

	private static Instance createBody(Value id, Instance current) {
		Instance annotation = current;
		if (annotation == null) {
			annotation = new AnnotationBody();
			annotation.setId(id);
		}
		return annotation;
	}

	private static Instance createResource(Value id, Instance current) {
		Instance annotation = current;
		if (annotation == null) {
			annotation = new SpecificResource();
			annotation.setId(id);
		}
		return annotation;
	}
}
