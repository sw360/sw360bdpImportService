/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl.model;

import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;

public class ProjectInfoEager implements ProjectInfo{

    private final String bdpId;
    private final Project project;

    public ProjectInfoEager(String bdpId, Project project) {
        this.bdpId = bdpId;
        this.project = project;
    }

    @Override
    public String getProjectName() {
        return project.getName();
    }

    @Override
    public String getProjectId() {
        return bdpId;
    }

    @Override
    public Project getProject() {
        return project;
    }
}
