
package br.com.anteros.nosql.spring.transaction;

import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.nosql.persistence.session.NoSQLSession;
import br.com.anteros.nosql.persistence.session.NoSQLSessionFactory;

public class NoSQLSessionFactoryUtils {

	private static Logger LOG = LoggerProvider.getInstance().getLogger(NoSQLSessionFactoryUtils.class);

	public static NoSQLSession getSession(NoSQLSessionFactory sessionFactory) {
		return doGetSession(null, sessionFactory, SessionSynchronization.ON_ACTUAL_TRANSACTION);
	}

	public static NoSQLSession getSession(NoSQLSessionFactory sessionFactory,
			SessionSynchronization sessionSynchronization) {
		return doGetSession(null, sessionFactory, sessionSynchronization);
	}

	private static NoSQLSession doGetSession(String dbName, NoSQLSessionFactory sessionFactory,
			SessionSynchronization sessionSynchronization) {

		Assert.notNull(sessionFactory, "SessionFactory must not be null!");

		return doGetSession(sessionFactory, sessionSynchronization);
	}

	private static NoSQLSession doGetSession(NoSQLSessionFactory sessionFactory,
			SessionSynchronization sessionSynchronization) {

		AnterosNoSQLSessionHolder sessionHolder = (AnterosNoSQLSessionHolder) TransactionSynchronizationManager
				.getResource(sessionFactory);

		if (sessionHolder != null && (sessionHolder.hasSession() || sessionHolder.isSynchronizedWithTransaction())) {
			if (!sessionHolder.hasSession()) {
				sessionHolder.setSession(sessionFactory.openSession());
			}
			return sessionHolder.getSession();
		}

		if (SessionSynchronization.ON_ACTUAL_TRANSACTION.equals(sessionSynchronization)) {
			sessionHolder = new AnterosNoSQLSessionHolder(sessionFactory.openSession());
			TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
			return sessionHolder.getSession();
		}

		sessionHolder = new AnterosNoSQLSessionHolder(sessionFactory.openSession());

		sessionHolder.getSession().getTransaction().begin();

		TransactionSynchronizationManager
				.registerSynchronization(new NoSQLSessionSynchronization(sessionHolder, sessionFactory));
		sessionHolder.setSynchronizedWithTransaction(true);
		TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);

		return sessionHolder.getSession();
	}

	private static class NoSQLSessionSynchronization
			extends ResourceHolderSynchronization<AnterosNoSQLSessionHolder, Object> {

		private final AnterosNoSQLSessionHolder resourceHolder;

		NoSQLSessionSynchronization(AnterosNoSQLSessionHolder resourceHolder, NoSQLSessionFactory sessionFactory) {
			super(resourceHolder, sessionFactory);
			this.resourceHolder = resourceHolder;
		}

		@Override
		protected boolean shouldReleaseBeforeCompletion() {
			return false;
		}

		@Override
		protected void processResourceAfterCommit(AnterosNoSQLSessionHolder resourceHolder) {
			if (isTransactionActive(resourceHolder)) {
				resourceHolder.getSession().commitTransaction();
			}
		}

		@Override
		public void afterCompletion(int status) {

			if (status == TransactionSynchronization.STATUS_ROLLED_BACK && isTransactionActive(this.resourceHolder)) {
				resourceHolder.getSession().abortTransaction();
			}

			super.afterCompletion(status);
		}

		@Override
		protected void releaseResource(AnterosNoSQLSessionHolder resourceHolder, Object resourceKey) {

			if (resourceHolder.getSession().getTransaction().isActive()) {
				resourceHolder.getSession().getTransaction().close();
			}
		}

		private boolean isTransactionActive(AnterosNoSQLSessionHolder resourceHolder) {

			if (!resourceHolder.hasSession()) {
				return false;
			}

			return resourceHolder.getSession().getTransaction().isActive();
		}
	}

	public static void closeSession(NoSQLSession session) {
		if (session != null) {
			LOG.debug("Closing Anteros NoSQLSession");
			try {
				session.close();
			} catch (Throwable ex) {
				LOG.debug("Unexpected exception on closing Anteros NoSQLSession", ex);
			}
		}
	}
}
