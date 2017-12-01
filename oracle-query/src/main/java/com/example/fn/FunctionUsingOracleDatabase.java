package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
import java.sql.*;
import com.fasterxml.jackson.core.*;
import java.util.Date;

public class FunctionUsingOracleDatabase {
    private Connection connection;

    @FnConfiguration
    public void setUp(RuntimeContext ctx) throws Exception {
    	JsonFactory factory = new JsonFactory();
		connection = DriverManager.getConnection("jdbc:oracle:thin:@//mac-as-net:1521/XE","scott", "tiger");
    }

    public String handleRequest(String user) throws Exception {

	    Statement statement = connection.createStatement();

        ResultSet emps = statement.executeQuery("select * from emp order by job,ename");
   
        String sRow = "";
   
        while (emps.next())
        {
           int number = emps.getInt(1);
           String name = emps.getString(2);
           String job = emps.getString(3);
           int manager = emps.getInt(4);
           Date hired = emps.getDate(5);
           int salary = emps.getInt(6);
           int commission = emps.getInt(7);
           int department = emps.getInt(8);
           sRow += number + "|" + name + "|" + job + "|" + manager + "|" + hired + "|" + salary + "|" + commission + "|" + department + "\n";
           //System.out.println(sRow);
        }
        
        emps.close();
        statement.close();
        //connection.close();
        
        return sRow;
    }
}