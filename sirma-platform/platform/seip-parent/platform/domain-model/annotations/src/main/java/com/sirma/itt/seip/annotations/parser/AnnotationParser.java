package com.sirma.itt.seip.annotations.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import org.eclipse.rdf4j.rio.RDFFormat;

import com.sirma.itt.emf.semantic.repository.creator.RepositoryUtils;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliterator;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Parser that reads a JSON-LD structure and converts it to {@link Annotation} instances with their replies.
 *
 * @author BBonev
 */
public class AnnotationParser {
	private static final String REPLY_KEY = EMF.PREFIX + ":" + EMF.REPLY.getLocalName();

	/**
	 * Hides default constructor for utility class.
	 */
	private AnnotationParser() {
		// nothing to do
	}

	/**
	 * Parses the annotation and their replies. This methods will fail it the stream points to a JSON array. The input
	 * will be closed after reading all of the input data.
	 *
	 * @param stream
	 *            the stream to parse
	 * @return the annotation found
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Annotation parseSingle(InputStream stream) throws IOException {
		JsonObject structure;
		try (JsonReader reader = Json.createReader(stream)) {
			structure = reader.readObject();
		}

		// read the internal tree structure
		ParserItem item = new ParserItem(structure);
		// parse all annotations and their replies
		Stream.of(item).flatMap(ParserItem::flatten).forEach(ParserItem::parse);
		// collect top level annotations
		return item.getAnnotation();
	}

	/**
	 * Parses the given stream to collection of {@link Annotation}s and their replies. The method can handle JSON array
	 * or JSON object as input. The input will be closed after reading all of the input data.
	 *
	 * @param stream
	 *            the stream to parse
	 * @return the collection of annotations found and their replies.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Collection<Annotation> parse(InputStream stream) throws IOException {
		JsonStructure structure;
		try (JsonReader reader = Json.createReader(stream)) {
			structure = reader.read();
		}

		// read all items and build the internal tree structure
		// the collection will have the top level annotations
		Collection<ParserItem> itemStream;
		if (structure instanceof JsonObject) {
			itemStream = Collections.singletonList(new ParserItem((JsonObject) structure));
		} else {
			itemStream = ((JsonArray) structure)
					.getValuesAs(JsonObject.class)
						.stream()
						.map(ParserItem::new)
						.collect(Collectors.toList());
		}

		// parse all annotations and their replies
		FixedBatchSpliterator
				.withBatchSize(itemStream.stream(), 100)
					.flatMap(ParserItem::flatten)
					.forEach(ParserItem::parse);

		// collect top level annotations only they will contain all their replies
		return itemStream.stream().map(ParserItem::getAnnotation).collect(Collectors.toList());
	}

	/**
	 * Single item that represents an annotation and their replies in raw form. The implementation will read the
	 * structure recursively.
	 *
	 * @author BBonev
	 */
	private static class ParserItem {
		private JsonObject itemData;
		private AnnotationCollectorHandler handler;
		private Annotation annotation;
		private Collection<ParserItem> items;
		private boolean build = false;

		/**
		 * Instantiates a new parser item and reads the sub tree.
		 *
		 * @param item
		 *            the item
		 */
		public ParserItem(JsonObject item) {
			itemData = item;
			readTree();
		}

		/**
		 * Flatten the item to stream consisting of the current item and it's replies
		 *
		 * @return the stream of items
		 */
		Stream<ParserItem> flatten() {
			if (CollectionUtils.isEmpty(items)) {
				return Stream.of(this);
			}
			// call flatten on the replies to work with structure of indefinite depth
			// current implementation only handles a single level of replies
			return Stream.concat(Stream.of(this), items.stream());
		}

		private void readTree() {
			if (itemData.containsKey(REPLY_KEY)) {
				// clone the item without it's replies
				JsonObjectBuilder builder = Json.createObjectBuilder();
				for (Entry<String, JsonValue> entry : itemData.entrySet()) {
					if (!entry.getKey().equals(REPLY_KEY)) {
						builder.add(entry.getKey(), entry.getValue());
					}
				}

				JsonArray array = itemData.getJsonArray(REPLY_KEY);
				// better not to use stream processing for recursive tree building this is why it's simple foreach
				items = new ArrayList<>(array.size());
				for (JsonObject reply : array.getValuesAs(JsonObject.class)) {
					items.add(new ParserItem(reply));
				}

				itemData = builder.build();
			}
		}

		/**
		 * Parses the the current item to {@link Annotation} object.
		 */
		void parse() {
			handler = new AnnotationCollectorHandler();
			String asString = itemData.toString();
			RepositoryUtils.parseRDFFile(new StringReader(asString), RDFFormat.JSONLD, "",
					handler.addHandlerToParser());
			annotation = handler.getAnnotations().iterator().next();
			// set the data that produces this annotation as content
			// if some modifications should happen to the persisted content it should go here before the set and after
			// the parse of the itemData
			annotation.setContent(asString);
		}

		/**
		 * Gets the parsed annotation and links it's replies to the instance.
		 *
		 * @return the annotation
		 */
		Annotation getAnnotation() {
			if (!build) {
				build = true;
				if (CollectionUtils.isNotEmpty(items)) {
					for (ParserItem item : items) {
						annotation.addReply(item.getAnnotation());
					}
				}
			}
			return annotation;
		}
	}
}
