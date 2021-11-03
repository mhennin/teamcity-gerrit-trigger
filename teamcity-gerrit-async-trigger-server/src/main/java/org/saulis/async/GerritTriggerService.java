package org.saulis.async;

import com.jcraft.jsch.JSch;
import jetbrains.buildServer.buildTriggers.async.*;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GerritTriggerService extends BuildTriggerService {
    @NotNull
    private static final Logger LOG = Logger.getInstance(Loggers.VCS_CATEGORY + GerritTriggerService.class);

    @NotNull
    private final BuildCustomizerFactory buildCustomizerFactory;

    @NotNull
    private final PluginDescriptor pluginDescriptor;

    @NotNull
    private final BuildTriggeringPolicy triggerPolicy;

    @NotNull
    private final GerritSettings gerritSettings;

    public GerritTriggerService(@NotNull final BuildCustomizerFactory buildCustomizerFactory,
                                @NotNull final AsyncPolledBuildTriggerFactory triggerFactory,
                                @NotNull final PluginDescriptor pluginDescriptor,
                                @NotNull final UserModel userModel,
                                @NotNull final GerritSettings gerritSettings) {

        this.buildCustomizerFactory = buildCustomizerFactory;
        this.pluginDescriptor = pluginDescriptor;
        this.gerritSettings = gerritSettings;
        triggerPolicy = triggerFactory.createBuildTrigger(new GerritAsyncBuildTrigger(this.gerritSettings, this.buildCustomizerFactory, userModel), LOG);
    }

    @NotNull
    @Override
    public String getName() {
        return "gerritAsyncBuildTrigger";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Gerrit Async Build Trigger";
    }

    @NotNull
    @Override
    public String describeTrigger(@NotNull BuildTriggerDescriptor buildTriggerDescriptor) {
        return gerritSettings.describeParameters(buildTriggerDescriptor.getParameters());
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {

        return pluginDescriptor.getPluginResourcesPath("editGerritTrigger.jsp");
    }

    @NotNull
    @Override
    public BuildTriggeringPolicy getBuildTriggeringPolicy() {

        return triggerPolicy;
    }

    @Override
    public boolean isMultipleTriggersPerBuildTypeAllowed() {

        return true;
    }
}
