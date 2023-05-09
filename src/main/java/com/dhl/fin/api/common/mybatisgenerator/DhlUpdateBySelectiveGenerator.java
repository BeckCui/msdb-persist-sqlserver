package com.dhl.fin.api.common.mybatisgenerator;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.UpdateByPrimaryKeySelectiveMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by CuiJianbo on 2020.02.15.
 */
public class DhlUpdateBySelectiveGenerator extends UpdateByPrimaryKeySelectiveMethodGenerator {


    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        FullyQualifiedJavaType parameterType;

        importedTypes.add(new FullyQualifiedJavaType("com.dhl.fin.api.common.dto.QueryDto"));
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
        importedTypes.add(new FullyQualifiedJavaType(getDomainType()));


        String domainPakName = getDomainType();
        String domainName = domainPakName.substring(domainPakName.lastIndexOf(".") + 1, domainPakName.length());

        Method method = new Method(introspectedTable
                .getUpdateByPrimaryKeySelectiveStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(new FullyQualifiedJavaType("@Param(\"domain\")" + domainName), "record")); //$NON-NLS-1$

        Parameter parameter1 = new Parameter(new FullyQualifiedJavaType("@Param(\"query\")QueryDto"), "queryDto");
        method.addParameter(parameter1);

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        addMapperAnnotations(method);

        if (context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method,
                        interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public String getDomainType() {
        String domainName = introspectedTable.getTableConfiguration().getDomainObjectName();
        return "com.dhl.fin.api.domain." + domainName;
    }

}
