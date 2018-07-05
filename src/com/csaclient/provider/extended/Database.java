package com.csaclient.provider.extended;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static ComboPooledDataSource cpds;
    private static String server, db, user, password;
    public Database() {
        
    }
    
    public static void setupPool(String server, String db, String user, String password) throws PropertyVetoException {
        if(cpds == null) return;
        
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        cpds.setJdbcUrl("jdbc:sqlserver://"+server +";databaseName=" +db+";integratedSecurity=true");
        cpds.setUser(user);
        cpds.setPassword(password);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
    }
    
    public Connection getConnectionToDatabase() throws SQLException, PropertyVetoException {
        if(cpds == null) setupPool(server,  db,  user,  password);
        return cpds.getConnection();
    }
    
}