/*
 * Document: Reporte.java
 * Created on: 12-12-2016  19:02:10
 * By: Facu
 */
package org.ccpm.reporte;

import java.sql.Connection;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import net.sf.jasperreports.engine.util.JRLoader;

/**
 *
 *
 */


public class Reporte {

    private JasperPrint masterPrint = null;
    private static JasperReport masterReport = null;
    private static final Map<String, Object> parametros = new HashMap<String, Object>();
    private String imgLogo = "/org/ccpm/reporte/tableroLogo.png";

    private InitialContext context;
    private DataSource dataSource;
    private Connection conexion;

    public Reporte() {
    }

    public InitialContext getContext() throws NamingException {
        context = new InitialContext();
        return context;
    }

    public DataSource getDataSource() throws Exception {
        try {
            dataSource = (DataSource) context.lookup("jdbc/tablero");
        } catch (NamingException e) {
            throw new Exception(e.getMessage());
        }
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConexion() throws Exception {
        conexion = dataSource.getConnection();
        return conexion;
    }

    public void setConexion(Connection conexion) {
        this.conexion = conexion;
    }

    public void setContext(InitialContext context) {
        this.context = context;
    }

    public JasperPrint impObjetivos() throws Exception, JRException {
        try {
            this.getContext();
            this.getDataSource();
            this.getConexion();
            parametros.put("imagen", imgLogo);
            masterReport = this.loadOrCompileReport("Objetivos");
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (JRException e) {
            throw new Exception(e.getMessage());
        } finally {
            if (conexion != null) {
                conexion.close();
            }
        }
        return masterPrint;
    }
    public JasperPrint impMetas() throws Exception, JRException {
        try {
            this.getContext();
            this.getDataSource();
            this.getConexion();
            parametros.put("imagen", imgLogo);
            masterReport = this.loadOrCompileReport("Metas");
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (JRException e) {
            throw new Exception(e.getMessage());
        } finally {
            if (conexion != null) {
                conexion.close();
            }
        }
        return masterPrint;
    }

    private JasperReport loadOrCompileReport(String reportBaseName) throws Exception {
        String jasperFile = reportBaseName + ".jasper";
        java.net.URL jasperUrl = this.getClass().getResource(jasperFile);
        if (jasperUrl != null) {
            return (JasperReport) JRLoader.loadObject(jasperUrl);
        }

        String jrxmlFile = reportBaseName + ".jrxml";
        InputStream jrxmlStream = this.getClass().getResourceAsStream(jrxmlFile);
        if (jrxmlStream == null) {
            throw new Exception("No se encontro el reporte: " + reportBaseName + " (.jasper/.jrxml)");
        }
        try {
            return JasperCompileManager.compileReport(jrxmlStream);
        } finally {
            jrxmlStream.close();
        }
    }

}
