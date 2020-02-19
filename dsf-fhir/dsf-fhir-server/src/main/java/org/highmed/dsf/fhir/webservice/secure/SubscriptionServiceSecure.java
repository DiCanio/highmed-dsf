package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.SubscriptionAuthorizationRule;
import org.highmed.dsf.fhir.dao.SubscriptionDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionServiceSecure extends
		AbstractResourceServiceSecure<SubscriptionDao, Subscription, SubscriptionService> implements SubscriptionService
{
	public SubscriptionServiceSecure(SubscriptionService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, SubscriptionDao subscriptionDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			SubscriptionAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Subscription.class, subscriptionDao,
				exceptionHandler, parameterConverter, authorizationRule);
	}
}
