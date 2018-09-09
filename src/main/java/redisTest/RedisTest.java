package redisTest;

import com.alibaba.fastjson.JSON;

import bean.Person;
import redis.clients.jedis.Jedis;

public class RedisTest {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        System.out.println("success!");
//        jedis.auth("root")args;
        System.out.println(jedis.ping());
        
        Person p = new Person("1", "monkwa");
        jedis.set("person", JSON.toJSONString(p));
        String s = jedis.get("person");
        System.out.println(s);
        Person p2 = JSON.parseObject(s, Person.class);
        System.out.println(p2.getName());
        
        jedis.hset("phash", "id", p.getId());
        jedis.hset("phash", "name", p.getName());
        System.out.println(jedis.hgetAll("phash"));
        
//        for(int i = 0; i<1000; i++) {
//            jedis.pfadd("count3", "user3" + i);
//            long total = jedis.pfcount("count");
//            if(total != i+1) {
//                System.out.printf("%d %d\n", total, i + 1);
//            }
//        }
        
        jedis.pfmerge("count3", "count2");
        System.out.println(jedis.pfcount("count3"));
        System.out.println(jedis.pfcount("count2"));
        
        jedis.close();
    }
}
