package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class SubscriptionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public SubscriptionUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "subscription");
	}
}
