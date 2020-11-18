package org.highmed.dsf.fhir.validation;

import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

public class ResourceValidatorImpl implements ResourceValidator
{
	private final FhirValidator validator;

	public ResourceValidatorImpl(FhirContext context, IValidationSupport validationSupport)
	{
		this.validator = configureValidator(context.newValidator(), validationSupport);
	}

	protected FhirValidator configureValidator(FhirValidator validator, IValidationSupport validationSupport)
	{
		FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
		validator.registerValidatorModule(instanceValidator);
		return validator;
	}

	@Override
	public ValidationResult validate(Resource resource)
	{
		return validator.validateWithResult(resource);
	}
}
