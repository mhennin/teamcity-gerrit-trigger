package org.saulis.async;

import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.Mockito.*;

public class GerritAsyncBuildTriggerTests {

    private GerritAsyncBuildTrigger sut;
    private GerritSettings gerritSettings;
    private PolledTriggerContext context;
    private BuildCustomizerFactory buildCustomerFactory;
    private GerritClient client;
    private ArrayList<GerritPatchSet> patchSets;
    private BuildPromotion buildPromotion;
    private BuildCustomizer buildCustomizer;

    @Before
    public void setup() {
        client = mock(GerritClient.class);
        buildCustomerFactory = mock(BuildCustomizerFactory.class);
        gerritSettings = mock(GerritSettings.class);

        sut = new GerritAsyncBuildTrigger(gerritSettings, buildCustomerFactory, mock(UserModel.class));

        context = mock(PolledTriggerContext.class);
        patchSets = new ArrayList<GerritPatchSet>();
        when(client.getNewPatchSets()).thenReturn(patchSets);

        buildCustomizer = mock(BuildCustomizer.class);
        when(buildCustomerFactory.createBuildCustomizer(any(SBuildType.class), any(SUser.class))).thenReturn(buildCustomizer);

        buildPromotion = mock(BuildPromotion.class);
        when(buildCustomizer.createPromotion()).thenReturn(buildPromotion);
    }

    private void triggerBuild() {
        sut.triggerBuild("someValue", context);
    }

    @Test
    public void noNewBuildAreAddedToQueue() {
        patchSets.clear();

        triggerBuild();

        verifyZeroInteractions(buildCustomerFactory);
    }

    @Test
    public void refIsSetAsBranch() {
        patchSets.add(new GerritPatchSet("project", "branch", "refs/changes/1", new Date().getTime(), "user"));

        triggerBuild();

        verify(buildCustomizer).setDesiredBranchName("changes/1");
    }

    @Test
    public void newBuildIsAddedToQueue() {
        patchSets.add(new GerritPatchSet("project", "branch", "refs/changes/1", new Date().getTime(), "user"));

        triggerBuild();

        verify(buildPromotion).addToQueue("Gerrit");
    }


}

