package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * Converts user semantic short uri to user display name
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 4)
public class UsernameByURIFieldValueRetriever extends PairFieldValueRetriever {
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	/** Optional request parameter indicating if full URIs have to be returned **/
	private static final String REQUEST_RETURN_FULL_URIS = "returnFullUris";

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.USERNAME_BY_URI);
	}

	@Inject
	private ResourceService resourceService;

	@Inject
	protected TypeConverter typeConverter;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null) {
			Resource resource = resourceService.findResource(value);
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
		boolean useFullUri = additionalParameters.getFirstBoolean(REQUEST_RETURN_FULL_URIS);

		List<Resource> allUsers = resourceService.getAllResources(ResourceType.USER, null);

		long total = allUsers.stream().filter(r -> filterUser(r, filter)).count();

		List<Pair<String, String>> results = Collections.emptyList();
		if (total > 0L) {
			results = allUsers
					.stream()
						.filter(r -> filterUser(r, filter))
						.skip(offset == null || offset.longValue() < 0L ? 0L : offset.longValue())
						.limit(limit == null ? Long.MAX_VALUE : limit.longValue())
						.map(r -> resourceToPair(useFullUri, r))
						.collect(Collectors.toCollection(LinkedList::new));
		}

		return new RetrieveResponse(total, results);
	}

	private Pair<String, String> resourceToPair(boolean useFullUri, Resource r) {
		return new Pair<>(useFullUri ? convertToFullUri((String) r.getId()) : (String) r.getId(),
				r.getDisplayName());
	}

	private static boolean filterUser(Resource r, String filter) {
		return StringUtils.isBlank(filter) || r.getDisplayName().toLowerCase().startsWith(filter.toLowerCase());
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}

	/**
	 * Converts the given short URI into a full URI.
	 *
	 * @param shortUri
	 *            is the short URI
	 * @return the full URI
	 */
	private String convertToFullUri(String shortUri) {
		Uri fullUri = typeConverter.convert(Uri.class, shortUri);
		if (fullUri != null) {
			return fullUri.toString();
		}
		return shortUri;
	}
}
