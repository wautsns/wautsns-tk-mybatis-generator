/**
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.wautsns.utility.tk.mybatis.handler;

import java.util.Properties;
import java.util.Set;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.github.wautsns.utility.tk.mybatis.Env.Comments;

/**
 *
 * @author wautsns
 * @version 0.1.0 Mar 26, 2019
 */
public abstract class CommonCommentGenerator implements CommentGenerator {

    @Override
    public final void addJavaFileComment(CompilationUnit compilationUnit) {
        Comments.licence.forEach(compilationUnit::addFileCommentLine);
    }

    @Override
    public final void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        Comments.javaDoc4class.forEach(innerClass::addJavaDocLine);
    }

    @Override
    public final void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
        Comments.javaDoc4class.forEach(innerEnum::addJavaDocLine);
    }

    @Override
    public final void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Comments.javaDoc4model(introspectedTable).forEach(topLevelClass::addJavaDocLine);
    }

    @Override
    public final void addClassComment(
        InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        addClassComment(innerClass, introspectedTable);
    }

    // ==================== 以下为内容清空的方法 =====================

    // 不需要额外添加属性
    @Override
    public final void addConfigurationProperties(Properties properties) {}

    // xml 注释
    @Override
    public final void addComment(XmlElement xmlElement) {}

    // xml 注释
    @Override
    public final void addRootComment(XmlElement rootElement) {}

    // 非 model 字段注释
    @Override
    public final void addFieldComment(Field field, IntrospectedTable introspectedTable) {}

    // 非 model 方法注释
    @Override
    public final void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {}

    // 非 model 方法注解
    @Override
    public final void addGeneralMethodAnnotation(
        Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {}

    // 非 model 字段注解
    @Override
    public final void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
        Set<FullyQualifiedJavaType> set) {}

    // 非 model 字段注解
    @Override
    public final void addFieldAnnotation(Field field, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> set) {}

    // 类注解
    @Override
    public final void addClassAnnotation(
        InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> set) {}

    // 普通方法注解
    @Override
    public final void addGeneralMethodAnnotation(
        Method method, IntrospectedTable introspectedTable,
        IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> set) {}

}
