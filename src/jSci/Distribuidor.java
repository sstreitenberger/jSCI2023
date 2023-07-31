package jSci;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Vector;

public class Distribuidor {
  String errores;
  
  String base;
  
  boolean esFechaSql;
  
  public Distribuidor(boolean esarg, boolean eschile, boolean escol) {
    this.errores = "";
    this.base = "micros";
    this.esFechaSql = true;
    this.errores = "";
    if (escol)
      this.base = "microsco"; 
    if (esarg) {
      DistribuidorGenerarSCINeo(esarg, eschile, escol);
    } else {
      //DistribuidorGenerarSCI(esarg, eschile, escol, "2018/11/12");
    } 
  }
  
  public void DistribuidorGenerarSCI(boolean esarg, boolean eschile, boolean escol, String fechamax) {
    Connection conn = null;
    Vector datos = new Vector(500, 250);
    this.base = "PoleoAR";
    if (eschile)
      this.base = "PoleoCL"; 
    try {
      String fechalimite = "";
      Statement stmt = null;
      ResultSet rs = null;
      String query = "SELECT min([ult_envio]) FROM [SCI_Envios] " + fechalimite;
      if (eschile) {
        conn = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
      } else {
        conn = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
      } 
      stmt = conn.createStatement();
      rs = stmt.executeQuery(query);
      String fecha = "";
      String ayer = darAyer(false, this.esFechaSql);
      String hoy = darHoy(this.esFechaSql);
      jFtpZohan ftp = new jFtpZohan();
      boolean conecto = true;
      if (conecto) {
        if (rs.next()) {
          fecha = rs.getString(1).substring(0, 10);
          fecha = fecha.substring(0, 4) + "-" + fecha.substring(5, 7) + "-" + fecha.substring(8, 10);
          fecha = darDiaSig(fecha, this.esFechaSql);
          while (!fecha.equals(hoy)) {
            System.out.println("Generando SCI fecha: " + fecha);
            GenerarSCI(esarg, eschile, escol, fecha, ftp);
            fecha = darDiaSig(fecha, this.esFechaSql);
          } 
        } 
      } else {
        System.out.println("FTP: NO CONECTA");
      } 
      if (rs != null)
        rs.close(); 
      if (stmt != null)
        stmt.close(); 
      if (conn != null)
        conn.close(); 
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
      e.printStackTrace();
    } 
  }
  
  public void DistribuidorGenerarSCINeo(boolean esarg, boolean eschile, boolean escol) {
    Connection conn = null;
    Vector datos = new Vector(500, 250);
    this.base = "Poleo5AR";
    if (eschile)
      this.base = "PoleoCL"; 
    try {
      Statement stmt = null;
      ResultSet rs = null;
      String query = "SELECT min([ult_envio]) FROM poleo5ar.dbo.[SCI_Envios] ";
      if (eschile) {
        conn = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
      } else {
        conn = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34"); //user=web;password=w3b4ls34
      } 
      stmt = conn.createStatement();
      rs = stmt.executeQuery(query);
      String fecha = "";
      String ayer = darAyer(false, this.esFechaSql);
      String hoy = darHoy(this.esFechaSql);
      jFtpZohan ftp = new jFtpZohan();
      boolean conecto = true;
      if (conecto) {
        if (rs.next()) {
          fecha = rs.getString(1).substring(0, 10);
          fecha = fecha.substring(0, 4) + "-" + fecha.substring(5, 7) + "-" + fecha.substring(8, 10);
          fecha = darDiaSig(fecha, this.esFechaSql);
          while (!fecha.equals(hoy)) {
            System.out.println("Generando SCI fecha: " + fecha);
            GenerarSCINeo(esarg, eschile, escol, fecha, ftp);
            fecha = darDiaSig(fecha, this.esFechaSql);
          } 
        } 
      } else {
        System.out.println("FTP: NO CONECTA");
      } 
      if (rs != null)
        rs.close(); 
      if (stmt != null)
        stmt.close(); 
      if (conn != null)
        conn.close(); 
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
      e.printStackTrace();
    } 
  }
  
  public void GenerarSCI(boolean esarg, boolean eschile, boolean escol, String fechasql, jFtpZohan ftp) {
    Connection conn = null;
    Vector<String> datos = new Vector(500, 250);
    this.base = "PoleoAR";
    if (eschile)
      this.base = "PoleoCL"; 
    try {
      Statement stmt = null;
      ResultSet rs = null;
      String query = "SELECT A.[id],[businessDay] fecha,round([total_gross_sales],2) bruto,round([total_net_sales],2) neto,A.[vat_percentage]/100 iva,[store_id] tienda,sci_id cuenta,exchange_rate cambio,1 tiporeg ";
      if (eschile)
        query = "SELECT A.[id],[businessDay] fecha,round(total_gross_sales+total_creditnotes,2) bruto,round(total_net_sales+(total_creditnotes/(1+A.vat_percentage*1.0/10000.0)),2) neto,A.[vat_percentage]/100 iva,[store_id] tienda,sci_id cuenta,exchange_rate cambio,1 tiporeg  "; 
      query = query + " FROM [sales_report] A, SCI_envios B, store C where ";
      query = query + " A.[store_id]=b.tienda and A.store_id=C.id and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and A.[store_id]=B.tienda and A.store_id=C.id and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      if (!eschile) {
        query = query + "UNION ALL SELECT A.[id],[businessDay] fecha,round((pos_acti_and_rec_amount-TOTALNOTASDECREDITOSTARBUCKSREWARDEFECTIVO-TOTALNOTASDECREDITOSTARBUCKSREWARDTARJETA),2) bruto,round((pos_acti_and_rec_amount-TOTALNOTASDECREDITOSTARBUCKSREWARDEFECTIVO-TOTALNOTASDECREDITOSTARBUCKSREWARDTARJETA),2) neto,0 iva,[store_id] tienda,sci_id cuenta,exchange_rate cambio,2 tiporeg ";
        query = query + " FROM [sales_report] A, SCI_envios B, store C where ";
        query = query + " A.[store_id]=b.tienda and A.store_id=C.id and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
        query = query + " and A.[store_id]=B.tienda and A.store_id=C.id and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
        query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      } 
      query = query + " order by A.store_id,fecha";
      if (eschile) {
        conn = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
      } else {
        conn = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
      } 
      stmt = conn.createStatement();
      System.out.println(query);
      rs = stmt.executeQuery(query);
      String fecha = "";
      String codtienda = "";
      int cant = 0;
      String tienda = "";
      String fechaant = "";
      boolean primero = true;
      while (rs.next()) {
        cant++;
        fecha = rs.getString("fecha");
        String tiendaaux = rs.getString("tienda");
        if (primero) {
          tienda = tiendaaux;
          fechaant = fecha;
        } 
        if (tienda.equals(tiendaaux))
          primero = false; 
        String res = "";
        if (rs.getInt("tiporeg") == 1) {
          if (rs.getDouble("bruto") != 0.0D) {
            res = rs.getString("cuenta") + fecha.substring(0, 4) + fecha.substring(5, 7) + fecha.substring(8, 10) + "|" + fecha.substring(0, 10) + "|";
            if (rs.getString("cuenta").equals("40029") || rs.getString("cuenta").equals("40065") || rs.getString("cuenta").equals("40131")) {
              res = res + "9999999|" + rs.getString("cuenta") + "|GLB||||" + fecha.substring(0, 10) + "|" + rs.getString("cambio") + "||1|VENTAS DIARIAS 0%|";
            } else {
              res = res + "9999999|" + rs.getString("cuenta") + "|GLB||||" + fecha.substring(0, 10) + "|" + rs.getString("cambio") + "||1|VENTAS DIARIAS|";
            } 
            res = res + dosDecimales(rs.getString("neto")) + "|" + dosDecimales(rs.getString("iva")) + "|40005|" + dosDecimales(rs.getString("bruto")) + "\r\n";
          } 
        } else if (rs.getDouble("neto") != 0.0D) {
          res = rs.getString("cuenta") + fecha.substring(0, 4) + fecha.substring(5, 7) + fecha.substring(8, 10) + "|" + fecha.substring(0, 10) + "|";
          res = res + "9999999|" + rs.getString("cuenta") + "|GLB||||" + fecha.substring(0, 10) + "|" + rs.getString("cambio") + "||2|STARBUCKS CARD|";
          res = res + dosDecimales(rs.getString("neto")) + "|" + dosDecimales(rs.getString("iva")) + "|20670|" + dosDecimales(rs.getString("bruto")) + "\r\n";
        } 
        if (!res.equals(""))
          datos.addElement(res); 
        fechaant = fecha;
      } 
      if (datos.size() > 0) {
        generarArchivoSCI(fecha, datos, esarg, eschile, escol, true, ftp);
        System.out.println("Generando SCI Medios fecha: " + fecha);
        GenerarSCIMedios(esarg, eschile, escol, fechasql, ftp);
      } 
      System.out.println("Cant SCI=" + cant);
      if (rs != null)
        rs.close(); 
      if (stmt != null)
        stmt.close(); 
      if (conn != null)
        conn.close(); 
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
      e.printStackTrace();
    } 
  }
  
  public void GenerarSCINeo(boolean esarg, boolean eschile, boolean escol, String fechasql, jFtpZohan ftp) {
    Connection conn = null;
    Vector<String> datos = new Vector(500, 250);
    this.base = "Poleo5AR";
    if (eschile)
      this.base = "PoleoCL"; 
    try {
      Statement stmt = null;
      ResultSet rs = null;
      String query = "select r.id,r.FECHA,round(r.TOTALVENTABRUTA_CANTIDAD,2) as bruto,round(r.TOTALVENTANETA_CANTIDAD,2) as neto,t.PORCENTAJEIVA as iva,t.id as tienda,t.IDSCI as cuenta,r.TASADECAMBIODEDOLARESAMONEDALOCAL_CANTIDAD as cambio,1 as tiporeg ";
      query = query + " from poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id inner join poleo5ar.dbo.sci_envios e on t.id = tienda where";
      query = query + " r.fecha='" + fechasql + "' and e.ult_envio< r.fecha ";
      if (!eschile) {
        query = query + " UNION ALL SELECT r.id,r.FECHA,round((p.montoActivacionYRecarga_CANTIDAD-act.totalDevolucionesConTarjeta_CANTIDAD-act.totalDevolucionesEnEfectivo_CANTIDAD),2) bruto,round((p.montoActivacionYRecarga_CANTIDAD-act.totalDevolucionesConTarjeta_CANTIDAD-act.totalDevolucionesEnEfectivo_CANTIDAD),2) neto,0,t.id as tienda,t.IDSCI as cuenta,r.TASADECAMBIODEDOLARESAMONEDALOCAL_CANTIDAD as cambio,2 as tiporeg";
        query = query + " from poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id inner join poleo5ar.dbo.sci_envios e on t.id = tienda ";
        query = query + " inner join poleo5ar.dbo.NEOPOL_RESUMEN_DE_VENTAS_DEL_POS P on P.ID=r.resumenDeVentasDelPos_ID ";
        query = query + " inner join poleo5ar.dbo.NEOPOL_RESUMEN_ACTIVACION_Y_RECARGA_MSR act on R.resumenActivacionYRecargaMSR_ID=act.ID where ";
        query = query + " r.fecha='" + fechasql + "' and e.ult_envio< r.fecha ";
        query = query + " and round((p.montoActivacionYRecarga_CANTIDAD-act.totalDevolucionesConTarjeta_CANTIDAD-act.totalDevolucionesEnEfectivo_CANTIDAD),2)<>0";
      } 
      query = query + " order by r.id,r.fecha";
      if (eschile) {
        conn = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
      } else {
        conn = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
      } 
      stmt = conn.createStatement();
      System.out.println(query);
      rs = stmt.executeQuery(query);
      String fecha = "";
      String codtienda = "";
      int cant = 0;
      String tienda = "";
      String fechaant = "";
      boolean primero = true;
      int contadorMsr = 0;
      while (rs.next()) {
        cant++;
        fecha = rs.getString("fecha");
        String tiendaaux = rs.getString("tienda");
        String cuenta = rs.getString("cuenta");
        if (cuenta.length() == 4)
        {
        	cuenta = "0" + cuenta;
        }
        if (primero) {
          tienda = tiendaaux;
          fechaant = fecha;
        } 
        if (tienda.equals(tiendaaux))
          primero = false; 
        String res = "";
        if (rs.getInt("tiporeg") == 1) {
          if (rs.getDouble("bruto") != 0.0D) {
            res = cuenta + fecha.substring(0, 4) + fecha.substring(5, 7) + fecha.substring(8, 10) + "|" + fecha.substring(0, 10) + "|";
            if (cuenta.equals("40029") || cuenta.equals("40065") || cuenta.equals("40131")) {
              res = res + "9999999|" + cuenta + "|GLB||||" + fecha.substring(0, 10) + "|" + rs.getString("cambio") + "||1|VENTAS DIARIAS 0%|";
            } else {
              res = res + "9999999|" + cuenta + "|GLB||||" + fecha.substring(0, 10) + "|" + rs.getString("cambio") + "||1|VENTAS DIARIAS|";
            } 
            res = res + dosDecimales(rs.getString("neto")) + "|" + dosDecimales(rs.getString("iva")) + "|40005|" + dosDecimales(rs.getString("bruto")) + "\r\n";
          } 
        } else if (rs.getDouble("neto") != 0.0D) {
          res = cuenta + fecha.substring(0, 4) + fecha.substring(5, 7) + fecha.substring(8, 10) + "|" + fecha.substring(0, 10) + "|";
          res = res + "9999999|" + cuenta + "|GLB||||" + fecha.substring(0, 10) + "|" + rs.getString("cambio") + "||2|STARBUCKS CARD|";
          res = res + dosDecimales(rs.getString("neto")) + "|" + dosDecimales(rs.getString("iva")) + "|20670|" + dosDecimales(rs.getString("bruto")) + "\r\n";
        } 
        if (!res.equals("")) {
          datos.addElement(res); 
       
        
        
        
        
        
      
      
      
      
//agregar datos de REcarga Online y recarga automatica desde msr.-----------------------------------------
      if (contadorMsr == 0) {
      Connection conn2 = null;
      conn2 = DriverManager.getConnection("jdbc:sqlserver://192.168.245.2;databaseName=" + "Msr" + ";user=web;password=w3b4ls34r3w4rd52021");
      String queryMSR = "SELECT * FROM MSR_OPERACIONES where tipodeoperacion in ('Recarga_Online','Auto_Recarga')  and instanteEnElQueOcurrio like '%"+fechasql+"%'\r\n"
      		+ "";

      Statement stmtMSR = conn2.createStatement();
      ResultSet rsMSR = null;
      rsMSR = stmtMSR.executeQuery(queryMSR);
      Double MontoMsrTotalBruto = 0.0;
      String resMsr = "";
     
      while (rsMSR.next()) { 
    	  contadorMsr ++;
          Double MontoMSRAux = rsMSR.getDouble("total");
          MontoMsrTotalBruto = MontoMsrTotalBruto + MontoMSRAux;   
      
      }
      Double MontoIva = MontoMsrTotalBruto * 0.21;
      Double MontoMsrTotalNeto = MontoMsrTotalBruto  - MontoIva;
      System.out.println(fechasql);
      resMsr = "60049" + fechasql.substring(0, 4) + fechasql.substring(5, 7) +fechasql.substring(8, 10) + "|" + fechasql + "|";
      resMsr = resMsr + "9999999|" + "60049" + "|GLB||||" +fechasql + "|" + "90.0" + "||3|RECARGAS ONLINE|";
      resMsr = resMsr + dosDecimales(MontoMsrTotalNeto.toString())+ "|" + "0.00" + "|20670|" + dosDecimales(MontoMsrTotalNeto.toString()) + "\r\n";
      if (!resMsr.equals(""))
          datos.addElement(resMsr); 
      
        }
     
        }
      //------------------------------------------------------------------  Acivar nuevamente el update despues de la prueba
      fechaant = fecha;
      
    } 
      if (datos.size() > 0) {
        generarArchivoSCI(fecha , datos, esarg, eschile, escol, true, ftp);
        System.out.println("Generando SCI Medios fecha sql: " + fechasql);
       GenerarSCIMediosNeo(esarg, eschile, escol, fechasql, ftp);
      } 
      System.out.println("Cant SCI=" + cant);
      if (rs != null)
        rs.close(); 
      if (stmt != null)
        stmt.close(); 
      if (conn != null)
        conn.close(); 
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
      e.printStackTrace();
    } 
  }
  
  public void GenerarSCIMedios(boolean esarg, boolean eschile, boolean escol, String fechasql, jFtpZohan ftp) {
    Connection conn = null;
    Connection conn2 = null;
    Vector<String> datos = new Vector(500, 250);
    this.base = "PoleoAR";
    String moneda = "ARS";
    if (eschile) {
      this.base = "PoleoCL";
      moneda = "CLP";
    } 
    try {
      Statement stmt = null;
      Statement stmt2 = null;
      ResultSet rs = null;
      String query = "select tienda,fecha,sci_id,medio_sci,marca, divisa, cambio,round(sum(total_gross_amount),2) monto from (";
      query = query + " SELECT F.id tienda,[businessDay] fecha,sci_id,medio_sci,case when F.brand='BURGERKING' then 'BK'  when F.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(D.total_gross_amount,2) total_gross_amount ";
      query = query + " FROM sales_report A,sr_sales_by_creditcard_online B, ";
      query = query + " sales_by_payment_type D, ";
      query = query + " payment_type E,store F,sci_MediosPago G,sci_envios H ";
      query = query + " where B.sales_report_id=A.id ";
      query = query + " and A.store_id=H.tienda ";
      query = query + " and B.sales_by_payment_type_id=D.id  ";
      query = query + " and D.payment_type_id=E.id  ";
      query = query + " and F.id=A.store_id  ";
      query = query + " and E.name=G.medio_poleo  ";
      query = query + " and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE'  ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0  ";
      if (eschile) {
        query = query + "  union all ";
        query = query + " SELECT F.id tienda,[businessDay] fecha,sci_id,medio_sci,case when F.brand='BURGERKING' then 'BK'  when F.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(D.total_gross_amount,2) total_gross_amount ";
        query = query + " FROM sales_report A,sr_sales_by_tickets C,sales_by_payment_type D,payment_type E,store F,sci_MediosPago G,sci_envios H ";
        query = query + " where C.sales_report_id=A.id and A.store_id=H.tienda and C.sales_by_ticket_id=D.id and D.payment_type_id=E.id ";
        query = query + " and F.id=A.store_id and E.name=G.medio_poleo and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
        query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      } 
      query = query + "  union all ";
      query = query + " SELECT F.id tienda,[businessDay] fecha,sci_id,'DIFERENCIA DE CAJA' medio_sci,case when F.brand='BURGERKING' then 'BK'  when F.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(over_or_short*-1,2) total_gross_amount ";
      query = query + " FROM sales_report A,store F,sci_envios H ";
      query = query + " where A.store_id=H.tienda ";
      query = query + " and F.id=A.store_id  and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      query = query + "  union all ";
      query = query + " SELECT F.id tienda,[businessDay] fecha,sci_id,'VALES A RENDIR' medio_sci,case when F.brand='BURGERKING' then 'BK'  when F.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(total_exp_vouchers,2) total_gross_amount ";
      query = query + " FROM sales_report A,store F,sci_envios H ";
      query = query + " where A.store_id=H.tienda ";
      query = query + " and F.id=A.store_id  and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      query = query + " union all  ";
      query = query + " SELECT F.id tienda,[businessDay] fecha,sci_id,medio_sci,case when F.brand='BURGERKING' then 'BK'  when F.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(D.total_gross_amount,2) total_gross_amount ";
      query = query + "  FROM sales_report A, ";
      query = query + " sr_sales_by_creditcard_offline C, ";
      query = query + " sales_by_payment_type D, ";
      query = query + " \tpayment_type E,store F,sci_MediosPago G,sci_envios H ";
      query = query + " where  ";
      query = query + " C.sales_report_id=A.id ";
      query = query + " and A.store_id=H.tienda ";
      query = query + " and C.sales_by_payment_type_id=D.id ";
      query = query + " and D.payment_type_id=E.id ";
      query = query + " and F.id=A.store_id ";
      query = query + " and E.name=G.medio_poleo ";
      query = query + " and datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      query = query + " union all ";
      query = query + " SELECT D.id tienda,businessDay fecha,sci_id,'DOLARES' medio_sci,case when D.brand='BURGERKING' then 'BK'  when D.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'USD' divisa,exchange_rate cambio,round(amount*exchange_rate,2) total_gross_amount  ";
      query = query + "        FROM [sales_report] A,[SR_dollar_deposits] B,[deposit] C,store D ,sci_envios H ";
      query = query + "        where   datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and A.store_id=H.tienda\t\t ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      query = query + "        and A.store_id=D.id and A.id=B.sales_report_id and B.dollar_deposit_id=C.id  ";
      query = query + " union all ";
      if (eschile) {
        query = query + " sELECT D.id tienda,businessDay fecha,sci_id,'EFECTIVO' medio_sci,case when D.brand='BURGERKING' then 'BK'  when D.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(sum(total_deposits),2) total_gross_amount ";
      } else {
        query = query + " sELECT D.id tienda,businessDay fecha,sci_id,'EFECTIVO' medio_sci,case when D.brand='BURGERKING' then 'BK'  when D.brand='STARBUCKS' then 'SBX'  else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(sum(total_deposits),2) total_gross_amount ";
      } 
      query = query + "        FROM [sales_report] A,store D ,sci_envios H ";
      query = query + "        where   datediff(day,businessDay,CONVERT(DATETIME, '" + fechasql + "',20))=0 and owner='STORE' ";
      query = query + " and A.store_id=H.tienda\t ";
      query = query + " and datediff(day,CONVERT(DATETIME, '" + fechasql + "',20),ult_envio)<0 ";
      query = query + "         and A.store_id=D.id  group by D.id,businessDay,sci_id,D.brand ";
      query = query + " ) as q1 group by tienda,fecha,sci_id,medio_sci,marca,divisa,cambio having sum(total_gross_amount)<>0 order by tienda,sci_id,medio_sci ";
      String queryupdate = "update sci_envios set ult_envio=";
      if (eschile) {
        conn = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
        conn2 = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
      } else {
        conn = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
        conn2 = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
      } 
      stmt = conn.createStatement();
      stmt2 = conn2.createStatement();
      System.out.println("\r\n\r\n" + query + "\r\n\r\n");
      rs = stmt.executeQuery(query);
      String fecha = "";
      String codtienda = "";
      int cant = 0;
      String tienda = "";
      String fechaant = "";
      boolean primero = true;
      String tiendaaux = "";
      double dolares = 0.0D;
      double monto = 0.0D;
      while (rs.next()) {
        cant++;
        fecha = rs.getString("fecha");
        tiendaaux = rs.getString("tienda");
        if (primero) {
          tienda = tiendaaux;
          fechaant = fecha;
          primero = false;
        } 
        if (!tienda.equals(tiendaaux)) {
          System.out.println(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tienda);
          if (this.esFechaSql) {
            stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tienda);
          } else {
            stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tienda);
          } 
          tienda = tiendaaux;
          dolares = 0.0D;
        } 
        monto = rs.getDouble("monto");
        if (rs.getString("medio_sci").equals("DOLARES")) {
          dolares = rs.getDouble("monto");
        } else if (rs.getString("medio_sci").equals("EFECTIVO")) {
          monto -= dolares;
        } 
        String res = fecha.substring(0, 10) + "|" + rs.getString("sci_id") + "|" + rs.getString("medio_sci") + "|" + rs.getString("marca");
        res = res + "|" + rs.getString("divisa") + "|" + rs.getString("cambio") + "|" + dosDecimales(monto + "") + "\r\n";
        System.out.println(res);
        datos.addElement(res);
        fechaant = fecha;
      } 
      if (cant > 0) {
        if (this.esFechaSql) {
          stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tiendaaux);
        } else {
          stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tiendaaux);
        } 
        generarArchivoSCI(fecha, datos, esarg, eschile, escol, false, ftp);
      } 
      System.out.println("Cant SCI Medios=" + cant);
      if (rs != null)
        rs.close(); 
      if (stmt != null)
        stmt.close(); 
      if (conn != null)
        conn.close(); 
      if (stmt2 != null)
        stmt2.close(); 
      if (conn2 != null)
        conn2.close(); 
    } catch (Exception e) {
      System.out.println("\r\n***********************");
      System.out.println(e.toString());
      e.printStackTrace();
      System.out.println("\r\n***********************");
    } 
  }
  
  public void GenerarSCIMediosNeo(boolean esarg, boolean eschile, boolean escol, String fechasql, jFtpZohan ftp) {
    Connection conn = null;
    Connection conn2 = null;
    Vector<String> datos = new Vector(500, 250);
    this.base = "Poleo5AR";
    String moneda = "ARS";
    if (eschile) {
      this.base = "PoleoCL";
      moneda = "CLP";
    } 
    try {
      Statement stmt = null;
      Statement stmt2 = null;
      ResultSet rs = null;
      String query = " select tienda,fecha,sci_id,medio_sci,marca, divisa, cambio,round(sum(total_gross_amount),2) monto from ( ";
      query = query + " SELECT t.id tienda,r.fecha fecha,t.idsci sci_id,smp.medio_sci medio_sci,case when upper(m.nombre)='BURGER KING' then 'BK'  when upper(m.nombre)='STARBUCKS URUGUAY' then 'SBX' when upper(m.nombre)='STARBUCKS' then 'SBX' when upper(m.nombre)='DOMINOS URUGUAY' then 'DPZ' else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(sum(isnull(vt.TOTALSEGUNRESPONSABLE_CANTIDAD, vt.totalSegunPos_CANTIDAD)),2)  total_gross_amount ";
      query = query + " FROM poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_RESUMEN_DE_VENTAS_CON_TARJETA vt on r.id = vt.REPORTEDEVENTAS_ID ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MEDIOS_DE_PAGO mp on vt.MEDIODEPAGO_ID = mp.id ";
      query = query + " inner join poleo5ar.dbo.sci_mediosPago smp on mp.nombre = smp.medio_poleo ";
      query = query + " inner join poleo5ar.dbo.sci_envios e on t.id = e.tienda ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MARCAS m on t.MARCA_ID = m.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_PAISES p on m.PAIS_ID = p.id ";
      query = query + " where r.fecha = '" + fechasql + "'  and r.fecha >  e.ult_envio ";
      query = query + " group by t.id,r.fecha,t.IDSCI,smp.medio_sci,m.NOMBRE ";
      query = query + "  union all  ";
      query = query + " SELECT t.id tienda,r.fecha fecha,t.idsci sci_id,smp.medio_sci medio_sci,case when upper(m.nombre)=''BURGER KING' then 'BK'  when upper(m.nombre)='STARBUCKS URUGUAY' then 'SBX' when upper(m.nombre)='STARBUCKS' then 'SBX' when upper(m.nombre)='DOMINOS URUGUAY' then 'DPZ' else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round(sum(vt.TOTAL_CANTIDAD),2)  total_gross_amount ";
      query = query + " FROM poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_RESUMEN_CASHBACK vt on r.id = vt.REPORTEDEVENTAS_ID ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MEDIOS_DE_PAGO mp on vt.MEDIODEPAGO_ID = mp.id ";
      query = query + " inner join poleo5ar.dbo.sci_mediosPago smp on mp.nombre = smp.medio_poleo ";
      query = query + " inner join poleo5ar.dbo.sci_envios e on t.id = e.tienda ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MARCAS m on t.MARCA_ID = m.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_PAISES p on m.PAIS_ID = p.id ";
      query = query + " where r.fecha = '" + fechasql + "'  and r.fecha >  e.ult_envio ";
      query = query + " group by t.id,r.fecha,t.IDSCI,smp.medio_sci,m.NOMBRE ";
      query = query + "  union all  ";
      query = query + " SELECT t.id tienda,r.fecha fecha,t.idsci sci_id,'DIFERENCIA DE CAJA' medio_sci,case when upper(m.nombre)='BURGER KING' then 'BK'  when upper(m.nombre)='STARBUCKS URUGUAY' then 'SBX' when upper(m.nombre)='STARBUCKS' then 'SBX' when upper(m.nombre)='DOMINOS URUGUAY' then 'DPZ' else 'PFC' end marca ,'" + moneda + "' divisa,'1' cambio,round([OVERORSHORT_CANTIDAD]*-1,2) total_gross_amount  ";
      query = query + " FROM poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id ";
      query = query + " inner join poleo5ar.dbo.sci_envios e on t.id = e.tienda ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MARCAS m on t.MARCA_ID = m.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_PAISES p on m.PAIS_ID = p.id ";
      query = query + " where r.fecha = '" + fechasql + "'  and r.fecha >  e.ult_envio ";
      query = query + " UNION ALL ";
      query = query + " SELECT t.id tienda,r.fecha fecha,t.idsci sci_id,'VALES A RENDIR' medio_sci,case when upper(m.nombre)='BURGER KING' then 'BK'  when upper(m.nombre)='STARBUCKS URUGUAY' then 'SBX' when upper(m.nombre)='STARBUCKS' then 'SBX' when upper(m.nombre)='DOMINOS URUGUAY' then 'DPZ' else 'PFC' end marca ,'" + moneda + "' divisa,1 cambio,round(sum(i.MONTO_CANTIDAD),2) total_gross_amount  ";
      query = query + " FROM poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id ";
      query = query + " inner join poleo5ar.dbo.sci_envios e on t.id = e.tienda ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MARCAS m on t.MARCA_ID = m.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_I_EXPENSES i on r.ID=i.REPORTEDEVENTAS_ID ";
      query = query + " where  r.fecha = '" + fechasql + "' and r.fecha >  e.ult_envio ";
      query = query + " group by t.id,r.fecha,t.idsci,m.nombre ";
      query = query + " union all  ";
      query = query + " SELECT t.id tienda,r.fecha fecha,t.idsci sci_id,'DOLARES' medio_sci,case when upper(m.nombre)='BURGER KING' then 'BK'  when upper(m.nombre)='STARBUCKS URUGUAY' then 'SBX' when upper(m.nombre)='STARBUCKS' then 'SBX' when upper(m.nombre)='DOMINOS URUGUAY' then 'DPZ' else 'PFC' end marca ,'USD' divisa,r.TASADECAMBIODEDOLARESAMONEDALOCAL_CANTIDAD cambio,round(sum(r.TASADECAMBIODEDOLARESAMONEDALOCAL_CANTIDAD*d.IMPORTE_CANTIDAD),2) total_gross_amount  ";
      query = query + " FROM poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id ";
      query = query + " inner join poleo5ar.dbo.sci_envios e on t.id = e.tienda ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MARCAS m on t.MARCA_ID = m.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_DEPOSITOS d on r.ID=d.REPORTEDEVENTAS_ID ";
      query = query + " where d.esparacambio=0 and d.IMPORTE_MONEDA='USD' and r.fecha = '" + fechasql + "'  and r.fecha >  e.ult_envio ";
      query = query + " group by t.id,r.fecha,t.idsci,m.nombre,r.TASADECAMBIODEDOLARESAMONEDALOCAL_CANTIDAD ";
      query = query + "  union all  ";
      query = query + "  SELECT t.id tienda,r.fecha fecha,t.idsci sci_id,'EFECTIVO' medio_sci,case when upper(m.nombre)='BURGER KING' then 'BK'  when upper(m.nombre)='STARBUCKS URUGUAY' then 'SBX' when upper(m.nombre)='STARBUCKS' then 'SBX' when upper(m.nombre)='DOMINOS URUGUAY' then 'DPZ' else 'PFC' end marca ,'" + moneda + "' divisa,1 cambio,round(sum(d.IMPORTE_CANTIDAD),2) total_gross_amount  ";
      query = query + " FROM poleo5ar.dbo.NEOPOL_REPORTES_DE_VENTA r ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_TIENDAS t on r.TIENDA_ID = t.id ";
      query = query + " inner join poleo5ar.dbo.sci_envios e on t.id = e.tienda ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_MARCAS m on t.MARCA_ID = m.id ";
      query = query + " inner join poleo5ar.dbo.NEOPOL_DEPOSITOS d on r.ID=d.REPORTEDEVENTAS_ID ";
      query = query + " where d.esparacambio=0 and (d.IMPORTE_MONEDA='ARS' or d.IMPORTE_MONEDA='UYU')  ";
      query = query + " and r.fecha = '" + fechasql + "'  and r.fecha >  e.ult_envio ";
      query = query + " group by t.id,r.fecha,t.idsci,m.nombre ";
      query = query + " ) as q1 group by tienda,fecha,sci_id,medio_sci,marca,divisa,cambio having sum(total_gross_amount)<>0 order by tienda,sci_id,medio_sci  ";
      String queryupdate = "update sci_envios set ult_envio=";
      if (eschile) {
        conn = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
        conn2 = DriverManager.getConnection("jdbc:sqlserver://192.168.198.197;databaseName=" + this.base + ";user=poleooracle;password=poleooracle");
      } else {
        conn = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
        conn2 = DriverManager.getConnection("jdbc:sqlserver://172.31.1.14;databaseName=" + this.base + ";user=web;password=w3b4ls34");
      } 
      stmt = conn.createStatement();
      stmt2 = conn2.createStatement();
      System.out.println("\r\n\r\n" + query + "\r\n\r\n");
      rs = stmt.executeQuery(query);
      String fecha = "";
      String codtienda = "";
      int cant = 0;
      String tienda = "";
      String fechaant = "";
      boolean primero = true;
      String tiendaaux = "";
      double dolares = 0.0D;
      double monto = 0.0D;
      while (rs.next()) {
        cant++;
        fecha = rs.getString("fecha");
        tiendaaux = rs.getString("tienda");
        if (primero) {
          tienda = tiendaaux;
          fechaant = fecha;
          primero = false;
        } 
        if (!tienda.equals(tiendaaux)) {
          System.out.println(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tienda);
          if (this.esFechaSql) {
            stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tienda);
          } else {
            stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tienda);
          } 
          tienda = tiendaaux;
          dolares = 0.0D;
        } 
        String sci_id = rs.getString("sci_id");
        if (sci_id.length() == 4)
        {
        	sci_id = "0" + sci_id;
        }
        monto = rs.getDouble("monto");
        if (rs.getString("medio_sci").equals("DOLARES"))
          dolares = rs.getDouble("monto"); 
        String res = fecha.substring(0, 10) + "|" + sci_id + "|" + rs.getString("medio_sci") + "|" + rs.getString("marca");
        res = res + "|" + rs.getString("divisa") + "|" + rs.getString("cambio") + "|" + dosDecimales(monto + "") + "\r\n";
        System.out.println(res);
        datos.addElement(res);
        fechaant = fecha;
      } 
      if (cant > 0) {
        if (this.esFechaSql) {
          stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tiendaaux);
        } else {
          stmt2.executeUpdate(queryupdate + "CONVERT(DATETIME, '" + fechaant + "',20) where tienda=" + tiendaaux);
        } 
        generarArchivoSCI(fecha, datos, esarg, eschile, escol, false, ftp);
      } 
      System.out.println("Cant SCI Medios=" + cant);
      if (rs != null)
        rs.close(); 
      if (stmt != null)
        stmt.close(); 
      if (conn != null)
        conn.close(); 
      if (stmt2 != null)
        stmt2.close(); 
      if (conn2 != null)
        conn2.close(); 
    } catch (Exception e) {
      System.out.println("\r\n***********************");
      System.out.println(e.toString());
      e.printStackTrace();
      System.out.println("\r\n***********************");
    } 
  }
  
  void generarArchivoSCI(String fecha, Vector datos, boolean esarg, boolean eschile, boolean escol, boolean vtadiaria, jFtpZohan ftp) {
    String nombrearch = nombreArchivo(fecha, esarg, eschile, escol, vtadiaria);
    nombrearch = grabarArchivo(nombrearch, datos);
    //ftp.enviarArchivo("/interface/i_alse/PALS1I/incoming/VentaDiaria/", nombrearch, "ftpprodalse.oracleoutsourcing.com", "i_alse");
    if (esarg) {
    ftp.enviarArchivo("/poleo/AR/", nombrearch, "150.136.204.188", "poleo");
    }
    
  }
  
  String grabarArchivo(String nombre, Vector<String> datos) {
    String path = "E:\\Grupos\\Usuarios\\SCI-Oracle\\" + nombre;
    System.out.println(path);
    File file = new File(path);
    try {
      FileOutputStream out = new FileOutputStream(file);
      BufferedOutputStream os = new BufferedOutputStream(out, 1000);
      DataOutputStream data = new DataOutputStream(os);
      for (int i = 0; i < datos.size(); i++)
        data.write(((String)datos.elementAt(i)).getBytes()); 
      data.flush();
      os.flush();
      out.flush();
      data.close();
      os.close();
      out.close();
    } catch (IOException ioe) {
      System.out.println(ioe);
    } 
    return path;
  }
  
  String nombreArchivo(String fecha, boolean esarg, boolean eschile, boolean escol, boolean vtadiaria) {
    String nombre = "ALSE_CIVD_ARG";
    if (vtadiaria) {
      if (eschile) {
        nombre = "ALSE_CIVD_CHI";
      } else if (escol) {
        nombre = "ALSE_CIVD_COL";
      } 
    } else if (esarg) {
      nombre = "ALSE_CIFP_ARG";
    } else if (eschile) {
      nombre = "ALSE_CIFP_CHI";
    } else if (escol) {
      nombre = "ALSE_CIFP_COL";
    } 
    Calendar ahora = Calendar.getInstance();
    int mes = ahora.get(2) + 1;
    int dia = ahora.get(5);
    int hour = ahora.get(11);
    int min = ahora.get(12);
    int sec = ahora.get(13);
    int msec = ahora.get(14);
    System.out.println("Fecha del archivo: |"+fecha);
    nombre = nombre + fecha.substring(0, 4) + fecha.substring(5, 7) + fecha.substring(8, 10) + Dos0(hour) + Dos0(min) + Dos0(sec) + msec + ".txt"; //cambiar nuevamente
    return nombre;
  }
  
  String Dos0(int aux) {
    String dos = aux + "";
    if (dos.length() < 2)
      dos = "0" + dos; 
    return dos;
  }
  
  String dosDecimales(String texto) {
    String sacar = "";
    if (texto != null && texto.length() > 0) {
      int punto = texto.indexOf(".");
      if (punto >= 0 && texto.length() >= punto + 3) {
        sacar = texto.substring(0, punto + 3);
      } else if (punto >= 0 && texto.length() == punto + 2) {
        sacar = texto + "0";
      } else {
        sacar = texto + ".00";
      } 
    } 
    return sacar;
  }
  
  public String reemplazarTexto(String original, String abuscar, String reemplazo) {
    String nuevo = "";
    int adonde = original.indexOf(abuscar);
    if (adonde < 0)
      nuevo = original; 
    while (adonde >= 0) {
      if (adonde > 0)
        nuevo = original.substring(0, adonde); 
      nuevo = nuevo + reemplazo;
      nuevo = nuevo + original.substring(adonde + abuscar.length(), original.length());
      original = nuevo;
      adonde = original.indexOf(abuscar, adonde + reemplazo.length());
    } 
    return nuevo;
  }
  
  String fechaAsql(String aux) {
    aux = aux.substring(0, 5) + aux.substring(8, 10) + aux.substring(4, 7);
    return aux;
  }
  
  String darDiaSig(String aux, boolean msql) {
    String anoa = aux.substring(0, 4);
    String diaa = aux.substring(8, 10);
    String mesa = aux.substring(5, 7);
    Calendar ahora = Calendar.getInstance();
    ahora.set(5, Strtoint(diaa));
    ahora.set(2, Strtoint(mesa) - 1);
    ahora.set(1, Strtoint(anoa));
    ahora.add(5, 1);
    int mes = ahora.get(2) + 1;
    int dia = ahora.get(5);
    int year = ahora.get(1);
    String fecha = year + "-" + Dos0(mes) + "-" + Dos0(dia);
    return fecha;
  }
  
  String darAyer(boolean reporte, boolean msql) {
    Calendar ahora = Calendar.getInstance();
    ahora.add(5, -1);
    int mes = ahora.get(2) + 1;
    int dia = ahora.get(5);
    int year = ahora.get(1);
    String fecha = year + "-" + Dos0(mes) + "-" + Dos0(dia);
    if (reporte)
      fecha = year + "-" + Dos0(mes) + "-" + Dos0(dia); 
    System.out.println("Fecha=" + fecha);
    return fecha;
  }
  
  int Strtoint(String aux) {
    int n = 0;
    try {
      n = Integer.parseInt(aux);
    } catch (Exception e) {}
    return n;
  }
  
  String darHoy(boolean msql) {
    Calendar ahora = Calendar.getInstance();
    int mes = ahora.get(2) + 1;
    int dia = ahora.get(5);
    int year = ahora.get(1);
    String fecha = year + "-" + Dos0(mes) + "-" + Dos0(dia);
    System.out.println("Fecha=" + fecha);
    return fecha;
  }
  
  String darHoyHora() {
    Calendar ahora = Calendar.getInstance();
    int mes = ahora.get(2) + 1;
    int dia = ahora.get(5);
    int year = ahora.get(1);
    int hora = ahora.get(10);
    String fecha = year + "-" + mes + "-" + dia + " " + hora;
    System.out.println("Fecha Hora=" + fecha);
    return fecha;
  }
}
