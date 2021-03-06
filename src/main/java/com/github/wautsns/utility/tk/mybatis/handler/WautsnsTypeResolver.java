/**
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.wautsns.utility.tk.mybatis.handler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

/**
 *
 * @author wautsns
 * @version 0.1.0 Mar 25, 2019
 */
public class WautsnsTypeResolver extends JavaTypeResolverDefaultImpl {

    private static final FullyQualifiedJavaType CHARACTER = new FullyQualifiedJavaType(Character.class.getName());

    private static final FullyQualifiedJavaType SHORT = new FullyQualifiedJavaType(Short.class.getName());
    private static final FullyQualifiedJavaType INTEGER = new FullyQualifiedJavaType(Integer.class.getName());
    private static final FullyQualifiedJavaType LONG = new FullyQualifiedJavaType(Long.class.getName());
    private static final FullyQualifiedJavaType BIG_INTEGER = new FullyQualifiedJavaType(BigInteger.class.getName());
    private static final FullyQualifiedJavaType DOUBLE = new FullyQualifiedJavaType(Double.class.getName());
    private static final FullyQualifiedJavaType BIG_DECIMAL = new FullyQualifiedJavaType(BigDecimal.class.getName());

    @Override
    protected FullyQualifiedJavaType overrideDefaultType(
        IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        if (column.getJdbcType() == Types.CHAR && column.getLength() == 1)
            return CHARACTER;
        if (column.isUnsigned()) {
            // TODO 是否还有其他的? etc.
            switch (column.getJdbcType()) {
                case Types.TINYINT:
                    return SHORT;
                case Types.SMALLINT:
                    return INTEGER;
                case Types.INTEGER:
                    return LONG;
                case Types.BIGINT:
                    return BIG_INTEGER;
                case Types.FLOAT:
                    return DOUBLE;
                case Types.DOUBLE:
                    return BIG_DECIMAL;
            }
        }
        return super.overrideDefaultType(column, defaultType);
    }

}
