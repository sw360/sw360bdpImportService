/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.model.Component;
import com.bosch.osmi.bdp.access.api.model.License;
import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Test up data source to receive all user information.
 */
public class BdpApiAccessWrapperMockTest {

    private BdpApiAccessWrapperMock mock;
    private static URL jsonFilePath;

    @BeforeClass
    public static void setUpBefore() {
        jsonFilePath = BdpApiAccessWrapperMockTest.class.getResource("/mockdata.json");
        assertThat("File could not be found. Put Json-File /mockdata.json into /src/test/resources and try again.", jsonFilePath, is(notNullValue()));
    }

    @Before
    public void setUp() throws Exception {
        mock = new BdpApiAccessWrapperMock(jsonFilePath.getFile());
        assertThat(mock, is(notNullValue()));
    }

    @Test
    public void testProjectInfo() {
        Collection<ProjectInfo> projectInfos = mock.getUserProjectInfos();
        assertThat(projectInfos.size(), is(3));
        TreeSet<ProjectInfo> sortedProjectInfo = new TreeSet(Comparator.comparing(ProjectInfo::getProjectName).reversed());
        sortedProjectInfo.addAll(projectInfos);

        sortedProjectInfo.pollFirst();
        ProjectInfo projectInfo = sortedProjectInfo.first();
        Assertions.assertProjectInfo(projectInfo);

        Project project = projectInfo.getProject();
        Assertions.assertProject(projectInfo);

        Collection<Component> components = project.getComponents();
        assertThat(components.size(), is(7));
        TreeSet<Component> sortedComponent = new TreeSet(Comparator.comparing(Component::getName).reversed());
        sortedComponent.addAll(components);

        Component component = Assertions.firstOf(sortedComponent);
        Assertions.assertComponent(component);

        License license = component.getLicense();
        Assertions.assertLicense(license);
    }

    @Test
    public void testSuggestProjectInfos() {
        Collection<ProjectInfo> projectInfos = mock.suggestProjectInfos("");
        assertThat(projectInfos.size(), is(3));
        TreeSet<ProjectInfo> sortedProjectInfo = new TreeSet(Comparator.comparing(ProjectInfo::getProjectName).reversed());
        sortedProjectInfo.addAll(projectInfos);

        sortedProjectInfo.pollFirst();
        ProjectInfo projectInfo = sortedProjectInfo.first();
        Assertions.assertProjectInfo(projectInfo);

        Project project = projectInfo.getProject();
        Assertions.assertProject(projectInfo);

        Collection<Component> components = project.getComponents();
        assertThat(components.size(), is(7));
        TreeSet<Component> sortedComponent = new TreeSet(Comparator.comparing(Component::getName).reversed());
        sortedComponent.addAll(components);

        Component component = Assertions.firstOf(sortedComponent);
        Assertions.assertComponent(component);

        License license = component.getLicense();
        Assertions.assertLicense(license);
    }

    @Test
    public void testGetUserProjectInfos() {
        Collection<ProjectInfo> userProjectInfos = mock.getUserProjectInfos();
        assertThat(userProjectInfos.size(), is(3));

        ((List) userProjectInfos).remove(0);//c_bdp-api-access_8104 is third project
        ((List) userProjectInfos).remove(0);
        Assertions.assertProjectInfo(firstOf(userProjectInfos));
    }

    @Test
    public void testGetAllProject() {
        Collection<ProjectInfo> allProjectInfos = mock.getUserProjectInfos();
        TreeSet<ProjectInfo> sortedProjects = new TreeSet<>(Comparator.comparing(ProjectInfo::getProjectId).reversed());
        sortedProjects.addAll(allProjectInfos);
        assertThat(sortedProjects.size(), is(3));

        sortedProjects.pollFirst();//c_bdp-api-access_8104 is second project
        Assertions.assertProject((ProjectInfo) sortedProjects.first());
    }

    private static <T> T firstOf(Collection<T> value) {
        return Assertions.firstOf(value);
    }
}
