package com.krock.xmlapp.model.v2;

import lombok.*;

import javax.xml.bind.annotation.*;


@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SimpleXmlRequest")
@XmlType(name = "SimpleXmlRequest", namespace = "mySchema-v2")
public class SimpleXmlRequest {

    @XmlElement(namespace = "mySchema-v2")
    private String name;
    @XmlElement(namespace = "mySchema-v2")
    private Integer age;

    @XmlElement(namespace = "mySchema-v2")
    private String gender;
}
