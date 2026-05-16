package org.ccpm.sysse.utilidad;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;

public class UserActivityLogger {
    
    private static final String LOG_FILE_NAME = "log.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final Object LOCK = new Object();
    
    /**
     * Detecta si el sistema operativo es Windows
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }
    
    /**
     * Detecta si el sistema operativo es Linux o Unix
     */
    private static boolean isLinux() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("linux") || os.contains("unix") || os.contains("nix");
    }
    
    /**
     * Normaliza una ruta de archivo según el sistema operativo
     */
    private static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        
        // Decodificar URL encoding
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // Si falla, continuar con la ruta original
        }
        
        // Normalizar rutas de Windows
        if (isWindows()) {
            // En Windows, las URLs pueden tener formato file:/C:/ o file:///C:/
            // Necesitamos convertir a C:/
            if (path.startsWith("/") && path.length() > 2 && path.charAt(2) == ':') {
                path = path.substring(1); // Remover el / inicial
            }
            // También puede venir como file:/C:/ sin el tercer /
            if (path.startsWith("file:/") && path.length() > 7 && path.charAt(7) == ':') {
                path = path.substring(6); // Remover "file:"
            }
        }
        
        return path;
    }
    
    /**
     * Obtiene la ruta completa del archivo de log, ubicado al lado del JAR
     */
    private static String getLogFilePath() {
        // Si es Linux, usar /opt/logExp.txt
        if (isLinux()) {
            String logPath = "/opt/logExp.txt";
            System.out.println("[UserActivityLogger] Sistema operativo: Linux - Usando ruta fija: " + logPath);
            return logPath;
        }
        
        try {
            // Método 1: Intentar obtener desde el CodeSource (más confiable para JARs)
            try {
                java.net.URL location = UserActivityLogger.class.getProtectionDomain()
                        .getCodeSource().getLocation();
                
                if (location != null) {
                    String locationStr = location.toString();
                    String osInfo = isWindows() ? "Windows" : (isLinux() ? "Linux" : "Otro");
                    System.out.println("[UserActivityLogger] Sistema operativo detectado: " + osInfo);
                    System.out.println("[UserActivityLogger] Location URL: " + locationStr);
                    
                    // Manejar URLs tipo jar:file:/ruta/al/jar!/ruta/dentro/del/jar
                    if (locationStr.startsWith("jar:file:")) {
                        // Extraer la ruta del JAR desde jar:file:/ruta/al/jar!/...
                        int exclamationIndex = locationStr.indexOf('!');
                        if (exclamationIndex > 0) {
                            String jarPath = locationStr.substring(9, exclamationIndex); // 9 = "jar:file:".length()
                            jarPath = normalizePath(jarPath);
                            
                            File jarFile = new File(jarPath);
                            if (jarFile.isFile()) {
                                File parentDir = jarFile.getParentFile();
                                if (parentDir != null) {
                                    String logPath = new File(parentDir, LOG_FILE_NAME).getAbsolutePath();
                                    System.out.println("[UserActivityLogger] Log path (desde JAR jar: URL): " + logPath);
                                    return logPath;
                                }
                            }
                        }
                    } else {
                        // URL normal file://
                        File jarFile;
                        try {
                            jarFile = new File(location.toURI());
                        } catch (URISyntaxException e) {
                            // Si falla, intentar convertir directamente desde la URL
                            String path = location.getPath();
                            if (path != null) {
                                path = normalizePath(path);
                                jarFile = new File(path);
                            } else {
                                throw e;
                            }
                        }
                        
                        if (jarFile.isFile()) {
                            // Es un JAR, obtener el directorio padre
                            File parentDir = jarFile.getParentFile();
                            if (parentDir != null) {
                                String logPath = new File(parentDir, LOG_FILE_NAME).getAbsolutePath();
                                System.out.println("[UserActivityLogger] Log path (desde JAR): " + logPath);
                                return logPath;
                            }
                        } else if (jarFile.isDirectory()) {
                            // Es un directorio (desarrollo), usar ese directorio
                            String logPath = new File(jarFile, LOG_FILE_NAME).getAbsolutePath();
                            System.out.println("[UserActivityLogger] Log path (desarrollo): " + logPath);
                            return logPath;
                        }
                    }
                }
            } catch (URISyntaxException | SecurityException | NullPointerException e) {
                System.err.println("[UserActivityLogger] Error obteniendo ubicación desde CodeSource: " + e.getMessage());
            }
            
            // Método 2: Intentar obtener desde el classpath
            String classPath = System.getProperty("java.class.path");
            if (classPath != null && !classPath.isEmpty()) {
                String[] paths = classPath.split(File.pathSeparator);
                for (String path : paths) {
                    File file = new File(path);
                    if (file.isFile() && path.toLowerCase().endsWith(".jar")) {
                        File parentDir = file.getParentFile();
                        if (parentDir != null) {
                            String logPath = new File(parentDir, LOG_FILE_NAME).getAbsolutePath();
                            System.out.println("[UserActivityLogger] Log path (desde classpath): " + logPath);
                            return logPath;
                        }
                    }
                }
            }
            
            // Método 3: Fallback - usar el directorio de trabajo actual
            // Cuando ejecutas "java -jar archivo.jar", normalmente estás en el directorio donde está el JAR
            String logPath = new File(System.getProperty("user.dir"), LOG_FILE_NAME).getAbsolutePath();
            System.out.println("[UserActivityLogger] Log path (fallback user.dir): " + logPath);
            return logPath;
            
        } catch (Exception e) {
            // Último fallback: usar el directorio de trabajo actual
            String logPath = new File(System.getProperty("user.dir"), LOG_FILE_NAME).getAbsolutePath();
            System.err.println("[UserActivityLogger] Error, usando fallback: " + logPath);
            System.err.println("[UserActivityLogger] Excepción: " + e.getMessage());
            return logPath;
        }
    }
    
    /**
     * Registra una acción del usuario en el archivo log.txt
     * 
     * @param usuario Nombre del usuario (puede ser null si no está logueado)
     * @param accion Descripción de la acción realizada
     * @param organismo Nombre del organismo del usuario (puede ser null)
     * @param request Request HTTP para obtener información adicional (puede ser null)
     */
    public static void logActivity(String usuario, String accion, String organismo, HttpServletRequest request) {
        synchronized (LOCK) {
            try {
                String logPath = getLogFilePath();
                System.out.println("========================================");
                System.out.println("RUTA DEL ARCHIVO LOG.TXT: " + logPath);
                System.out.println("========================================");
                try (FileWriter fw = new FileWriter(logPath, true);
                     PrintWriter pw = new PrintWriter(fw)) {
                
                Date now = new Date();
                String fecha = new SimpleDateFormat("dd/MM/yyyy").format(now);
                String hora = new SimpleDateFormat("HH:mm:ss").format(now);
                
                String usuarioStr = (usuario != null && !usuario.isEmpty()) ? usuario : "NO_LOGGED_IN";
                String organismoStr = (organismo != null && !organismo.isEmpty()) ? organismo : "SIN_ORGANISMO";
                
                StringBuilder logEntry = new StringBuilder();
                logEntry.append("Fecha: ").append(fecha).append(" | ");
                logEntry.append("Hora: ").append(hora).append(" | ");
                logEntry.append("Usuario: ").append(usuarioStr).append(" | ");
                logEntry.append("Organismo: ").append(organismoStr).append(" | ");
                logEntry.append("Acción: ").append(accion);
                
                if (request != null) {
                    String method = request.getMethod();
                    String uri = request.getRequestURI();
                    String queryString = request.getQueryString();
                    String ipAddress = getClientIpAddress(request);
                    
                    logEntry.append(" | Método: ").append(method);
                    logEntry.append(" | URI: ").append(uri);
                    if (queryString != null && !queryString.isEmpty()) {
                        logEntry.append("?").append(queryString);
                    }
                    logEntry.append(" | IP: ").append(ipAddress);
                }
                
                pw.println(logEntry.toString());
                pw.flush();
                }
            } catch (IOException e) {
                System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Registra una acción del usuario con organismo pero sin request
     */
    public static void logActivity(String usuario, String accion, String organismo) {
        logActivity(usuario, accion, organismo, null);
    }
    
    /**
     * Registra una acción simple sin request ni organismo (para compatibilidad)
     */
    public static void logActivity(String usuario, String accion) {
        logActivity(usuario, accion, null, null);
    }
    
    /**
     * Obtiene la dirección IP real del cliente
     */
    private static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

