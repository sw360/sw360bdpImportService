/*
 * Copyright (c) Bosch Software Innovations GmbH 2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.entitytranslation;

import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;

import java.util.Map;

public class BdpUsageLevelToSw360ReleaseRelationship implements EntityTranslator<String, ReleaseRelationship> {

    private static final Map<String, ReleaseRelationship> TRANSLATION = ImmutableMap
            .<String, ReleaseRelationship>builder()
            .put(UsageLevel.SNIPPET.name(), ReleaseRelationship.CONTAINED)
            .put(UsageLevel.FILE.name(), ReleaseRelationship.CONTAINED)
            .put(UsageLevel.COMPONENT.name(), ReleaseRelationship.CONTAINED)
            .put(UsageLevel.COMPONENT_MERELY_AGGREGATED.name(), ReleaseRelationship.SIDE_BY_SIDE)
            .put(UsageLevel.COMPONENT_SEPARATE_WORK.name(), ReleaseRelationship.STANDALONE)
            .put(UsageLevel.COMPONENT_DYNAMIC_LIBRARY.name(), ReleaseRelationship.DYNAMICALLY_LINKED)
            .put(UsageLevel.COMPONENT_MODULE.name(), ReleaseRelationship.STATICALLY_LINKED)
            .put(UsageLevel.PREREQUISITE.name(), ReleaseRelationship.CONTAINED)
            .put(UsageLevel.PREREQUISITE_MERLY_AGGREGATED.name(), ReleaseRelationship.SIDE_BY_SIDE)
            .put(UsageLevel.PREREQUISITE_SEPARATE_WORK.name(), ReleaseRelationship.STANDALONE)
            .put(UsageLevel.PREREQUISITE_DYNAMIC_LIBRARY.name(), ReleaseRelationship.DYNAMICALLY_LINKED)
            .put(UsageLevel.PREREQUISITE_MODULE.name(), ReleaseRelationship.STATICALLY_LINKED)
            .put(UsageLevel.PREREQUISITE_SERVICE.name(), ReleaseRelationship.STANDALONE)
            .put(UsageLevel.IMPLEMENTATION_OF_STANDARD.name(), ReleaseRelationship.REFERRED)
            .put(UsageLevel.DEVELOPMENT_TOOL.name(), ReleaseRelationship.REFERRED)
            .put(UsageLevel.ORIGINAL_CODE.name(), ReleaseRelationship.INTERNAL_USE)
            .build();

    private static final ReleaseRelationship DEFAULT_VALUE = ReleaseRelationship.UNKNOWN;

    @Override
    public ReleaseRelationship apply(String usageLevel) {
        return MoreObjects.firstNonNull(TRANSLATION.get(usageLevel), DEFAULT_VALUE);
    }
}
