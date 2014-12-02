package com.sirma.itt.emf.semantic.queries;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * SPARQL named query builder
 *
 * @author Valeri Tishev
 */
@ApplicationScoped
public class QueryBuilder {

	public static final String SELECT_BY_IDS = "SELECT_BY_IDS";
	public static final String LOAD_PROPERTIES = "LOAD_PROPERTIES_BY_IDS";
	public static final String CHECK_EXISTING_INSTANCE = "CHECK_EXISTING_INSTANCE";

	public static final String URI = "uri";
	public static final String PROPERTY_NAME = "propertyName";
	public static final String PROPERTY_VALUE = "propertyValue";
	public static final String PARENT = "parent";
	public static final String PARENT_TYPE = "parentType";

	private static final String SELECT_SINGLE =
					"\t { \n" +
					"\t\t SELECT DISTINCT (%s as ?" + URI + ")"
					+ " ?" + PROPERTY_NAME + " ?" + PROPERTY_VALUE + " ?" + PARENT + " ?" + PARENT_TYPE
					+ " { \n" + "\t\t\t %s ?" + PROPERTY_NAME + " ?" + PROPERTY_VALUE + " . \n"
					// fetch only registered properties and does not return relations
					+ "\t\t\t ?" + PROPERTY_NAME + " a owl:DatatypeProperty. "
//					+ "\n \t\t\t optional { %s ptop:partOf" +	" ?" + PARENT + " ."
//					+ " ?" + PARENT + " a ?pt. ?pt emf:definitionId ?" + PARENT_TYPE + " .}" +
					// filter deleted objects
					+ "\n\t\t\t  %s emf:" + EMF.IS_DELETED.getLocalName()
					+ " \"false\"^^xsd:boolean . " +
					"\n\t\t } \n" +
					"\t }";

	private static final String UNION = "\n\t UNION";

	private static final String SELECT_MULTIPLE_START = "SELECT DISTINCT" +
			" ?" + URI +
 " ?"
			+ PROPERTY_NAME + " ?" + PROPERTY_VALUE + " ?" + PARENT + " ?" + PARENT_TYPE
			+ " WHERE { \n";

	private static final String SELECT_MULTIPLE_END = "\n}";

	private static final String SELECT_PROPERTIES = "\t { \n" + "\t\t SELECT DISTINCT (%s as ?"
			+ URI + ")" + " ?" + PROPERTY_NAME + " ?" + PROPERTY_VALUE + " { \n" + "\t\t\t %s ?"
			+ PROPERTY_NAME + " ?" + PROPERTY_VALUE + " . \n" + "\t\t } \n" + "\t }";

	/** The select instances callback. */
	private final QueryBuilderCallback selectInstancesCallback = new QueryBuilderCallback() {

		Set<String> PARAMS = Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("URIS")));
		@Override
		public String singleValue(Serializable object) {
			return String.format(SELECT_SINGLE, object, object, object, object);
		}

		@Override
		public String getStart() {
			return SELECT_MULTIPLE_START;
		}

		@Override
		public String getEnd() {
			return SELECT_MULTIPLE_END;
		}

		@Override
		public Set<String> paramNames() {
			return PARAMS;
		}
	};

	/** The load properties callback. */
	private final QueryBuilderCallback loadPropertiesCallback = new QueryBuilderCallback() {
		Set<String> PARAMS = Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("URIS")));
		@Override
		public String singleValue(Serializable object) {
			return String.format(SELECT_PROPERTIES, object, object);
		}

		@Override
		public String getStart() {
			return "SELECT DISTINCT" + " ?" + URI + " ?" + PROPERTY_NAME + " ?" + PROPERTY_VALUE
					+ " WHERE { \n";
		}

		@Override
		public String getEnd() {
			return SELECT_MULTIPLE_END;
		}

		@Override
		public Set<String> paramNames() {
			return PARAMS;
		}
	};

	/** The check existing instance query - checks if the passed URIs as parameter
	 * exist in the repository and the objects aren`t deleted. */
	private final QueryBuilderCallback checkExistingInstances = new QueryBuilderCallback() {
		Set<String> PARAMS = Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("URIS")));
		@Override
		public String singleValue(Serializable object) {
			return String
					.format("{ select (%s as ?instance) ?instanceType where {%s a ?type. %s sesame:directType ?instanceType .} }",
							object, object, object);
		}

		@Override
		public String getStart() {
			return "SELECT DISTINCT ?instance ?instanceType WHERE {";
		}

		@Override
		public String getEnd() {
			return " filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) . }";
		}

		@Override
		public Set<String> paramNames() {
			return PARAMS;
		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/** The builders. */
	private Map<String, QueryBuilderCallback> builders;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		builders = CollectionUtils.createHashMap(5);
		builders.put(LOAD_PROPERTIES, loadPropertiesCallback);
		builders.put(SELECT_BY_IDS, selectInstancesCallback);
		builders.put(CHECK_EXISTING_INSTANCE, checkExistingInstances);
	}

	/**
	 * Builds the query.
	 *
	 * @param callback
	 *            the callback
	 * @param objectUris
	 *            the object uris
	 * @return the string
	 */
	private String buildQuery(QueryBuilderCallback callback,
			Collection<Serializable> objectUris) {
		StringBuilder builder = new StringBuilder();

		Iterator<Serializable> iterator = objectUris.iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		builder.append(callback.getStart());
		boolean isFirst = true;
		while (iterator.hasNext()) {
			Serializable uri = iterator.next();
			if (isFirst) {
				isFirst = false;
			} else {
				builder.append(UNION);
			}
			builder.append(callback.singleValue(uri));
		}
		builder.append(callback.getEnd());
		return builder.toString();
	}


		/**
	 * Builds predefined SPARQL query by given name
	 *
	 * @param <E>
	 *            the query parameters element type
	 * @param name
	 *            the query name
	 * @param params
	 *            the query parameters
	 * @return the the query or {@code null} in case no named query is defined for given name
	 */
	@SuppressWarnings("unchecked")
	public <E extends Pair<String, Object>> String buildQueryByName(String name, List<E> params) {
		QueryBuilderCallback callback = builders.get(name);
		if (callback == null) {
			LOGGER.warn("Undefined SPARQL query for name [" + name + "]");
			return null;
		}
		Set<String> names = callback.paramNames();
		if (params.isEmpty() && names.isEmpty()) {
			return buildQuery(callback, Collections.EMPTY_LIST);
		}
		String query = null;
		// TODO: this should be optimized but for now will do the work
		for (E param : params) {
			if (names.contains(param.getFirst())) {
				Collection<Serializable> uris = (Collection<Serializable>) param.getSecond();
				query = buildQuery(callback, uris);
			}
		}
		if (query != null) {
			String namespaces = namespaceRegistryService.getNamespaces();
			StringBuilder builder = new StringBuilder(namespaces.length() + query.length());
			builder.append(namespaces).append(query);
			return builder.toString();
		}

		return null;
	}

	/**
	 * Callback used when building dynamic semantic queries. The callback provides information about
	 * the query that is going to be build using it. If the query does not have any iterable
	 * arguments the methods {@link #getStart()} and {@link #getEnd()} will be the only one called.
	 *
	 * @author BBonev
	 */
	public interface QueryBuilderCallback {

		/**
		 * Gets the start of the query
		 *
		 * @return the start of a query. Should not be null.
		 */
		String getStart();

		/**
		 * Gets the end of the query
		 *
		 * @return the end of the query if needed. If not return empty string but not null.
		 */
		String getEnd();

		/**
		 * Single value query. intermediate query to be build using the given argument.
		 *
		 * @param object
		 *            the object
		 * @return the string of the query to be joined using UNION
		 */
		String singleValue(Serializable object);

		/**
		 * The parameter names that are expected to be passed when building the query.
		 *
		 * @return the sets of parameter names
		 */
		Set<String> paramNames();
	}

}
