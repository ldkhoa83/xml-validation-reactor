package com.krock.xmlapp.sax;

import com.krock.xmlapp.model.SimpleXmlRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MapSimpleRequestSaxHandler extends DefaultHandler {
    private StringBuilder currentValue = new StringBuilder();
    private SimpleXmlRequest currentSimpleRequest;

    public SimpleXmlRequest getResult() {
        return this.currentSimpleRequest;
    }

    @Override
    public void startDocument() throws SAXException {
        this.currentSimpleRequest = new SimpleXmlRequest();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // reset the tag value
        currentValue.setLength(0);
        if (qName.equalsIgnoreCase("SimpleXmlRequest")) {
            System.out.println("STARTING with root element...");

        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("name")) {
//            this.currentSimpleRequest.setName(currentValue.toString());
        }
        if (qName.equalsIgnoreCase("bio")) {
//            this.currentSimpleRequest.setBio(currentValue.toString());
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        currentValue.append(ch, start, length);

    }
}
