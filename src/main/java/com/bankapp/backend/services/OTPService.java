package com.bankapp.backend.services;

import redis.clients.jedis.Jedis;
import com.bankapp.backend.config.RedisUtil;

public class OTPService
{

    private static final int OTP_EXPIRY_SECONDS = 120; // 2 min
    private static final int MAX_ATTEMPTS = 3;
    private static final int ATTEMPT_EXPIRY_SECONDS = 120;

    // =========================
    // STORE OTP
    // =========================
    public static void storeOTP(String email, String otp) {

        try (Jedis jedis = RedisUtil.getConnection()) {

            String otpKey = "otp:" + email;
            String attemptKey = "otp_attempts:" + email;

            // store OTP
            jedis.setex(otpKey, OTP_EXPIRY_SECONDS, otp);

            // reset attempts
            jedis.del(attemptKey);
        }
    }

    // =========================
    // VERIFY OTP WITH LIMIT
    // =========================
    public static boolean verifyOTP(String email, String inputOtp) {

        if (inputOtp == null || inputOtp.isEmpty()) {
            return false;
        }

        try (Jedis jedis = RedisUtil.getConnection()) {

            String otpKey = "otp:" + email;
            String attemptKey = "otp_attempts:" + email;

            // 🔒 Check attempts
            String attemptsStr = jedis.get(attemptKey);
            int attempts = attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);

            if (attempts >= MAX_ATTEMPTS) {
                System.out.println("⚠️ Too many OTP attempts. Try again later.");
                return false;
            }

            String storedOtp = jedis.get(otpKey);

            // ❌ expired or missing
            if (storedOtp == null) {
                return false;
            }

            // ✅ correct OTP
            if (storedOtp.trim().equals(inputOtp.trim())) {

                // delete OTP + attempts
                jedis.del(otpKey);
                jedis.del(attemptKey);

                return true;
            }

            // ❌ wrong OTP → increment attempts
            attempts++;
            jedis.setex(attemptKey, ATTEMPT_EXPIRY_SECONDS, String.valueOf(attempts));

            return false;
        }
    }
}