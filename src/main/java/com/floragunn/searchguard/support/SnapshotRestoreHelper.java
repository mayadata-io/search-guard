/*
 * Copyright 2015-2017 floragunn GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.floragunn.searchguard.support;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.snapshots.SnapshotId;
import org.elasticsearch.snapshots.SnapshotInfo;
import org.elasticsearch.snapshots.SnapshotUtils;
import org.elasticsearch.threadpool.ThreadPool;

import com.floragunn.searchguard.SearchGuardPlugin;

public class SnapshotRestoreHelper {

    protected static final Logger log = LogManager.getLogger(SnapshotRestoreHelper.class);
    
    public static List<String> resolveOriginalIndices(RestoreSnapshotRequest restoreRequest) {
        final SnapshotInfo snapshotInfo = getSnapshotInfo(restoreRequest);

        if (snapshotInfo == null) {
            log.warn("snapshot repository '" + restoreRequest.repository() + "', snapshot '" + restoreRequest.snapshot() + "' not found");
            return null;
        } else {
            return SnapshotUtils.filterIndices(snapshotInfo.indices(), restoreRequest.indices(), restoreRequest.indicesOptions());
        }    
        
        
    }
    
    public static SnapshotInfo getSnapshotInfo(RestoreSnapshotRequest restoreRequest) {
        final RepositoriesService repositoriesService = Objects.requireNonNull(SearchGuardPlugin.GuiceHolder.getRepositoriesService(), "RepositoriesService not initialized");     
        final Repository repository = repositoriesService.repository(restoreRequest.repository());
        final String threadName = Thread.currentThread().getName();
        SnapshotInfo snapshotInfo = null;
        
        try {
            setCurrentThreadName(ThreadPool.Names.GENERIC);            
            for (final SnapshotId snapshotId : repository.getRepositoryData().getSnapshotIds()) {
                if (snapshotId.getName().equals(restoreRequest.snapshot())) {

                    if(log.isDebugEnabled()) {
                        log.debug("snapshot found: {} (UUID: {})", snapshotId.getName(), snapshotId.getUUID());
                    }

                    snapshotInfo = repository.getSnapshotInfo(snapshotId);
                    break;
                }
            }
        } finally {
            setCurrentThreadName(threadName);
        }
        return snapshotInfo;
    }
    
    private static void setCurrentThreadName(final String name) {
        final SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                Thread.currentThread().setName(name);
                return null;
            }
        });
    }
    
}
