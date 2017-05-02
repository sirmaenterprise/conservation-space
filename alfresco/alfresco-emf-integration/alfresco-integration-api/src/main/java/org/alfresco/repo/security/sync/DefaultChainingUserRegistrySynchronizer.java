/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.sync;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * A <code>TenantChainingUserRegistrySynchronizer</code> is responsible for
 * synchronizing Alfresco's local user (person) and group (authority)
 * information with the external subsystems in the authentication chain (most
 * typically LDAP directories). When the {@link #synchronize(boolean)} method is
 * called, it visits each {@link UserRegistry} bean in the 'chain' of
 * application contexts, managed by a {@link ChildApplicationContextManager},
 * and compares its timestamped user and group information with the local users
 * and groups last retrieved from the same source. Any updates and additions
 * made to those users and groups are applied to the local copies. The ordering
 * of each {@link UserRegistry} in the chain determines its precedence when it
 * comes to user and group name collisions. The {@link JobLockService} is used
 * to ensure that in a cluster, no two nodes actually run a synchronize at the
 * same time.
 * <p>
 * The <code>force</code> argument determines whether a complete or partial set
 * of information is queried from the {@link UserRegistry}. When
 * <code>true</code> then <i>all</i> users and groups are queried. With this
 * complete set of information, the synchronizer is able to identify which users
 * and groups have been deleted, so it will delete users and groups as well as
 * update and create them. Since processing all users and groups may be fairly
 * time consuming, it is recommended this mode is only used by a background
 * scheduled synchronization job. When the argument is <code>false</code> then
 * only those users and groups modified since the most recent modification date
 * of all the objects last queried from the same {@link UserRegistry} are
 * retrieved. In this mode, local users and groups are created and updated, but
 * not deleted (except where a name collision with a lower priority
 * {@link UserRegistry} is detected). This 'differential' mode is much faster,
 * and by default is triggered on subsystem startup and also by
 * {@link #createMissingPerson(String)} when a user is successfully
 * authenticated who doesn't yet have a local person object in Alfresco. This
 * should mean that new users and their group information are pulled over from
 * LDAP servers as and when required.
 * 
 * @author dward
 */
public class DefaultChainingUserRegistrySynchronizer extends ChainingUserRegistrySynchronizer
		implements UserRegistrySynchronizer, ApplicationEventPublisherAware {

	@Override
	public void synchronize(String tenantId, boolean forceUpdate, boolean isFullSync, boolean splitTxns) {
		// do the default
		synchronize(forceUpdate, isFullSync, splitTxns);
	}

}