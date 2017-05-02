package com.sirma.itt.emf.audit.command.instance;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.AuditAbstractCommand;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
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

	@Override
	public void execute(AuditablePayload payload, AuditActivity activity) {
		Instance instance = getInstance(payload);
		if (instance != null && activity != null) {
			List<Instance> parentPath = InstanceUtil.getParentPath(instance, false);
			List<Instance> extraPath = InstanceUtil.getParentPath(payload.getExtraContext(), false);

			int estimatedSize = parentPath.size() + extraPath.size();
			Set<Instance> contextSet = Stream
					.concat(parentPath.stream(), extraPath.stream())
						.filter(i -> !EqualsHelper.entityEquals(i, instance))
						.collect(Collectors.toCollection(() -> CollectionUtils.createLinkedHashSet(estimatedSize)));

			if (!contextSet.isEmpty() && payload.showParentPath()) {
				String context = getContext(contextSet, activity.getContext());
				activity.setContext(context);
			}
		}
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		if (StringUtils.isNotNullOrEmpty(activity.getContext())) {
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
	 * @param parentPath
	 *            - the path as a collection
	 * @return a string of URIs separated by semicolon
	 */
	private static String getContext(Collection<Instance> parentPath, String existingContext) {
		StringBuilder contextBuilder = new StringBuilder(parentPath.size() * URI_SIZE);

		if (StringUtils.isNotNullOrEmpty(existingContext)) {
			contextBuilder.append(existingContext);
		}

		for (Instance parentInstance : parentPath) {
			if (contextBuilder.length() > 0) {
				contextBuilder.append(CONTEXT_SEPARATOR);
			}
			contextBuilder.append(parentInstance.getId());
		}

		return contextBuilder.toString();
	}
}
