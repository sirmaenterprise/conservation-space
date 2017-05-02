package com.sirma.itt.seip.permissions;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;

/**
 * The RestorePermissionInhiratanceModel action is responsible to invoke permission set for each instance provided in
 * the context.
 */
@ApplicationScoped
@Named(RestorePermissionInheritanceModel.ACTION_NAME)
public class RestorePermissionInheritanceModel extends SchedulerActionAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String ACTION_NAME = "RestorePermissionInheritanceModel";
	public static final String KEY_INSTANCES = "permissions.restore.inheritance.instanceids";

	private static final String KEY_FAILED_INSTANCES = "permissions.restore.inheritance.failed.instanceids";

	@Inject
	private PermissionService permissionService;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	/**
	 * Executes the actual update.
	 *
	 * @param context
	 *            the context
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void execute(final SchedulerContext context) {
//		Collection<String> references = (Collection<String>) context.get(KEY_INSTANCES);
//		if (references != null) {
//			Collection<InstanceReference> resolvedReferences = instanceTypeResolver.resolveReferences(references);
//			executeRestore(context, resolvedReferences);
//			return;
//		}
//		references = (Collection<String>) context.get(KEY_FAILED_INSTANCES);
//		Collection<InstanceReference> resolvedReferences = instanceTypeResolver.resolveReferences(references);
//		executeRestoreOnFailed(resolvedReferences);

	}

	// private void executeRestore(SchedulerContext context, Collection<InstanceReference> references) {
	// Iterator<InstanceReference> iterator = references.iterator();
	// while (iterator.hasNext()) {
	// InstanceReference instanceReference = iterator.next();
	// try {
	// // set the inherited and remove the special
	// permissionService.setInheritedPermissions(instanceReference, true);
	// // remove on success
	// iterator.remove();
	// } catch (Exception e) {
	// LOGGER.warn("Restore permission for '{}' failed with error: {} ", instanceReference, e);
	// }
	// }
	// if (!references.isEmpty()) {
	// context.remove(KEY_INSTANCES);
	// context.put(KEY_FAILED_INSTANCES, (Serializable) references);
	// SchedulerConfiguration config = schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED);
	// config.setSynchronous(false);
	// config.setMaxRetryCount(10);
	// config.setRetryDelay(60L);
	// config.setTransactionMode(TransactionMode.NOT_SUPPORTED);
	// config.setRemoveOnSuccess(true);
	// config.setScheduleTime(new Date());
	// schedulerService.reschedule(context, config);
	// }
	// }
	//
	// private void executeRestoreOnFailed(Collection<InstanceReference> references) {
	// Iterator<InstanceReference> iterator = references.iterator();
	// while (iterator.hasNext()) {
	// InstanceReference instanceReference = iterator.next();
	// // set the inherited and remove the special
	// permissionService.setInheritedPermissions(instanceReference, true);
	// }
	// }
}