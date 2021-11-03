package org.saulis.async;

import jetbrains.buildServer.ssh.ServerSshKeyManager;
import org.jetbrains.annotations.NotNull;

public class Parameters {
	public static final String PORT = "gerritPort";
    public static final String QUERY_LIMIT = "gerritQuerylimit";
    public static final String POLL_INTERVAL_SECONDS = "gerritPollIntervalSeconds";
    public static final String SERVER_CONNECT_TIMEOUT_SECONDS = "serverConnectTimeoutSeconds";

    public static final String HOST = "gerritServer";
    public static final String PROJECT = "gerritProject";
    public static final String USERNAME = "gerritUsername";
    public static final String BRANCH = "gerritBranch";

    public static final String TIMESTAMP_KEY = "timestamp";

    @NotNull
    public String getSshKey() {
        return ServerSshKeyManager.TEAMCITY_SSH_KEY_PROP;
    }

    @NotNull
    public String getHost() {
        return HOST;
    }

    @NotNull
    public String getProject() {
        return PROJECT;
    }

    @NotNull
    public String getUsername() {
        return USERNAME;
    }

    @NotNull
    public String getBranch() {
        return BRANCH;
    }

    @NotNull
    public String getPollIntervalSeconds() {
        return POLL_INTERVAL_SECONDS;
    }

    @NotNull
    public String getServerConnectTimeoutSeconds() {
        return SERVER_CONNECT_TIMEOUT_SECONDS;
    }

    @NotNull
    public String getQueryLimit() {
        return QUERY_LIMIT;
    }

    @NotNull
    public String getPort() {
        return PORT;
    }
}