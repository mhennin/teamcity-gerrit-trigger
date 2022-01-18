<%@ include file="/include.jsp" %>
<%@ page import="org.saulis.async.Parameters" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<jsp:useBean id="keys" class="org.saulis.async.Parameters" scope="request" />
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>
<jsp:useBean id="buildForm" type="jetbrains.buildServer.controllers.admin.projects.EditableBuildTypeSettingsForm" scope="request"/>

<tr class="noBorder" >
    <td colspan="2">
        <em>Gerrit Trigger will add a new build to the queue after a new patchset is detected.</em>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.host}">Host: <l:star/></label></td>
    <td>
       <props:textProperty name="${keys.host}" style="width:18em;"/>
      <span class="smallNote">
          Example: dev.gerrit.com<br/>
      </span>
        <span class="error" id="error_${keys.host}"></span>
    </td>
</tr>


<tr class="noBorder" >
    <td><label for="${keys.port}">Port: <l:star/></label></td>
    <td>
       <props:textProperty name="${keys.port}" style="width:18em;"/>
      <span class="smallNote">
          Example: 29418<br/>
      </span>
        <span class="error" id="error_${keys.port}"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.username}">Username: <l:star/></label></td>
    <td>
       <props:textProperty name="${keys.username}" style="width:18em;"/>
        <span class="error" id="error_${keys.username}"></span>
    </td>
</tr>

<tr>
    <td><label for="${keys.sshKey}">SSH Key: <l:star/></label></td>
    <td>
        <admin:sshKeys projectId="${buildForm.project.externalId}"/>
        <span class="error" id="error_${keys.sshKey}"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.serverConnectTimeoutSeconds}">Host Connection Timeout Seconds: </label></td>
    <td>
        <props:textProperty name="${keys.serverConnectTimeoutSeconds}" style="width:18em;"/>
        <span class="smallNote">
            Default 10 seconds<br/>
        </span>
        <span class="error" id="error_${keys.serverConnectTimeoutSeconds}"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.queryLimit}">Gerrit Review Query Limit: </label></td>
    <td>
        <props:textProperty name="${keys.queryLimit}" style="width:18em;"/>
        <span class="smallNote">
            Default 50 reviews<br/>
        </span>
        <span class="error" id="error_${keys.queryLimit}"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.pollIntervalSeconds}">Trigger Poll Interval Seconds: </label></td>
    <td>
        <props:textProperty name="${keys.pollIntervalSeconds}" style="width:18em;"/>
        <span class="smallNote">
            Default 60 seconds<br/>
        </span>
        <span class="error" id="error_${keys.pollIntervalSeconds}"></span>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.project}">Project: <l:star/></label></td>
    <td>
        <props:textProperty name="${keys.project}" style="width:18em;"/>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.branch}">Branch: </label></td>
    <td>
        <props:textProperty name="${keys.branch}" style="width:18em;"/>
    </td>
</tr>

<tr class="noBorder" >
    <td><label for="${keys.queueAsPersonalBuild}">Queue as Personal Build: </label></td>
    <td>
        <props:checkboxProperty name="${keys.queueAsPersonalBuild}"/>
    </td>
</tr>
