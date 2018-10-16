package br.com.anteros.nosql.spring.config;

import java.util.Properties;

import br.com.anteros.nosql.persistence.metadata.configuration.AnterosNoSQLProperties;

public class NoSQLDataSourceConfiguration {

	private String host;
	
	private Integer port;

	private String username;

	private String password;

	private String databaseName;

	private NoSQLDataSourceConfiguration() {

	}

	public static NoSQLDataSourceConfiguration create() {
		return new NoSQLDataSourceConfiguration();
	}

	public static NoSQLDataSourceConfiguration of(String host, Integer port, String username, String password, String databaseName) {
		return new NoSQLDataSourceConfiguration().host(host).port(port).userName(username).password(password).databaseName(databaseName);
	}

	public String getUserName() {
		if (username ==null)
			return "";
		return username;
	}

	public NoSQLDataSourceConfiguration userName(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		if (password ==null)
			return "";
		return password;
	}

	public NoSQLDataSourceConfiguration password(String password) {
		this.password = password;
		return this;
	}

	public String getHost() {
		return host;
	}

	public NoSQLDataSourceConfiguration host(String host) {
		this.host = host;
		return this;
	}

	public Integer getPort() {
		return port;
	}

	public NoSQLDataSourceConfiguration port(Integer port) {
		this.port = port;
		return this;
	}
	public String getDatabaseName() {
		return databaseName;
	}

	public NoSQLDataSourceConfiguration databaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}
	
	public Properties getProperties() {
		Properties result = new Properties();
		result.put(AnterosNoSQLProperties.CONNECTION_HOST, getHost());
		result.put(AnterosNoSQLProperties.CONNECTION_PASSWORD, getPassword());
		result.put(AnterosNoSQLProperties.CONNECTION_PORT, getPort()+"");
		result.put(AnterosNoSQLProperties.CONNECTION_USER, getUserName());
		result.put(AnterosNoSQLProperties.DATABASE_NAME, getDatabaseName());		
		return result;
	}


}
