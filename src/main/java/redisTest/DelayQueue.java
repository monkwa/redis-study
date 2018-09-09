package redisTest;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class DelayQueue {

    private Jedis jedis;
    
    @Before
    public void before() {
        jedis = new Jedis("127.0.0.1", 6379);
    }
    
    @Test
    public void test() {
        long now = System.currentTimeMillis();
        Task task = new Task(UUID.randomUUID().toString(), now + 10*1000, 10*1000 + "后执行");
        addToDelayQue(task);
        task = new Task(UUID.randomUUID().toString(), now + 20*1000, 20*1000 + "后执行");
        addToDelayQue(task);
        task = new Task(UUID.randomUUID().toString(), now + 30*1000, 30*1000 + "后执行");
        addToDelayQue(task);
        transferFromDelayQue();
    }
    
    @After
    public void after() {
        jedis.close();
    }
    
    void addToTaskQue(String taskInfo) {
        System.out.println(taskInfo + "已经从延时队列中转至队列" + "当前时间: " + System.currentTimeMillis());
        System.out.println();
    }
    
    void addToDelayQue(Task task) {
        System.out.println(task.toString() + "已经加入延时队列");
        jedis.zadd("delay_queue",task.getTime(), task.toString());
    }
    
    void transferFromDelayQue() {
        while(true) {
            Set<Tuple> item = jedis.zrangeWithScores("delay_queue", 0, 0);
            if(item != null && !item.isEmpty()) {
                Tuple tuple = item.iterator().next();
                if(System.currentTimeMillis() >= tuple.getScore()) {
                    jedis.zrem("delay_queue", tuple.getElement());
                    addToTaskQue(tuple.getElement());
                }
            }
        }
    }
    
    static class Task{
        private String id;
        private long time;
        private String desc;
        
        public Task(String id, long time, String desc) {
            this.id = id;
            this.time = time;
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "Task [id=" + id + ", time=" + time + ", desc=" + desc + "]";
        }
    }
}
