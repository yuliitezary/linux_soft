package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.*;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import launcher.helper.LogHelper;

import java.net.Proxy;

public class YggdrasilAuthenticationService implements AuthenticationService {
    private final Environment environment;

    private Proxy proxy;

    public YggdrasilAuthenticationService(final Proxy proxy) {
        this(proxy, determineEnvironment());
    }

    public YggdrasilAuthenticationService(final Proxy proxy, final Environment environment) {
        this(proxy, null, environment);
    }

    public YggdrasilAuthenticationService(final Proxy proxy, final String clientToken) {
        this(proxy, clientToken, determineEnvironment());
    }

    public YggdrasilAuthenticationService(final Proxy proxy, final String clientToken, final Environment environment) {
        this.environment = environment;
        this.proxy = proxy;
        LogHelper.debug("Patched AuthenticationService created: '%s'", clientToken);
    }

    private static Environment determineEnvironment() {
        return EnvironmentParser.getEnvironmentFromProperties().orElse(YggdrasilEnvironment.PROD.getEnvironment());
    }

    public UserAuthentication createUserAuthentication(final Agent agent) {
        throw new UnsupportedOperationException("createUserAuthentication is used only by Mojang Launcher");
    }

    public MinecraftSessionService createMinecraftSessionService() {
        return (MinecraftSessionService)new YggdrasilMinecraftSessionService((AuthenticationService)this, this.environment);
    }

    public GameProfileRepository createProfileRepository() {
        return (GameProfileRepository)new YggdrasilGameProfileRepository(this, this.environment);
    }

    public YggdrasilSocialInteractionsService createSocialInteractionsService(final String accessToken) throws AuthenticationException {
        return (YggdrasilSocialInteractionsService)new YggdrasilSocialInteractionsService(this, accessToken, this.environment);
    }

    public UserApiService createUserApiService(final String accessToken) throws AuthenticationException {
        return (UserApiService)new YggdrasilUserApiService(accessToken, proxy, environment);
    }
}
