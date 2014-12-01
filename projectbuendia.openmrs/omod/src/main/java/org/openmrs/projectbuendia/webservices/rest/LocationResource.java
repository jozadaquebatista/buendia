package org.openmrs.projectbuendia.webservices.rest;

import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.openmrs.module.webservices.rest.web.resource.api.Listable;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.resource.api.Updatable;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.projectbuendia.openmrs.webservices.rest.RestController;

import java.util.List;

/**
 * Rest API for getting locations in an EMC (Ebola Management Centre). Example JSON for a location looks like:
 *
 * <pre>
 * {
 *   uuid: “1234-5”
 *   names {
 *     “en”: “Kailahun” // Or suspect zone, or tent 3, or bed2, tent 3
 *     “fr”: “Kailahun” // we won’t localize at first, but this gives us the ability to later without code changes
 *   }
 *   parent_uuid: “4567-3” // uuid of parent location
 * }
 * </pre>
 */
@Resource(name = RestController.REST_VERSION_1_AND_NAMESPACE + "/location", supportedClass = Location.class, supportedOpenmrsVersions = "1.10.*,1.11.*")
public class LocationResource implements Listable, Retrievable, Creatable, Updatable {

    // JSON Constants.
    private static final String UUID = "uuid";
    private static final String PARENT_UUID = "parent_uuid";
    private static final String NAMES = "names";

    // Known zones uuids.
    private static final String EMC_UUID = "3449f5fe-8e6b-4250-bcaa-fca5df28ddbf";
    private static final String TRIAGE_ZONE_UUID = "3f75ca61-ec1a-4739-af09-25a84e3dd237";
    private static final String SUSPECT_ZONE_UUID = "2f1e2418-ede6-481a-ad80-b9939a7fde8e";
    private static final String PROBABLE_ZONE_UUID = "3b11e7c8-a68a-4a5f-afb3-a4a053592d0e";
    private static final String CONFIRMED_ZONE_UUID = "b9038895-9c9d-4908-9e0d-51fd535ddd3c";
    private static final String MORGUE_ZONE_UUID = "4ef642b9-9843-4d0d-9b2b-84fe1984801f";
    // TODO: The design doc mentions "discharged", check this

    private final LocationService locationService = Context.getLocationService();

    @Override
    public Object create(SimpleObject simpleObject, RequestContext requestContext) throws ResponseException {
        return null;
    }

    @Override
    public SimpleObject getAll(RequestContext requestContext) throws ResponseException {
        return null;
    }

    @Override
    public Object retrieve(String s, RequestContext requestContext) throws ResponseException {
        return null;
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return null;
    }

    @Override
    public Object update(String s, SimpleObject simpleObject, RequestContext requestContext) throws ResponseException {
        return null;
    }

    @Override
    public String getUri(Object o) {
        return null;
    }
}
