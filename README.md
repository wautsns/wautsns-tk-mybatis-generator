# wautsns-tk-mybatis-generator

config.xml 的配置方式实在是令我用起来不顺手,而 mybatis-generator 中的一些处理,我也不是太喜欢,故对 mybatis-generator-core 与 mapper-generator(tk) 的部分源码进行修改.由于是初版本,可能存在bug,且仍需做许多优化.

以下给出 env.properties 的配置说明, 在无特殊需要的情况下,不要修改 /generator/config.xml

- metadata.author
  - 作者名, 默认为 <a href="http://www.github.com/wautsns">wautsns</a>
- metadata.version
  - 版本号, 默认为 0.1.0
- metadata.package
  - 生成的 java 文件所在的基本包, 默认为空
--- 
- jdbc.driver
  - mysql 5.7-: com.mysql.jdbc.Driver
  - mysql 8.0+: com.mysql.cj.jdbc.Driver
  - oracle: oracle.jdbc.OracleDriver
- jdbc.url
  - mysql 5.7-: jdbc:mysql://ip:port(3306)/database
  - mysql 8.0+: jdbc:mysql://ip:port(3306)/database?userSSL=false&serverTimezone=GMT
  - oracle: jdbc:oracle:thin:@ip:port(1521):instance
- jdbc.username
- jdbc.password
---
- table.name
  - 表名, 默认为 %, 支持 SQL 通配符,可为 [catalog].[schema].table
- table.key.name
  - 表中的主键名, 默认为 id
- table.key.generator
  - 表中主键的生成策略, 默认根据连接的数据库自动生成(仅包含了些常用的数据库,所以对于某些数据库需手动配置),
  - 用于生成 tk-mybatis 中的 @KeySql
  - 格式
    - JDBC(仅支持自增的数据库可用)
    - <BEFORE/AFTER>,primaryKeyGenerateSQL
      - 可用 {table} 表示小写的表名, 用 {TABLE} 表示大写的表名
      - oracle 数据库默认为: BEFORE,select SEQ_{TABLE}.nextval from dual,若不是,请额外配置
---
- model.subpackage
  - PO 类相对基本包所在的子包, 默认为 domain.po
- model.template
  - PO 类的名称, 默认为 {model}PO
---
- mapper.subpackage
  - Mapper 接口相对基本包所在的子包, 默认为 mapper
 - mapper.template
   - Mapper 接口的名称, 默认为 {model}Mapper
- mapper.extends
  - Mapper 所继承的接口,默认为 Mapper + MySqlMapper/OracleMapper/SqlserverMapper/无,会自动为接口增加一个对应 PO 类的泛型参数
---
- extra.lombok
  - 需要开启的 lombok 注解, 默认为 @Getter,@Setter,@Accessors(chain = true)
- extra.swagger
  - 是否需要开启 swagger, 默认为 false