package com.sirma.sep.model.management.deploy;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Utility class for aggregating and grouping {@link ModelChangeSetInfo} into {@link ModelChangeSetExtension}.
 *
 * @author Mihail Radkov
 * @see ModelChangeSetExtension
 */
public class ChangeSetAggregator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String MAP_PREFIX = "key";

	private ChangeSetAggregator() {
		// Private constructor for utility class
	}

	/**
	 * Aggregates the provided list of {@link ModelChangeSetInfo} by grouping them into {@link ModelChangeSetExtension} and skips any
	 * intermediate or equal changes.
	 *
	 * @param changeSetInfos the changes to aggregate
	 * @return mapping of {@link ModelChangeSetExtension}s {@link com.sirma.sep.model.management.Path} and its first and last
	 * {@link ModelChangeSetExtension}
	 */
	public static Map<String, List<ModelChangeSetExtension>> aggregate(List<ModelChangeSetInfo> changeSetInfos) {
		Map<String, List<ModelChangeSetExtension>> pathToChangesMapping = groupChanges(changeSetInfos);
		return aggregate(pathToChangesMapping);
	}

	/**
	 * Aggregates the provided list of {@link ModelChangeSetInfo} by grouping them into {@link ModelChangeSetExtension} and skips any
	 * intermediate or equal changes.
	 * <p>
	 * Any {@link Map} values will be converted to new {@link com.sirma.sep.model.management.Path} for {@link ModelChangeSetExtension} with
	 * the original paths value and new prefix <code>/key=</code>. The actual value is constructed with the provided mapper.
	 *
	 * @param changeSetInfos the changes to aggregate
	 * @return mapping of {@link ModelChangeSetExtension}s {@link com.sirma.sep.model.management.Path} and its first and last
	 * {@link ModelChangeSetExtension}
	 */
	public static Map<String, List<ModelChangeSetExtension>> aggregate(List<ModelChangeSetInfo> changeSetInfos,
			BiFunction<String, Object, Object> mapValuesMapper) {
		Map<String, List<ModelChangeSetExtension>> pathToChangesMapping = groupChanges(changeSetInfos, mapValuesMapper);
		return aggregate(pathToChangesMapping);
	}

	private static Map<String, List<ModelChangeSetExtension>> aggregate(
			Map<String, List<ModelChangeSetExtension>> pathToChangesMapping) {
		Map<String, List<ModelChangeSetExtension>> aggregated = new LinkedHashMap<>();

		pathToChangesMapping.forEach((key, changeSets) -> {
			List<ModelChangeSetExtension> result = new ArrayList<>(changeSets.size());
			int lastIndex = 0;
			do {
				List<ModelChangeSetExtension> sameOperationList = new ArrayList<>();
				lastIndex = findChangesWithSameContinuedOperation(changeSets, lastIndex, sameOperationList::add);
				aggregateOperation(sameOperationList, result::add);
			} while (lastIndex > 0);

			if (!result.isEmpty()) {
				aggregated.put(key, result);
			}
		});

		return aggregated;
	}

	private static void aggregateOperation(List<ModelChangeSetExtension> sameOperationList,
			Consumer<ModelChangeSetExtension> changeConsumer) {
		ModelChangeSetExtension firstChange = sameOperationList.get(0);
		ModelChangeSetExtension lastChange = sameOperationList.get(sameOperationList.size() - 1);
		LOGGER.trace("Aggregating {} {} changes for path {}", sameOperationList.size(), firstChange.getOperation(),
				firstChange.getPath());

		// single change, nothing to do just return it
		if (firstChange == lastChange) {
			changeConsumer.accept(lastChange);
			LOGGER.trace("Aggregation found single change. Returning it.");
			return;
		}
		// same operation that does not modify data directly no need to return it multiple times
		// for example multiple restore operations (intermediate and actual restore)
		boolean isDataModifyingChange = sameOperationList.stream()
				.allMatch(change -> change.getNewValue() != null || change.getOldValue() != null);
		if (!isDataModifyingChange) {
			changeConsumer.accept(lastChange);
			LOGGER.trace("Aggregation found repetitive non modifying changes. Returning last change.");
			return;
		}


		if (!EqualsHelper.nullSafeEquals(firstChange.getOldValue(), lastChange.getNewValue())) {
			changeConsumer.accept(firstChange);
			changeConsumer.accept(lastChange);
			LOGGER.trace("Aggregation found data modification changes. Returning first and last change.");
		} else {
			LOGGER.trace("Aggregation found data modification changes that result to the same value. Nothing is returned.");
		}
	}

	private static int findChangesWithSameContinuedOperation(List<ModelChangeSetExtension> changes, int fromId,
			Consumer<ModelChangeSetExtension> changeConsumer) {
		if (fromId < 0 || fromId >= changes.size()) {
			return -1;
		}
		String operation = null;
		for (int i = fromId; i < changes.size(); i++) {
			ModelChangeSetExtension extension = changes.get(i);
			if (operation == null) {
				operation = extension.getOperation();
				changeConsumer.accept(extension);
			} else if (operation.equals(extension.getOperation())) {
				changeConsumer.accept(extension);
			} else {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Groups the provided list of {@link ModelChangeSetInfo} into {@link ModelChangeSetExtension}
	 * <p>
	 * Any {@link Map} values will be converted to new {@link com.sirma.sep.model.management.Path} for {@link ModelChangeSetExtension} with
	 * the original paths value and new prefix <code>/key=</code>
	 *
	 * @param changeSetInfos the changes to group
	 * @param mapValuesMapper a mapper used to map {@link Map} values in the change sets
	 * @return mapping of {@link ModelChangeSetExtension}s {@link com.sirma.sep.model.management.Path} and its related
	 * {@link ModelChangeSetExtension}
	 */
	public static Map<String, List<ModelChangeSetExtension>> groupChanges(List<ModelChangeSetInfo> changeSetInfos,
			BiFunction<String, Object, Object> mapValuesMapper) {
		return changeSetInfos.stream()
				.flatMap(changeSetInfo -> ChangeSetAggregator.flatMapValues(changeSetInfo, mapValuesMapper))
				.collect(groupChangeSets());
	}

	private static Map<String, List<ModelChangeSetExtension>> groupChanges(List<ModelChangeSetInfo> changeSetInfos) {
		return changeSetInfos.stream()
				.map(ModelChangeSetExtension::copyFrom)
				.collect(groupChangeSets());
	}

	private static Stream<ModelChangeSetExtension> flatMapValues(ModelChangeSetInfo modelChangeSet,
			BiFunction<String, Object, Object> mapValuesMapper) {
		ModelChangeSet changeSet = modelChangeSet.getChangeSet();
		if (changeSet.getOperation().equals("modifyMapAttribute")) {
			return toMapDiffStream(modelChangeSet, mapValuesMapper);
		}
		return Stream.of(ModelChangeSetExtension.copyFrom(modelChangeSet));
	}

	private static Stream<ModelChangeSetExtension> toMapDiffStream(ModelChangeSetInfo changeSetInfo,
			BiFunction<String, Object, Object> mapValuesMapper) {
		ModelChangeSet change = changeSetInfo.getChangeSet();
		Map<String, String> newValues = getAsMap(change.getNewValue());
		Map<String, String> oldValues = getAsMap(change.getOldValue());
		// create unique selector for each map key
		return Stream.concat(newValues.keySet().stream(), oldValues.keySet().stream())
				.map(flatChangeSetMapValues(changeSetInfo, change, newValues, oldValues, mapValuesMapper));
	}

	private static Function<String, ModelChangeSetExtension> flatChangeSetMapValues(ModelChangeSetInfo changeSetInfo,
			ModelChangeSet change, Map<String, String> newValues, Map<String, String> oldValues,
			BiFunction<String, Object, Object> mapValuesMapper) {
		return key -> {
			ModelChangeSetExtension extension = new ModelChangeSetExtension(changeSetInfo);
			extension.setSelector(buildMapSelector(change.getSelector(), key))
					.setNewValue(mapValuesMapper.apply(key, newValues.get(key)))
					.setOldValue(mapValuesMapper.apply(key, oldValues.get(key)));
			return extension;
		};
	}

	private static String buildMapSelector(String selector, String key) {
		return selector + "/" + MAP_PREFIX + "=" + key;
	}

	private static Map<String, String> getAsMap(Object value) {
		if (value instanceof Map) {
			return (Map<String, String>) value;
		}
		if (value instanceof String) {
			Map<String, String> aMap = new HashMap<>();
			aMap.put("en", (String) value);
			return aMap;
		}
		return Collections.emptyMap();
	}

	private static Collector<ModelChangeSetExtension, ?, LinkedHashMap<String, List<ModelChangeSetExtension>>> groupChangeSets() {
		return Collectors.groupingBy(change -> change.getPath().toString(), LinkedHashMap::new, Collectors.toList());
	}

}
