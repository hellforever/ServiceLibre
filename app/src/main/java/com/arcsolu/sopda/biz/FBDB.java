package com.arcsolu.sopda.biz;

import java.lang.String;
import java.sql.SQLException;

import android.util.Log;

public class FBDB extends absDB implements IDB {
	// private static final String URL = "jdbc:firebirdsql://localhost:3050/";
	private static final int REGISTER_CLASS_FOR_NAME = 1;
	private static final int REGISTER_PROPERTIES = 2;
	private static final int REGISTER_JDBC4 = 3;
	private static final int CONNECT_DRIVERMANAGER = 1;
	private static final int CONNECT_DRIVER = 2;
	private String Server;
	private String Database;
	private String Username;
	private String Password;
	private java.sql.Driver driver = null;
	protected java.sql.Connection con = null;

	public boolean loadDriver() {

		// Log.d("FBDB load driver", "start loading");
		/*
		 * Before a JDBC driver can be used, it must have been registered with
		 * the DriverManager.
		 * 
		 * Demonstrate the different methods to register the Firebird JCA-JDBC
		 * driver with the driver manager
		 */
		int registrationAlternative = REGISTER_CLASS_FOR_NAME;
		switch (registrationAlternative) {

		case REGISTER_CLASS_FOR_NAME:
			/*
			 * For JDBC 3.0 and earlier, the standard method of registering the
			 * driver is by loading the class.
			 * 
			 * Class.forName() instructs the java class loader to load and
			 * initialize a class. As part of the class initialization any
			 * static clauses associated with the class are executed.
			 * 
			 * Every driver class is required by the jdbc specification to
			 * create an instance of itself and register that instance with the
			 * DriverManager when the driver class is loaded by the java
			 * classloader (this is done via a static clause associated with the
			 * driver class).
			 * 
			 * Notice that the driver name could have been supplied dynamically,
			 * so that an application is not hardwired to any particular driver
			 * as would be the case if a driver constructor were used, eg. new
			 * org.firebirdsql.jdbc.FBDriver().
			 */
			try {
				Class.forName("org.firebirdsql.jdbc.FBDriver").newInstance();
			} catch (Exception e) {
				// A call to Class.forName() forces us to consider this
				// exception
				System.out
						.println("Firebird JCA-JDBC driver not found in class path");
				System.out.println(e.getMessage());
				return false;
			}
			break;

		case REGISTER_PROPERTIES:
			/*
			 * Add the Firebird JCA-JDBC driver name to your system's
			 * jdbc.drivers property list.
			 * 
			 * The driver manager will load drivers from this system property
			 * list.
			 */
			java.util.Properties sysProps = System.getProperties();
			StringBuffer drivers = new StringBuffer(
					"org.firebirdsql.jdbc.FBDriver");
			String oldDrivers = sysProps.getProperty("jdbc.drivers");
			if (oldDrivers != null)
				drivers.append(":" + oldDrivers);
			sysProps.put("jdbc.drivers", drivers.toString());
			System.setProperties(sysProps);
			break;

		case REGISTER_JDBC4:
			/*
			 * From JDBC 4.0 (Java 6), drivers are required to have a file
			 * /META-INF/services/java.sql.Driver with the classname(s) of the
			 * drivers.
			 * 
			 * The DriverManager will automatically load all drivers, so there
			 * is no need to explicitly load the driver.
			 */
			break;
		}

		/*
		 * At this point the driver should be registered with the DriverManager.
		 * Try to find the registered driver that recognizes Firebird URLs
		 */
		try {
			// We pass the entire database URL, but we could just pass
			// "jdbc:firebirdsql:"
			// jdbc:firebirdsql://localhost:3050/
			driver = java.sql.DriverManager.getDriver("jdbc:firebirdsql://"
					+ this.Server + ":3050/" + this.Database);
			System.out.println("Firebird JCA-JDBC driver version "
					+ driver.getMajorVersion() + "." + driver.getMinorVersion()
					+ " registered with driver manager.");
		} catch (java.sql.SQLException e) {
			System.out
					.println("Unable to find Firebird JCA-JDBC driver among the registered drivers.");
			showSQLException(e);
			return false;
		}

		/*
		 * Now that Firebird JCA-JDBC driver is registered with the
		 * DriverManager, try to get a connection to an employee.fdb database on
		 * this local machine using one of two alternatives for obtaining
		 * connections
		 */
		Log.d("FBDB load driver", "loading successed");
		return true;
	}

	public FBDB() {
		Server = "192.168.0.100";
		Database = "arcresto";
		Username = "SYSDBA";
		Password = "masterkey";
		con = null;
		if (!loadDriver()) {
			System.out.println("load driver failed!");
		}
		// Log.d("FBDB constructor", "create objet finished");
	}

	/**
	 * 
	 * @param server
	 * @param db
	 * @param user
	 * @param pwd
	 */
	public FBDB(String server, String db, String user, String pwd) {
		Server = server;
		Database = db;
		Username = user;
		Password = pwd;
		con = null;
		if (!loadDriver()) {
			System.out.println("load driver failed!");
		}
		Log.d("FBDB constructor(param)", "create objet finished");
	}

	@Override
	public boolean CreateDB() {
		return false;
	}

	@Override
	public boolean Connect() {
		java.sql.DriverManager.setLoginTimeout(2);
		// Log.d("FBDB connector", "start connecting");
		int connectionAlternative = CONNECT_DRIVERMANAGER;
		switch (connectionAlternative) {

		case CONNECT_DRIVERMANAGER:
			/*
			 * This alternative is driver independent; the DriverManager will
			 * find the right driver for you based on the jdbc subprotocol.
			 */
			try {
				con = java.sql.DriverManager.getConnection(
						"jdbc:firebirdsql://" + this.Server + ":3050/"
								+ this.Database, this.Username, this.Password);
				System.out.println("Connection established.");
			} catch (java.sql.SQLException e) {
				System.out
						.println("Unable to establish a connection through the driver manager.");
				showSQLException(e);
				return false;
			}
			break;

		case CONNECT_DRIVER:
			/*
			 * If you're working with a particular driver, which may or may not
			 * be registered, you can get a connection directly from it,
			 * bypassing the DriverManager
			 */
			try {
				java.util.Properties connectionProperties = new java.util.Properties();
				connectionProperties.put("user", this.Username);
				connectionProperties.put("password", this.Password);
				connectionProperties.put("lc_ctype", "WIN1251");
				con = driver.connect("jdbc:firebirdsql://" + this.Server
						+ ":3050/" + this.Database, connectionProperties);
				System.out.println("Connection established.");
			} catch (java.sql.SQLException e) {
				System.out
						.println("Unable to establish a connection through the driver.");
				showSQLException(e);
				return false;
			}
			break;
		}
		Log.d("FBDB connector", "connecting successed");
		return true;
	}
	
	@Override
	public boolean IsValid(){
		boolean bool = false;
		try {
			bool=con.isValid(1000);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bool;
	}

	@Override
	public void Disconnect() {
		try {
			if (con == null) {
				return;
			}
			System.out.println("connection closed!!");
			con.close();
		} catch (SQLException e) {
			System.out.println("Unable to disconnect a connection.");
			showSQLException(e);
		}

	}

	@Override
	public boolean StartTransAction() {

		try {
			con.setAutoCommit(false);
			System.out.println("Auto-commit is disabled.");
		} catch (java.sql.SQLException e) {
			System.out.println("Unable to disable autocommit.");
			showSQLException(e);
			return false;
		}

		return true;
	}

	@Override
	public boolean CommitTransAction() {

		try {
			con.commit();
			System.out.println("Statement commit.");
			con.setAutoCommit(true);
			System.out.println("Auto-commit is enabled.");
		} catch (java.sql.SQLException e) {
			System.out.println("Unable to disable autocommit.");
			showSQLException(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean RollbackTransAction() {
		//
		try {
			con.rollback();
			System.out.println("Statement rollback.");
		} catch (java.sql.SQLException e) {
			System.out.println("Unable to rollback statement.");
			showSQLException(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean BackupTo() {
		//
		return false;
	}
	// public static void main(String args[]) throws Exception {
	// FBDB fb = new FBDB();
	// fb.Connect();
	// }

}
