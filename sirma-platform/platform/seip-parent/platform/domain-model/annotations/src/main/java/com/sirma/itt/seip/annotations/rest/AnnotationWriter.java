package com.sirma.itt.seip.annotations.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import com.github.jsonldjava.utils.JsonUtils;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Writer that converts and writes the given {@link Annotation} objects to format suitable for REST response. When
 * building the response the original content ({@link Annotation#getContent()}) will be used and updated with the custom
 * internal data before for writing it to the output.
 *
 * @author BBonev
 */
@Singleton
public class AnnotationWriter {

	private static final String EMF_USER = EMF.PREFIX + ":" + EMF.USER.getLocalName();

	@Inject
	private TypeConverter typeConverter;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Writes the given data to the given output stream. The supported input types are {@link Annotation} and
	 * {@link Collection} of {@link Annotation}s
	 *
	 * @param toWrite
	 *            the data to write. Should not be <code>null</code>. The supported formats are {@link Annotation} or
	 *            {@link Collection} of {@link Annotation}s.
	 * @param entityStream
	 *            the entity stream to write to. The stream will be closed after the method call
	 * @param autoclose
	 *            the automatically close the output after writing the data. If <code>true</code> the
	 *            {@link OutputStream} will be closed.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public void writeTo(Object toWrite, OutputStream entityStream, boolean autoclose) throws IOException {
		Objects.requireNonNull(toWrite);

		Object data;
		if (toWrite instanceof Annotation) {
			data = convert((Annotation) toWrite);
		} else if (toWrite instanceof Collection<?>) {
			data = convert((Collection<Annotation>) toWrite);
		} else {
			throw new IllegalArgumentException("The data format " + toWrite.getClass() + " is not supported");
		}
		if (autoclose) {
			try (Writer output = new OutputStreamWriter(entityStream)) {
				JsonUtils.write(output, data);
			}
		} else {
			JsonUtils.write(new OutputStreamWriter(entityStream), data);
		}
	}

	/**
	 * Converts a single annotation to data suitable for writing with {@link JsonUtils#write(Writer, Object)}. The
	 * method reads the content located at {@link Annotation#getContent()} and adds all properties from
	 * {@link Annotation#getResponseProperties()}. Property names are converted to short IRI format if not and values
	 * are converted to more suitable format for response. Also the methods converts and adds all replies of the given
	 * annotation to the response.
	 *
	 * @param annotation
	 *            the annotation to convert. Should not be <code>null</code>.
	 * @return the converted object ready for writing.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public Object convert(Annotation annotation) throws IOException {
		String content = annotation.getContent();

		// handle empty annotation object
		if (content == null || annotation.getId() == null) {
			return new HashMap<>(0);
		}

		Object object = JsonUtils.fromString(content);
		if (!(object instanceof Map<?, ?>)) {
			return object;
		}
		Map<Object, Object> map = (Map<Object, Object>) object;

		// insert the custom properties to the response
		map.put("@id", namespaceRegistryService.getShortUri(annotation.getId().toString()));
		map.putAll(convertProperties(annotation));
		map.remove(AnnotationProperties.ACTION.getLocalName());

		// Recursively append all information about the replies if any
		Collection<Annotation> repliesAnnotations = annotation.getReplies();
		if (CollectionUtils.isNotEmpty(repliesAnnotations)) {
			List<Object> replies = new ArrayList<>(repliesAnnotations.size());
			for (Annotation reply : repliesAnnotations) {
				replies.add(convert(reply));
			}
			map.put("emf:reply", replies);
		}
		return object;
	}

	/**
	 * Converts a multiple annotation to data suitable for writing with {@link JsonUtils#write(Writer, Object)}. The
	 * method effectively builds a collection of items produced by {@link #convert(Annotation)} method.
	 *
	 * @param annotations
	 *            the annotations to convert. Should not be <code>null</code>.
	 * @return the converted object ready for writing.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see #convert(Annotation)
	 */
	public Object convert(Collection<Annotation> annotations) throws IOException {
		List<Object> builder = new ArrayList<>(annotations.size());
		for (Annotation annotation : annotations) {
			builder.add(convert(annotation));
		}
		return builder;
	}

	private Map<String, Serializable> convertProperties(Annotation annotation) {
		Map<String, Serializable> response = annotation.getResponseProperties();
		Map<String, Serializable> converted = new HashMap<>();
		for (Entry<String, Serializable> entry : response.entrySet()) {
			String key = namespaceRegistryService.getShortUri(entry.getKey());
			addNonNullValue(converted, key, convertValue(entry.getValue()));
		}
		return converted;
	}

	private Serializable convertValue(Serializable value) {
		Serializable result;
		if (value instanceof Collection<?>) {
			result = convertCollection((Collection<?>) value);
		} else if (value instanceof Literal) {
			result = ValueConverter.convertValue((Value) value);
		} else if (value instanceof IRI) {
			result = namespaceRegistryService.getShortUri((IRI) value);
		} else if (isPrimitive(value)) {
			result = value;
		} else if (value instanceof Resource) {
			result = convertResource((Resource) value);
		} else if (value instanceof Action) {
			result = (Serializable) JSON.jsonToMap(Action.convertAction((Action) value).build());
		} else {
			result = typeConverter.convert(String.class, value);
		}
		return result;

	}

	private static Serializable convertResource(Resource value) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("@id", value.getId());
		properties.put("@type", EMF_USER);
		properties.put("emf:label", value.getDisplayName());
		properties.put("emf:icon", value.get(ResourceProperties.AVATAR, ""));
		return (Serializable) properties;
	}

	private static boolean isPrimitive(Serializable value) {
		return value instanceof String || value instanceof Boolean || value instanceof Number;
	}

	private Serializable convertCollection(Collection<?> collection) {
		if (isEmpty(collection)) {
			return null;
		}
		List<Serializable> result = new ArrayList<>(collection.size());
		for (Object object : collection) {
			result.add(convertValue((Serializable) object));
		}
		return (Serializable) result;
	}
}
