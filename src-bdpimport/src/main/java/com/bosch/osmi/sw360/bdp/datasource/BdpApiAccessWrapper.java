/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.model.ProjectInfo;

import java.util.Collection;

public interface BdpApiAccessWrapper {

    boolean validateCredentials();

    /**
     * Too slow on production environments, please use suggestProjectInfos instead
     */
    @Deprecated
    Collection<ProjectInfo> getUserProjectInfos();

    Collection<ProjectInfo> suggestProjectInfos(String projectName);

    ProjectInfo getProjectInfo(String bdpId);
}
