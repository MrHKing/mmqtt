/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.monkey.mmq.core.consistency.persistent;

import com.alipay.sofa.jraft.util.CRC64;
import org.monkey.mmq.core.consistency.snapshot.AbstractSnapshotOperation;
import org.monkey.mmq.core.consistency.snapshot.LocalFileMeta;
import org.monkey.mmq.core.consistency.snapshot.Reader;
import org.monkey.mmq.core.consistency.snapshot.Writer;
import org.monkey.mmq.core.storage.kv.KvStorage;
import org.monkey.mmq.core.utils.DiskUtils;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.core.utils.Objects;

import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.Checksum;

/**
 * Snapshot processing of persistent service data for accelerated Raft protocol recovery and data synchronization.
 *
 * @author solley
 */
public class MmqSnapshotOperation extends AbstractSnapshotOperation {

    private static final String NAMING_SNAPSHOT_SAVE = MmqSnapshotOperation.class.getSimpleName() + ".SAVE";

    private static final String NAMING_SNAPSHOT_LOAD = MmqSnapshotOperation.class.getSimpleName() + ".LOAD";

    private final String snapshotDir = "naming_persistent";

    private final String snapshotArchive = "naming_persistent.zip";

    private final KvStorage storage;

    public MmqSnapshotOperation(KvStorage storage, ReentrantReadWriteLock lock) {
        super(lock);
        this.storage = storage;
    }
    
    @Override
    protected boolean writeSnapshot(Writer writer) throws Exception {
        final String writePath = writer.getPath();
        final String parentPath = Paths.get(writePath, snapshotDir).toString();
        DiskUtils.deleteDirectory(parentPath);
        DiskUtils.forceMkdir(parentPath);
        
        storage.doSnapshot(parentPath);
        final String outputFile = Paths.get(writePath, snapshotArchive).toString();
        final Checksum checksum = new CRC64();
        DiskUtils.compress(writePath, snapshotDir, outputFile, checksum);
        DiskUtils.deleteDirectory(parentPath);
        
        final LocalFileMeta meta = new LocalFileMeta();
        meta.append(CHECK_SUM_KEY, Long.toHexString(checksum.getValue()));
        return writer.addFile(snapshotArchive, meta);
    }
    
    @Override
    protected boolean readSnapshot(Reader reader) throws Exception {
        final String readerPath = reader.getPath();
        final String sourceFile = Paths.get(readerPath, snapshotArchive).toString();
        final Checksum checksum = new CRC64();
        DiskUtils.decompress(sourceFile, readerPath, checksum);
        LocalFileMeta fileMeta = reader.getFileMeta(snapshotArchive);
        if (fileMeta.getFileMeta().containsKey(CHECK_SUM_KEY)) {
            if (!Objects.equals(Long.toHexString(checksum.getValue()), fileMeta.get(CHECK_SUM_KEY))) {
                throw new IllegalArgumentException("Snapshot checksum failed");
            }
        }
        final String loadPath = Paths.get(readerPath, snapshotDir).toString();
        storage.snapshotLoad(loadPath);
        Loggers.RAFT.info("snapshot load from : {}", loadPath);
        DiskUtils.deleteDirectory(loadPath);
        return true;
    }
    
    @Override
    protected String getSnapshotSaveTag() {
        return NAMING_SNAPSHOT_SAVE;
    }
    
    @Override
    protected String getSnapshotLoadTag() {
        return NAMING_SNAPSHOT_LOAD;
    }
}
