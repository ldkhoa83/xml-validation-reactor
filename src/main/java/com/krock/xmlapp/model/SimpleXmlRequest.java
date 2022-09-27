package com.krock.xmlapp.model;

import lombok.*;

import javax.xml.bind.annotation.*;


@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SimpleXmlRequest")
public class SimpleXmlRequest {

    @XmlElement(namespace = "mySchema")
    private String name;
    @XmlElement(namespace = "mySchema")
    private String bio;

    @XmlElement(namespace = "mySchema")
    private String gender;
}
