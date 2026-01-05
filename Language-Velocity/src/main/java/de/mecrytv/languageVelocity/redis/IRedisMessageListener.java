package de.mecrytv.languageVelocity.redis;

import com.google.gson.JsonObject;

public interface IRedisMessageListener {
    void onReceive(JsonObject data);
}