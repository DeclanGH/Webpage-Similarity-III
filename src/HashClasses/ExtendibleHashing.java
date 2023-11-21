/**
 * Author: Declan Onunkwo
 * Date: 29-oct-2023
 *
 * Description: Maps a string (URL in our case) to a byte array (which is a persistent object)
 */

package HashClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;

public class ExtendibleHashing implements Serializable {
    private int globalDepth;
    private ArrayList<Bucket> directory;

    public ExtendibleHashing() {
        globalDepth = 0;
        directory = new ArrayList<>();
        directory.add(new Bucket());
    }

    public Bucket getBucket(String k) {
        int h = k.hashCode();
        return directory.get(h & ((1 << globalDepth) - 1));
    }

    public void insert(String k, byte[] v) {
        Bucket bucket = getBucket(k);
        boolean full = bucket.isFull();
        bucket.put(k, v);
        if (full) {
            if (bucket.getLocalDepth() == globalDepth) {
                directory.addAll(new ArrayList<>(directory));
                globalDepth++;
            }

            Bucket p0 = new Bucket();
            Bucket p1 = new Bucket();
            p0.setLocalDepth(bucket.getLocalDepth() + 1);
            p1.setLocalDepth(bucket.getLocalDepth() + 1);
            int highBit = bucket.getLocalHighBit();
            for (SimpleEntry<String, byte[]> entry : bucket.getMap()) {
                int h = entry.getKey().hashCode();
                Bucket newBucket = (h & highBit) != 0 ? p1 : p0;
                newBucket.put(entry.getKey(), entry.getValue());
            }

            int h = k.hashCode();
            for (int i = h & (highBit - 1); i < directory.size(); i += highBit) {
                directory.set(i, (i & highBit) != 0 ? p1 : p0);
            }
        }
    }

    public byte[] find(String k) {
        return getBucket(k).get(k);
    }
}

class Bucket implements Serializable {
    private static final int BUCKET_SIZE = 10;
    private ArrayList<SimpleEntry<String, byte[]>> map;
    private int localDepth;

    public Bucket() {
        map = new ArrayList<>();
        localDepth = 0;
    }

    public boolean isFull() {
        return map.size() >= BUCKET_SIZE;
    }

    public void put(String k, byte[] v) {
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i).getKey().equals(k)) {
                map.remove(i);
                break;
            }
        }
        map.add(new SimpleEntry<>(k, v));
    }

    public byte[] get(String k) {
        for (SimpleEntry<String, byte[]> entry : map) {
            if (entry.getKey().equals(k)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public ArrayList<SimpleEntry<String, byte[]>> getMap() {
        return this.map;
    }

    public int getLocalHighBit() {
        return 1 << localDepth;
    }

    public int getLocalDepth() {
        return localDepth;
    }

    public void setLocalDepth(int localDepth) {
        this.localDepth = localDepth;
    }
}