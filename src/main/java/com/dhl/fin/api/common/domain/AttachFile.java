package com.dhl.fin.api.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 附件
 * <p>
 * Created by CuiJianbo on 2020.02.15.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_attachfile")
public class AttachFile extends BasicDomain {

    @Column(columnDefinition = "varchar(250)")
    private String directory;

    @Column(columnDefinition = "varchar(50) ")
    private String md5;

    @Column(columnDefinition = "varchar(200) ")
    private String fileName;

    @Column(columnDefinition = "varchar(10) ")
    private String type;

    @Column(columnDefinition = "int")
    private Long size;

    @Column(columnDefinition = "varchar(10) ")
    private String storeLocation;


}
