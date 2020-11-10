package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.fhir.variables.FhirResourceJacksonDeserializer;
import org.highmed.dsf.fhir.variables.FhirResourceJacksonSerializer;
import org.highmed.dsf.fhir.variables.FhirResourceSerializer;
import org.highmed.dsf.fhir.variables.FhirResourcesListSerializer;
import org.highmed.dsf.fhir.variables.TargetSerializer;
import org.highmed.dsf.fhir.variables.TargetsSerializer;
import org.highmed.openehr.json.OpenEhrObjectMapperFactory;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class SerializerConfig
{
	@Autowired
	private FhirContext fhirContext;

	@Bean
	public ObjectMapper objectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();

		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);

		SimpleModule module = new SimpleModule();
		module.addSerializer(Resource.class, new FhirResourceJacksonSerializer(fhirContext));
		module.addDeserializer(Resource.class, new FhirResourceJacksonDeserializer(fhirContext));

		mapper.registerModule(module);
		mapper.registerModule(OpenEhrObjectMapperFactory.openEhrModule());

		return mapper;
	}

	@Bean
	public FhirResourceSerializer fhirResourceSerializer()
	{
		return new FhirResourceSerializer(fhirContext);
	}

	@Bean
	public FhirResourcesListSerializer fhirResourcesListSerializer()
	{
		return new FhirResourcesListSerializer(objectMapper());
	}

	@Bean
	public TargetSerializer targetSerializer()
	{
		return new TargetSerializer(objectMapper());
	}

	@Bean
	public TargetsSerializer targetsSerializer()
	{
		return new TargetsSerializer(objectMapper());
	}
}
