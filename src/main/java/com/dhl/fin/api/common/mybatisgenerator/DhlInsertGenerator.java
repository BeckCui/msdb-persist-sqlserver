package com.dhl.fin.api.common.mybatisgenerator;

import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.InsertMethodGenerator;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by CuiJianbo on 2020.02.15.
 */
public class DhlInsertGenerator extends InsertMethodGenerator {

    private boolean isSimple;

    public DhlInsertGenerator(boolean isSimple) {
        super(isSimple);
        this.isSimple = true;
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        Method method = new Method(introspectedTable.getInsertStatementId());

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType parameterType;
        if (isSimple) {
            parameterType = new FullyQualifiedJavaType(getBaseRecordType());
        } else {
            parameterType = introspectedTable.getRules()
                    .calculateAllFieldsClass();
        }

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        importedTypes.add(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodComment(method,
                introspectedTable);

        addMapperAnnotations(method);

        if (context.getPlugins().clientInsertMethodGenerated(method, interfaze,
                introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public String getBaseRecordType() {
        String domainName = introspectedTable.getTableConfiguration().getDomainObjectName();
        return "com.dhl.fin.api.domain." + domainName;
    }

}
