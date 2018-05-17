/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * With contribution from Verifa Oy, 2018.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.service;

import com.bosch.osmi.sw360.bdp.datasink.thrift.ThriftUploader;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapper;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapperMock;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapperNew;
import com.bosch.osmi.sw360.bdp.entitytranslation.BdpProjectInfoToSw360ProjectTranslator;
import com.bosch.osmi.sw360.bdp.entitytranslation.TranslationConstants;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.importstatus.ImportStatus;
import org.eclipse.sw360.datahandler.thrift.projectimport.ProjectImportService;
import org.eclipse.sw360.datahandler.thrift.projectimport.RemoteCredentials;
import org.eclipse.sw360.datahandler.thrift.projectimport.TokenCredentials;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.users.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by maximilian.huber@tngtech.com on 12/2/15.
 *
 * @author maximilian.huber@tngtech.com
 * @author andreas.reichel@tngtech.com
 * @author ksoranko@verifa.io
 */
public class BdpImportHandler implements ProjectImportService.Iface {

    private static final Logger log = Logger.getLogger(BdpImportHandler.class);

    private static BdpApiAccessWrapper getBdpApiAccessWrapper(RemoteCredentials remoteCredentials) {
        String serverUrl = remoteCredentials.getServerUrl();

        log.info("server: " + remoteCredentials.getServerUrl());
        if ("mock".equals(serverUrl)) {
            log.error("Using the mock with canned data.");
            return new BdpApiAccessWrapperMock();
        } else {
            return new BdpApiAccessWrapperNew(remoteCredentials);
        }
    }

    @Override
    public synchronized ImportStatus importDatasources(List<String> bdpProjectIds, User user, RemoteCredentials remoteCredentials) throws TException {
        BdpApiAccessWrapper bdpApiAccessWrapper = getBdpApiAccessWrapper(remoteCredentials);
        ThriftUploader thriftUploader = new ThriftUploader(bdpApiAccessWrapper);

        return thriftUploader.importBdpProjects(bdpProjectIds, user);
    }

    @Override
    public boolean validateCredentials(RemoteCredentials remoteCredentials) throws TException {
        return getBdpApiAccessWrapper(remoteCredentials)
                .validateCredentials();
    }

    @Override
    public List<Project> loadImportables(RemoteCredentials remoteCredentials) {
        return getBdpApiAccessWrapper(remoteCredentials)
                .getUserProjectInfos()
                .stream()
                .map(new BdpProjectInfoToSw360ProjectTranslator())
                .collect(Collectors.toList());
    }

    @Override
    public List<Project> suggestImportables(RemoteCredentials remoteCredentials, String projectName) throws TException {
        return getBdpApiAccessWrapper(remoteCredentials)
                .suggestProjectInfos(projectName)
                .stream()
                .map(new BdpProjectInfoToSw360ProjectTranslator())
                .collect(Collectors.toList());
    }

    @Override
    public String getIdName(){
        return TranslationConstants.BDP_ID;
    }

    @Override
    public ImportStatus importData(List<String> list, User user, TokenCredentials tokenCredentials) throws TException {
        return null;
    }
    
}
