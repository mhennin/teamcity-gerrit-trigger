package org.saulis.async;

import com.jcraft.jsch.JSch;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.buildTriggers.async.BaseAsyncPolledBuildTrigger;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.SBuildType;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

class GerritAsyncBuildTrigger extends BaseAsyncPolledBuildTrigger {
    private static final ConcurrentHashMap<String, ReentrantLock> buildConfigToLock = new ConcurrentHashMap<String, ReentrantLock>();

    private static final Logger LOG = Logger.getInstance(Loggers.VCS_CATEGORY + GerritAsyncBuildTrigger.class.getName());
    private final BuildCustomizerFactory buildCustomizerFactory;
    private static final String BRANCH_NAME_PARAMETER = "GerritBranch";
    private static final String TEAMCITY_BRANCH_NAME_PARAMETER = "teamcity.build.branch";
    private static final String TRIGGERED_BY_NAME = "Gerrit";
    private static final long MAX_GERRIT_CLIENT_RUNTIME_MINUTES = 5;

    @NotNull
    private final UserModel userModel;

    @NotNull
    private final GerritSettings gerritSettings;

    public GerritAsyncBuildTrigger(GerritSettings gerritSettings, BuildCustomizerFactory buildCustomizerFactory, UserModel userModel) {
        this.gerritSettings = gerritSettings;
        this.buildCustomizerFactory = buildCustomizerFactory;
        this.userModel = userModel;
    }

    @Override
    @Nullable
    public String triggerBuild(@Nullable String previousValue, @NotNull PolledTriggerContext context) throws BuildTriggerException {
        SBuildType buildType = context.getBuildType();
        String buildConfigId = buildType.getExternalId();
        ReentrantLock lock = getTriggerBuildLock(buildConfigId);

        if (!lock.tryLock()) {
            LOG.warn("Trigger is already running: " + buildConfigId);
            return previousValue;
        }

        GerritTriggerContext gerritTriggerContext = gerritSettings.createContext(context);
        try {
            GerritClient gerritClient = new GerritClient(new JSch(), gerritTriggerContext);
            Thread t = new Thread(gerritClient);
            LOG.debug("Starting Gerrit Client Thread: " + buildConfigId);
            t.start();

            t.join(TimeUnit.MINUTES.toMillis(MAX_GERRIT_CLIENT_RUNTIME_MINUTES));

            if(t.isAlive()) {
                LOG.error("Gerrit Client has exceeded it's maximum runtime.");
                t.interrupt();
                t.join();
            } else {
                queuePatchSets(gerritClient.getNewPatchSets(), buildType, buildConfigId);
            }
        } catch (Exception e) {
            LOG.error("GERRIT:", e);
        } finally {
            lock.unlock();
        }

        if(gerritTriggerContext.hasTimestamp()) {
            return gerritTriggerContext.getTimestamp().toString();
        }

        return null;
    }

    private void queuePatchSets(List<GerritPatchSet> patchSets, SBuildType buildType, String buildConfigId) {
        if(patchSets != null) {
            LOG.debug(String.format("GERRIT: Going to trigger %s new build(s): %s", patchSets.size(), buildConfigId));

            for (GerritPatchSet p : patchSets) {
                String branchName = p.getRefBranchName();
                SUser user = getUser(p);
                BuildCustomizer buildCustomizer = buildCustomizerFactory.createBuildCustomizer(buildType, user);
                buildCustomizer.setDesiredBranchName(branchName);
                buildCustomizer.setParameters(GetCustomParameters(branchName));

                buildCustomizer.createPromotion().addToQueue(TRIGGERED_BY_NAME);
            }
        }
    }

    private SUser getUser(GerritPatchSet patchSet) {
        SUser user = null;
        if(patchSet.getUserName() != null) {
            user = userModel.findUserAccount(null, patchSet.getUserName());
        }

        return user;
    }

    @NotNull
    private static ReentrantLock getTriggerBuildLock(String buildConfigId) {
        if(! buildConfigToLock.containsKey(buildConfigId)) {
            buildConfigToLock.putIfAbsent(buildConfigId, new ReentrantLock());
        }

        return buildConfigToLock.get(buildConfigId);
    }

    // Returns the number of seconds between when the trigger should be called.
    // Tortugas was running the trigger every 20 seconds by default.
    @Override
    public int getPollInterval(@NotNull PolledTriggerContext context) {
        GerritTriggerContext gerritTriggerContext = gerritSettings.createContext(context);
        return gerritTriggerContext.getPolledTriggerIntervalSeconds();
    }

    // Sets GerritBranch parameter on the triggered build and all reverse dependencies.
    private Map<String,String> GetCustomParameters(String branchName) {
        Map<String,String> customParameters = new HashMap<String,String>();
        customParameters.put(String.format("reverse.dep.*.%s", TEAMCITY_BRANCH_NAME_PARAMETER), branchName);
        customParameters.put(String.format("reverse.dep.*.%s", BRANCH_NAME_PARAMETER), branchName);
        customParameters.put(BRANCH_NAME_PARAMETER, branchName);

        return customParameters;
    }
}
