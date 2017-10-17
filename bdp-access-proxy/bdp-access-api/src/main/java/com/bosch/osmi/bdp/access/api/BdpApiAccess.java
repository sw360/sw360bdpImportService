/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.bdp.access.api;

import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.api.model.User;

import java.util.Collection;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/20/15.
 */
public interface BdpApiAccess {
    boolean validateCredentials();

    Collection<ProjectInfo> suggestProjectInfos(String projectName);

    Project getProject(String bdpId);

    /**
     * This old method of lazily loading all the data is very slow. Use only for mock data generation
     * and avoid it in production code
     */
    @Deprecated
    User retrieveUser();
}
