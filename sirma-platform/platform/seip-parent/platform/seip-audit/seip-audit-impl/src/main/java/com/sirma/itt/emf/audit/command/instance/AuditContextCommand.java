package com.sirma.itt.emf.audit.command.instance;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Collects the context of given object by its parent path.
 *
 * @author Mihail Radkov
 */
@Extension(target = AuditCommand.TARGET_NAME, order = 16)
public class AuditContextCommand extends AuditAbstractCommand {

	// TODO: Review after redesign
	/* Short URI + separator */
	private static final int URI_SIZE = 50;

	private static final String CONTEXT_SEPARATOR = ";";
	@Inject
	private InstanceContextService contextService;

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null && activity != null && payload.showParentPath()) {
			List<InstanceReference> parentPath = contextService.getFullPath(instance);
			List<InstanceReference> extraPath = contextService.getFullPath(payload.getExtraContext());

			int estimatedSize = parentPath.size() + extraPath.size();
			Set<InstanceReference> contextSet = Stream
					.concat(parentPath.stream(), extraPath.stream())
						.filter(i -> !EqualsHelper.nullSafeEquals(i.getId(), instance.getId()))
						.collect(Collectors.toCollection(() -> CollectionUtils.createLinkedHashSet(estimatedSize)));

			if (!contextSet.isEmpty()) {
				String context = getContext(contextSet, activity.getContext());
				activity.setContext(context);
			}
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		if (StringUtils.isNotBlank(activity.getContext())) {
			String[] uris = activity.getContext().split(CONTEXT_SEPARATOR);
			// TODO: Use list/set?
			for (int i = 0; i < uris.length; i++) {
				String label = context.getObjectHeaders().get(uris[i]);
				if (label != null) {
					uris[i] = label;
				}
			}
			activity.setContext(org.apache.commons.lang.StringUtils.join(uris, ", "));
		}
	}

	/**
	 * Collects the instance URIs from the provided path.
	 *
	 * @param contextSet
	 *            - the path as a collection
	 * @return a string of URIs separated by semicolon
	 */
	private static String getContext(Set<InstanceReference> contextSet, String existingContext) {
		StringBuilder contextBuilder = new StringBuilder(contextSet.size() * URI_SIZE);

		if (StringUtils.isNotBlank(existingContext)) {
			contextBuilder.append(existingContext);
		}

		for (InstanceReference parentInstance : contextSet) {
			if (contextBuilder.length() > 0) {
				contextBuilder.append(CONTEXT_SEPARATOR);
			}
			contextBuilder.append(parentInstance.getId());
		}

		return contextBuilder.toString();
	}
}
