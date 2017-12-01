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
		connection = DriverManager.getConnection("jdbc:oracle:thin:@10.129.181.149:1521:fopaqa1","nfo_cn", "nfo2008");
    }

    public String handleRequest(String phoneNumber) throws Exception {

        System.out.println(phoneNumber);
        
	    Statement statement = connection.createStatement();

        ResultSet tbl = statement.executeQuery("select IDLINHABASE,IDAREAREGISTRO,NRLINHA,DTULTIMAALTERACAO,IDESTADOLINHA, DTESTADOLINHA, IDNUMEROSISTEMAORIGEM, IDSISTEMAORIGEM FROM LINHABASE where NRLINHA = " + phoneNumber);
   
        String sRow = "IDLINHABASE|IDAREAREGISTRO|NRLINHA|DTULTIMAALTERACAO|IDESTADOLINHA|DTESTADOLINHA|IDNUMEROSISTEMAORIGEM|IDSISTEMAORIGEM\n";
   
		System.out.println(sRow);
   
        while (tbl.next())
        {
           int idLinhaBase = tbl.getInt(1);
           int idAreaRegistro = tbl.getInt(2);
           String nrLinha = tbl.getString(3);
           Date dtUltimaAlteracao = tbl.getDate(4);
           int idEstadoLinha = tbl.getInt(5);
           Date dtEstadoLinha = tbl.getDate(6);
           int idNumSistemaOrigem = tbl.getInt(7);
           int idSistemaOrigem = tbl.getInt(8);
           sRow += idLinhaBase + "|" + idAreaRegistro + "|" + nrLinha + "|" + dtUltimaAlteracao + "|" + idEstadoLinha + "|" + dtEstadoLinha + "|" + idNumSistemaOrigem + "|" + idSistemaOrigem + "\n";
           //System.out.println(sRow);
        }
        
        System.out.println(sRow);
        
        tbl.close();
        statement.close();
        //connection.close();
        
        return sRow;
    }
}