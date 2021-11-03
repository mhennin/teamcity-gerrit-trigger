package org.saulis.async;

import java.util.Date;

class GerritPatchSet {
    private final String project;
    private final String branch;
    private final String ref;
    private final Date createdOn;
    private final String userName;

    public GerritPatchSet(String project, String branch, String ref, long createdOn, String userName) {
        this.project = project;
        this.branch = branch;
        this.ref = ref;
        this.createdOn = new Date(createdOn);
        this.userName = userName;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getRef() {
        return ref;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    // Convert refs/changes/A/BBBB/C to A/BBBB/C
    public String getRefBranchName() {
        return ref.replace("refs/changes/", "");
    }

    public String getUserName() { return userName; }
}
