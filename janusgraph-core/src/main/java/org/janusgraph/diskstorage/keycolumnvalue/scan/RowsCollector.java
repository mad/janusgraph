// Copyright 2017 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.janusgraph.diskstorage.keycolumnvalue.scan;

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.PermanentBackendException;
import org.janusgraph.diskstorage.keycolumnvalue.KeyColumnValueStore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.janusgraph.diskstorage.keycolumnvalue.scan.StandardScannerExecutor.PROCESSOR_TIMEOUT_MIN;
import static org.janusgraph.diskstorage.keycolumnvalue.scan.StandardScannerExecutor.Row;

/**
 * Produces data to {@link BlockingQueue<Row>}
 * for each key in {@link KeyColumnValueStore}
 *
 * @author Sergii Karpenko (sergiy.karpenko@gmail.com)
 */
abstract class RowsCollector {

    protected final KeyColumnValueStore store;
    protected final BlockingQueue<Row> rowQueue;

    RowsCollector(KeyColumnValueStore store, BlockingQueue<Row> rowQueue) {
        this.store = store;
        this.rowQueue = rowQueue;
    }

    void add(Row e) throws InterruptedException, PermanentBackendException {
        if (!rowQueue.offer(e, PROCESSOR_TIMEOUT_MIN, TimeUnit.MINUTES)) {
            throw new PermanentBackendException("Timed out waiting for processing row data - processor error likely");
        }
    }

    abstract void run() throws InterruptedException, BackendException;

    abstract void join() throws InterruptedException;

    abstract void interrupt();

    abstract void cleanup() throws PermanentBackendException;

}
