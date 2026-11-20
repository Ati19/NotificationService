package ns;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Atithi Kumari
 * @date 22/09/26
 * @description
 *
 *
Q. Design and Implement a Key Value Data Store
Following Operations should be supported:
GET
PUT
DELETE

//TTL based fetch and delete -- 5s ,

 // HAshmap --> key, <value,time>
 */
public class KeyValueStore {

    Map<Integer,Pair> map;
    int DEFAULT_TTL;
    KeyValueStore(){
        map=new HashMap<>();
        DEFAULT_TTL=5;
    }
    //ttl based
    public int get(int key){
        if(map.containsKey(key)) {
            if(map.get(key).time>System.currentTimeMillis())
             return map.get(key).value;
            else
                return -1;
        }
        else
            return -1;
    }

    public void put(int key,int value,int ttl){
        map.put(key,new Pair(value,System.currentTimeMillis()+ttl));

    }
    //Proactive - faster , continuously running
    //Lazy - relatively slower,
    //millions of keys -
    public void delete(int key){
        map.remove(key);
    }

    static class Pair{
        int value;
        long time;

        Pair(int value){
            this.value=value;
        }
        Pair(int value,long expiryTime){
            this.value=value;
            this.time=expiryTime;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        KeyValueStore kv =new KeyValueStore();
        kv.put(5,4,7000);
        kv.put(2,3,5);
        kv.put(3,6,5);
        System.out.println(kv.get(5));
        kv.delete(5);
        System.out.println(kv.get(5));
        System.out.println(kv.get(2));
        Thread.sleep(6000);
        System.out.println(kv.get(2));
    }
}
