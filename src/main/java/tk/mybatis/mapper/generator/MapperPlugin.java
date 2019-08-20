/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy
 * of this software and associated documentation files (the "Software"),
 * to deal
 * in the Software without restriction, including without limitation the
 * rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT
 * SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN
 * THE SOFTWARE.
 */

package tk.mybatis.mapper.generator;

import java.util.Arrays;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import com.github.wautsns.utility.tk.mybatis.Env.Comments;
import com.github.wautsns.utility.tk.mybatis.Env.DB;
import com.github.wautsns.utility.tk.mybatis.Env.Extra;
import com.github.wautsns.utility.tk.mybatis.Env.Key;
import com.github.wautsns.utility.tk.mybatis.Env.Mapper;

/**
 * 通用Mapper生成器插件
 *
 * @author liuzh
 * @author wautsns(modify)
 */
public class MapperPlugin extends FalseMethodPlugin {

    @Override
    public boolean clientGenerated(
        Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // FIXME 给 Mapper 加 javadoc 注释
        Comments.javaDoc4class.forEach(interfaze::addJavaDocLine);
        // end
        // 获取实体类
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        // import接口
        for (String mapper : Mapper.parents) {
            interfaze.addImportedType(new FullyQualifiedJavaType(mapper));
            interfaze.addSuperInterface(new FullyQualifiedJavaType(mapper + "<" + entityType.getShortName() + ">"));
        }
        // import实体类
        interfaze.addImportedType(entityType);
        return true;
    }

    private void processEntityClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.getImportedTypes().add(new FullyQualifiedJavaType("java.io.Serializable"));
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("Serializable"));
        Field serialVersionUID = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
        serialVersionUID.setStatic(true);
        serialVersionUID.setFinal(true);
        serialVersionUID.setInitializationString("1L");
        topLevelClass.getFields().add(0, serialVersionUID);
        // XXX 引入需要的JPA注解
        Arrays.asList(
            "javax.persistence.Table",
            "javax.persistence.Id",
            "javax.persistence.Column"
        ).forEach(topLevelClass::addImportedType);
        // FIXME 修改 lombok 注解加入方式
        Key.imports.forEach(topLevelClass::addImportedType);
        Extra.Lombok.enabled.forEach((pakkage, anno) -> {
            topLevelClass.addImportedType(pakkage);
            topLevelClass.addAnnotation(anno);
        });

        // region swagger扩展
        if (Extra.useSwagger) {
            // 导包
            topLevelClass.addImportedType("io.swagger.annotations.ApiModel");
            topLevelClass.addImportedType("io.swagger.annotations.ApiModelProperty");
            // 增加注解(去除注释中的转换符)
            String remarks = introspectedTable.getRemarks();
            if (remarks == null) {
                remarks = "";
            }
            topLevelClass.addAnnotation("@ApiModel(\"" + remarks.replaceAll("\r", "").replaceAll("\n", "") + "\")");
        }
        // endregion swagger扩展
        // FIXME 从 [catalog].[schema].table 中获取表名
        String[] temp = introspectedTable.getFullyQualifiedTableNameAtRuntime().split("\\.");
        String shortTableName = temp[temp.length - 1];
        // end
        topLevelClass.addAnnotation("@Table(name = \"" + DB.toFullTableName(shortTableName) + "\")");
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
        TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
        IntrospectedTable introspectedTable,
        ModelClassType modelClassType) {
        return !Extra.Lombok.needGetter;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method,
        TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
        IntrospectedTable introspectedTable,
        ModelClassType modelClassType) {
        return !Extra.Lombok.needSetter;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processEntityClass(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        processEntityClass(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
        IntrospectedTable introspectedTable) {
        processEntityClass(topLevelClass, introspectedTable);
        return false;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    protected String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    protected String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    protected Boolean getPropertyAsBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

}
