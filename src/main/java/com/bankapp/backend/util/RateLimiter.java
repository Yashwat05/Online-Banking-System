package com.bankapp.backend.util;

import com.bankapp.backend.config.RedisUtil;
import redis.clients.jedis.Jedis;

public class RateLimiter {

    public static boolean allow(String key) {

        try (Jedis jedis = RedisUtil.getConnection()) {

            String val = jedis.get(key);

            int count = val == null ? 0 : Integer.parseInt(val);

            if (count >= 5) return false;

            jedis.setex(key, 300, String.valueOf(count + 1));

            return true;
        }
    }
}