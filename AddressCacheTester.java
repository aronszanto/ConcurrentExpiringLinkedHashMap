import java.util.*;
import java.net.InetAddress;
public class AddressCacheTester{
    public static void main(String[] args) throws java.net.UnknownHostException,InterruptedException {
        INetAddressCache cache = new INetAddressCache(5);
        InetAddress testval = InetAddress.getByName("255.255.255.255");
        cache.offer(testval);
        String notThere = "256.256.256.256";
        for (int i = 0; i < 1000; i++){
            cache.offer(randINA());
        }
        System.out.println("Cache Size = " + cache.size());
        System.out.println("Testing known key " + testval.toString().substring(1) + "..." + cache.contains(testval));
        System.out.println("Testing impossible key " + notThere + "..." + cache.containsKey(notThere));
        cache.remove(testval);
        System.out.println("Retrieving by nonexistent key..." + cache.remove(testval));
        cache.close();
        System.out.println("Cleared. Size = " +  cache.size());
        System.out.println("Popping..." + cache.remove());
        System.out.println("Taking from empty list and waiting 3 seconds before adding element...");
        Thread t = new Thread(new Runnable(){
            public void run(){
                try{
                    System.out.println(cache.take().toString().substring(1));
                }
                catch(InterruptedException iex){
                }
            }   
        });
        t.start();
        Thread.sleep(3000);
        cache.offer(testval);
        
    }
    
    public static InetAddress randINA() throws java.net.UnknownHostException{
        String s1 = Integer.toString((int) (Math.random() * 256));
        String s2 = Integer.toString((int) (Math.random() * 256));
        String s3 = Integer.toString((int) (Math.random() * 256));
        String s4 = Integer.toString((int) (Math.random() * 256));
        return InetAddress.getByName(s1 + "." + s2 + "." + s3 + "." + s4);
    }
}