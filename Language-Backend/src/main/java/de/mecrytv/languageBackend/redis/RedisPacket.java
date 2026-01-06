package de.mecrytv.languageBackend.redis;

import com.google.gson.JsonObject;

public record RedisPacket(String type, JsonObject data) {}