package com.dhl.fin.api.common.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@MappedSuperclass
public class BasicDomain implements Serializable {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(columnDefinition = "bit default 0 ")
    protected Boolean remove = false;

    @Column(columnDefinition = "datetime")
    protected Timestamp createTime;

    @Column(columnDefinition = "datetime ")
    protected Timestamp updateTime;

    @Column(columnDefinition = "varchar(50) ")
    protected String createUser;

    @Column(columnDefinition = "varchar(50) ")
    protected String updateUser;

    @Transient
    private String createTimeStr;

    @Transient
    private String updateTimeStr;
}
