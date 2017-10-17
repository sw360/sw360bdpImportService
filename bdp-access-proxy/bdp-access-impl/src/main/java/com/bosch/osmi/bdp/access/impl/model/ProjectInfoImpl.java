/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl.model;

import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/17/15.
 */
public class ProjectInfoImpl implements ProjectInfo {

    private final BdpApiAccessImpl access;
    private final com.blackducksoftware.sdk.protex.project.ProjectInfo projectInfo;

    public ProjectInfoImpl(com.blackducksoftware.sdk.protex.project.ProjectInfo projectInfo, BdpApiAccessImpl access){
        this.projectInfo= projectInfo;
        this.access = access;
    }


    @Override
    public String getProjectName() {
        return projectInfo.getName();
    }

    @Override
    public String getProjectId() {
        return projectInfo.getProjectId();
    }

    @Override
    public Project getProject() {
        return access.getProject(projectInfo.getProjectId());
    }
}
