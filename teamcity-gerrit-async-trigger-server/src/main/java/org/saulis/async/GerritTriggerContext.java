package org.saulis.async;

import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.ssh.TeamCitySshKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GerritTriggerContext {
    public @NotNull final PolledTriggerContext context;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 60;
    private static final int DEFAULT_QUERY_LIMIT = 50;
    private final int GERRIT_CONNECT_TIMEOUT_SECONDS = 10;
    private final GerritSettings gerritSettings;
    private final Map<String, String> parameters;

    //GerritSettings Related usage from: https://github.com/erikvdv1/teamcity-gerrit-trigger/commit/3b11ca7835cbbb85f308fa6224730a608834cff3
    public GerritTriggerContext(@NotNull final GerritSettings gerritSettings, @NotNull final PolledTriggerContext polledTriggerContext) {
        this.context = polledTriggerContext;
        this.gerritSettings = gerritSettings;
        this.parameters = context.getTriggerDescriptor().getParameters();
    }

    public String getUsername() {
        return gerritSettings.getTrimmedParameter(parameters, Parameters.USERNAME);
    }

    public String getHost() {
        return gerritSettings.getTrimmedParameter(parameters, Parameters.HOST);
    }
	
    public String getPort() {
        return gerritSettings.getTrimmedParameter(parameters, Parameters.PORT);
    }

    public int getConnectTimeoutMs() {
        int connectTimeoutSeconds = GERRIT_CONNECT_TIMEOUT_SECONDS;
        try {
            connectTimeoutSeconds = Integer.parseInt(gerritSettings.getTrimmedParameter(parameters, Parameters.SERVER_CONNECT_TIMEOUT_SECONDS));
        } catch(Exception ex) { }

        return (int)TimeUnit.SECONDS.toMillis(connectTimeoutSeconds);
    }

    public int getPolledTriggerIntervalSeconds() {
        int pollIntervalSeconds = DEFAULT_POLL_INTERVAL_SECONDS;
        try {
            pollIntervalSeconds = Integer.parseInt(gerritSettings.getTrimmedParameter(parameters, Parameters.POLL_INTERVAL_SECONDS));
        } catch(Exception ex) { }

        return pollIntervalSeconds;
    }

    @Nullable
    public TeamCitySshKey getSshKey() {
        return gerritSettings.getSshKey(context.getBuildType().getProject(), parameters);
    }

    public boolean hasProjectParameter() {
        return getProjectParameter().length() > 0;
    }

    public String getProjectParameter() {
        return gerritSettings.getTrimmedParameter(parameters, Parameters.PROJECT);
    }

    public int getQueryLimit() {
        int queryLimit = DEFAULT_QUERY_LIMIT;
        try {
            queryLimit = Integer.parseInt(gerritSettings.getTrimmedParameter(parameters, Parameters.QUERY_LIMIT));
        } catch(Exception ex) { }

        return queryLimit;
    }

    public boolean getQueueAsPersonalBuild() {
        boolean queueAsPersonalBuild = false;
        try {
            queueAsPersonalBuild = Boolean.parseBoolean(gerritSettings.getTrimmedParameter(parameters, Parameters.QUEUE_AS_PERSONAL_BUILD));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to convert: " + gerritSettings.getTrimmedParameter(parameters, Parameters.QUEUE_AS_PERSONAL_BUILD) + " to a boolean.", ex);
        }

        return queueAsPersonalBuild;
    }

    public boolean hasBranchParameter() {
        return getBranchParameter().length() > 0;
    }

    public String getBranchParameter() {

        return gerritSettings.getTrimmedParameter(parameters, Parameters.BRANCH);
    }

    public void updateTimestampIfNewer(Date timestamp) {
        if(hasTimestamp()) {
            Date previousTimestamp = getTimestamp();

            if(timestamp.after(previousTimestamp)) {
                setTimestamp(timestamp);
            }
        } else {
            setTimestamp(timestamp);
        }
    }

    private void setTimestamp(Date timestamp) {
        context.getCustomDataStorage().putValue(getBuildTimeStampKey(), String.valueOf(timestamp.getTime()));
    }

    public boolean hasTimestamp() {
        if(hasStoredValues()) {
            Map<String, String> storedValues = getStoredValues();

            return storedValues.containsKey(getBuildTimeStampKey());
        }

        return false;
    }

    private boolean hasStoredValues() {

        return getStoredValues() != null;
    }

    private Map<String, String> getStoredValues() {
        return context.getCustomDataStorage().getValues();
    }

    public Date getTimestamp() {
        Map<String, String> values = getStoredValues();

        return new Date(Long.parseLong(values.get(getBuildTimeStampKey())));
    }

    private String getBuildTimeStampKey() {
        return Parameters.TIMESTAMP_KEY + context.getBuildType().getConfigId();
    }
}
