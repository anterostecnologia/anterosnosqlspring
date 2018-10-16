package br.com.anteros.nosql.spring.config;

import java.util.ArrayList;
import java.util.List;

import br.com.anteros.nosql.persistence.session.ShowCommandsType;
import br.com.anteros.nosql.persistence.session.configuration.PackageScanEntity;


public class NoSQLSessionFactoryConfiguration {

	private List<Class<?>> entitySourceClasses = new ArrayList<Class<?>>();
	private PackageScanEntity packageScanEntity = null;
	private Boolean includeSecurityModel = true;
	private String dialect;
	private ShowCommandsType[] showCommands = { ShowCommandsType.NONE };
	private Boolean formatCommands = true;
	private Boolean useBeanValidation = true;
	private String databaseName="";
	private Boolean withoutTransactionControl = false;

	private NoSQLSessionFactoryConfiguration() {

	}

	public static NoSQLSessionFactoryConfiguration create() {
		return new NoSQLSessionFactoryConfiguration();
	}

	public List<Class<?>> getEntitySourceClasses() {
		return entitySourceClasses;
	}

	public NoSQLSessionFactoryConfiguration addEntitySourceClass(Class<?> entitySourceClass) {
		this.entitySourceClasses.add(entitySourceClass);
		return this;
	}

	public PackageScanEntity getPackageScanEntity() {
		return packageScanEntity;
	}

	public NoSQLSessionFactoryConfiguration packageScanEntity(PackageScanEntity packageScanEntity) {
		this.packageScanEntity = packageScanEntity;
		return this;
	}

	public boolean isIncludeSecurityModel() {
		return includeSecurityModel;
	}

	public NoSQLSessionFactoryConfiguration includeSecurityModel(boolean includeSecurityModel) {
		this.includeSecurityModel = includeSecurityModel;
		return this;
	}

	public String getDialect() {
		return dialect;
	}

	public NoSQLSessionFactoryConfiguration dialect(String dialect) {
		this.dialect = dialect;
		return this;
	}

	public ShowCommandsType[] getShowCommands() {
		return showCommands;
	}

	public NoSQLSessionFactoryConfiguration showCommands(ShowCommandsType... showCommands) {
		this.showCommands = showCommands;
		return this;
	}

	public boolean isFormatCommands() {
		return formatCommands;
	}

	public NoSQLSessionFactoryConfiguration formatCommands(boolean formatCommands) {
		this.formatCommands = formatCommands;
		return this;
	}

	public Boolean getUseBeanValidation() {
		return useBeanValidation;
	}

	public NoSQLSessionFactoryConfiguration useBeanValidation(Boolean useBeanValidation) {
		this.useBeanValidation = useBeanValidation;
		return this;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public NoSQLSessionFactoryConfiguration databaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	public Boolean getWithoutTransactionControl() {
		return withoutTransactionControl;
	}

	public NoSQLSessionFactoryConfiguration withoutTransactionControl(Boolean withoutTransactionControl) {
		this.withoutTransactionControl = withoutTransactionControl;
		return this;
	}

}
