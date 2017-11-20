/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl.model;

import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.api.model.User;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;

import java.util.Collection;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/17/15.
 */
public class UserImpl implements User {

    private final String name;
    private final BdpApiAccessImpl access;

    public UserImpl(String name, BdpApiAccessImpl bdpApiAccess) {
        this.name = name;
        this.access = bdpApiAccess;
    }

    @Override
    public String getEmailAddress() {
        return name;
    }

    @Override
    public Collection<ProjectInfo> getProjectInfos() {
        return access.suggestProjectInfos("");
    }

}
