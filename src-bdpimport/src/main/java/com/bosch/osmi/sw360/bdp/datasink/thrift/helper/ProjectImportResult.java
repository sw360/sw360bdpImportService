/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasink.thrift.helper;

import java.util.Optional;

public class ProjectImportResult {
    private final Optional<String> projectId;
    private final Optional<ProjectImportError> error;

    public ProjectImportResult(String projectId) {
        this.projectId = Optional.of(projectId);
        this.error = Optional.empty();
    }

    public ProjectImportResult(ProjectImportError error) {
        this.projectId = Optional.empty();
        this.error = Optional.of(error);
    }

    public boolean isSuccess() {
        return projectId.isPresent();
    }

    public ProjectImportError getError() {
        return error.get();
    }
}
