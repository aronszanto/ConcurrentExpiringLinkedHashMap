import java.util.*;
/**
 * This is a specialized collection ideal for a FIFO-eviction/LIFO-retrieve
 * cache whose elements have a specific time to live (TTL). The CLEHM is 
 * based on a LinkedHashMap and includes extended multithreading and 
 * time expiry functions.
 * 
 * Users may choose a global time to live, in which all elements time out
 * after a constant number of seconds. Alternatively, users may choose
 * individual TTLs for each element added.
 * 
 * This structure supports constant-time lookup/search, add/remove, peek,
 * size, and delete. Internally, it is built on a doubly-linked list of hashed
 * key, value pairs, excellent for retrieval and addition on opposite sides.
 */
@SuppressWarnings("unchecked")
public class ConcurrentLinkedExpiringHashMap<K,V>{
    private final long cleanFrequency;
    private Map<K,CLEHMObject> cacheData;
    private long globalTimeOut = 10;
    protected class CLEHMObject{
        public V value;
        public long expireTime;
        protected CLEHMObject(V value, long timeOut){
            this.value = value;
            this.expireTime = System.currentTimeMillis() + timeOut * 1000;
        }

        public boolean isExpired(){
            return System.currentTimeMillis() > this.expireTime;
        }

    }
    /**
     * Constructor for CLEHM allowing user to set universal time to live (TTL) in cache
     */
    ConcurrentLinkedExpiringHashMap(final long cleanFrequency, final long globalTimeOut){
        this.cacheData = new LinkedHashMap<K,CLEHMObject>(1024,0.75F,true);
        this.cleanFrequency = cleanFrequency * 1000;
        this.globalTimeOut = globalTimeOut;

        if (cleanFrequency > 0){
            Thread thr = new Thread(new Runnable(){
                        public void run(){
                            while (true){
                                try{
                                    Thread.sleep(cleanFrequency * 1000);
                                }
                                catch (InterruptedException iex) {
                                }
                                synchronized(cacheData){
                                    evictStale();
                                }
                            }

                        }
                    });

            thr.setDaemon(true);
            thr.start();
        }
    }
    /**
     * Constructor for CLEHM not requiring global TTL- user sets TTL of each element 
     * or resorts to default 10s.
     */
    ConcurrentLinkedExpiringHashMap(final long cleanFrequency){
        this.cacheData = new LinkedHashMap<K,CLEHMObject>(1024,0.75F,true);
        this.cleanFrequency = cleanFrequency * 1000;

        if (cleanFrequency > 0){
            Thread thr = new Thread(new Runnable(){
                        public void run(){
                            while (true){
                                try{
                                    Thread.sleep(cleanFrequency * 1000);
                                }
                                catch (InterruptedException iex) {
                                }
                                synchronized(cacheData){
                                    evictStale();
                                }
                            }

                        }
                    });

            thr.setDaemon(true);
            thr.start();
        }
    }
    
    /**
     * inserts key, value pair given, with TTL of timeOut seconds. Runs in O(1) time.
     */
    public synchronized boolean put(K key, V value, long timeOut) {
        try {
            if (cacheData.containsKey(key)){
                cacheData.remove(key);
            }
            cacheData.put(key, new CLEHMObject(value, timeOut));
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    
    /**
     * inserts key,value pair, with TTL globally inferred. Runs in O(1) time.
     */
    public synchronized boolean put(K key, V value) {
        try {
            if (cacheData.containsKey(key)){
                cacheData.remove(key);
            }
            cacheData.put(key, new CLEHMObject(value, globalTimeOut));
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    
    /**
     * Gets value associated with given key. Runs in O(1) time.
     */
    public synchronized V get(K key) {
        CLEHMObject cl = (CLEHMObject) cacheData.get(key);
        return cl == null ? null : cl.value;
    }
    
    /**
     * Removes value associated with given key. Runs in O(1) time.
     */
    public synchronized V remove(K key){
        CLEHMObject cl = cacheData.remove(key);
        return cl == null ? null : cl.value;
    }
    
    /**
     * Returns size of map. Runs in O(1) time.
     */
    public synchronized int size(){
        return cacheData.size();
    }
    
    /**
     * Returns value at front of list (eldest value). Runs in O(1) time.
     */
    public synchronized V peek() {
        if(cacheData.size() == 0)
            return null;
        else {
            Object key = cacheData.keySet().iterator().next();
            return (V) ((CLEHMObject) cacheData.get(key)).value;
        }
    }
    
    /**
     * Removes eldest value from list and returns it. Runs in O(1) time.
     */
    public synchronized V pop() {
        if (cacheData.size() == 0)
            return null;
        else{
            Object key = cacheData.keySet().iterator().next();
            V value = ((CLEHMObject) cacheData.get(key)).value;
            cacheData.remove(key);
            return value;
        }
    }
    
    /**
     * Clears list and releases all resources. Runs in O(n) time.
     */
    public synchronized void clear(){
        cacheData.clear();
    }
    
    /**
     * Returns true if there is a key in the list that matches the given key. Runs in O(1) time.
     */
    public synchronized boolean containsKey(K key){
        return cacheData.containsKey(key);
    }
    
    /**
     * Periodic cleaning of cache, runs every cleanFrequency seconds. Runs in O(n) time.
     */
    public void evictStale() {
        ArrayList<K> delKeys = null;
        synchronized(cacheData){
            Iterator it = cacheData.entrySet().iterator();
            delKeys = new ArrayList<K>((cacheData.size() / 2) + 1);
            K key = null;
            CLEHMObject cl = null;
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                key = (K) entry.getKey();
                if( ((CLEHMObject) entry.getValue()).isExpired())
                    delKeys.add(key);
            }
        }

        for (K key : delKeys){
            synchronized(cacheData){
                cacheData.remove(key);
            }
            Thread.yield();
        }

    }
}
