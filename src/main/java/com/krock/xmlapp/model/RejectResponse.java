package com.krock.xmlapp.model;


import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class RejectResponse {

    @XmlElement(name = "RespGrp", namespace = "mySchema")
    private ResponseGroup responseGroup;
}
