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

package org.janusgraph.diskstorage.lucene;

import com.google.common.collect.Sets;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.StoredFields;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class SumCollector implements Collector {
    double sum = 0.0;
    final String fieldName;
    final IndexSearcher searcher;
    final Set<String> fieldSet;

    public SumCollector(String fieldName, IndexSearcher searcher) {
        this.fieldName = fieldName;
        this.searcher = searcher;
        this.fieldSet = Sets.newHashSet(fieldName);
    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
        final StoredFields storedFields = searcher.storedFields();
        final int docBase = context.docBase;
        return new LeafCollector() {
            @Override
            public void setScorer(Scorable scorer) throws IOException {}

            @Override
            public void collect(int doc) throws IOException {
                sum += storedFields.document(docBase + doc, fieldSet).getField(fieldName).numericValue().doubleValue();
            }
        };
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    public double getValue() {
        return sum;
    }

    public static class Manager implements CollectorManager<SumCollector, Double> {
        private final String fieldName;
        private final IndexSearcher searcher;

        public Manager(String fieldName, IndexSearcher searcher) {
            this.fieldName = fieldName;
            this.searcher = searcher;
        }

        @Override
        public SumCollector newCollector() throws IOException {
            return new SumCollector(fieldName, searcher);
        }

        @Override
        public Double reduce(Collection<SumCollector> collectors) throws IOException {
            double total = 0.0;
            for (SumCollector collector : collectors) {
                total += collector.getValue();
            }
            return total;
        }
    }
}
