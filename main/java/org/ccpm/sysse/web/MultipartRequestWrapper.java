package org.ccpm.sysse.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.*;

/**
 * Request wrapper que expone getPart()/getParts() a partir de
 * los ítems ya parseados por Commons FileUpload (para cuando el
 * request pasa por RewriteFilter y el multipart-config del servlet no se aplica).
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    private final List<Part> parts;
    private final Map<String, String[]> parameterMap;

    public MultipartRequestWrapper(HttpServletRequest request, List<Part> parts, Map<String, String[]> parameterMap) {
        super(request);
        this.parts = parts != null ? parts : Collections.emptyList();
        this.parameterMap = parameterMap != null ? parameterMap : Collections.emptyMap();
    }

    @Override
    public Part getPart(String name) throws IOException {
        for (Part p : parts) {
            if (name != null && name.equals(p.getName())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Collection<Part> getParts() throws IOException {
        return new ArrayList<>(parts);
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return new HashMap<>(parameterMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }
}
