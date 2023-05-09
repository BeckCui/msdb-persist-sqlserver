package com.dhl.fin.api.common.mybatisgenerator;

import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.javamapper.AnnotatedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.MixedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator;
import org.mybatis.generator.codegen.mybatis3.model.PrimaryKeyGenerator;
import org.mybatis.generator.codegen.mybatis3.model.RecordWithBLOBsGenerator;
import org.mybatis.generator.internal.ObjectFactory;

import java.util.List;

/**
 * Created by CuiJianbo on 2020.02.15.
 */
public class DhlMyBatisImpl extends IntrospectedTableMyBatis3Impl {

    /**
     * 重写 Dao 包路径
     *
     * @param mybatis3JavaMapperType
     */
    @Override
    public void setMyBatis3JavaMapperType(String mybatis3JavaMapperType) {
        super.setMyBatis3JavaMapperType(mybatis3JavaMapperType.replaceAll("Mapper", "Dao"));
    }

    /**
     * 重写 Dao 类
     *
     * @return
     */
    @Override
    protected AbstractJavaClientGenerator createJavaClientGenerator() {
        if (context.getJavaClientGeneratorConfiguration() == null) {
            return null;
        }

        String type = context.getJavaClientGeneratorConfiguration()
                .getConfigurationType();

        AbstractJavaClientGenerator javaGenerator;
        if ("XMLMAPPER".equalsIgnoreCase(type)) {
            javaGenerator = new DhlMybatisDaoGenerator(getClientProject());
        } else if ("MIXEDMAPPER".equalsIgnoreCase(type)) {
            javaGenerator = new MixedClientGenerator(getClientProject());
        } else if ("ANNOTATEDMAPPER".equalsIgnoreCase(type)) {
            javaGenerator = new AnnotatedClientGenerator(getClientProject());
        } else if ("MAPPER".equalsIgnoreCase(type)) {
            javaGenerator = new DhlMybatisDaoGenerator(getClientProject());
        } else {
            javaGenerator = (AbstractJavaClientGenerator) ObjectFactory
                    .createInternalObject(type);
        }

        return javaGenerator;
    }


    /**
     * 重写 Entity 类
     *
     * @param warnings
     * @param progressCallback
     */
    @Override
    protected void calculateJavaModelGenerators(List<String> warnings, ProgressCallback progressCallback) {
        if (getRules().generateExampleClass()) {
            AbstractJavaGenerator javaGenerator = new ExampleGenerator(getExampleProject());
            initializeAbstractGenerator(javaGenerator, warnings,
                    progressCallback);
            javaGenerators.add(javaGenerator);
        }

        if (getRules().generatePrimaryKeyClass()) {
            AbstractJavaGenerator javaGenerator = new PrimaryKeyGenerator(getModelProject());
            initializeAbstractGenerator(javaGenerator, warnings,
                    progressCallback);
            javaGenerators.add(javaGenerator);
        }

        if (getRules().generateBaseRecordClass()) {
            AbstractJavaGenerator javaGenerator = new DhlMybatisEntityGenerator(getModelProject());
            initializeAbstractGenerator(javaGenerator, warnings,
                    progressCallback);
            javaGenerators.add(javaGenerator);
        }

        if (getRules().generateRecordWithBLOBsClass()) {
            AbstractJavaGenerator javaGenerator = new RecordWithBLOBsGenerator(getModelProject());
            initializeAbstractGenerator(javaGenerator, warnings,
                    progressCallback);
            javaGenerators.add(javaGenerator);
        }
    }


}
