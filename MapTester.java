public class MapTester{
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException {
        // new CLEHM that cleans every 4 seconds
        ConcurrentLinkedExpiringHashMap cache = new ConcurrentLinkedExpiringHashMap(4);
        System.out.println("Inserting 4 elements with increasing timout lengths.\n" + 
            "Element 2 is offered twice- successful if V3 precedes V2 in the printout");
        cache.put("K1","V1",3);
        cache.put("K2","V2",7);
        cache.put("K3","V3",11);
        cache.put("K2","V2",15);
        cache.put("K4","V4",19);
        System.out.println("Printing peeks with 1 sec delay between");
        while(cache.size() > 0){
            System.out.println(cache.peek());
            Thread.sleep(1000);
        }
        
        System.out.println("Testing time to insert 100000 elements");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++){
            String val = Integer.toString(i);
            cache.put(val,val,100);
        }
        long end = System.currentTimeMillis();
        double t1 = ((double) (end - start)) / 1000.0;
        System.out.println("Took " +  t1 + " seconds.");
        
        
        
    }
}