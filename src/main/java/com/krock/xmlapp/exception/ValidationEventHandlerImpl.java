package com.krock.xmlapp.exception;

import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

@Component
public class ValidationEventHandlerImpl implements ValidationEventHandler {

    @Override
    public boolean handleEvent(ValidationEvent event) {

        String message = event.getMessage();
        boolean ignoreValidationEvent = true;
        if(event.getLinkedException() != null && event.getLinkedException() instanceof SAXParseException){
            ignoreValidationEvent = false;
        }
        if(!ignoreValidationEvent){
            String nodeName = "";
            if(event.getLocator() != null && event.getLocator().getNode() != null)
                nodeName = event.getLocator().getNode().getNodeName();

            //This is the important line
            throw new RuntimeException("Error parsing '" + nodeName + "': " + event.getMessage());

        }
        return ignoreValidationEvent;
    }
}
