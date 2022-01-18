package org.monkey.mmq.core.storage.kv;

import org.monkey.mmq.core.exception.ErrorCode;
import org.monkey.mmq.core.exception.KvStorageException;
import org.monkey.mmq.core.utils.ByteUtils;
import org.monkey.mmq.core.utils.DiskUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassNameRocksDBStorage
 * @Description
 * @Author Solley
 * @Date2022/1/18 18:16
 * @Version V1.0
 **/
public class RocksDBStorage implements KvStorage {

    private final String baseDir;

    /**
     * Ensure that a consistent view exists when implementing file copies.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    static{
        RocksDB.loadLibrary();
    }

    RocksDB rocksDB;

    public RocksDBStorage(String baseDir) throws RocksDBException, IOException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        this.baseDir = baseDir;
        DiskUtils.forceMkdir(baseDir);
        rocksDB = RocksDB.open(options, baseDir);
    }

    @Override
    public byte[] get(byte[] key) throws KvStorageException {
        try {
            return rocksDB.get(key);
        } catch (RocksDBException e) {
            throw new KvStorageException(ErrorCode.KVStorageReadError, e.getMessage());
        }
    }

    @Override
    public Map<byte[], byte[]> batchGet(List<byte[]> keys) throws KvStorageException {
        Map<byte[], byte[]> result = new HashMap<>(keys.size());
        for (byte[] key : keys) {
            byte[] val = get(key);
            if (val != null) {
                result.put(key, val);
            }
        }
        return result;
    }

    @Override
    public void put(byte[] key, byte[] value) throws KvStorageException {
        try {
            rocksDB.put(key, value);
        } catch (RocksDBException e) {
            throw new KvStorageException(ErrorCode.KVStorageWriteError, e);
        }
    }

    @Override
    public void batchPut(List<byte[]> keys, List<byte[]> values) throws KvStorageException {
        if (keys.size() != values.size()) {
            throw new KvStorageException(ErrorCode.KVStorageBatchWriteError,
                    "key's size must be equal to value's size");
        }
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            put(keys.get(i), values.get(i));
        }
    }

    @Override
    public void delete(byte[] key) throws KvStorageException {
        try {
            rocksDB.delete(key);
        } catch (RocksDBException e) {
            throw new KvStorageException(ErrorCode.KVStorageDeleteError, e);
        }
    }

    @Override
    public void batchDelete(List<byte[]> keys) throws KvStorageException {
        for (byte[] key : keys) {
            delete(key);
        }
    }

    @Override
    public void doSnapshot(String backupPath) throws KvStorageException {
        writeLock.lock();
        try {
            File srcDir = Paths.get(baseDir).toFile();
            File descDir = Paths.get(backupPath).toFile();
            DiskUtils.copyDirectory(srcDir, descDir);
        } catch (IOException e) {
            throw new KvStorageException(ErrorCode.IOCopyDirError, e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void snapshotLoad(String path) throws KvStorageException {
        writeLock.lock();
        try {
            File srcDir = Paths.get(path).toFile();
            // If snapshot path is non-exist, means snapshot is empty
            if (srcDir.exists()) {
                // First clean up the local file information, before the file copy
                DiskUtils.deleteDirThenMkdir(baseDir);
                File descDir = Paths.get(baseDir).toFile();
                DiskUtils.copyDirectory(srcDir, descDir);
            }
        } catch (IOException e) {
            throw new KvStorageException(ErrorCode.IOCopyDirError, e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<byte[]> allKeys() throws KvStorageException {
        RocksIterator iter = rocksDB.newIterator();
        List<byte[]> keys = new ArrayList<>();
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            keys.add(iter.key());
        }
        return keys;
    }

    @Override
    public void shutdown() {
        rocksDB.close();
    }
}
