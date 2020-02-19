package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.search.parameters.user.StructureDefinitionUserFilter;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionDaoJdbc extends AbstractStructureDefinitionDaoJdbc implements StructureDefinitionDao
{
	public StructureDefinitionDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext,
			OrganizationType organizationType)
	{
		super(dataSource, fhirContext, organizationType, "structure_definitions", "structure_definition",
				"structure_definition_id", StructureDefinitionUserFilter::new);
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}
}
