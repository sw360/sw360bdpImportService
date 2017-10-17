/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import com.bosch.osmi.bdp.access.impl.model.ProjectInfoEager;
import org.eclipse.sw360.datahandler.thrift.bdpimport.RemoteCredentials;

import java.util.Collection;

public class BdpApiAccessWrapperNew implements BdpApiAccessWrapper{

    private final BdpApiAccess bdpApiAccess;

    public BdpApiAccessWrapperNew(RemoteCredentials remoteCredentials) {
        this(new BdpApiAccessImpl(remoteCredentials));
    }

    public BdpApiAccessWrapperNew(BdpApiAccess bdpApiAccess) {
        this.bdpApiAccess = bdpApiAccess;
    }

    @Override
    public boolean validateCredentials() {
        return bdpApiAccess.validateCredentials();
    }

    @Override
    public Collection<ProjectInfo> getUserProjectInfos() {
        return suggestProjectInfos("");
    }

    @Override
    public Collection<ProjectInfo> suggestProjectInfos(String projectName) {
        return bdpApiAccess.suggestProjectInfos(projectName);
    }

    @Override
    public ProjectInfo getProjectInfo(String bdpId) {
        return new ProjectInfoEager(bdpId, bdpApiAccess.getProject(bdpId));
    }
}
