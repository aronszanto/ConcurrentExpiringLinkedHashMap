import java.net.InetAddress;
/**
 * This is the implementation of the InetAddress cache. It relies on an instance of a
 * ConcurrentLinkedExpiringHashMap, which hashes InetAddresses based on their string representations,
 * which are guaranteed to be exactly as unique as the values are. It is because of this dual
 * uniqueness that the project lends itself well to some form of hashing. Because of the nature
 * of a cache, a data structure that provides ordered access to elements was critical. A 
 * LinkedHashMap proved to be an excellent base. Further modification that was necessary included
 * the ability to expire elements after their time to live had passed in addition to, critically,
 * enabling multiple thread access to the cache.
 * 
 * Time complexity documented here and in CLEHM implementation, where more information about
 * the methods used can be found.
 */
public class INetAddressCache implements AddressCache{
    private long cleanFrq = 5;
    private ConcurrentLinkedExpiringHashMap<String,InetAddress> cache;

    public INetAddressCache(){
        cache = new
            ConcurrentLinkedExpiringHashMap<String,InetAddress>(cleanFrq);
    }
    public INetAddressCache(int globalTimeOut){
        cache = new
            ConcurrentLinkedExpiringHashMap<String,InetAddress>(cleanFrq, globalTimeOut);
    }
    
    /**
     * O(1)
     */
    public boolean offer(InetAddress i){
        return cache.put(i.toString().substring(1), i);
    }
    
    /**
     * O(1)
     */
    public boolean contains(InetAddress i){
        return cache.containsKey(i.toString().substring(1));
    }
    
    public boolean containsKey(String s){
        return cache.containsKey(s);
    }
    
    /**
     * O(1)
     */
    public InetAddress peek(){
        return cache.peek();
    }
    
    /**
     * O(1)
     */
    public InetAddress remove(){
        return cache.pop();
    }
    
    /**
     * O(1)
     */
    public boolean remove(InetAddress i){
        return (cache.remove(i.toString().substring(1)) != null);
    }
    
    /**
     * O(1) given non-empty cache
     */
    public InetAddress take() throws InterruptedException{
        InetAddress in = cache.pop();
        if (in != null)
            return in;
            
        final InetAddress[] ret = new InetAddress[1];
        Thread thr = new Thread(new Runnable(){
            public void run(){
                while (ret[0] == null){
                    try{
                        // avoid constant polling overhead, check every .1 seconds
                        Thread.sleep(100);
                        synchronized(cache){
                            ret[0] = cache.pop();
                        }
                    }
                    catch (InterruptedException iex) {
                    }
                }
    
            }
        });
        thr.start();
        thr.join();
        return ret[0];
    }
    
    /**
     * O(1)
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * O(1)
     */
    public boolean isEmpty(){
        return cache.size() == 0;
    }
    
    /**
     * O(n)
     */
    public void close(){
        cache.clear();
    }
}
