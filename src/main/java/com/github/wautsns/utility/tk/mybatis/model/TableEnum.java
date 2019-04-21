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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.wautsns.utility.tk.mybatis.model;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;

import com.github.wautsns.utility.tk.mybatis.Env;

/**
 *
 * @author wautsns
 * @version 0.1.0 Apr 18, 2019
 */
public class TableEnum extends InnerEnum {

	private static final Method wrap;
	static {
		wrap = new Method("wrap");
		wrap.setVisibility(JavaVisibility.PUBLIC);
		wrap.setReturnType(new FullyQualifiedJavaType("String"));
		String value = Env.DB.wrapper.apply("\" + name() + \"");
		if (value.equals("\" + name() + \""))
			value = "name()";
		else
			value = '"' + value + '"';
		wrap.addBodyLine("return " + value + ";");
	}

	public TableEnum(IntrospectedTable table) {
		super(new FullyQualifiedJavaType("Tb_" + table.getFullyQualifiedTableNameAtRuntime()));
		setVisibility(JavaVisibility.PUBLIC);
		for (IntrospectedColumn column : table.getAllColumns())
			addEnumConstant(column.getActualColumnName());
		addMethod(wrap);
	}

}
