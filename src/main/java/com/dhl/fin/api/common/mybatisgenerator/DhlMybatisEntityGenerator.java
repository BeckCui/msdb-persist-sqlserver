package com.dhl.fin.api.common.mybatisgenerator;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CuiJianbo on 2020.02.15.
 */
public class DhlMybatisEntityGenerator extends BaseRecordGenerator {
    public DhlMybatisEntityGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        return new ArrayList<>();
    }

}

