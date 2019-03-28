/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package tk.mybatis.mapper.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.internal.util.StringUtility;

import com.github.wautsns.utility.tk.mybatis.Env;
import com.github.wautsns.utility.tk.mybatis.Env.Comments;
import com.github.wautsns.utility.tk.mybatis.Env.Extra;
import com.github.wautsns.utility.tk.mybatis.handler.CommonCommentGenerator;

public class MapperCommentGenerator extends CommonCommentGenerator {

	/**
	 * 给字段添加数据库备注
	 *
	 * @param field
	 * @param introspectedTable
	 * @param introspectedColumn
	 */
	@Override
	public void addFieldComment(Field field, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		// FIXME 添加字段注释,若为主键则不添加(因为一般都为逻辑主键)
		Comments.javaDoc4field(introspectedColumn).forEach(field::addJavaDocLine);
		if (field.isTransient())
			field.addAnnotation("@Transient");
		for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
			if (introspectedColumn == column) {
				field.addAnnotation("@Id");
				break;
			}
		}
		// FIXME 不加@Column,若有下划线之类需要转换, 请通过 mybatis 参数设置
		// FIXME 使用新注解 @KeySql 替代 @GeneratedValue
		if (introspectedColumn.getActualColumnName().equalsIgnoreCase(Env.Key.name)) {
			String[] temp = introspectedTable.getFullyQualifiedTableNameAtRuntime().split("\\.");
			String tableName = temp[temp.length - 1];
			field.addAnnotation(Env.Key.generator
				.replace("{table}", tableName.toLowerCase())
				.replace("{TABLE}", tableName.toUpperCase()));
		}
		if (Extra.useSwagger) {
			String remarks = introspectedColumn.getRemarks();
			if (remarks == null) remarks = "";
			field.addAnnotation("@ApiModelProperty(\"" + remarks.replaceAll("\r", "").replaceAll("\n", "") + "\")");
		}
	}

	@Override
	public void addGetterComment(Method method, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		StringBuilder sb = new StringBuilder();
		method.addJavaDocLine("/**");
		if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
			sb.append(" * 获取");
			sb.append(introspectedColumn.getRemarks());
			method.addJavaDocLine(sb.toString());
			method.addJavaDocLine(" *");
		}
		sb.setLength(0);
		sb.append(" * @return ");
		sb.append(introspectedColumn.getActualColumnName());
		if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
			sb.append(" - ");
			sb.append(introspectedColumn.getRemarks());
		}
		method.addJavaDocLine(sb.toString());
		method.addJavaDocLine(" */");
	}

	@Override
	public void addSetterComment(Method method, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		StringBuilder sb = new StringBuilder();
		method.addJavaDocLine("/**");
		if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
			sb.append(" * 设置");
			sb.append(introspectedColumn.getRemarks());
			method.addJavaDocLine(sb.toString());
			method.addJavaDocLine(" *");
		}
		Parameter parm = method.getParameters().get(0);
		sb.setLength(0);
		sb.append(" * @param ");
		sb.append(parm.getName());
		if (StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
			sb.append(" ");
			sb.append(introspectedColumn.getRemarks());
		}
		method.addJavaDocLine(sb.toString());
		method.addJavaDocLine(" */");
	}

}
