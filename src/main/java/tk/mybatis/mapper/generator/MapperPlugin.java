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
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import com.github.wautsns.utility.tk.mybatis.Env.Comments;
import com.github.wautsns.utility.tk.mybatis.Env.DB;
import com.github.wautsns.utility.tk.mybatis.Env.Extra;
import com.github.wautsns.utility.tk.mybatis.Env.Key;
import com.github.wautsns.utility.tk.mybatis.Env.Mapper;
import com.github.wautsns.utility.tk.mybatis.model.TableEnum;

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
        topLevelClass.addInnerEnum(new TableEnum(introspectedTable));
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
        // FIXME 移除字段名常量, 因为 Weekend 有 lambda 方式
        // FIXME 移除默认值的 instance, 感觉无用
        // FIXME 添加 hashcode/equals
        // List<IntrospectedColumn> keyCols = introspectedTable.getPrimaryKeyColumns();
        // if (keyCols.size() == 0)
        // keyCols = introspectedTable.getAllColumns();
        // List<String> pks = keyCols.stream()
        // .map(IntrospectedColumn::getJavaProperty)
        // .collect(Collectors.toList());
        // TODO 需要优化
        // Method hashcode = new Method("hashCode");
        // hashcode.setReturnType(FullyQualifiedJavaType.getIntInstance());
        // hashcode.addAnnotation("@Override");
        // hashcode.setVisibility(JavaVisibility.PUBLIC);
        // Method equals = new Method("equals");
        // equals.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        // equals.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "obj"));
        // equals.addAnnotation("@Override");
        // equals.addBodyLine("if (this == obj) return true;");
        // equals.addBodyLine("if (obj == null) return false;");
        // equals.addBodyLine("if (getClass() != obj.getClass())");
        // equals.addBodyLine("\treturn false;");
        // String modelName = topLevelClass.getType().getShortName();
        // equals.addBodyLine(String.format("%s other = (%s) obj;", modelName, modelName));
        // equals.setVisibility(JavaVisibility.PUBLIC);
        // if (pks.size() == 1) {
        // hashcode.addBodyLine(String.format("return Objects.hashCode(%s);", pks.get(0)));
        // equals.addBodyLine(String.format("return Objects.equals(%s, other.%s);", pks.get(0), pks.get(0)));
        // } else {
        // StringBuilder bder = new StringBuilder();
        // pks.forEach(pk -> bder.append(pk).append(", "));
        // bder.delete(bder.length() - 2, bder.length());
        // hashcode.addBodyLine(String.format("return Objects.hash(%s);", bder));
        // pks.forEach(pk -> {
        // equals.addBodyLine(String.format("if (!Objects.equals(%s,other.%s))", pk, pk));
        // equals.addBodyLine("\treturn false;");
        // });
        // equals.addBodyLine("return true;");
        // }
        // topLevelClass.addMethod(hashcode);
        // topLevelClass.addMethod(equals);
        // topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Objects"));
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
