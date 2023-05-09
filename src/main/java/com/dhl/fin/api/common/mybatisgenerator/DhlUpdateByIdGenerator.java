package com.dhl.fin.api.common.mybatisgenerator;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.UpdateByPrimaryKeyWithoutBLOBsMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by CuiJianbo on 2020.02.15.
 */
public class DhlUpdateByIdGenerator extends UpdateByPrimaryKeyWithoutBLOBsMethodGenerator {
    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(getDomainType());
        importedTypes.add(parameterType);

        Method method = new Method(introspectedTable.getUpdateByPrimaryKeyStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        addMapperAnnotations(method);

        if (context.getPlugins()
                .clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method,
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
