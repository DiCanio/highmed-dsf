package org.highmed.pseudonymization.encoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;

import org.highmed.openehr.OpenEhrObjectMapperFactory;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.pseudonymization.bloomfilter.BloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGenerator;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl.FieldBloomFilterLengths;
import org.highmed.pseudonymization.bloomfilter.RecordBloomFilterGeneratorImpl.FieldWeights;
import org.highmed.pseudonymization.crypto.AesGcmUtil;
import org.highmed.pseudonymization.mpi.Idat;
import org.highmed.pseudonymization.mpi.MasterPatientIndexClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSetTranslatorToTtpTest
{
	private static final Logger logger = LoggerFactory.getLogger(ResultSetTranslatorToTtpTest.class);

	@Test
	public void testTranslateForTtp() throws Exception
	{
		Random random = new Random();

		int recordBloomFilterLength = 2000;
		byte[] permutationSeed = new byte[64];
		random.nextBytes(permutationSeed);

		FieldWeights weights = new FieldWeights(0.1, 0.1, 0.1, 0.2, 0.05, 0.1, 0.05, 0.2, 0.1);
		FieldBloomFilterLengths lengths = new FieldBloomFilterLengths(500, 500, 250, 50, 500, 250, 500, 500, 500);

		byte[] hashKey1 = new byte[64];
		random.nextBytes(hashKey1);
		byte[] hashKey2 = new byte[64];
		random.nextBytes(hashKey2);

		RecordBloomFilterGenerator recordBloomFilterGenerator = new RecordBloomFilterGeneratorImpl(
				recordBloomFilterLength, permutationSeed, weights, lengths,
				() -> new BloomFilterGenerator.HmacMd5HmacSha1BiGramHasher(hashKey1, hashKey2));

		Map<String, Idat> idats = Map.of("ehrId1", new IdatTestImpl("medicId1", "firstName1", "lastName1", "birthday1",
				"sex1", "street1", "zipCode1", "city1", "country1", "insuranceNumber"));
		MasterPatientIndexClient masterPatientIndexClient = new MasterPatientIndexClientTestImpl(idats);
		String organizationIdentifier = "org1";
		SecretKey organizationKey = AesGcmUtil.generateAES256Key();
		String researchStudyIdentifier = "researchStudy1";
		SecretKey researchStudyKey = AesGcmUtil.generateAES256Key();

		ResultSetTranslatorToTtp translator = new ResultSetTranslatorToTtp(organizationIdentifier, organizationKey,
				researchStudyIdentifier, researchStudyKey, recordBloomFilterGenerator, masterPatientIndexClient);

		ObjectMapper openEhrObjectMapper = OpenEhrObjectMapperFactory.createObjectMapper();
		ResultSet resultSet = openEhrObjectMapper
				.readValue(Files.readAllBytes(Paths.get("src/test/resources/result_5.json")), ResultSet.class);
		assertNotNull(resultSet);
		assertNotNull(resultSet.getColumns());
		assertEquals(4, resultSet.getColumns().size());
		assertNotNull(resultSet.getRows());
		assertEquals(1, resultSet.getRows().size());
		assertNotNull(resultSet.getRow(0));
		assertEquals(4, resultSet.getRow(0).size());

		ResultSet translatedResultSet = translator.translate(resultSet);
		assertNotNull(translatedResultSet);
		assertNotNull(translatedResultSet.getColumns());
		assertEquals(5, translatedResultSet.getColumns().size());
		assertNotNull(translatedResultSet.getRows());
		assertEquals(1, translatedResultSet.getRows().size());
		assertNotNull(translatedResultSet.getRow(0));
		assertEquals(5, translatedResultSet.getRow(0).size());

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		logger.debug("Encoded ResultSet {}",
				openEhrObjectMapper.writer(prettyPrinter).writeValueAsString(translatedResultSet));
	}
}
