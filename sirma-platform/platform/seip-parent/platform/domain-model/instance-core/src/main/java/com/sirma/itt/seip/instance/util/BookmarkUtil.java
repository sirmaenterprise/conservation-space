package com.sirma.itt.seip.instance.util;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.instance.util.LinkProviderService;

/**
 * BookmarkUtil is responsible of building bookmarkable links to given instance. If any of required request parameters
 * are missing, then the link builder fails and builds an empty link as result.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class BookmarkUtil implements LinkProviderService {

	private static final String BROKEN_LINK = "/";

	@Inject
	private TypeConverter typeConverter;

	@Override
	public String buildLink(Serializable instanceId) {
		if (instanceId == null) {
			return BROKEN_LINK;
		}
		StringBuilder linkBuilder = new StringBuilder(64);
		buildUI2EntityOpenLink(instanceId, linkBuilder);
		return linkBuilder.toString();
	}

	private void buildUI2EntityOpenLink(Serializable id, StringBuilder builder) {
		builder.append("#/idoc");
		builder.append("/").append(typeConverter.convert(ShortUri.class, id));
	}
}
