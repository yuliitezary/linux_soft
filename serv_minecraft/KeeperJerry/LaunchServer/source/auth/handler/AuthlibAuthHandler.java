package launchserver.auth.handler;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import launcher.helper.IOHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.provider.AuthProviderResult;
import launchserver.auth.provider.AuthlibAuthProviderResult;
import launchserver.helpers.HTTPRequestHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthlibAuthHandler extends AuthHandler
{
    private static java.net.URL URL_join, URL_hasJoin;
    private static String joinUrl, hasJoinUrl;

    public final HashMap<String, UUID> usernameToUUID = new HashMap<>();

    AuthlibAuthHandler(BlockConfigEntry block)
    {
        super(block);
        joinUrl = block.getEntryValue("joinUrl", StringConfigEntry.class);
        hasJoinUrl = block.getEntryValue("hasJoinUrl", StringConfigEntry.class);

        try
        {
            // Docs: https://wiki.vg/Protocol_Encryption#Client
            URL_join = new URL(joinUrl); // "https://sessionserver.mojang.com/session/minecraft/join"

            // Docs: https://wiki.vg/Protocol_Encryption#Server
            URL_hasJoin = new URL(hasJoinUrl); // "https://sessionserver.mojang.com/session/minecraft/hasJoined"
        }
        catch (MalformedURLException e)
        {
            throw new InternalError(e);
        }
    }

    @Override
    public UUID auth(AuthProviderResult authResult) {
        if (authResult instanceof AuthlibAuthProviderResult) {
            AuthlibAuthProviderResult result = (AuthlibAuthProviderResult) authResult;
            usernameToUUID.put(result.username, result.uuid);
            return result.uuid;
        }
        return null;
    }

    @Override
    public UUID checkServer(String username, String serverID)
    {
        JsonObject uuidResponse;
        try {
            URL uuidURL = new URL(URL_hasJoin + "?username=" + IOHelper.urlEncode(username) + "&serverId=" + IOHelper.urlEncode(serverID));
            uuidResponse = HTTPRequestHelper.makeAuthlibRequest(uuidURL, null, "Authlib");
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Empty UUID response");
        }
        if (uuidResponse.get("error") != null)
        {
            throw new IllegalArgumentException(String.valueOf(uuidResponse.get("errorMessage")));
        }
        if (uuidResponse.get("id") == null)
        {
            throw new IllegalArgumentException("Empty UUID response");
        }
        return UUID.fromString(uuidResponse.get("id").asString().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    @Override
    public void close() {
    }

    @Override
    public boolean joinServer(String username, String accessToken, String serverID) throws IOException {
        JsonObject request = Json.object().
                add("accessToken", accessToken).
                add("selectedProfile", usernameToUUID(username).toString().replace("-", "")).
                add("serverId", serverID);

        int response = HTTPRequestHelper.authJoinRequest(URL_join, request, "AuthLib");

        if (200 <= response && response < 300 )
        {
            return true;
        }
        else
        {
            authError("Empty Authlib Handler response");
        }
        return false;
    }

    @Override
    public UUID usernameToUUID(String username) {
        return usernameToUUID.get(username);
    }

    @Override
    public String uuidToUsername(UUID uuid) {
        for (Map.Entry<String, UUID> entry : usernameToUUID.entrySet()) {
            if (entry.getValue().equals(uuid)) return entry.getKey();
        }
        return null;
    }
}
