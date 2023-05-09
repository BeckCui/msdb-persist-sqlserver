package com.dhl.fin.api.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * 用户自定义列表title哪些显示
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_custom_field")
public class CustomField extends BasicDomain {

    /**
     * 用户uuid
     */
    private String uuid;

    /**
     * 常用的字段
     */
    private String field;

    /**
     * 应用在界面上的哪个table列表
     */
    private String tableCode;

}