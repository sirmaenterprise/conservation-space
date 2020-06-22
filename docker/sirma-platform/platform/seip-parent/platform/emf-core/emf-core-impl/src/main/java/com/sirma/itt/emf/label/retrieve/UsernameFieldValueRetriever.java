package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;

/**
 * Converts username to user display name
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 5)
public class UsernameFieldValueRetriever extends PairFieldValueRetriever {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.USERNAME);
	}

	@Inject
	private ResourceService resourceService;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null) {
			User resource = resourceService.findResource(value);
			if (resource != null) {
				return resource.getDisplayName();
			}
			return value;
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		int localOffset = offset != null ? offset.intValue() : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();

		List<Resource> allUsers = resourceService.getAllResources(ResourceType.USER, null);
		for (int i = 0; i < allUsers.size(); i++) {
			Resource anUser = allUsers.get(i);
			if (StringUtils.isBlank(filter)
					|| anUser.getDisplayName().toLowerCase().startsWith(filter.toLowerCase())) {
				validateAndAddPair(results, anUser.getName(), anUser.getDisplayName(), filter, localOffset, limit,
						total);
				total++;
			}
		}
		return new RetrieveResponse(total, results);
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
