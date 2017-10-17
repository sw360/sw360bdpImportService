/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.entitytranslation.helper;

import org.eclipse.sw360.datahandler.thrift.MainlineState;
import org.eclipse.sw360.datahandler.thrift.ProjectReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;

public class ReleaseRelation {
    private final String releaseId;
    private final ReleaseRelationship releaseRelationship;

    public ReleaseRelation(String releaseId, ReleaseRelationship releaseRelationship) {
        this.releaseId = releaseId;
        this.releaseRelationship = releaseRelationship;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public ReleaseRelationship getReleaseRelationship() {
        return releaseRelationship;
    }

    public ProjectReleaseRelationship getProjectReleaseRelationship() {
        return new ProjectReleaseRelationship(releaseRelationship, MainlineState.OPEN);
    }
}
