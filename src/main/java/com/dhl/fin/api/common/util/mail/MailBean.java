package com.dhl.fin.api.common.util.mail;

import lombok.Data;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 7/29/2020
 */
@Data
public class MailBean {

    private String from;
    private List<String> to = new LinkedList<>();
    private List<String> cc = new LinkedList<>();
    private String title;
    private String content;
    private String template;
    private Map templateParams = new HashMap();
    private List<File> attachFiles = new LinkedList<>();
    private Map<String, String> imgs = new HashMap();

}
