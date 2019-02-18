package org.highmed.fhir.webservice.specification;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionService extends BasicService<StructureDefinition>
{
	Response getSnapshotExisting(String snapshotPath, String id, String format, UriInfo uri);

	Response postSnapshotExisting(String snapshotPath, String id, String format, UriInfo uri);

	Response snapshotNew(String snapshotPath, String format, Parameters parameters, UriInfo uri);
}
