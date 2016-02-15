package org.ihtsdo.json;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.mapdb.BTreeKeySerializer.Tuple2KeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import static org.mapdb.Fun.filter;
import static org.mapdb.Fun.t2;
import org.mapdb.Serializer;

/**
 * Information on why this class is the right way to implement Maps of Lists in
 * MapDB here:
 *
 * https://github.com/jankotek/mapdb/blob/mapdb-1.0.8/src/test/java/examples/MultiMap.java
 */
public final class StringMultiMap<V> extends AbstractMap<String, Iterable<V>> implements Serializable {

    private static final int NODE_SIZE = 120;
    private final NavigableSet<Tuple2<String, ComparableElement<V>>> multiMap;
    private final AtomicLong multiMapSize;

    public StringMultiMap(final DB db, final String name, final Serializer<String> keySerializer) throws IOException {

        multiMap = db
                .createTreeSet(name)
                .nodeSize(NODE_SIZE)
                .serializer(new Tuple2KeySerializer<>(
                                Comparator.naturalOrder(),
                                keySerializer,
                                null))
                .make();

        multiMapSize = new AtomicLong();
    }

    @Override
    public Iterable<V> get(final Object key) {

        return new Iterable<V>() {

            private final Iterator<ComparableElement<V>> valuesIterator = filter(multiMap, (String) key).iterator();

            @Override
            public Iterator<V> iterator() {

                return new Iterator<V>() {

                    @Override
                    public boolean hasNext() {
                        return valuesIterator.hasNext();
                    }

                    @Override
                    public V next() {
                        return valuesIterator.next().getValue();
                    }
                };
            }
        };
    }

    @Override
    public Iterable<V> put(final String key, final Iterable<V> values) {

        for (V value : values) {
            multiMap.add(t2(key, new ComparableElement<V>(multiMapSize.incrementAndGet(), value)));
        }

        return null;
    }

    @Override
    public int size() {
        return multiMapSize.intValue();
    }

    @Override
    public Set<String> keySet() {

        return new AbstractSet<String>() {

            @Override
            public Iterator<String> iterator() {

                return multiMap.parallelStream()
                        .map(t -> t.a)
                        .distinct()
                        .iterator();
            }

            @Override
            public int size() {
                return multiMapSize.intValue();
            }
        };
    }

    @Override
    public Set<Entry<String, Iterable<V>>> entrySet() {

        return new AbstractSet<Entry<String, Iterable<V>>>() {

            @Override
            public int size() {
                return multiMapSize.intValue();
            }

            @Override
            public Iterator<Entry<String, Iterable<V>>> iterator() {

                return new Iterator<Entry<String, Iterable<V>>>() {

                    private final Iterator<String> keyIterator = keySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return keyIterator.hasNext();
                    }

                    @Override
                    public Entry<String, Iterable<V>> next() {

                        String key = keyIterator.next();

                        return new SimpleImmutableEntry<>(key, get(key));
                    }
                };
            }
        };
    }

    /**
     * This is a hack to be able to use Fun.filter() which requires objects to
     * implement the <code>Comparable</code> interface.
     *
     * It it wouldn't be required if model objects implemented
     * <code>Comparable</code>, but they don't and I didn't want to mess with
     * the domain objects.
     */
    private static final class ComparableElement<V> implements Comparable<ComparableElement<V>>, Serializable {

        private final long index;
        private final V value;

        public ComparableElement(final long index, final V value) {

            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(final ComparableElement<V> other) {
            return Long.compare(index, other.index);
        }

        public V getValue() {
            return value;
        }
    }
}
