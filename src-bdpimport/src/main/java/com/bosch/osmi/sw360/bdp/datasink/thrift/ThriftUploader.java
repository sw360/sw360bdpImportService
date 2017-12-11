/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasink.thrift;

import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.sw360.bdp.datasink.thrift.helper.ProjectImportError;
import com.bosch.osmi.sw360.bdp.datasink.thrift.helper.ProjectImportResult;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapper;
import com.bosch.osmi.sw360.bdp.entitytranslation.*;
import com.bosch.osmi.sw360.bdp.entitytranslation.helper.ReleaseRelation;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.importstatus.ImportStatus;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.users.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

// TODO: this contains logic and knowledge of the inner relations => move them to entitytranslation?
public class ThriftUploader {

    private static final Logger logger = Logger.getLogger(ThriftUploader.class);

    private final BdpComponentToSw360ComponentTranslator componentToComponentTranslator = new BdpComponentToSw360ComponentTranslator();
    private final BdpComponentToSw360ReleaseTranslator componentToReleaseTranslator = new BdpComponentToSw360ReleaseTranslator();
    private final BdpLicenseToSw360LicenseTranslator licenseToLicenseTranslator = new BdpLicenseToSw360LicenseTranslator();
    private final BdpProjectInfoToSw360ProjectTranslator projectInfoToProjectTranslator = new BdpProjectInfoToSw360ProjectTranslator();
    private final BdpUsageLevelToSw360ReleaseRelationship usageLevelToReleaseRelationshipTranslator = new BdpUsageLevelToSw360ReleaseRelationship();

    private final ThriftExchange thriftExchange;
    private final BdpApiAccessWrapper bdpApiAccessWrapper;

    public ThriftUploader(BdpApiAccessWrapper bdpApiAccessWrapper) {
        this(new ThriftExchange(), bdpApiAccessWrapper);
    }

    @VisibleForTesting
    ThriftUploader(ThriftExchange thriftExchange, BdpApiAccessWrapper bdpApiAccessWrapper) {
        this.thriftExchange=thriftExchange;
        this.bdpApiAccessWrapper = bdpApiAccessWrapper;
    }

    private <T> Optional<String> searchExistingEntityId(Optional<List<T>> nomineesOpt, Function<T, String> idExtractor, String nameBdp, String nameSW360) {
        return nomineesOpt.flatMap(
                nominees -> {
                    Optional<String> nomineeId = nominees.stream()
                            .findFirst()
                            .map(idExtractor);
                    if (nomineeId.isPresent()) {
                        logger.info(nameBdp + " to import matches a " + nameSW360 + " with id: " + nomineeId.get());
                        nominees.stream()
                                .skip(1)
                                .forEach(n -> logger.error(nameBdp + " to import would also match a " + nameSW360 + " with id: " + idExtractor.apply(n)));
                    }
                    return nomineeId;
                }
        );
    }

    protected <T> Optional<String> searchExistingEntityId(Optional<List<T>> nomineesOpt, Function<T, String> idExtractor, String name) {
        return searchExistingEntityId(nomineesOpt, idExtractor, name, name);
    }

    protected String getOrCreateLicenseId(com.bosch.osmi.bdp.access.api.model.License licenseBdp, User user) {
        logger.info("Try to import bdp License: " + licenseBdp.getName());

        Optional<String> potentialLicenseId = searchExistingEntityId(thriftExchange.searchLicenseByBdpId(licenseBdp.getId()),
                License::getId,
                "License");
        if (potentialLicenseId.isPresent()) {
            return potentialLicenseId.get();
        } else {
            License licenseSW360 = licenseToLicenseTranslator.apply(licenseBdp);
            String licenseId = thriftExchange.addLicense(licenseSW360, user);
            logger.info("Imported license: " + licenseId);
            return licenseId;
        }
    }

    private String getOrCreateComponent(com.bosch.osmi.bdp.access.api.model.Component componentBdp, User user) {
        logger.info("Try to import bdp Component: " + componentBdp.getName());

        String componentVersion = isNullOrEmpty(componentBdp.getComponentVersion()) ? BdpComponentToSw360ReleaseTranslator.unknownVersionString : componentBdp.getComponentVersion();
        Optional<String> potentialReleaseId = searchExistingEntityId(thriftExchange.searchReleaseByNameAndVersion(componentBdp.getName(), componentVersion),
                Release::getId,
                "Component",
                "Release");
        if (potentialReleaseId.isPresent()) {
            return potentialReleaseId.get();
        }

        Release releaseSW360 = componentToReleaseTranslator.apply(componentBdp);
        releaseSW360.getModerators().add(user.getEmail());

        Optional<String> potentialComponentId = searchExistingEntityId(thriftExchange.searchComponentByName(componentBdp.getName()),
                Component::getId,
                "Component");
        String componentId;
        if (potentialComponentId.isPresent()) {
            componentId = potentialComponentId.get();
        } else {
            Component componentSW360 = componentToComponentTranslator.apply(componentBdp);
            componentId = thriftExchange.addComponent(componentSW360, user);
        }
        releaseSW360.setComponentId(componentId);

        String licenseId = getOrCreateLicenseId(componentBdp.getLicense(), user);
        releaseSW360.setMainLicenseIds(Collections.singleton(licenseId));

        return thriftExchange.addRelease(releaseSW360, user);
    }

    private ReleaseRelation createReleaseRelation(com.bosch.osmi.bdp.access.api.model.Component componentBdp, User user) {
        String releaseId = getOrCreateComponent(componentBdp, user);
        if (releaseId == null) {
            return null;
        } else {
            ReleaseRelationship releaseRelationship = usageLevelToReleaseRelationshipTranslator.apply(componentBdp.getUsageLevel());
            return new ReleaseRelation(releaseId, releaseRelationship);
        }
    }

    private Set<ReleaseRelation> createReleaseRelations(ProjectInfo projectBdp, User user) {
        Collection<com.bosch.osmi.bdp.access.api.model.Component> componentsBdp = projectBdp.getProject().getComponents();
        if (componentsBdp == null) {
            return ImmutableSet.of();
        }

        Set<ReleaseRelation> releaseRelations = componentsBdp.stream()
                .map(c -> createReleaseRelation(c, user))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (releaseRelations.size() != componentsBdp.size()) {
            logger.warn("expected to get " + componentsBdp.size() + " different ids of releases but got " + releaseRelations.size());
        } else {
            logger.info("The expected number of releases was imported or already found in database.");
        }

        return releaseRelations;
    }

    protected ProjectImportResult createProject(String bdpId, User user) throws TException  {
        logger.info("Try to import bdp Project: " + bdpId);
        logger.info("Sw360-User: " + user.email);

        com.bosch.osmi.bdp.access.api.model.ProjectInfo projectBdp = bdpApiAccessWrapper.getProjectInfo(bdpId);
        if (projectBdp == null) {
            logger.error("Unable to get Project from BDP Server named: " + bdpId);
            return new ProjectImportResult(ProjectImportError.PROJECT_NOT_FOUND);
        }

        String bdpName = projectBdp.getProjectName();
        if (thriftExchange.doesProjectAlreadyExists(bdpId, bdpName, user)) {
            logger.error("Project already in database: " + bdpId);
            return new ProjectImportResult(ProjectImportError.PROJECT_ALREADY_EXISTS);
        }

        Set<ReleaseRelation> releaseRelations = createReleaseRelations(projectBdp, user);

        Project projectSW360 = projectInfoToProjectTranslator.apply(projectBdp);
        projectSW360.setProjectResponsible(user.getEmail());
        projectSW360.setReleaseIdToUsage(releaseRelations.stream()
                .collect(Collectors.toMap(ReleaseRelation::getReleaseId, ReleaseRelation::getProjectReleaseRelationship)));

        String project = thriftExchange.addProject(projectSW360, user);
        if(isNullOrEmpty(project)) {
            return new ProjectImportResult(ProjectImportError.OTHER);
        } else {
            return new ProjectImportResult(project);
        }
    }

    public ImportStatus importBdpProjects(Collection<String> bdpProjectIds, User user) {
        List<String> successfulIds = new ArrayList<>();
        Map<String, String> failedIds = new HashMap<>();
        ImportStatus bdpImportStatus = new ImportStatus().setRequestStatus(RequestStatus.SUCCESS);

        for (String bdpId : bdpProjectIds) {
            ProjectImportResult projectImportResult;
            try{
                projectImportResult = createProject(bdpId, user);
            } catch (TException e){
                logger.error("Error when creating the project", e);
                bdpImportStatus.setRequestStatus(RequestStatus.FAILURE);
                return bdpImportStatus;
            }
            if (projectImportResult.isSuccess()) {
                successfulIds.add(bdpId);
            } else {
                logger.error("Could not import project with bdpId: " + bdpId);
                failedIds.put(bdpId, projectImportResult.getError().getText());
            }
        }
        return bdpImportStatus
                .setFailedIds(failedIds)
                .setSuccessfulIds(successfulIds);
    }
}
