package com.krock.xmlapp.jaxb;

import lombok.RequiredArgsConstructor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class XmlMapper<T> {

    private final Jaxb2Marshaller jaxb2Marshaller;

    public T bindingWithAutoValidation(String xmlString, Class<T> declaredType) throws JAXBException {
        try (InputStream is = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
            StreamSource streamSource = new StreamSource(is);
            return jaxb2Marshaller.createUnmarshaller().unmarshal(streamSource, declaredType).getValue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toXml(T model) throws JAXBException {
        StringWriter writer = new StringWriter();
        Marshaller marshaller = jaxb2Marshaller.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(model, writer);
        return writer.toString();
    }
}
