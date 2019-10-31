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
package com.github.wautsns.utility.tk.mybatis;

import java.util.LinkedList;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

/**
 *
 * @author wautsns
 * @version 0.1.0 Mar 25, 2019
 */
public class Startup {

    public static void main(String[] args) throws Exception {
        LinkedList<String> warnings = new LinkedList<>();
        Configuration configuration = new ConfigurationParser(warnings)
            .parseConfiguration(Startup.class.getResourceAsStream("/generator/config.xml"));
        new MyBatisGenerator(configuration, new DefaultShellCallback(true), warnings).generate(null);
        warnings.forEach(System.err::println);
        System.out.println("Finished");
    }

}
