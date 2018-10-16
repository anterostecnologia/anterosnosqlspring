package br.com.anteros.nosql.spring.transaction;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.util.Assert;

import br.com.anteros.nosql.persistence.session.NoSQLSession;
import br.com.anteros.nosql.persistence.session.NoSQLSessionFactory;
import br.com.anteros.nosql.persistence.session.transaction.NoSQLTransaction;
import br.com.anteros.nosql.persistence.session.transaction.NoSQLTransactionException;
import br.com.anteros.nosql.persistence.session.transaction.NoSQLTransactionOptions;

public class AnterosNoSQLTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, InitializingBean {

	private static final long serialVersionUID = 1L;

	private NoSQLSessionFactory sessionFactory;
	private NoSQLTransactionOptions options;

	public AnterosNoSQLTransactionManager() {
	}

	public AnterosNoSQLTransactionManager(NoSQLSessionFactory sessionFactory) {
		this(sessionFactory, null);
	}

	public AnterosNoSQLTransactionManager(NoSQLSessionFactory sessionFactory, NoSQLTransactionOptions options) {
		Assert.notNull(sessionFactory, "NoSQLSessionFactory must not be null!");
		this.sessionFactory = sessionFactory;
		this.options = options;
	}

	@Override
	protected Object doGetTransaction() throws TransactionException {

		AnterosNoSQLSessionHolder sessionHolder = (AnterosNoSQLSessionHolder) TransactionSynchronizationManager
				.getResource(getSessionFactory());
		if (sessionHolder != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found thread-bound Session [" + sessionHolder.getSession() + "] for NoSQL transaction");
			}
		}
		return new NoSQLTransactionObject(sessionHolder);
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		NoSQLTransactionObject txObject = extractNoSQLTransaction(transaction);
		return txObject.hasResourceHolder() && txObject.isTransactionActive();
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws NoSQLTransactionException {

		NoSQLTransactionObject txObject = extractNoSQLTransaction(transaction);

		if (txObject.getSessionHolder() == null || txObject.getSessionHolder().isSynchronizedWithTransaction()) {
			NoSQLSession session = sessionFactory.openSession();
			if (logger.isDebugEnabled()) {
				logger.debug("Opened new Session [" + session + "] for NoSQL transaction");
			}
			txObject.setSessionHolder(new AnterosNoSQLSessionHolder(session));
		}

		NoSQLSession session = txObject.getSessionHolder().getSession();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("About to start NoSQL transaction for session %s.",
					session.getTransaction().debugString()));
		}

		try {
			txObject.startTransaction(options);
		} catch (Exception ex) {
			throw new TransactionSystemException(String.format("Could not start NoSQL transaction for session %s.",
					txObject.getTransaction().debugString()), ex);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Started transaction for session %s.", session.getTransaction().debugString()));
		}

		txObject.getSessionHolder().setSynchronizedWithTransaction(true);
		TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject.getSessionHolder());
	}

	@Override
	protected Object doSuspend(Object transaction) throws TransactionException {

		NoSQLTransactionObject txObject = extractNoSQLTransaction(transaction);
		txObject.setSessionHolder(null);

		return TransactionSynchronizationManager.unbindResource(getSessionFactory());
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		TransactionSynchronizationManager.bindResource(getSessionFactory(), suspendedResources);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws NoSQLTransactionException {

		NoSQLTransactionObject txObject = extractNoSQLTransaction(status);

		if (txObject != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("About to commit transaction for session %s.",
						txObject.getTransaction().debugString()));
			}

			try {
				txObject.commitTransaction();
			} catch (Exception ex) {
				throw new TransactionSystemException(String.format("Could not commit NoSQL transaction for session %s.",
						txObject.getTransaction().debugString()), ex);
			}
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {

		NoSQLTransactionObject txObject = extractNoSQLTransaction(status);

		if (txObject != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("About to abort transaction for session %s.",
						txObject.getTransaction().debugString()));
			}

			try {
				txObject.abortTransaction();
			} catch (Exception ex) {

				throw new TransactionSystemException(String.format("Could not abort NoSQL transaction for session %s.",
						txObject.getTransaction().debugString()), ex);
			}
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {

		NoSQLTransactionObject txObject = extractNoSQLTransaction(status);
		txObject.getRequiredSessionHolder().setRollbackOnly();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {

		Assert.isInstanceOf(NoSQLTransactionObject.class, transaction,
				() -> String.format("Expected to find a %s but it turned out to be %s.", NoSQLTransactionObject.class,
						transaction.getClass()));

		NoSQLTransactionObject txObject = (NoSQLTransactionObject) transaction;

		// Remove the connection holder from the thread.
		TransactionSynchronizationManager.unbindResource(getSessionFactory());
		txObject.getRequiredSessionHolder().clear();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("About to release Session %s after transaction.",
					txObject.getTransaction().debugString()));
		}

		txObject.close();
	}

	public void setSessionFactory(NoSQLSessionFactory sessionFactory) {

		Assert.notNull(sessionFactory, "SessionFactory must not be null!");
		this.sessionFactory = sessionFactory;
	}

	public void setOptions(NoSQLTransactionOptions options) {
		this.options = options;
	}

	@Override
	public void afterPropertiesSet() {
		getSessionFactory();
	}

	private NoSQLSessionFactory getSessionFactory() {

		Assert.state(sessionFactory != null,
				"AnterosNoSQLTransactionManager operates upon a NoSQLSession. Did you forget to provide one? It's required.");

		return sessionFactory;
	}

	private static NoSQLTransactionObject extractNoSQLTransaction(Object transaction) {

		Assert.isInstanceOf(NoSQLTransactionObject.class, transaction,
				() -> String.format("Expected to find a %s but it turned out to be %s.", NoSQLTransactionObject.class,
						transaction.getClass()));

		return (NoSQLTransactionObject) transaction;
	}

	private static NoSQLTransactionObject extractNoSQLTransaction(DefaultTransactionStatus status) {

		Assert.isInstanceOf(NoSQLTransactionObject.class, status.getTransaction(),
				() -> String.format("Expected to find a %s but it turned out to be %s.", NoSQLTransactionObject.class,
						status.getTransaction().getClass()));

		return (NoSQLTransactionObject) status.getTransaction();
	}

	static class NoSQLTransactionObject implements SmartTransactionObject {

		private AnterosNoSQLSessionHolder sessionHolder;

		NoSQLTransactionObject(AnterosNoSQLSessionHolder sessionHolder) {
			this.sessionHolder = sessionHolder;
		}

		void setSessionHolder(AnterosNoSQLSessionHolder sessionHolder) {
			this.sessionHolder = sessionHolder;
		}

		boolean hasResourceHolder() {
			return sessionHolder != null;
		}

		boolean isTransactionActive() {
			return sessionHolder.getSession().getTransaction().isActive();
		}

		void startTransaction(NoSQLTransactionOptions options) {

			NoSQLTransaction transaction = sessionHolder.getSession().getTransaction();
			if (options != null) {
				transaction.begin(options);
			} else {
				transaction.begin();
			}
		}

		void commitTransaction() throws Exception {
			getRequiredTransaction().commit();
		}

		void abortTransaction() {
			getRequiredTransaction().rollback();
		}

		void close() {
			NoSQLTransaction transaction = getRequiredTransaction();
			transaction.close();
		}

		NoSQLTransaction getTransaction() {
			return sessionHolder != null ? sessionHolder.getSession().getTransaction() : null;
		}

		AnterosNoSQLSessionHolder getSessionHolder() {
			return sessionHolder;
		}

		private AnterosNoSQLSessionHolder getRequiredSessionHolder() {
			Assert.state(sessionHolder != null, "AnterosNoSQLSessionHolder is required but not present. o_O");
			return sessionHolder;
		}

		private NoSQLTransaction getRequiredTransaction() {
			NoSQLTransaction session = getTransaction();
			Assert.state(session != null, "A Transaction is required but it turned out to be null.");
			return session;
		}

		@Override
		public boolean isRollbackOnly() {
			return this.sessionHolder != null && this.sessionHolder.isRollbackOnly();
		}

		@Override
		public void flush() {
			TransactionSynchronizationUtils.triggerFlush();
		}

	}

	@Override
	public Object getResourceFactory() {
		return getSessionFactory();
	}
}