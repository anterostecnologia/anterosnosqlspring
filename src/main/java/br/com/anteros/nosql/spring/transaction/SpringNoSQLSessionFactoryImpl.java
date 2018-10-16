package br.com.anteros.nosql.spring.transaction;

import br.com.anteros.nosql.persistence.client.NoSQLDataSource;
import br.com.anteros.nosql.persistence.metadata.NoSQLDescriptionEntityManager;
import br.com.anteros.nosql.persistence.session.configuration.NoSQLSessionFactoryConfiguration;
import br.com.anteros.nosql.persistence.session.context.CurrentNoSQLSessionContext;
import br.com.anteros.nosql.persistence.session.impl.SimpleNoSQLSessionFactory;

public class SpringNoSQLSessionFactoryImpl extends SimpleNoSQLSessionFactory {

	public SpringNoSQLSessionFactoryImpl(NoSQLDescriptionEntityManager descriptionEntityManager, NoSQLDataSource dataSource,
			NoSQLSessionFactoryConfiguration configuration) throws Exception {
		super(descriptionEntityManager, dataSource, configuration);
	}
	
	@Override
	protected CurrentNoSQLSessionContext buildCurrentSessionContext() throws Exception {
		return new SpringNoSQLSessionContext(this);
	}

	
}
