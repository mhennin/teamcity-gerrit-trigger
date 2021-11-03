package org.saulis.async;

import jetbrains.buildServer.ExtensionsProvider;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.ssh.ServerSshKeyManager;
import jetbrains.buildServer.ssh.TeamCitySshKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static jetbrains.buildServer.ssh.ServerSshKeyManager.TEAMCITY_SSH_KEY_PROP;
//GerritSettings from: https://github.com/erikvdv1/teamcity-gerrit-trigger/commit/3b11ca7835cbbb85f308fa6224730a608834cff3
public class GerritSettings {
    private final ExtensionsProvider extensionProvider;

    private final String[] mandatoryProperties = new String[] {
            Parameters.HOST, Parameters.PROJECT, Parameters.USERNAME,
            TEAMCITY_SSH_KEY_PROP};

    public GerritSettings(@NotNull ExtensionsProvider extensionProvider) {
        this.extensionProvider = extensionProvider;
    }

    @NotNull
    public GerritTriggerContext createContext(PolledTriggerContext context) {
        return new GerritTriggerContext(this, context);
    }

    @Nullable
    public TeamCitySshKey getSshKey(@NotNull SProject project, @NotNull Map<String,String> parameters) {
        Collection<ServerSshKeyManager> extensions = extensionProvider.getExtensions(ServerSshKeyManager.class);
        String keyId = parameters.get(ServerSshKeyManager.TEAMCITY_SSH_KEY_PROP);
        TeamCitySshKey key = null;

        if (!extensions.isEmpty() && keyId != null) {
            ServerSshKeyManager keyManager = extensions.iterator().next();
            key = keyManager.getKey(project, keyId);
        }

        return key;
    }

    @NotNull
    public String describeParameters(@NotNull Map<String,String> parameters) {
        StringBuilder description = new StringBuilder();

        description.append("Listening");

        String project = getTrimmedParameter(parameters, Parameters.PROJECT);
        if(project.length() > 0) {
            description.append(" to ");
            description.append(project);

            String branch = getTrimmedParameter(parameters, Parameters.BRANCH);
            if(branch.length() > 0) {
                description.append("/" + branch);
            }
        }

        description.append(" on " + parameters.get(Parameters.HOST));

        return description.toString();
    }

    @Nullable
    public String getTrimmedParameter(Map<String, String> parameters, String key) {
        // Not sure how TeamCity inputs empty string parameters in UI, so playing it safe for nulls.
        if(parameters.containsKey(key)) {
            String value = parameters.get(key);

            if(value != null) {
                return value.trim();
            }
        }

        return "";
    }

    @NotNull
    public PropertiesProcessor getParametersProcessor() {
        return new PropertiesProcessor() {
            public Collection<InvalidProperty> process(Map<String, String> params) {
                List<InvalidProperty> errors = new ArrayList<InvalidProperty>();
                for (String mandatoryParam : mandatoryProperties) {
                    if (params.get(mandatoryParam) == null)
                        errors.add(new InvalidProperty(mandatoryParam, "must be specified"));
                }
                return errors;
            }
        };
    }
}
