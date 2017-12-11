/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.client.util.ServerAuthenticationException;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectInfoColumn;
import com.blackducksoftware.sdk.protex.project.ProjectInfoPageFilter;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;
import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.api.model.User;
import com.bosch.osmi.bdp.access.impl.model.ProjectImpl;
import com.bosch.osmi.bdp.access.impl.model.ProjectInfoImpl;
import com.bosch.osmi.bdp.access.impl.model.UserImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sw360.datahandler.thrift.projectimport.RemoteCredentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Allows an access to the Bdp APIs. An access has to be initialized with a username and a password for which
 * a user object can be retrieved. The user object is the actual entry point into the whole object net retrieved
 * from Bdp.
 *
 * @author johannes.kristan@bosch-si.com
 * @since 11/16/15.
 */
public class BdpApiAccessImpl implements BdpApiAccess {

    private static final Logger LOGGER = LogManager.getLogger(BdpApiAccessImpl.class);

    private static final long CONNECTION_TIME_OUT = 120 * 1000L;
    private final String serverUrl;

    private ProtexServerProxy bdpServerProxy;

    private String userName;
    private String password;

    public BdpApiAccessImpl(RemoteCredentials remoteCredentials) {
        this.serverUrl = remoteCredentials.getServerUrl();
        this.userName = remoteCredentials.getUsername();
        this.password = remoteCredentials.getPassword();
    }

    public BdpApiAccessImpl(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public boolean validateCredentials() {
        try {
            getServerProxy().validateCredentials();
            return true;
        } catch (ServerAuthenticationException e) {
            return false;
        }
    }

    @Override
    public Collection<ProjectInfo> suggestProjectInfos(String projectName) {
        try {
            ProjectInfoPageFilter pageFilter = PageFilterFactory.getAllRows(ProjectInfoColumn.PROJECT_ID);
            pageFilter.setFirstRowIndex(0);
            pageFilter.setLastRowIndex(50);
            return translate(getProjectApi().suggestProjects(projectName, pageFilter));
        } catch (SdkFault sdkFault) {
            LOGGER.error("Error occurred while retrieving project info for user " + userName + " from Bdp server: \n", sdkFault);
            throw new IllegalStateException(sdkFault);
        }
    }

    @Override
    public Project getProject(String bdpId) {
        try {
            return new ProjectImpl(getProjectApi().getProjectById(bdpId), this);
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data.", sdkFault);
            throw new IllegalStateException(sdkFault);
        }
    }

    @Override
    public User retrieveUser() {
        return new UserImpl(userName, this);
    }

    private ProjectApi getProjectApi() {
        LOGGER.debug("Bdp project api accessed.");
        return getServerProxy().getProjectApi(CONNECTION_TIME_OUT);
    }

    public BomApi getBomApi() {
        LOGGER.debug("Bdp BOM api accessed.");
        return getServerProxy().getBomApi(CONNECTION_TIME_OUT);
    }

    public LicenseApi getLicenseApi() {
        LOGGER.debug("Bdp license api accessed.");
        return getServerProxy().getLicenseApi(CONNECTION_TIME_OUT);
    }

    public ComponentApi getComponentApi() {
        LOGGER.debug("Bdp component api accessed.");
        return getServerProxy().getComponentApi(CONNECTION_TIME_OUT);
    }

    public DiscoveryApi getDiscoveryApi() {
        LOGGER.debug("Bdp discovery api accessed.");
        return getServerProxy().getDiscoveryApi(CONNECTION_TIME_OUT);
    }

    public CodeTreeApi getCodeTreeApi() {
        LOGGER.debug("Bdp code tree api accessed.");
        return getServerProxy().getCodeTreeApi(CONNECTION_TIME_OUT);
    }

    private void initializeServerProxy() {
        LOGGER.debug("Initializing bdp server proxy for server " + serverUrl);
        bdpServerProxy = new ProtexServerProxy(serverUrl, userName, password, CONNECTION_TIME_OUT);
    }

    private ProtexServerProxy getServerProxy() {
        if (bdpServerProxy == null) {
            initializeServerProxy();
        }
        return bdpServerProxy;
    }

    private Collection<ProjectInfo> translate(List<com.blackducksoftware.sdk.protex.project.ProjectInfo> projectInfos) {
        Collection<ProjectInfo> result = new ArrayList<ProjectInfo>();
        for (com.blackducksoftware.sdk.protex.project.ProjectInfo source : projectInfos) {
            result.add(new ProjectInfoImpl(source, this));
        }
        return result;
    }
}
