import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

class Cache<K, V> implements Serializable {
    private final int maxEntries;
    private transient Map<K, V> cache;

    public Cache(int maxEntries) {
        this.maxEntries = maxEntries;
        initializeCache();
    }

    private void initializeCache() {
        cache = new LinkedHashMap<>(maxEntries + 1, 1.0f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException, IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeCache();
    }

    public void remove(K key) {
        cache.remove(key);
    }
}

class NodeNotFoundException extends RuntimeException {
    public NodeNotFoundException(String message) {
        super(message);
    }
}