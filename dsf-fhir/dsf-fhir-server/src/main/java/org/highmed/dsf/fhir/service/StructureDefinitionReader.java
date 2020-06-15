package org.highmed.dsf.fhir.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

public class StructureDefinitionReader
{
	private final FhirContext context;

	public StructureDefinitionReader(FhirContext context)
	{
		this.context = context;
	}

	public List<StructureDefinition> readXml(Path... xmlPaths)
	{
		return readXml(Arrays.asList(xmlPaths));
	}

	public List<StructureDefinition> readXml(List<Path> xmlPaths)
	{
		return readXml(xmlPaths.stream()).collect(Collectors.toList());
	}

	public Stream<StructureDefinition> readXml(Stream<Path> xmlPaths)
	{
		return xmlPaths.map(this::readXml);
	}

	public StructureDefinition readXml(Path xmlPath)
	{
		try (InputStream in = Files.newInputStream(xmlPath))
		{
			return context.newXmlParser().parseResource(StructureDefinition.class, in);
		}
		catch (DataFormatException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
