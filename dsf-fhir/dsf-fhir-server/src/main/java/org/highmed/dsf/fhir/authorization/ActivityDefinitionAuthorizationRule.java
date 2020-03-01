package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionAuthorizationRule
		extends AbstractAuthorizationRule<ActivityDefinition, ActivityDefinitionDao>
{
	public ActivityDefinitionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		super(ActivityDefinition.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, ActivityDefinition newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, ActivityDefinition existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, ActivityDefinition oldResource,
			ActivityDefinition newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, ActivityDefinition oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
