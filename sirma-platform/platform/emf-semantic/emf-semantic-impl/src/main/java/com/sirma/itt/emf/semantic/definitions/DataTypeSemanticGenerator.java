package com.sirma.itt.emf.semantic.definitions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Dynamic load for {@link DataTypeDefinition}s generated based on the semantic classes
 *
 * @author BBonev
 */
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 150)
public class DataTypeSemanticGenerator implements DefinitionManagementServiceExtension {
	private static final String TYPE_ID = EMF.DEFINITION_ID.getLocalName();
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private Instance<TransactionalRepositoryConnection> connectionProvider;

	@Override
	@SuppressWarnings("rawtypes")
	public List<Class> getSupportedObjects() {
		return Arrays.asList(DataTypeDefinition.class, DataType.class);
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return Collections.singletonList(FileDescriptor.create("types.xml", () -> readSemanticTypes(), -1));
		}
		return Collections.emptyList();
	}

	private InputStream readSemanticTypes() {
		String query = SemanticQueries.QUERY_CLASSES_TYPES.getQuery();
		try (TransactionalRepositoryConnection connection = connectionProvider.get()) {
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(connection, query, CollectionUtils.emptyMap(),
					true);
			TupleQueryResult queryResult = tupleQuery.evaluate();
			try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(queryResult)) {
				Map<String, List<DataType>> types = iterator
						.stream(false)
							.filter(searchableClasses())
							.map(toDataType())
							.filter(validTypes())
							.collect(Collectors.groupingBy(DataType::getName, Collectors.toList()));
				List<DataType> merged = mergeGroups(types);
				return buildTempFile(merged);
			}
		} catch (RepositoryException | QueryEvaluationException e) {
			LOGGER.warn("Could not generate dynamic types.xml", e);
		}
		return new ByteArrayInputStream(new byte[0]);
	}

	private static Function<? super BindingSet, DataType> toDataType() {
		return bs -> {
			DataType type = new DataType();
			type.setName(Objects.toString(ValueConverter.convertValue(bs.getValue(TYPE_ID)), null));
			type.setUri(Objects.toString(ValueConverter.convertValue(bs.getValue("instance")), null));
			return type;
		};
	}

	private static Predicate<? super DataType> validTypes() {
		return type -> type.getName() != null && type.getUri() != null;
	}

	private static List<DataType> mergeGroups(Map<String, List<DataType>> types) {
		List<DataType> merged = new ArrayList<>(types.size());
		for (List<DataType> dataTypes : types.values()) {
			DataType type = new DataType();
			DataType dataType = dataTypes.get(0);
			type.setName(dataType.getName());
			type.setUri(dataTypes.stream().map(DataType::getUri).distinct().collect(Collectors.joining(",")));
			LOGGER.debug("Found for type {} the classes {}", type.getName(), type.getUri());
			merged.add(type);
		}
		return merged;
	}

	private static InputStream buildTempFile(List<DataType> merged) {
		String baseTemplateFile = loadTemplate("types-template.xml");
		String singleTypeTemplate = loadTemplate("type-template.xml");

		String types = merged
				.stream()
					.map(type -> MessageFormat.format(singleTypeTemplate, type.getName(), type.getUri()))
					.collect(Collectors.joining());
		String typeXml = MessageFormat.format(baseTemplateFile, types);
		return new ByteArrayInputStream(typeXml.getBytes(StandardCharsets.UTF_8));
	}

	private static String loadTemplate(String name) {
		return ResourceLoadUtil.loadResource(DataTypeSemanticGenerator.class, name);
	}

	private static Predicate<? super BindingSet> searchableClasses() {
		return bs -> {
			Value value = bs.getValue("searchable");
			Serializable convertedValue = ValueConverter.convertValue(value);
			return Boolean.TRUE.equals(convertedValue);
		};
	}

}
