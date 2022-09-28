package com.krock.xmlapp.model;

import lombok.*;

import javax.xml.bind.annotation.*;


@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SimpleXmlRequest", namespace = "mySchema")
public class SimpleXmlRequest {

    @XmlElement(namespace = "mySchema")
    private Person person;

    @XmlElement(namespace = "mySchema", name = "RejectResponse")
    private RejectResponse rejectResponse;
}
