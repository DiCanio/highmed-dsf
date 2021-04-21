package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityDefinitionAuthorizationRule
		extends AbstractAuthorizationRule<ActivityDefinition, ActivityDefinitionDao>
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionAuthorizationRule.class);

	public ActivityDefinitionAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(ActivityDefinition.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, ActivityDefinition newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Create of ActivityDefinition authorized for local user '{}', ActivityDefinition with version and url does not exist",
							user.getName());
					return Optional.of("local user, ActivityDefinition with version and url does not exist yet");
				}
				else
				{
					logger.warn(
							"Create of ActivityDefinition unauthorized, ActivityDefinition with url and version already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of ActivityDefinition unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of ActivityDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(ActivityDefinition newResource)
	{
		List<String> errors = new ArrayList<String>();

		ActivityDefinitionProcessAuthorizationExtensions extensions = new ActivityDefinitionProcessAuthorizationExtensions(
				newResource);
		if (!extensions.isValid())
		{
			errors.add("ActivityDefinition.extension with url "
					+ ActivityDefinitionProcessAuthorizationExtensions.PROCESS_AUTHORIZATION_EXTENSION_URL
					+ " not valid or missing, at least one expected");
		}

		if (newResource.hasStatus())
		{
			if (!EnumSet.of(PublicationStatus.DRAFT, PublicationStatus.ACTIVE, PublicationStatus.RETIRED)
					.contains(newResource.getStatus()))
			{
				errors.add("ActivityDefinition.status not one of DRAFT, ACTIVE or RETIRED");
			}
		}
		else
		{
			errors.add("ActivityDefinition.status not defined");
		}

		if (!newResource.hasUrl())
		{
			errors.add("ActivityDefinition.url not defined");
		}
		if (!newResource.hasVersion())
		{
			errors.add("ActivityDefinition.version not defined");
		}

		if (!hasLocalOrRemoteAuthorizationRole(newResource))
		{
			errors.add("missing authorization tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private boolean resourceExists(Connection connection, ActivityDefinition newResource)
	{
		try
		{
			return getDao()
					.readByUrlAndVersionWithTransaction(connection, newResource.getUrl(), newResource.getVersion())
					.isPresent();
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for ActivityDefinition", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, ActivityDefinition existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of ActivityDefinition authorized for local user '{}', ActivityDefinition has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized CodeSystem");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of ActivityDefinition authorized for remote user '{}', ActivityDefinition has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized ActivityDefinition");
		}
		else
		{
			logger.warn(
					"Read of ActivityDefinition unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, ActivityDefinition oldResource,
			ActivityDefinition newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (isSame(oldResource, newResource))
				{
					logger.info(
							"Update of ActivityDefinition authorized for local user '{}', url and version same as existing ActivityDefinition",
							user.getName());
					return Optional.of("local user; url and version same as existing ActivityDefinition");
				}
				else
				{
					logger.warn(
							"Update of ActivityDefinition unauthorized, url or version changed ({} -> {}, {} -> {})",
							oldResource.getUrl(), newResource.getUrl(), oldResource.getVersion(),
							newResource.getVersion());
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of ActivityDefinition unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of ActivityDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isSame(ActivityDefinition oldResource, ActivityDefinition newResource)
	{
		return oldResource.getUrl().equals(newResource.getUrl())
				&& oldResource.getVersion().equals(newResource.getVersion());
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, ActivityDefinition oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of ActivityDefinition authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of ActivityDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of ActivityDefinition authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of ActivityDefinition authorized for {} user '{}', will be fitered by user role",
				user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, ActivityDefinition oldResource) {
		if (isLocalUser(user))
		{
			logger.info("Expunge of ActivityDefinition authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of ActivityDefinition unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
