package com.pcdev.demoone.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PlanDao {

    private final Jedis jedis;

    //Add a field-value pair to the hash with given key
    public void hSet(String key, String field, String value) {
        jedis.hset(key, field, value);
    }

    //Check if the given key exists in the redis db
    public boolean checkIfExists(String key) {
        return jedis.exists(key);
    }

    //Delete the key-value pair
    public long del(String key) {
        return jedis.del(key);
    }

    //Get the value against the key passed
    public String get(String key) {
        return jedis.get(key);
    }

    //Get the value of a field in a Hash with given key
    public String hGet(String key, String field) {
        return jedis.hget(key, field);
    }

    public Map<String, String> getAllValuesByKey(String key) {
        return jedis.hgetAll(key);
    }
}
