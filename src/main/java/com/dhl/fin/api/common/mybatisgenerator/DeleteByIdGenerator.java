package com.dhl.fin.api.common.mybatisgenerator;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.DeleteByPrimaryKeyMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by CuiJianbo on 2020.02.22.
 */
public class DeleteByIdGenerator extends DeleteByPrimaryKeyMethodGenerator {

    public DeleteByIdGenerator(boolean isSimple) {
        super(isSimple);
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(getDomainType());
        Method method = new Method(introspectedTable.getDeleteByPrimaryKeyStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$


        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        addMapperAnnotations(method);

        if (context.getPlugins().clientDeleteByPrimaryKeyMethodGenerated(
                method, interfaze, introspectedTable)) {
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
