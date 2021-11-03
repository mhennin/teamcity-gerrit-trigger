package org.saulis.async;

import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import jetbrains.buildServer.buildTriggers.async.AsyncPolledBuildTriggerFactory;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class GerritTriggerTests {

    private GerritTriggerService service;
    private GerritSettings gerritSettings;

    @Before
    public void setup() {
        BuildCustomizerFactory buildCustomizerFactory = mock(BuildCustomizerFactory.class);
        AsyncPolledBuildTriggerFactory triggerFactory = mock(AsyncPolledBuildTriggerFactory.class);
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
        gerritSettings = mock(GerritSettings.class);

        service = new GerritTriggerService(buildCustomizerFactory, triggerFactory, pluginDescriptor, mock(UserModel.class), gerritSettings);
    }

    @Test
    public void gerritPolledBuildTriggerIsGiven() {
        BuildTriggeringPolicy buildTriggeringPolicy = service.getBuildTriggeringPolicy();

        assertThat(buildTriggeringPolicy, is(instanceOf(GerritAsyncBuildTrigger.class)));

    }
}
