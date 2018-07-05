package com.csaclient.provider.extended;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static ComboPooledDataSource cpds;
    private static String server, db, user, password;
    public Database() {
        
    }
    public static boolean poolIsValid() {
        if(cpds == null) return false;
        return true;
    }
    public static void setupPool(String server, String db, String user, String password) throws PropertyVetoException {
        if(!poolIsValid()) return;
        
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        cpds.setJdbcUrl("jdbc:sqlserver://"+server +";databaseName=" +db+";integratedSecurity=true");
        cpds.setUser(user);
        cpds.setPassword(password);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
    }
    
    public static Connection getConnectionToDatabase() throws SQLException, PropertyVetoException {
        if(!poolIsValid()) setupPool(server,  db,  user,  password);
        return cpds.getConnection();
    }
}