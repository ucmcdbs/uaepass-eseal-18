package com.ucmcswg.samples.eseal;

import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.AnyType;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.Base64Data;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.Document;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.InputDocuments;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.ObjectFactory;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.OptionalInputs;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.OptionalOutputs;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.Properties;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.PropertiesType;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.Property;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.SignRequest;
import org.oasis_open.docs.dss._2004._06.oasis_dss_1_0_core_schema_wd_27.SignResponse;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import com.safelayer.tws.KeySelector;
import com.safelayer.tws.KeyUsageType;
import com.safelayer.tws.PdfSignatureInfoType;

import oasis.names.tc.saml._1_0.assertion.NameIdentifierType;

public class EsealClient extends WebServiceGatewaySupport {

    private static final String TWS_AUTHN = "urn:safelayer:tws:policies:authentication:oauth:clients";
    private static final String PROFILE = "urn:safelayer:tws:dss:1.0:profiles:pades:1.0:sign";
    private static final String IDENTIFIER = "urn:safelayer:tws:dss:1.0:property:pdfattributes";
    private static final String STATUS_SUCCESS = "urn:oasis:names:tc:dss:1.0:resultmajor:Success";
    private static final String MIMETYPE = "application/pdf";
    private static final String SOAP_ACTION = "Sign";

    private final ObjectFactory objectFactory;
    private final EsealClientProperties esealClientProperties;

    public EsealClient(EsealClientProperties esealClientProperties) {
        this.objectFactory = new ObjectFactory();
        this.esealClientProperties = esealClientProperties;
    }

    private WebServiceMessageCallback getRequestCallback() {
        return message -> {
            TransportContext context = TransportContextHolder.getTransportContext();
            HttpUrlConnection connection = (HttpUrlConnection)context.getConnection();
            connection.addRequestHeader("TwsAuthN", TWS_AUTHN);
            connection.addRequestHeader("SOAPAction", SOAP_ACTION);
        };
    }

    public EsealResponse sealDocument(byte[] data) {

        InputDocuments inputDocuments = this.objectFactory.createInputDocuments();

        Document document = this.objectFactory.createDocument();

        document.setID(UUID.randomUUID().toString());

        Base64Data base64Data =  this.objectFactory.createBase64Data();
        base64Data.setMimeType(MIMETYPE);
        base64Data.setValue(data);
        document.setBase64Data(base64Data);
        inputDocuments.getDocumentOrDocumentHashOrBase64Signature().add(document);

        NameIdentifierType nameIdentifier = new NameIdentifierType();
        nameIdentifier.setValue(esealClientProperties.getSignature().get("key-name"));
        nameIdentifier.setFormat(esealClientProperties.getSignature().get("key-format"));

        KeySelector keySelector = new KeySelector();
        keySelector.setKeyUsage(KeyUsageType.NON_REPUDIATION.value());
        keySelector.setName(nameIdentifier);

        OptionalInputs optionalInputs = this.objectFactory.createOptionalInputs();
        optionalInputs.setKeySelector(this.objectFactory.createKeySelector());
        optionalInputs.getKeySelector().setAny(keySelector);

        JAXBElement<PdfSignatureInfoType> pdfSignatureInfo = new EsealSignature(esealClientProperties).getPdfSignature();

        Property property = this.objectFactory.createProperty();
        property.setIdentifier(IDENTIFIER);
        property.setValue(this.objectFactory.createAnyType());
        property.getValue().getAny().add(pdfSignatureInfo);

        Properties properties = this.objectFactory.createProperties();
        PropertiesType signedProperties = this.objectFactory.createPropertiesType();
        signedProperties.getProperty().add(property);
        properties.setSignedProperties(signedProperties);

        optionalInputs.setProperties(properties);

        SignRequest request = this.objectFactory.createSignRequest();
        request.setRequestID(UUID.randomUUID().toString());
        request.setProfile(PROFILE);
        request.setInputDocuments(inputDocuments);
        request.setOptionalInputs(optionalInputs);

        SignResponse response = (SignResponse) getWebServiceTemplate()
            .marshalSendAndReceive(
                esealClientProperties.getClient().get("uri"),
                request,
                getRequestCallback()
            );

        EsealResponse esealResponse = new EsealResponse();

        String status = response.getResult().getResultMajor();
        esealResponse.setStatus(status);

        if (status.equals(STATUS_SUCCESS)) {
            OptionalOutputs optionalOutputs = response.getOptionalOutputs();
            if (optionalOutputs != null) {
                AnyType responseObjects =  optionalOutputs.getDocumentWithSignature().getXMLData();
                for (Object responseObject : responseObjects.getAny()) {
                    if (responseObject instanceof Base64Data) {
                        Base64Data base64Response = (Base64Data) responseObject;
                        esealResponse.setData(base64Response.getValue());
                        esealResponse.isSuccess = true;
                    }
                }
            }
        }

        return esealResponse;
      }
}
