package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FunctionUsingSomeDatabase {
    private Connection connection;

    @FnConfiguration
    public void setUp(RuntimeContext ctx) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        connection = DriverManager.getConnection(ctx.getConfiguration().get("DB_URL"),
                ctx.getConfiguration().get("DB_USER"), ctx.getConfiguration().get("DB_PASSWORD"));
    }

    public String handleRequest(String user) throws Exception {
        try(PreparedStatement userQuery = connection.prepareStatement("SELECT name from USERS where user=?")) {
            userQuery.setString(1, user);
            ResultSet rs = userQuery.executeQuery();
            if(rs.next()) {
                return rs.getString("name");
            } else {
                return "Unknown";
            }
        }
    }
}
