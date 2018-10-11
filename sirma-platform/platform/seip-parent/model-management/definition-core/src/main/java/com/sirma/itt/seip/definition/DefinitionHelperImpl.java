package com.sirma.itt.seip.definition;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Helper methods for working with instance definitions: loading, sorting, copying.
 *
 * @author svelikov
 * @author A. Kunchev
 */
@ApplicationScoped
public class DefinitionHelperImpl implements DefinitionHelper {

	@Inject
	protected CodelistService codelistService;

	@Inject
	protected UserPreferences userPreferences;

	@Override
	public Collection<Ordinal> collectAllFields(DefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<Ordinal> result = new LinkedList<>(model.getFields());
		if (model instanceof RegionDefinitionModel) {
			result.addAll(getRegionsCopy((RegionDefinitionModel) model));
		}

		DefinitionUtil.sort(result);
		return result;
	}

	/**
	 * Creates a region definition copy to be used, when fields would be modified or reordered.
	 *
	 * @param model
	 *            the region definition that will be cloned
	 * @return the region definition copy
	 */
	private static Collection<RegionDefinition> getRegionsCopy(RegionDefinitionModel model) {
		Collection<RegionDefinition> regions = model.getRegions();
		Collection<RegionDefinition> regionsCopy = new ArrayList<>(regions.size());
		for (RegionDefinition region : regions) {
			regionsCopy.add(((RegionDefinitionImpl) region).createCopy());
		}

		return regionsCopy;
	}

	@Override
	public String getDefinitionLabel(DefinitionModel definition) {
		if (definition == null) {
			return null;
		}

		return definition
				.getField(TYPE)
					.filter(property -> property.getDefaultValue() != null && property.getCodelist() != null)
					.map(property -> codelistService.getCodeValue(property.getCodelist(), property.getDefaultValue()))
					.map(codeValue -> (String) codeValue.get(userPreferences.getLanguage()))
					.orElse(definition.getIdentifier());

	}

}
