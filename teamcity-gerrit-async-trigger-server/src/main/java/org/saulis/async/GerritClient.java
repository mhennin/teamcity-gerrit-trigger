package org.saulis.async;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jetbrains.buildServer.log.Loggers;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.ssh.TeamCitySshKey;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.*;

public class GerritClient implements Runnable {
    private static final Logger LOG = Logger.getInstance(Loggers.VCS_CATEGORY + GerritClient.class);
    private final JSch jsch;
    private final GerritTriggerContext context;
    private List<GerritPatchSet> newPatchSets = null;

    public GerritClient(JSch jsch, GerritTriggerContext context) {
        this.jsch = jsch;
        this.context = context;
    }

    public List<GerritPatchSet> getNewPatchSets() {
        return newPatchSets;
    }

    public void run() {
        ChannelExec channel = null;
        Session session = null;

        try {
            LOG.debug("Gerrit Client START connection: " + context.context.getBuildType().getExternalId());
            session = openSession(context, context.getConnectTimeoutMs());
            channel = openChannel(context, session);

            newPatchSets = readGerritPatchSets(context, channel);
            LOG.debug("Gerrit Client DONE " + context.context.getBuildType().getExternalId());
        } catch(InterruptedException | InterruptedIOException ex) {
            LOG.error("Gerrit trigger was stopped while running", ex);
        } catch (Exception ex) {
            LOG.error("Gerrit trigger failed while getting patch sets.", ex);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private Session openSession(GerritTriggerContext context, int connectTimeoutMs) throws Exception {
        TeamCitySshKey sshKey = context.getSshKey();

        if(sshKey == null) {
            throw new Exception("No SSH key found");
        } else {
            jsch.addIdentity(context.getUsername(), context.getSshKey().getPrivateKey(), null, null);

            Session session = jsch.getSession(context.getUsername(), context.getHost(), Integer.parseInt(context.getPort()));
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(connectTimeoutMs);

            return session;
        }
    }

    private ChannelExec openChannel(GerritTriggerContext context, Session session) throws JSchException {
        ChannelExec channel;
        channel = (ChannelExec)session.openChannel("exec");

        String command = createCommand(context);
        LOG.debug("GERRIT: " + command);
        channel.setPty(false);
        channel.setCommand(command);

        //Do not use the timeout here
        //It appears that function has a bug where the input stream may not complete
        channel.connect();

        return channel;
    }

    private String createCommand(GerritTriggerContext context) {
        StringBuilder command = new StringBuilder();
        command.append("gerrit query --format=JSON status:open");

        if(context.hasProjectParameter()) {
            command.append(" project:" + context.getProjectParameter());
        }

        if(context.hasBranchParameter()) {
            command.append(" branch:" + context.getBranchParameter());
        }

        // Ignore drafts from Gerrit
        command.append(" NOT is:draft");

        // Ignore wip from Gerrit
        command.append(" NOT is:wip");

        // Optimizing the query.
        // Assuming that no more than <limit> new patch sets are created during a single poll interval.
        // Adjust if needed.
        command.append(" limit:" + context.getQueryLimit());
        command.append(" --current-patch-set ");

        return command.toString();
    }


    private List<GerritPatchSet> readGerritPatchSets(GerritTriggerContext context, ChannelExec channel) throws IOException {
        JsonStreamParser parser = new JsonStreamParser(new InputStreamReader(channel.getInputStream()));

        List<GerritPatchSet> patchSets = new ArrayList<GerritPatchSet>();

        if(context.hasTimestamp()) {
            Date timestamp = context.getTimestamp();

            while(parser.hasNext()) {
               JsonObject row = parser.next().getAsJsonObject();

              if(isStatsRow(row)) {
                break;
              }

              GerritPatchSet patchSet = parsePatchSet(row);

              if(patchSet.getCreatedOn().after(timestamp)) {
                patchSets.add(patchSet);
                context.updateTimestampIfNewer(patchSet.getCreatedOn());
              }
            }
        }
        else {
            context.updateTimestampIfNewer(new Date());
        }

        return patchSets;
    }


    private GerritPatchSet parsePatchSet(JsonObject row) {
        String project = row.get("project").getAsString();
        String branch = row.get("branch").getAsString();
        JsonObject currentPatchSet = row.get("currentPatchSet").getAsJsonObject();
        String ref = currentPatchSet.get("ref").getAsString();
        long createdOn = currentPatchSet.get("createdOn").getAsLong() * 1000L;

        JsonObject owner = row.get("owner").getAsJsonObject();

        String userName = owner.get("username").getAsString();

        return new GerritPatchSet(project, branch, ref, createdOn, userName);
    }

    private boolean isStatsRow(JsonObject ticket) {
        return ticket.has("rowCount");
    }

}
