package org.ccpm.sysse.web;

import java.io.IOException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.ccpm.sysse.entity.Organismo;
import org.ccpm.sysse.entity.Usuario;
import org.ccpm.sysse.utilidad.UserActivityLogger;

@WebFilter(filterName = "UserActivityFilter", urlPatterns = {"/*"})
public class UserActivityFilter implements Filter {
    
    // Recursos estáticos que no necesitan ser logueados
    private static final String[] EXCLUDED_PATTERNS = {
        "/css/", "/js/", "/images/", "/fonts/", "/resources/",
        ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico",
        ".woff", ".woff2", ".ttf", ".eot", ".svg"
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización del filtro
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        
        // Saltar logging para recursos estáticos
        if (shouldSkipLogging(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Obtener usuario y organismo de la sesión
        HttpSession session = httpRequest.getSession(false);
        String usuario = null;
        String organismo = null;
        
        WebManagedBean webBean = null;
        
        if (session != null) {
            try {
                // Método 1: Intentar obtener usando CDI
                try {
                    BeanManager beanManager = CDI.current().getBeanManager();
                    Bean<?> bean = beanManager.resolve(beanManager.getBeans(WebManagedBean.class));
                    if (bean != null) {
                        jakarta.enterprise.context.spi.Context context = beanManager.getContext(jakarta.enterprise.context.SessionScoped.class);
                        if (context != null) {
                            webBean = (WebManagedBean) context.get(bean);
                        }
                    }
                } catch (Exception e) {
                    // Si CDI no está disponible, continuar con otros métodos
                }
                
                // Método 2: Intentar obtener del SessionMap de JSF
                if (webBean == null) {
                    jakarta.faces.context.FacesContext facesContext = jakarta.faces.context.FacesContext.getCurrentInstance();
                    if (facesContext != null) {
                        Object beanObj = facesContext.getExternalContext().getSessionMap().get("webManagedBean");
                        if (beanObj instanceof WebManagedBean) {
                            webBean = (WebManagedBean) beanObj;
                        } else {
                            // Intentar obtenerlo usando EL expression
                            try {
                                beanObj = facesContext.getApplication()
                                        .evaluateExpressionGet(facesContext, "#{webManagedBean}", WebManagedBean.class);
                                if (beanObj instanceof WebManagedBean) {
                                    webBean = (WebManagedBean) beanObj;
                                }
                            } catch (Exception e) {
                                // Continuar sin el bean
                            }
                        }
                    }
                }
                
                // Método 3: Intentar obtener directamente de la sesión HTTP
                if (webBean == null) {
                    Object beanObj = session.getAttribute("webManagedBean");
                    if (beanObj instanceof WebManagedBean) {
                        webBean = (WebManagedBean) beanObj;
                    }
                }
                
            } catch (Exception e) {
                // Si hay algún error, continuar sin el bean
                System.err.println("Error al obtener WebManagedBean del filtro: " + e.getMessage());
            }
            
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
        }
        
        // Determinar la acción basada en la URI y método
        String accion = determinarAccion(httpRequest);
        
        // Registrar la actividad
        if (usuario != null || !requestURI.equals("/") && !requestURI.endsWith("index.html")) {
            UserActivityLogger.logActivity(usuario, accion, organismo, httpRequest);
        }
        
        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // Limpieza del filtro
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

