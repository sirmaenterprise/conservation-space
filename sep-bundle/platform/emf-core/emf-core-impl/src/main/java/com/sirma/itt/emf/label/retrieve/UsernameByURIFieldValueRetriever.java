package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Converts user semantic short uri to user display name
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 4)
public class UsernameByURIFieldValueRetriever extends PairFieldValueRetriever {
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<String>(1);
		SUPPORTED_FIELDS.add(FieldId.USERNAME_BY_URI);
	}
	@Inject
	private ResourceService resourceService;

	@Override
	public String getLabel(String... value) {
		if (!ArrayUtils.isEmpty(value) && value[0] != null) {
			Resource resource = resourceService.getResource(value[0]);
			if (resource != null) {
				return resource.getDisplayName();
			}
			return null;
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		offset = offset != null ? offset : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();

		List<Resource> allUsers = resourceService.getAllResources(ResourceType.USER, null);
		for (int i = 0; i < allUsers.size(); i++) {
			Resource anUser = allUsers.get(i);
			if (StringUtils.isNullOrEmpty(filter)
					|| anUser.getDisplayName().toLowerCase().startsWith(filter.toLowerCase())) {
				validateAndAddPair(results, (String) anUser.getId(), anUser.getDisplayName(),
						filter, offset, limit, total);
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
