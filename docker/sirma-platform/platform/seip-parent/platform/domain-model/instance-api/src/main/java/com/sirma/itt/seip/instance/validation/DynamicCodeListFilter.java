package com.sirma.itt.seip.instance.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

/**
 * This class represents a code list filter that is represented with conditions in a definitions. Such a filter
 * would be:
 * <p>
 * <pre>
 * {@code
 * 	<field name="department">
 * 		<control id="RELATED_FIELDS">
 * 			<control-param id="fieldsToRerender" name="RERENDER">functional</control-param>
 * 			<control-param id="filterSource" name="FILTER_SOURCE">extra1</control-param>
 * 			<control-param id="filterInclusive" name="INCLUSIVE">true</control-param>
 * 		</control>
 * 	</field>
 * }
 * </pre>
 * </p>
 * <p>
 * This xml means that a field with name functional should be filtered in such matter that it contains only values that
 * have in extra1 the value set to department. If we set a string "OT1" to department then functional should have only
 * such code list values where their extra1 equals "OT1"
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class DynamicCodeListFilter {

	private final Function<DynamicCodeListFilter, Boolean> validateReRender = filter -> StringUtils.isNotBlank(
			filter.getReRenderFieldName());

	private final Function<DynamicCodeListFilter, Boolean> validateFilterSource = filter -> StringUtils
			.isNotBlank(
					filter.getFilterSource());

	private final Function<DynamicCodeListFilter, Boolean> validateValues = filter -> filter.getValues() != null &&
			!filter.getValues().isEmpty();

	private final Function<DynamicCodeListFilter, Boolean> validateInclusive = filter -> filter.isInclusive() != null;

	/**
	 * The other field that has to be rendered
	 */
	private String reRenderFieldName;

	private String sourceFilterFieldName;
	/**
	 * i. e. "extra1"
	 */
	private String filterSource;

	/**
	 * If the filter is inclusive.
	 *
	 * @see {@link com.sirma.itt.seip.domain.codelist.CodelistService#filterCodeValues(Integer, boolean, String, String...)}
	 */
	private Boolean isInclusive;

	/**
	 * The values of the source field from the instance. By source field we mean the field based on which we will
	 * filter. For example if field2 if filtered based on field1, here is placed the value of field1.
	 */
	private Collection<String> values;

	public Collection<String> getValues() {
		return values;
	}

	public void setValues(Collection<String> value) {
		this.values = value;
	}

	@SuppressWarnings("WeakerAccess")
	public String getFilterSource() {
		return filterSource;
	}

	public void setFilterSource(String filterSource) {
		this.filterSource = filterSource;
	}

	public String getReRenderFieldName() {
		return reRenderFieldName;
	}

	public void setReRenderFieldName(String reRenderFieldName) {
		this.reRenderFieldName = reRenderFieldName;
	}

	public String getSourceFilterFieldName() {
		return sourceFilterFieldName;
	}

	public void setSourceFilterFieldName(String sourceFilterFieldName) {
		this.sourceFilterFieldName = sourceFilterFieldName;
	}

	public Boolean isInclusive() {
		return isInclusive;
	}

	public void setInclusive(Boolean inclusive) {
		isInclusive = inclusive;
	}

	public boolean isFilterValid() {
		// The checks of validity became too many so implemented them in a  strategy-like way in order to be more
		// easy to read.
		List<Function<DynamicCodeListFilter, Boolean>> validators = new ArrayList<> (
				Arrays.asList(
						validateReRender,
						validateFilterSource,
						validateInclusive
				));
		// Values should be validated only if field is not self filtered
		if (reRenderFieldName != null && !reRenderFieldName.equals(sourceFilterFieldName)) {
			validators.add(validateValues);
		}
		return !validators
				.stream()
				.filter(validator -> !validator.apply(this))
				.findAny()
				.isPresent();
	}
}
