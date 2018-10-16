/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
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
 *******************************************************************************/
package br.com.anteros.nosql.spring.transaction;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.nosql.persistence.client.NoSQLDataSource;
import br.com.anteros.nosql.persistence.dialect.NoSQLDialect;
import br.com.anteros.nosql.persistence.metadata.configuration.AnterosNoSQLProperties;
import br.com.anteros.nosql.persistence.metadata.configuration.NoSQLPersistenceModelConfiguration;
import br.com.anteros.nosql.persistence.session.NoSQLSessionException;
import br.com.anteros.nosql.persistence.session.NoSQLSessionFactory;
import br.com.anteros.nosql.persistence.session.configuration.impl.AnterosNoSQLPersistenceConfiguration;


/**
 * Configuração Anteros usando uma fábrica criada para uso com sistema de transações do Spring.
 * 
 * @author Edson Martins edsonmartins2005@gmail.com
 *
 */
public class SpringNoSQLConfiguration extends AnterosNoSQLPersistenceConfiguration {

	public SpringNoSQLConfiguration() {
		super();
	}
	
	public SpringNoSQLConfiguration(NoSQLDataSource dataSource) {
		super(dataSource);
	}

	public SpringNoSQLConfiguration(NoSQLPersistenceModelConfiguration modelConfiguration) {
		super(modelConfiguration);
	}

	public SpringNoSQLConfiguration(NoSQLDataSource dataSource, NoSQLPersistenceModelConfiguration modelConfiguration) {
		super(dataSource, modelConfiguration);
	}

	@Override
	public NoSQLSessionFactory buildSessionFactory() throws Exception {
		prepareClassesToLoad();
		
		if (getSessionFactoryConfiguration().getProperty(AnterosNoSQLProperties.DIALECT) == null) {
			throw new NoSQLSessionException("Dialeto não definido. Não foi possível instanciar NoSQLSessionFactory.");
		}

		String dialectProperty = getSessionFactoryConfiguration().getProperty(AnterosNoSQLProperties.DIALECT);
		Class<?> dialectClass = Class.forName(dialectProperty);

		if (!ReflectionUtils.isExtendsClass(NoSQLDialect.class, dialectClass))
			throw new NoSQLSessionException("A classe " + dialectClass.getName() + " não implementa a classe "
					+ NoSQLDialect.class.getName() + ".");

		this.dialect = (NoSQLDialect) dialectClass.newInstance();
		
		buildDataSource();		
		SpringNoSQLSessionFactoryImpl sessionFactory = new SpringNoSQLSessionFactoryImpl(descriptionEntityManager, dataSource,
				this.getSessionFactoryConfiguration());
		loadEntities(sessionFactory.getDialect());		
		return sessionFactory;
	}

}
