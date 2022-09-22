package com.krock.xmlapp.controller;

import com.krock.xmlapp.model.SimpleXmlRequest;
import com.krock.xmlapp.sax.CustomErrorHandlerSax;
import com.krock.xmlapp.sax.MapSimpleRequestSaxHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.xml.XMLConstants;
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
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Configuration
public class SimpleController {

/*
--Correct payload
    <?xml version="1.0" encoding="UTF-8"?>
    <SimpleXmlRequest>
        <name>KhoaLe</name>
        <bio>2</bio>
    </SimpleXmlRequest>
--Wrong payload
    <?xml version="1.0" encoding="UTF-8"?>
    <SimpleXmlRequest>
        <name>KhoaLe</name>
        <bio>ABC</bio>
    </SimpleXmlRequest>
*/
    @Bean
    RouterFunction<ServerResponse> handleXmlWithSax() {
        return RouterFunctions.route().POST("/transactions",
//                request -> request.headers().contentType().map(mediaType -> mediaType.includes(MediaType.APPLICATION_XML)).orElse(false),
                request -> {
                    Mono<String> r = request.bodyToMono(String.class).publishOn(Schedulers.boundedElastic())
                            .doOnNext(validateXml)
                            .doOnNext(bindingXml);
                    return ServerResponse.ok().body(r, String.class);
                }).build();
    }

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