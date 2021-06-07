package org.highmed.dsf.bpe.spring.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelperImpl;
import org.highmed.dsf.fhir.client.FhirClientProviderImpl;
import org.highmed.dsf.fhir.client.FhirWebsocketClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.group.GroupHelperImpl;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.organization.OrganizationProviderImpl;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceCleanerImpl;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceExtractorImpl;
import org.highmed.dsf.fhir.task.TaskHandler;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.task.TaskHelperImpl;
import org.highmed.dsf.fhir.websocket.FhirConnector;
import org.highmed.dsf.fhir.websocket.FhirConnectorImpl;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.context.FhirContext;
import de.rwh.utils.crypto.CertificateHelper;
import de.rwh.utils.crypto.io.CertificateReader;

@Configuration
public class FhirConfig
{
	@Value("${org.highmed.dsf.bpe.fhir.organization.identifier.localValue}")
	private String organizationIdentifierLocalValue;

	@Value("${org.highmed.dsf.bpe.fhir.local.webservice.keystore.p12file}")
	private String webserviceKeyStoreFile;

	@Value("${org.highmed.dsf.bpe.fhir.local.webservice.keystore.password}")
	private char[] webserviceKeyStorePassword;

	@Value("${org.highmed.dsf.bpe.fhir.remote.webservice.readTimeout}")
	private int remoteReadTimeout;

	@Value("${org.highmed.dsf.bpe.fhir.remote.webservice.connectTimeout}")
	private int remoteConnectTimeout;

	@Value("${org.highmed.dsf.bpe.fhir.remote.webservice.proxy.password:#{null}}")
	private char[] remoteProxyPassword;

	@Value("${org.highmed.dsf.bpe.fhir.remote.webservice.proxy.username:#{null}}")
	private String remoteProxyUsername;

	@Value("${org.highmed.dsf.bpe.fhir.remote.webservice.proxy.schemeHostPort:#{null}}")
	private String remoteProxySchemeHostPort;

	@Value("${org.highmed.dsf.bpe.fhir.local.webservice.baseUrl}")
	private String localWebserviceBaseUrl;

	@Value("${org.highmed.dsf.bpe.fhir.local.webservice.readTimeout}")
	private int localReadTimeout;

	@Value("${org.highmed.dsf.bpe.fhir.local.webservice.connectTimeout}")
	private int localConnectTimeout;

	@Value("${org.highmed.dsf.bpe.fhir.local.websocket.url}")
	private String localWebsocketUrl;

	@Value("${org.highmed.dsf.bpe.fhir.local.websocket.keystore.p12file}")
	private String localWebsocketKeyStoreFile;

	@Value("${org.highmed.dsf.bpe.fhir.local.websocket.keystore.password}")
	private char[] localWebsocketKeyStorePassword;

	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.searchParameter}")
	private String subscriptionSearchParameter;

	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.lastEventTimeFile}")
	private String lastEventTimeFile;

	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.retrySleepMillis:5000}")
	private long websocketRetrySleepMillis;

	@Value("${org.highmed.dsf.bpe.fhir.task.subscription.maxRetries:-1}")
	private int websocketMaxRetries;

	@Autowired
	@Lazy
	private ProcessEngine processEngine;

	@Bean
	public FhirContext fhirContext()
	{
		return FhirContext.forR4();
	}

	@Bean
	public ReferenceCleaner referenceCleaner()
	{
		return new ReferenceCleanerImpl(referenceExtractor());
	}

	@Bean
	public ReferenceExtractor referenceExtractor()
	{
		return new ReferenceExtractorImpl();
	}

	@Bean
	public LastEventTimeIo lastEventTimeIo()
	{
		return new LastEventTimeIo(Paths.get(lastEventTimeFile));
	}

	@Bean
	public TaskHandler taskHandler()
	{
		return new TaskHandler(processEngine.getRuntimeService(), processEngine.getRepositoryService(),
				clientProvider().getLocalWebserviceClient(), taskHelper());
	}

	@Bean
	public FhirWebsocketClientProvider clientProvider()
	{
		try
		{
			Path localWebserviceKsFile = Paths.get(webserviceKeyStoreFile);

			if (!Files.isReadable(localWebserviceKsFile))
				throw new IOException(
						"Webservice keystore file '" + localWebserviceKsFile.toString() + "' not readable");

			KeyStore localWebserviceKeyStore = CertificateReader.fromPkcs12(localWebserviceKsFile,
					webserviceKeyStorePassword);
			KeyStore localWebserviceTrustStore = CertificateHelper.extractTrust(localWebserviceKeyStore);

			Path localWebsocketKsFile = Paths.get(localWebsocketKeyStoreFile);

			if (!Files.isReadable(localWebsocketKsFile))
				throw new IOException("Websocket keystore file '" + localWebsocketKsFile.toString() + "' not readable");

			KeyStore localWebsocketKeyStore = CertificateReader.fromPkcs12(localWebsocketKsFile,
					localWebsocketKeyStorePassword);
			KeyStore localWebsocketTrustStore = CertificateHelper.extractTrust(localWebsocketKeyStore);

			return new FhirClientProviderImpl(fhirContext(), referenceCleaner(), localWebserviceBaseUrl,
					localReadTimeout, localConnectTimeout, localWebserviceTrustStore, localWebserviceKeyStore,
					webserviceKeyStorePassword, remoteReadTimeout, remoteConnectTimeout, remoteProxyPassword,
					remoteProxyUsername, remoteProxySchemeHostPort, localWebsocketUrl, localWebsocketTrustStore,
					localWebsocketKeyStore, localWebsocketKeyStorePassword);
		}
		catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Bean
	public OrganizationProvider organizationProvider()
	{
		return new OrganizationProviderImpl(clientProvider(), organizationIdentifierLocalValue);
	}

	@Bean
	public FhirConnector fhirConnector()
	{
		return new FhirConnectorImpl(clientProvider(), taskHandler(), lastEventTimeIo(), fhirContext(),
				subscriptionSearchParameter, websocketRetrySleepMillis, websocketMaxRetries);
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		fhirConnector().connect();
	}

	@Bean
	public TaskHelper taskHelper()
	{
		return new TaskHelperImpl();
	}

	@Bean
	public GroupHelper groupHelper()
	{
		return new GroupHelperImpl();
	}

	@Bean
	public ReadAccessHelper readAccessHelper()
	{
		return new ReadAccessHelperImpl();
	}
}
