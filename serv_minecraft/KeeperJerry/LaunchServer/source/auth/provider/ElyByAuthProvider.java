package launchserver.auth.provider;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import launcher.serialize.config.entry.BlockConfigEntry;
import launchserver.helpers.HTTPRequestHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

public class ElyByAuthProvider extends AuthProvider
{
    private static final Pattern UUID_REGEX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final URL URL;

    static
    {
        try
        {
            URL = new URL("https://authserver.ely.by/auth/authenticate");
        }
        catch (MalformedURLException e)
        {
            throw new InternalError(e);
        }
    }

    ElyByAuthProvider(BlockConfigEntry block)
    {
        super(block);
    }

    @Override
    public AuthProviderResult auth(String login, String password, String ip) throws Throwable
    {
        String clientToken = UUID.randomUUID().toString().replaceAll("-", "");
        // https://wiki.vg/Authentication#Payload
        JsonObject request = Json.object().
                add("agent", Json.object().add("name", "Minecraft").add("version", 1)).
                add("username", login).add("password", password).add("clientToken", clientToken);

        // Verify there's no error
        JsonObject response = HTTPRequestHelper.makeAuthlibRequest(URL, request, "ElyBy");
        if (response == null)
        {
            authError("Empty ElyBy response");
        }
        JsonValue errorMessage = response.get("errorMessage");
        if (errorMessage != null)
        {
            authError(errorMessage.asString());
        }

        // Parse JSON data
        JsonObject selectedProfile = response.get("selectedProfile").asObject();
        String username = selectedProfile.get("name").asString();
        String accessToken = response.get("accessToken").asString();
        UUID uuid = UUID.fromString(UUID_REGEX.matcher(selectedProfile.get("id").asString()).replaceFirst("$1-$2-$3-$4-$5"));
        String launcherToken = response.get("clientToken").asString();

        // We're done
        return new AuthlibAuthProviderResult(username, accessToken, uuid, launcherToken);
    }

    @Override
    public void close()
    {
        // Do nothing
    }
}
