/**
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.wautsns.utility.tk.mybatis;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModelException;
import tk.mybatis.mapper.code.IdentityDialect;

/**
 *
 * @author wautsns
 * @version 0.1.0 Mar 27, 2019
 */
public class Env {

	public static class Comments {

		public static LinkedList<String> licence = commentFile("licence", null);
		public static LinkedList<String> javaDoc4class = commentFile("javaDoc4class", null);

		public static LinkedList<String> javaDoc4model(IntrospectedTable table) {
			return commentFile("javaDoc4model", Collections.singletonMap("table", table));
		}

		public static LinkedList<String> javaDoc4field(IntrospectedColumn column) {
			return commentFile("javaDoc4field", Collections.singletonMap("column", column));
		}
	}

	public static class Metadata {

		public static String author = val("metadata.author",
			"<a href=\"http://www.github.com/wautsns\">wautsns</a>");
		public static String version = val("metadata.version", "0.1.0");
		public static String pakkage = val("metadata.package", "");
		static {
			try {
				ftl.setSharedVariable("author", Metadata.author);
				ftl.setSharedVariable("version", Metadata.version);
			} catch (TemplateModelException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class DB {

		public static String type;
		public static String catalog;
		public static String schema;
		public static String table;
		public static String arg4getTableRemarks;
		/** 该 wrapper 会对自动对分隔符中的双引号转义 */
		public static UnaryOperator<String> wrapper;
		static {
			String url = nonblank("jdbc.url").toUpperCase().substring(5);
			type = url.substring(0, url.indexOf(':'));
			LinkedList<String> cst = new LinkedList<>(Arrays.asList(
				val("table.name", "%").split("\\.", 3)));
			table = cst.removeLast();
			if (!cst.isEmpty()) schema = cst.removeLast();
			if (!cst.isEmpty()) catalog = cst.removeLast();

			switch (DB.type) {
			case "MYSQL":
				wrapper = text -> '`' + text + '`';
				arg4getTableRemarks = "useInformationSchema";
				break;
			case "SQLSERVER":
				wrapper = text -> '[' + text + ']';
				break;
			case "ORACLE":
				wrapper = text -> "\\\"" + text + "\\\"";
				arg4getTableRemarks = "remarksReporting";
				break;
			default:
				wrapper = text -> text;
			}
		}
	}

	public static class Key {

		public static String name = val("table.key", "id");
		public static String generator;
		public static List<FullyQualifiedJavaType> imports;
		static {
			imports = new LinkedList<>();
			imports.add(new FullyQualifiedJavaType("tk.mybatis.mapper.annotation.KeySql"));
			String generator = val("table.key.generator", "");
			if ("JDBC".equalsIgnoreCase(generator))
				initJDBCKeySQL();
			else if (!generator.isEmpty())
				initSpecifiedKeySQL(generator);
			else if ("ORACLE".equals(DB.type))
				initSpecifiedKeySQL("BEFORE,select SEQ_{TABLE}.nextval from dual");
			else
				initIdentityKeySQL();
		}

		private static void initJDBCKeySQL() {
			generator = "@KeySql(useGeneratedKeys = true)";
		}

		private static void initIdentityKeySQL() {
			boolean supported = false;
			for (IdentityDialect dialect : IdentityDialect.values())
				if (dialect.name().equals(DB.type)) {
					supported = true;
					break;
				}
			if (!supported) throw new RuntimeException(String.format(
				"无法自动识别数据库[%s]的主键生成策略,请配置 table.key.generator", DB.type));
			generator = String.format("@KeySql(dialect = IdentityDialect.%s)", DB.type);
			imports.add(new FullyQualifiedJavaType("tk.mybatis.mapper.code.IdentityDialect"));
		}

		private static void initSpecifiedKeySQL(String generator) {
			String[] orderAndSql = generator.split(":", 2);
			if (orderAndSql.length == 1)
				throw new RuntimeException(
					"table.key.generator 格式必须为 <BEFORE/AFTER>,keyGenerateSql");
			String order = orderAndSql[0];
			if (!"BEFORE".equals(order) && !"AFTER".equals(order))
				throw new RuntimeException("keysql 的 order 只能被指定为 BEFORE 或 AFTER");
			generator = String.format("@KeySql(order = ORDER.%s, sql=\"%s\")", order, orderAndSql[1]);
			imports.add(new FullyQualifiedJavaType("tk.mybatis.mapper.code.ORDER"));
		}
	}

	public static class Model {

		public static String pakkage = Metadata.pakkage + '.' + val("model.subpackage", "domain.po");
		public static UnaryOperator<String> template;
		static {
			String _template = val("model.template", "{model}PO");
			template = model -> _template.replace("{model}", model);
		}
	}

	public static class Mapper {

		public static String pakkage = Metadata.pakkage + '.' + val("mapper.subpackage", "mapper");
		public static UnaryOperator<String> template;
		public static List<String> parents;
		static {
			String _template = val("mapper.template", "{model}Mapper");
			template = model -> _template.replace("{model}", model);

			parents = new LinkedList<>();
			String extendz = val("mapper.extends", "");
			if (!extendz.isEmpty())
				parents = new LinkedList<>(Arrays.asList(extendz.split("\\s*,\\s*")));
			else {
				parents.add("tk.mybatis.mapper.common.Mapper");
				if ("MYSQL".equals(DB.type))
					parents.add("tk.mybatis.mapper.common.MySqlMapper");
				else if ("ORACLE".equals(DB.type))
					parents.add("tk.mybatis.mapper.additional.dialect.oracle.OracleMapper");
				else if ("SQLSERVER".equals(DB.type))
					parents.add("tk.mybatis.mapper.common.SqlServerMapper");
			}
		}
	}

	public static class Extra {

		public static class Lombok {

			/** key: package, value: annotation */
			public static Map<String, String> enabled;
			public static boolean needGetter;
			public static boolean needSetter;
			static {
				enabled = new HashMap<>(4);
				String val = val("extra.lombok", "@Getter,@Setter,@Accessors(chain = true)");
				if (val.contains("@Date"))
					enabled.put("@Data", "lombok.Data");
				else {
					Arrays.asList("Getter", "Setter", "ToString", "EqualsAndHashCode").stream()
						.filter(val::contains)
						.forEach(v -> enabled.put("lombok." + v, "@" + v));
					needGetter = !enabled.containsKey("lombok.Getter");
					needSetter = !enabled.containsKey("lombok.Setter");
					if (!needGetter || !needSetter) {
						Pattern pattern = Pattern.compile("(@Accessors[^)]*\\))");
						Matcher matcher = pattern.matcher(val);
						if (matcher.find())
							enabled.put("lombok.experimental.Accessors", matcher.group(1));
					}
				}
			}
		}

		public static boolean useSwagger = Boolean.valueOf(val("extra.swagger", "false"));

	}

	private static Properties props;
	static {
		props = new Properties();
		try {
			props.load(Env.class.getResourceAsStream("/env.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String val(String name, String valIfNull) {
		return props.getProperty(name, valIfNull);
	}

	private static String nonblank(String name) {
		return Objects.requireNonNull(
			val(name, null),
			String.format("属性[%s]不能为空", name));
	}

	private static Configuration ftl = new Configuration(Configuration.getVersion());
	static {
		ftl.setClassForTemplateLoading(Env.class, "/comments");
		try {
			String date = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
			String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			ftl.setSharedVariable("year", year);
			ftl.setSharedVariable("date", date);
		} catch (TemplateModelException e) {
			throw new RuntimeException(e);
		}
	}

	private static LinkedList<String> commentFile(String name, Map<String, Object> data) {
		if (data == null) data = Collections.emptyMap();
		try {
			Template template = ftl.getTemplate(name + ".ftl");
			StringWriter writer = new StringWriter();
			template.process(data, writer);
			return new LinkedList<>(Arrays.asList(writer.toString().split("\r?\n")));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
