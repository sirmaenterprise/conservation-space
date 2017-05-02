/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.util.storage;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple task that periodically sweeps over a {@link StorageService} and removes expired entries.
 */
public class ExpiringObjectStorageServiceSweeper extends TimerTask {

    /** Storage service whose entries will be periodically checked. */
    private StorageService store;

    /** Storage partitions to sweep. */
    private Set<String> partitions;

    /**
     * Constructor. Registers this task with the given timer.
     * 
     * @param taskTimer timer that will sweep the given storage service
     * @param sweepInterval interval, in milliseconds, that the storage service will be swept
     * @param sweptStore storage service that will be swept
     */
    public ExpiringObjectStorageServiceSweeper(Timer taskTimer, long sweepInterval, StorageService sweptStore) {
        store = sweptStore;
        taskTimer.schedule(this, sweepInterval);
    }

    /**
     * Constructor. Registers this task with the given timer.
     * 
     * @param taskTimer timer that will sweep the given storage service
     * @param sweepInterval interval, in milliseconds, that the storage service will be swept
     * @param sweptStore storage service that will be swept
     * @param sweptParitions the partitions to sweep, if null or empty all partitions are swept
     */
    public ExpiringObjectStorageServiceSweeper(Timer taskTimer, long sweepInterval, StorageService sweptStore,
            Set<String> sweptParitions) {
        store = sweptStore;
        if (sweptParitions != null || sweptParitions.isEmpty()) {
            partitions = sweptParitions;
        }
        taskTimer.schedule(this, sweepInterval);
    }

    /** {@inheritDoc} */
    public void run() {
        Iterator<String> sweepPartitions;
        if (partitions != null) {
            sweepPartitions = partitions.iterator();
        } else {
            sweepPartitions = store.getPartitions();
        }

        String currentParition;
        Iterator<?> partitionKeys;
        Object partitionKey;
        Object partitionValue;
        while (sweepPartitions.hasNext()) {
            currentParition = sweepPartitions.next();
            partitionKeys = store.getKeys(currentParition);
            if (partitionKeys == null) {
                continue;
            }

            while (partitionKeys.hasNext()) {
                partitionKey = partitionKeys.next();
                partitionValue = store.get(currentParition, partitionKey);
                if (partitionValue instanceof ExpiringObject) {
                    if (((ExpiringObject) partitionValue).isExpired()) {
                        partitionKeys.remove();
                    }
                }
            }
        }
    }
}