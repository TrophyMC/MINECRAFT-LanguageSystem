package de.mecrytv.languageBackend.models.redis;

import com.google.gson.JsonObject;
import de.mecrytv.languageBackend.models.ICacheModel;

import java.util.UUID;

public class LanguageModel implements ICacheModel {

    private UUID uuid;
    private String languageCode;
    private boolean firstJoin;

    public LanguageModel() {}

    public LanguageModel(UUID uuid, String languageCode, boolean firstJoin) {
        this.uuid = uuid;
        this.languageCode = languageCode;
        this.firstJoin = firstJoin;
    }

    @Override
    public String getIdentifier() {
        return uuid.toString();
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid.toString());
        json.addProperty("language", languageCode);
        json.addProperty("firstJoin", firstJoin);
        return json;
    }

    @Override
    public void deserialize(JsonObject data) {
        this.uuid = UUID.fromString(data.get("uuid").getAsString());
        this.languageCode = data.get("language").getAsString();
        this.firstJoin = data.get("firstJoin").getAsBoolean();
    }

    public UUID getUuid() { return uuid; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public boolean isFirstJoin() { return firstJoin; }
    public void setFirstJoin(boolean firstJoin) { this.firstJoin = firstJoin; }
}
