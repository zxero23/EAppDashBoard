package org.ccpm.sysse.reporte;

import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public class ExpedienteReport {

    private JasperPrint masterPrint = null;
    private static URL url = null;
    private static JasperReport masterReport = null;
    private static Map parametros = new HashMap();
    private static Connection conexion;
    public Long anioActual = (long) Calendar.getInstance().get(Calendar.YEAR);

    public JasperPrint imprimirCaratula(Long codOrganismo,
            Long nroCorrelativo, int anio, String nombreOrganismo,
            String nombre, Integer cuerpo, String iniciador) throws Exception, JRException {
        try {
            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("jdbc/expediente");
            if (cuerpo == null) {
                cuerpo = 1;
            }
            conexion = dataSource.getConnection();
            url = this.getClass().getResource("/reportes/caratulaCuerpo.jasper");
            parametros.put("parameter1", codOrganismo);
            parametros.put("parameter2", nroCorrelativo);
            parametros.put("parameter3", anio);
            parametros.put("NombreOrganismo", nombreOrganismo);
            parametros.put("Organismo", nombreOrganismo);
            parametros.put("Organismo", nombre);
            parametros.put("cuerpo", cuerpo.toString());
            parametros.put("iniciador", iniciador);
            masterReport = (JasperReport) JRLoader.loadObject(url);
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        conexion.close();
        return masterPrint;
    }

    public JasperPrint imprimirListadoEnviado(Date fechaEnviado, Date horaEnvio,
            Long idMesaEnviado, String nombreReporte,
            String nombreOrganismo, String nombre, Long idMesa, String leyendaAnual) throws Exception, JRException {
        try {

            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("jdbc/expediente");
            conexion = dataSource.getConnection();
            url = this.getClass().getResource("/reportes/" + nombreReporte + ".jasper");
            if (horaEnvio != null) {
                fechaEnviado.setHours(horaEnvio.getHours());
                fechaEnviado.setMinutes(horaEnvio.getMinutes());
            }
            java.sql.Timestamp fec = new java.sql.Timestamp(fechaEnviado.getTime());
            parametros.put("fecha", fec);
            parametros.put("idMesa", idMesa);
            parametros.put("idMesaEnviado", idMesaEnviado);
            parametros.put("nombreOrganismo", nombreOrganismo);
            parametros.put("organismo", nombre);
            parametros.put("imagenHeader", Paths.get(getClass().getClassLoader().getResource("/reportes/logoProvincia.png").toURI()).toFile().getAbsolutePath());
            parametros.put("leyendaProvincia", leyendaAnual);
            masterReport = (JasperReport) JRLoader.loadObject(url);
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        conexion.close();
        return masterPrint;
    }

    public JasperPrint imprimirHistorial(Long codOrganismo,
            Long nroCorrelativo, int anio, String leyendaAnual) throws Exception, JRException {
        try {
            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("jdbc/expediente");
            conexion = dataSource.getConnection();
            url = this.getClass().getResource("/reportes/historial.jasper");
            parametros.put("cod", codOrganismo);
            parametros.put("correl", nroCorrelativo);
            parametros.put("anio", anio);
            parametros.put("imagenHeader", Paths.get(getClass().getClassLoader().getResource("/reportes/logoProvincia.png").toURI()).toFile().getAbsolutePath());
            parametros.put("leyendaProvincia", leyendaAnual);
            masterReport = (JasperReport) JRLoader.loadObject(url);
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        conexion.close();
        return masterPrint;
    }

    public JasperPrint imprimirReporte(
            String nombreReporte, String nombreOrganismoPadre, String nombreOrganismo, String mesa, String reporteEstado, String orgDest,
            int estado, Date fechaDesde, Date fechaHasta, Long origen, Long destino, String leyendaAnual, String mesaExt, Long codigo,Long idUsuario) throws Exception, JRException {
        try {
            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("jdbc/expediente");
            conexion = dataSource.getConnection();
            url = this.getClass().getResource("/reportes/" + nombreReporte + ".jasper");
            parametros.put("orgPadre", nombreOrganismoPadre);
            parametros.put("organismo", nombreOrganismo);
            parametros.put("mesa", mesa);
            parametros.put("estado", (long) estado);
            parametros.put("desde", fechaDesde);
            parametros.put("hasta", fechaHasta);
            parametros.put("origen", origen);
            parametros.put("destino", destino);
            parametros.put("reporteEstado", reporteEstado);
            parametros.put("orgDest", orgDest);
            parametros.put("imagenHeader", Paths.get(getClass().getClassLoader().getResource("/reportes/logoProvincia.png").toURI()).toFile().getAbsolutePath());
            parametros.put("leyendaProvincia", leyendaAnual);
            parametros.put("mesaExt", mesaExt);
            parametros.put("codigo", codigo);
            parametros.put("usuario", idUsuario);
            masterReport = (JasperReport) JRLoader.loadObject(url);
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        conexion.close();
        return masterPrint;
    }

    public JasperPrint imprimirReporteRecienEnviados(
            String nombreReporte, String nombreOrganismoPadre, String nombreOrganismo, String mesa,
            Date fechaDesde, Date fechaHasta, String leyendaAnual, String mesaExt, String listado) throws Exception, JRException {
        try {
            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup("jdbc/expediente");
            conexion = dataSource.getConnection();
            url = this.getClass().getResource("/reportes/" + nombreReporte + ".jasper");
            parametros.put("orgPadre", nombreOrganismoPadre);
            parametros.put("organismo", nombreOrganismo);
            parametros.put("desde", fechaDesde);
            parametros.put("imagenHeader",  Paths.get(getClass().getClassLoader().getResource("/reportes/logoProvincia.png").toURI()).toFile().getAbsolutePath());
            parametros.put("leyendaProvincia", leyendaAnual);
            parametros.put("hasta", fechaHasta);
            parametros.put("mesa", mesa);
            parametros.put("mesaExt", mesaExt);
            parametros.put("listado", listado);
            masterReport = (JasperReport) JRLoader.loadObject(url);
            masterPrint = JasperFillManager.fillReport(masterReport, parametros, conexion);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        conexion.close();
        return masterPrint;
    }
}
