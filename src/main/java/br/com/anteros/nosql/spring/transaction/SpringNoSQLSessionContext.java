package br.com.anteros.nosql.spring.transaction;

import br.com.anteros.nosql.persistence.session.NoSQLSession;
import br.com.anteros.nosql.persistence.session.NoSQLSessionFactory;
import br.com.anteros.nosql.persistence.session.context.CurrentNoSQLSessionContext;

public class SpringNoSQLSessionContext implements CurrentNoSQLSessionContext {

	private static final long serialVersionUID = 1L;
	private final NoSQLSessionFactory sessionFactory;

	public SpringNoSQLSessionContext(NoSQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public NoSQLSession currentSession() {
		return NoSQLSessionFactoryUtils.getSession(this.sessionFactory);
	}
}
