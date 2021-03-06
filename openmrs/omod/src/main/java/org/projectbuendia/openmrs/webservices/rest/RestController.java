// Copyright 2015 The Project Buendia Authors
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at: http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distrib-
// uted under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
// OR CONDITIONS OF ANY KIND, either express or implied.  See the License for
// specific language governing permissions and limitations under the License.

package org.projectbuendia.openmrs.webservices.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the REST resources in this module. This implicitly picks up
 * all the resources with the Resource annotation.
 */
@Controller
@RequestMapping("/rest/" + RestController.REST_VERSION_1_AND_NAMESPACE)
public class RestController extends MainResourceController {

    public static final String REST_VERSION_1_AND_NAMESPACE =
            RestConstants.VERSION_1 + "/projectbuendia";
    private final Log log = LogFactory.getLog(getClass());

    public RestController() {
        log.warn("Created ProjectBuendia RestController");
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return REST_VERSION_1_AND_NAMESPACE;
    }
}
