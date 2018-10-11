package com.sirma.itt.seip.permissions.mails;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.BiComputationChain;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.mails.UsersMailExtractor;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Contains logic for extraction mails from passed collection of values. Handles extraction from resources(users,
 * groups), users with given role for instance, instance property, or direct mail. Can be extended easily to handle more
 * options.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class UsersMailExtractorImpl implements UsersMailExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
			Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private BiComputationChain<String, Instance, BiFunction<String, Instance, Collection<String>>> chain = new BiComputationChain<>();

	@Inject
	private ResourceService resourceService;

	@Inject
	private PermissionService permissionService;

	/**
	 * Builds the chain steps.
	 */
	@PostConstruct
	void init() {
		buildChain();
	}

	@Override
	public Collection<String> extractMails(Collection<String> values, Instance instance) {
		if (CollectionUtils.isEmpty(values) || instance == null) {
			LOGGER.warn("The collection is empty or the instance is null.");
			return CollectionUtils.emptyList();
		}

		Set<String> mails = new HashSet<>();
		for (String value : values) {
			Collection<String> emails = executeChain(instance, value);
			mails.addAll(emails);
		}
		return mails;
	}

	/**
	 * Builds the chain with which the mails will be extracted.
	 */
	private void buildChain() {
		chain.addStep(isResource(), extractMailsFromResource());
		chain.addStep(isRole(), extractUsersByRole());
		chain.addStep(isProperty(), extractMailsFromProperty());
		chain.addStep(isEmail(), (value, instance) -> Collections.singletonList(value));
		chain.addDefault((value, instance) -> Collections.emptyList());
	}

	/**
	 * Executes the chain and applies the result function. The result for the function is returned.
	 *
	 * @param instance
	 *            the instance, from which will be extracted properties and roles, if necessary
	 * @param value
	 *            the values from which will be extracted mails
	 * @return the result from the applied function
	 */
	private Collection<String> executeChain(Instance instance, String value) {
		return chain.execute(value, instance).apply(value, instance);
	}

	// ------------------------------- resources -------------------------------------

	/**
	 * Checks, if the given value is a resource.
	 *
	 * @return predicate that tests, if given value is resource.
	 */
	private BiPredicate<String, Instance> isResource() {
		return (value, instance) -> resourceService.findResource(value) != null;
	}

	/**
	 * When applied, extract the resource and then checks if the resource is user or group. If the resource is user
	 * extract its mail property, if the resource is a group, extracts the users from it and then for every user extract
	 * the mail.
	 *
	 * @return BiFunction which extract mails from passed user or users in group
	 */
	private BiFunction<String, Instance, Collection<String>> extractMailsFromResource() {
		return (value, instance) -> {
			Resource resource = resourceService.findResource(value);
			if (Resource.IS_USER.test(resource) && resource.isActive()) {
				if (resource.getEmail() != null) {
					return Collections.singletonList(resource.getEmail());
				}
				return CollectionUtils.emptyList();
			}

			// the resource is probably group
			List<Instance> users = resourceService.getContainedResources(resource.getId());
			return users
					.stream()
					.map(Resource.class::cast)
					.filter(Resource::isActive)
					.map(Resource::getEmail)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		};
	}

	// ------------------------------- properties ------------------------------------

	/**
	 * Checks, if the passed value is a property of the passed instance.
	 *
	 * @return predicate that tests, if the given value is a property of the given instance
	 */
	private static BiPredicate<String, Instance> isProperty() {
		return (value, instance) -> instance.isPropertyPresent(value);
	}

	/**
	 * When applied, extracts the value of the instance property. Then this value is parsed(in case of a multivalue),
	 * and then the resulting values are passed to the chain one more time. This is done, because this values could be
	 * roles, users, groups, mails, etc. Before the properties are split, if there are brackets in the value(in case of
	 * array, map, JSON), they are removed.
	 * <p>
	 * Supported multivalue splitters: <b>,</b> | <b>¶</b> | <b>;</b>
	 * </p>
	 *
	 * @return BiFunction which extracts mails from the value of the given instance property
	 */
	private BiFunction<String, Instance, Collection<String>> extractMailsFromProperty() {
		return (value, instance) -> {

			Collection<String> propertyValues = getAsCollection(instance, value);

			Collection<String> emails = new LinkedList<>();
			for (String propValue : propertyValues) {
				String mail = StringUtils.trimToEmpty(propValue);
				if (!mail.isEmpty()) {
					Collection<String> userMails = executeChain(instance, mail);
					emails.addAll(userMails);
				}
			}

			return emails;
		};
	}

	@SuppressWarnings("unchecked")
	private static Collection<String> getAsCollection(Instance instance, String value) {
		Serializable propertyValue = instance.get(value);
		if (propertyValue instanceof Collection<?>) {
			return (Collection<String>) propertyValue;
		} else if (propertyValue instanceof String[]) {
			return asList((String[]) propertyValue);
		} else if (propertyValue instanceof String) {
			return asList(((String) propertyValue).replaceAll("\\{|\\}|\\[|\\]|\\(|\\)", "").split(",|¶|;"));
		}

		return Collections.emptyList();
	}

	// ------------------------------- roles ----------------------------------------------

	/**
	 * Checks, if the given value is one of the system roles. If the value is null or empty this predicate will return
	 * false.
	 *
	 * @return predicate that tests, if the passed value is role
	 */
	private static BiPredicate<String, Instance> isRole() {
		return (value, instance) -> StringUtils.isNotEmpty(value) && SecurityModel.BaseRoles.ALL.contains(
				SecurityModel.BaseRoles.getIdentifier(value.toUpperCase()));
	}

	/**
	 * When applied, extracts mails from user for given role. The roles and the users are extracted from the passed
	 * instance (second function argument). If the passed role is equals to some of the extracted of the instance, the
	 * users with this role are extracted and from them, their mails.
	 *
	 * @return BiFunction which extracts mails from users for given role
	 */
	private BiFunction<String, Instance, Collection<String>> extractUsersByRole() {
		return (value, instance) -> {
			Map<String, ResourceRole> rolesMap = permissionService.getPermissionAssignments(instance.toReference());
			Collection<String> roleMails = new LinkedList<>();

			for (Entry<String, ResourceRole> entry : rolesMap.entrySet()) {
				if (EqualsHelper.nullSafeEquals(value, entry.getValue().getRole().getIdentifier())) {
					Resource resource = resourceService.findResource(entry.getKey());
					if (resource != null) {
						String name = resource.getName();
						Collection<String> mails = extractMailsFromResource().apply(name, instance);
						roleMails.addAll(mails);
					} else {
						LOGGER.warn("Skipping mail extraction from resource {} as it was not found", entry.getKey());
					}
				}
			}

			return roleMails;
		};
	}

	// ------------------------------- mails ------------------------------------------------

	/**
	 * Checks for valid eMails using a regex pattern.
	 *
	 * @return predicate that tests the passed value, if it is email
	 */
	private static BiPredicate<String, Instance> isEmail() {
		return (value, instance) -> value != null && EMAIL_PATTERN.matcher(value).matches();
	}

}
