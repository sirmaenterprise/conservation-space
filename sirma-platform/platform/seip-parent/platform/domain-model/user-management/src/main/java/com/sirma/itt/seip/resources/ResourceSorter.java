package com.sirma.itt.seip.resources;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DateConverterImpl;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Provides means to sort {@link Resource}s.
 *
 * @author smustafov
 */
@ApplicationScoped
public class ResourceSorter {

	@Inject
	private UserPreferences userPreferences;

	private static Locale locale;

	private static Comparator<Instance> resourceComparator = (Instance current, Instance next) -> {
		Collator coll = Collator.getInstance(locale);
		coll.setStrength(Collator.PRIMARY);
		return coll.compare(current.getLabel(), next.getLabel());
	};

	private static void setLocale(String lang) {
		if (locale == null) {
			locale = DateConverterImpl.getLocaleForLang(lang);
		}
	}

	/**
	 * Sorts resources by display name.
	 *
	 * @param resources
	 *            the resources to sort
	 */
	public void sort(List<? extends Instance> resources) {
		setLocale(userPreferences.getLanguage());
		Collections.sort(resources, resourceComparator);
	}

}
