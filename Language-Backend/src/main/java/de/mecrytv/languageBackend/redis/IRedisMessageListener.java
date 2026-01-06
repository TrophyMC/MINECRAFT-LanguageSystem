package de.mecrytv.languageBackend.redis;

import com.google.gson.JsonObject;

public interface IRedisMessageListener {
    void onReceive(JsonObject data);
}