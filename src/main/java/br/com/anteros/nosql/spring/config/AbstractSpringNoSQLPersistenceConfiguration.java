package br.com.anteros.nosql.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import br.com.anteros.core.utils.ReflectionUtils;
import br.com.anteros.nosql.persistence.client.NoSQLDataSource;
import br.com.anteros.nosql.persistence.dialect.NoSQLDialect;
import br.com.anteros.nosql.persistence.metadata.configuration.AnterosNoSQLProperties;
import br.com.anteros.nosql.persistence.session.NoSQLSessionException;
import br.com.anteros.nosql.persistence.session.NoSQLSessionFactory;
import br.com.anteros.nosql.persistence.session.ShowCommandsType;
import br.com.anteros.nosql.spring.transaction.SessionSynchronization;
import br.com.anteros.nosql.spring.transaction.SpringNoSQLConfiguration;

@EnableTransactionManagement
@Configuration
public abstract class AbstractSpringNoSQLPersistenceConfiguration {

	public abstract NoSQLDataSourceConfiguration getDataSourceConfiguration();

	public abstract NoSQLSessionFactoryConfiguration getSessionFactoryConfiguration();

	protected NoSQLSessionFactory sessionFactory;

	protected NoSQLDataSource dataSource;

	@Bean
	public NoSQLSessionFactory sessionFactoryNoSQL() throws Exception {
		if (sessionFactory != null) {
			return sessionFactory;
		}

		NoSQLSessionFactoryConfiguration sessionFactoryConfiguration = getSessionFactoryConfiguration();
		if (sessionFactoryConfiguration != null) {
			SpringNoSQLConfiguration configuration = new SpringNoSQLConfiguration(dataSourceNoSQL());
			for (Class<?> sourceClass : sessionFactoryConfiguration.getEntitySourceClasses()) {
				configuration.addAnnotatedClass(sourceClass);
			}
			configuration.withoutTransactionControl(sessionFactoryConfiguration.getWithoutTransactionControl());
			configuration.getSessionFactoryConfiguration()
					.setPackageToScanEntity(sessionFactoryConfiguration.getPackageScanEntity());
			configuration.getSessionFactoryConfiguration()
					.setIncludeSecurityModel(sessionFactoryConfiguration.isIncludeSecurityModel());
			configuration.addProperty(AnterosNoSQLProperties.DIALECT, sessionFactoryConfiguration.getDialect());
			configuration.addProperty(AnterosNoSQLProperties.SHOW_COMMANDS,
					ShowCommandsType.parse(sessionFactoryConfiguration.getShowCommands()));
			configuration.addProperty(AnterosNoSQLProperties.FORMAT_COMMANDS,
					String.valueOf(sessionFactoryConfiguration.isFormatCommands()));
			configuration.addProperty(AnterosNoSQLProperties.DATABASE_NAME,
					sessionFactoryConfiguration.getDatabaseName());
			configuration.addProperty(AnterosNoSQLProperties.USE_BEAN_VALIDATION,
					sessionFactoryConfiguration.getUseBeanValidation() + "");
			sessionFactory = configuration.buildSessionFactory();
		}
		return sessionFactory;
	}

	@Bean
	public NoSQLDataSource dataSourceNoSQL() throws Exception {
		if (dataSource != null) {
			return dataSource;
		}
		NoSQLSessionFactoryConfiguration sqlSessionFactoryConfiguration = getSessionFactoryConfiguration();
		NoSQLDataSourceConfiguration dataSourceConfiguration = getDataSourceConfiguration();

		if (dataSourceConfiguration != null && sqlSessionFactoryConfiguration != null) {
			String dialectProperty = sqlSessionFactoryConfiguration.getDialect();
			Class<?> dialectClass = Class.forName(dialectProperty);

			if (!ReflectionUtils.isExtendsClass(NoSQLDialect.class, dialectClass))
				throw new NoSQLSessionException("A classe " + dialectClass.getName() + " não implementa a classe "
						+ NoSQLDialect.class.getName() + ".");

			NoSQLDialect dialect = (NoSQLDialect) dialectClass.newInstance();
			this.dataSource = dialect.getDataSourceBuilder().configure(dataSourceConfiguration.getProperties()).build();
		}
		return dataSource;
	}

}
