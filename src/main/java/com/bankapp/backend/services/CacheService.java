package com.bankapp.backend.services;

import redis.clients.jedis.Jedis;
import com.bankapp.backend.config.RedisUtil;

public class CacheService {

    public static Double getBalance(String accountNumber) {

        try (Jedis jedis = RedisUtil.getConnection()) {

            String val = jedis.get("balance:" + accountNumber);

            return val == null ? null : Double.parseDouble(val);
        }
    }

    public static void cacheBalance(String accountNumber, double balance) {

        try (Jedis jedis = RedisUtil.getConnection()) {
            jedis.setex("balance:" + accountNumber, 60, String.valueOf(balance));
        }
    }

    public static void invalidateBalance(String accountNumber) {

        try (Jedis jedis = RedisUtil.getConnection()) {
            jedis.del("balance:" + accountNumber);
        }
    }
}
