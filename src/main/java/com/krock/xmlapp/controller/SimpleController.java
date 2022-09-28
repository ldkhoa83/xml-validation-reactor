package com.krock.xmlapp.controller;

import com.krock.xmlapp.exception.GlobalException;
import com.krock.xmlapp.jaxb.XmlMapper;
import com.krock.xmlapp.model.RejectResponse;
import com.krock.xmlapp.model.ResponseGroup;
import com.krock.xmlapp.model.SimpleXmlRequest;
import com.krock.xmlapp.sax.CustomErrorHandlerSax;
import com.krock.xmlapp.sax.MapSimpleRequestSaxHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SimpleController {


    /*
    --Correct payload
        <?xml version="1.0" encoding="UTF-8"?>
        <SimpleXmlRequest xmlns="mySchema">
            <person>
                <name>KhoaLe</name>
                <bio>2</bio>
                <gender>male</gender>
            </person>
        </SimpleXmlRequest>
    --Wrong payload
        <?xml version="1.0" encoding="UTF-8"?>
        <SimpleXmlRequest xmlns="mySchema">
            <person>
                <name>KhoaLe</name>
                <bio>ABC</bio>
                <gender>male</gender>
            </person>
        </SimpleXmlRequest>
    */
    @Bean
    RouterFunction<ServerResponse> handleXmlWithJAXBV1(XmlMapper<SimpleXmlRequest> xmlMapper) {
        return RouterFunctions.route().POST("/v1/persons",
                        request -> request.bodyToMono(String.class)
                                .map(s -> {
                                    try {
                                        return xmlMapper.bindingWithAutoValidation(s, SimpleXmlRequest.class);
                                    } catch (JAXBException e) {
                                        throw Exceptions.propagate(e);
                                    }
                                })
                                .map(m -> {
                                    try {
                                        return xmlMapper.toXml(m);
                                    } catch (JAXBException e) {
                                        throw Exceptions.propagate(e);
                                    }
                                })
                                .onErrorResume(e -> {
                                    SimpleXmlRequest sxr = new SimpleXmlRequest();
                                    RejectResponse rejectResponse = new RejectResponse();
                                    rejectResponse.setResponseGroup(new ResponseGroup(xmlValidationErrorMessageHandler(e)));
                                    sxr.setRejectResponse(rejectResponse);
                                    try {
                                        log.error(e.getMessage(),e);
                                        return Mono.error(new GlobalException(HttpStatus.BAD_REQUEST.value(), xmlMapper.toXml(sxr), e));
                                    } catch (JAXBException ex) {
                                        log.error(ex.getMessage(),ex);
                                        throw Exceptions.propagate(ex);
                                    }
                                })
                                .flatMap(s -> ServerResponse.ok().contentType(MediaType.APPLICATION_XML).body(Mono.just(s), String.class)))
                .onError(GlobalException.class, (e, r) -> ServerResponse
                        .badRequest()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(Mono.justOrEmpty(e.getReason()), String.class))
                .onError(e -> e instanceof JAXBException || e instanceof RuntimeException, (e,r) -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Mono.justOrEmpty("Internal server error"),String.class))
                .build();
    }

    private String xmlValidationErrorMessageHandler(Throwable e) {
        if (e instanceof UnmarshalException) {
            return "EC000 - XML Validation Error: " + ((UnmarshalException) e).getLinkedException().getMessage();
        }
        return e.getMessage();
    }

    @Bean
    RouterFunction<ServerResponse> handleXmlWithJAXBV2(XmlMapper<com.krock.xmlapp.model.v2.SimpleXmlRequest> xmlMapper) {
        return RouterFunctions.route().POST("/v2/persons",
                request -> {
                    Mono<String> r = request.bodyToMono(String.class)
                            .map(s -> {
                                try {
                                    return xmlMapper.bindingWithAutoValidation(s, com.krock.xmlapp.model.v2.SimpleXmlRequest.class);
                                } catch (JAXBException e) {
                                    throw Exceptions.propagate(e);
                                }
                            })
                            .map(Object::toString);
                    return ServerResponse.ok().body(r, String.class);
                }).build();
    }



    @Bean
    @SneakyThrows
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setSchemas(new PathMatchingResourcePatternResolver().getResources("/xsd/*"));
        marshaller.setPackagesToScan("com.krock.xmlapp.model");
        return marshaller;
    }

//---SAX parser way---
    private final Consumer<String> validateXml =
            xmlString -> {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                try (InputStream is = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
                    // create a SchemaFactory capable of understanding WXS schemas
                    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                    // load a WXS schema, represented by a Schema instance
                    Source schemaFile = new StreamSource(ResourceUtils.getFile("classpath:mySchema.xsd"));
                    Schema schema = schemaFactory.newSchema(schemaFile);
                    factory.setSchema(schema);

                    // create a Validator instance, which can be used to validate an instance document
                    Validator validator = schema.newValidator();

                    InputSource source = new InputSource(is);

                    validator.validate(new SAXSource(source));
                } catch (SAXException | IOException e) {
                    throw new RuntimeException(e);
                }
            };

    private final Consumer<String> bindingXml = (xmlString) -> {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try (InputStream is = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
            SAXParser saxParser = factory.newSAXParser();

            MapSimpleRequestSaxHandler handler = new MapSimpleRequestSaxHandler();
            //try XMLReader
//          saxParser.parse(is,handler);

            // more options for configuration
            XMLReader xmlReader = saxParser.getXMLReader();

            // set our custom error handler
            xmlReader.setErrorHandler(new CustomErrorHandlerSax(System.err));
            xmlReader.setContentHandler(handler);
            InputSource source = new InputSource(is);

            xmlReader.parse(source);
            SimpleXmlRequest simpleXmlRequest = handler.getResult();
            System.out.println(simpleXmlRequest);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    };

}
