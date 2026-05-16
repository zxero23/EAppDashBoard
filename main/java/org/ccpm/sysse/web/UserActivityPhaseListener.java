package org.ccpm.sysse.web;

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpServletRequest;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.utilidad.UserActivityLogger;

public class UserActivityPhaseListener implements PhaseListener {
    
    // Recursos estáticos que no necesitan ser logueados
    private static final String[] EXCLUDED_PATTERNS = {
        "/css/", "/js/", "/images/", "/fonts/", "/resources/",
        "jakarta.faces.resource", ".css", ".js", ".png", ".jpg", 
        ".jpeg", ".gif", ".ico", ".woff", ".woff2", ".ttf", ".eot", ".svg"
    };
    
    @Override
    public void afterPhase(PhaseEvent event) {
        // Ejecutar después de la fase INVOKE_APPLICATION para capturar acciones del usuario
        if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
            try {
                jakarta.faces.context.FacesContext facesContext = event.getFacesContext();
                HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
                String requestURI = request.getRequestURI();
                
                // Saltar logging para recursos estáticos
                if (shouldSkipLogging(requestURI)) {
                    return;
                }
                
                // Obtener el bean de la sesión
                WebManagedBean webBean = null;
                try {
                    Object beanObj = facesContext.getExternalContext().getSessionMap().get("webManagedBean");
                    if (beanObj == null) {
                        beanObj = facesContext.getApplication()
                                .evaluateExpressionGet(facesContext, "#{webManagedBean}", WebManagedBean.class);
                    }
                    if (beanObj instanceof WebManagedBean) {
                        webBean = (WebManagedBean) beanObj;
                    }
                } catch (Exception e) {
                    // Continuar sin el bean
                }
                
                String usuario = null;
                String organismo = null;
                
                if (webBean != null && webBean.getUsuario() != null) {
                    Usuario usuarioObj = webBean.getUsuario();
                    usuario = usuarioObj.getNombreUsuario() + " (" + 
                             (usuarioObj.getNombre() != null ? usuarioObj.getNombre() : "") + " " +
                             (usuarioObj.getApellido() != null ? usuarioObj.getApellido() : "") + ")";
                    
                    // Obtener organismo del usuario
                    if (webBean.getOrganismo() != null) {
                        Organismo org = webBean.getOrganismo();
                        organismo = (org.getCodOrganigrama() != null ? org.getCodOrganigrama() + " - " : "") +
                                   (org.getNombre() != null ? org.getNombre() : "Sin nombre");
                    } else if (usuarioObj.getOrganismo() != null) {
                        Organismo org = usuarioObj.getOrganismo();
                        organismo = (org.getCodOrganigrama() != null ? org.getCodOrganigrama() + " - " : "") +
                                   (org.getNombre() != null ? org.getNombre() : "Sin nombre");
                    }
                }
                
                // Determinar la acción
                String accion = determinarAccion(request);
                
                // Registrar solo si hay usuario logueado o es una acción importante
                if (usuario != null || (!requestURI.equals("/") && !requestURI.endsWith("index.html"))) {
                    UserActivityLogger.logActivity(usuario, accion, organismo, request);
                }
                
            } catch (Exception e) {
                // Ignorar errores para no interrumpir el flujo de la aplicación
                System.err.println("Error en UserActivityPhaseListener: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void beforePhase(PhaseEvent event) {
        // No hacer nada antes de la fase
    }
    
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.INVOKE_APPLICATION;
    }
    
    /**
     * Determina si la petición debe ser omitida del logging
     */
    private boolean shouldSkipLogging(String uri) {
        if (uri == null) {
            return true;
        }
        
        uri = uri.toLowerCase();
        
        for (String pattern : EXCLUDED_PATTERNS) {
            if (uri.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determina la descripción de la acción basada en la petición
     */
    private String determinarAccion(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        
        // Extraer el nombre de la página/acción de la URI
        String accion = "Acceso a: " + uri;
        
        // Detectar acciones específicas basadas en parámetros o URI
        if (queryString != null) {
            if (queryString.contains("action=")) {
                accion = "Ejecución de acción: " + queryString;
            }
        }
        
        // Detectar acciones comunes en la URI
        if (uri.contains("expediente")) {
            if (uri.contains("nuevo") || uri.contains("create")) {
                accion = "Crear nuevo expediente";
            } else if (uri.contains("editar") || uri.contains("edit")) {
                accion = "Editar expediente";
            } else if (uri.contains("eliminar") || uri.contains("delete")) {
                accion = "Eliminar expediente";
            } else if (uri.contains("enviar") || uri.contains("send")) {
                accion = "Enviar expediente";
            } else if (uri.contains("aceptar") || uri.contains("accept")) {
                accion = "Aceptar expediente";
            } else if (uri.contains("rechazar") || uri.contains("reject")) {
                accion = "Rechazar expediente";
            } else if (uri.contains("Conf") || uri.contains("conf")) {
                accion = "Acceso a módulo de expedientes";
            } else {
                accion = "Acceso a módulo de expedientes";
            }
        } else if (uri.contains("nota")) {
            if (uri.contains("nuevo") || uri.contains("create")) {
                accion = "Crear nueva nota";
            } else if (uri.contains("editar") || uri.contains("edit")) {
                accion = "Editar nota";
            } else if (uri.contains("eliminar") || uri.contains("delete")) {
                accion = "Eliminar nota";
            } else {
                accion = "Acceso a módulo de notas";
            }
        } else if (uri.contains("usuario")) {
            if (uri.contains("nuevo") || uri.contains("create")) {
                accion = "Crear nuevo usuario";
            } else if (uri.contains("editar") || uri.contains("edit")) {
                accion = "Editar usuario";
            } else {
                accion = "Acceso a módulo de usuarios";
            }
        } else if (uri.contains("reporte") || uri.contains("report")) {
            accion = "Generar reporte";
        } else if (uri.contains("administracion") || uri.contains("admin")) {
            accion = "Acceso a administración";
        } else if (uri.equals("/") || uri.endsWith("index.html")) {
            accion = "Acceso a página de inicio";
        }
        
        return accion;
    }
}

