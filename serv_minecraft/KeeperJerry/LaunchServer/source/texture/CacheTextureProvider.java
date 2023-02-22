package launchserver.texture;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile.Texture;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launchserver.helpers.HTTPRequestHelper;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CacheTextureProvider
{
    @LauncherAPI
    public static final long CACHE_DURATION_MS = VerifyHelper.verifyLong(
            Long.parseLong(System.getProperty("launcher.mysql.cacheDurationHours", Integer.toString(24))),
            VerifyHelper.L_NOT_NEGATIVE, "launcher.mysql.cacheDurationHours can't be < 0") * 60L * 60L * 1000L;

    // Instance
    private final Map<String, CacheDataTexture> cache = new HashMap<>(1024);

    // Since November 2020, Mojang stopped supporting the timestamp parameter.
    // If a timestamp is provided, it is silently ignored and the current uuid is returned. Please remind them to fix this here:
    // https://bugs.mojang.com/browse/WEB-3367
    protected CacheDataTexture getCached(UUID uuid, String username, String in_profileURL, String serviceName)
    {
        CacheDataTexture result = cache.get(uuid);

        // Have cached result?
        if (result != null && System.currentTimeMillis() < result.until)
        {
            if (result.exc != null)
            {
                JVMHelper.UNSAFE.throwException(result.exc);
            }
            return result;
        }

        try
        {
            // Obtain player profile
            URL profileURL = new URL(in_profileURL + IOHelper.urlEncode(serviceName.equals("ElyBy") ? username : uuid.toString())); // Как я это не хотел делать...
            JsonObject profileResponse = HTTPRequestHelper.makeAuthlibRequest(profileURL, null, serviceName);
            if (profileResponse == null)
            {
                throw new IllegalArgumentException("Empty Authlib response");
            }
            JsonArray properties = (JsonArray) profileResponse.get("properties");
            if (properties == null)
            {
                LogHelper.subDebug("No properties");
                return cache(username, null, null, null);
            }

            // Find textures property
            JsonObject texturesProperty = null;
            for (JsonValue property : properties)
            {
                JsonObject property0 = property.asObject();
                if (property0.get("name").asString().equals("textures"))
                {
                    byte[] asBytes = Base64.getDecoder().decode(property0.get("value").asString());
                    String asString = new String(asBytes, StandardCharsets.UTF_8);
                    texturesProperty = Json.parse(asString).asObject();
                    break;
                }
            }
            if (texturesProperty == null)
            {
                LogHelper.subDebug("No textures property");
                return cache(username, null, null, null);
            }

            // Extract skin&cloak texture
            texturesProperty = (JsonObject) texturesProperty.get("textures");
            JsonObject skinProperty = (JsonObject) texturesProperty.get("SKIN");
            Texture skinTexture = skinProperty == null ? null : new Texture(skinProperty.get("url").asString(), false);
            JsonObject cloakProperty = (JsonObject) texturesProperty.get("CAPE");
            Texture cloakTexture = cloakProperty == null ? null : new Texture(cloakProperty.get("url").asString(), true);

            // We're done
            return cache(username, skinTexture, cloakTexture, null);
        }
        catch (Throwable exc)
        {
            cache(username, null, null, exc);
            JVMHelper.UNSAFE.throwException(exc);
        }

        // We're dones
        return result;
    }

    private CacheDataTexture cache(String username, Texture skin, Texture cloak, Throwable exc)
    {
        long until = CACHE_DURATION_MS == 0L ? Long.MIN_VALUE : System.currentTimeMillis() + CACHE_DURATION_MS;
        CacheDataTexture data = exc == null ? new CacheDataTexture(skin, cloak, until) : new CacheDataTexture(exc, until);
        if (CACHE_DURATION_MS != 0L)
        {
            cache.put(username, data);
        }
        return data;
    }
}
