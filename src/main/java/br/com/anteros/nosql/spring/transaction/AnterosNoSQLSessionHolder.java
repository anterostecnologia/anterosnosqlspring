package br.com.anteros.nosql.spring.transaction;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.ResourceHolderSupport;

import br.com.anteros.nosql.persistence.session.NoSQLSession;

@SuppressWarnings("rawtypes")
public class AnterosNoSQLSessionHolder extends ResourceHolderSupport {
	
	private NoSQLSession session;

	public AnterosNoSQLSessionHolder(NoSQLSession session) {
		this.session = session;
	}

	public NoSQLSession getSession() {		
		return session;
	}
	
	public void setSession(NoSQLSession session) {
		this.session = session;
	}

	void setTimeoutIfNotDefaulted(int seconds) {

		if (seconds != TransactionDefinition.TIMEOUT_DEFAULT) {
			setTimeoutInSeconds(seconds);
		}
	}
	
	public boolean hasSession() {
		return session != null;
	}

}
