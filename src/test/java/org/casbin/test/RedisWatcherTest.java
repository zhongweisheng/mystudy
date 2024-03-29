package org.casbin.test;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.main.SyncedEnforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.watcher.RedisWatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class RedisWatcherTest {
    private RedisWatcher redisWatcher, redisConfigWatcher;
    private final String expect = "update msg";
    private final String expectConfig = "update msg for config";

    /**
     * You should replace the initWatcher() method's content with your own Redis instance.
     */
    @Before
    public void initWatcher() {
        String redisTopic = "jcasbin-topic";
        String redisConfig = "jcasbin-config";
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(2);
        config.setMaxWaitMillis(100 * 1000);
//        redisWatcher = new RedisWatcher("127.0.0.1",6379, redisTopic, 2000, "foobared");
//        redisConfigWatcher = new RedisWatcher(config,"127.0.0.1",6376, redisConfig, 2000, "foobaredConfig");

        redisWatcher = new RedisWatcher("127.0.0.1", 6379, redisTopic, 2000, null);
        redisConfigWatcher = new RedisWatcher("127.0.0.1", 6379, redisTopic, 2000, null);

//        redisConfigWatcher = new RedisWatcher(config, "124.220.96.98", 6379, redisConfig, 2000, "xinhulian188");
        Enforcer enforcer = new SyncedEnforcer();

        Enforcer configEnforcer = new SyncedEnforcer();


        Model model = new Model();

        // request definition
        model.addDef("r", "r", "sub, obj, act");
        // policy definition
        model.addDef("p", "p", "sub, obj, act");
        // role definition
//            model.addDef("g", "g", "_, _");
        // policy effect
        model.addDef("e", "e", "some(where (p.eft == allow))");
        // matchers
        model.addDef("m", "m", "r.obj == p.obj && r.act == p.act");

        Model model2 = new Model();

        // request definition
        model2.addDef("r", "r", "sub, obj, act");
        // policy definition
        model2.addDef("p", "p", "sub, obj, act");
        // role definition
//            model.addDef("g", "g", "_, _");
        // policy effect
        model2.addDef("e", "e", "some(where (p.eft == allow))");
        // matchers
        model2.addDef("m", "m", "r.obj == p.obj && r.act == p.act");

        enforcer.setModel(model);

        enforcer.setWatcher(redisWatcher);
        configEnforcer.setModel(model2);

        configEnforcer.setWatcher(redisConfigWatcher);

        try {

            enforcer.addPolicy("eve", "data3", "read");

            List<List<String>> myRes111 = enforcer.getPolicy();

            Thread.sleep(100);
            List<List<String>> myRes222 = configEnforcer.getPolicy();
            System.out.println(myRes222);
            Thread.sleep(100);

            enforcer.addPolicy("eve555", "data3", "read");
            myRes222 = configEnforcer.getPolicy();
            myRes111 = enforcer.getPolicy();
            System.out.println(myRes222);
            System.out.println(myRes111);

            configEnforcer.addPolicy("eve666555", "data3", "read");
            myRes222 = configEnforcer.getPolicy();
            System.out.println(
                    myRes222
            );

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    public void testConfigUpdate() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        redisConfigWatcher.setUpdateCallback(() -> System.out.print(expectConfig));
        redisConfigWatcher.update();
        Thread.sleep(100);
        Assert.assertEquals(expectConfig, expectConfig);

    }

    @Test
    public void testUpdate() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        redisWatcher.setUpdateCallback(() -> System.out.print(expect));
        redisWatcher.update();
        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testConsumerCallback() throws InterruptedException {
        redisWatcher.setUpdateCallback((s) -> {
            System.out.print(s);
        });
        redisWatcher.update();
        Thread.sleep(100);

        redisConfigWatcher.setUpdateCallback((s) -> {
            System.out.print(s);
        });
        redisConfigWatcher.update();
        Thread.sleep(100);
    }

    @Test
    public void testConnectWatcherWithoutPassword() {
        String redisTopic = "jcasbin-topic";
        RedisWatcher redisWatcherWithoutPassword = new RedisWatcher("127.0.0.1", 6378, redisTopic);
        Assert.assertNotNull(redisWatcherWithoutPassword);

        String redisConfig = "jcasbin-config";
        RedisWatcher redisConfigWatcherWithoutPassword = new RedisWatcher("127.0.0.1", 6377, redisConfig);
        Assert.assertNotNull(redisConfigWatcherWithoutPassword);
    }
}
