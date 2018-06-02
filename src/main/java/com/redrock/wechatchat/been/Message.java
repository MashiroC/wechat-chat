package com.redrock.wechatchat.been;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data

public class Message {
    private String fromUser;
    private String toUser;
    private String text;
    private String type;
}
