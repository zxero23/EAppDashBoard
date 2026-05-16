/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ccpm.sysse.utilidad;

/**
 *
 * @author Matias
 */
public enum GrupoEnum {
    
    SUPER_ADMIN(1L, "SUPER"),
    ADMIN(2L, "ADMIN"),
    CONTADURIA(21L, "CONTADURÍA GENERAL"),
    USUARIO_EXTERNO(3L, "USUARIO EXTERNO"),
    USUARIO_EXTERNO_ALM(16L, "USUARIO EXTERNO ALM"),
    MESA_INTERNA(4L, "MESA INTERNA"),
    MESA_INTERNA_INGRESO(17L, "MESA INTERNA CON INGRESO"),
     MESA_INTERNA_BASICA(26L, "MESA INTERNA - BÁSICA"),
    MESA_GENERAL(5L, "MESA GENERAL"),
    MESA_GENERAL_SIN_INGRESO_EXTERNO(30L, "MESA GENERAL SIN INGRESO EXTERNO"),
    BUSQUEDA_AVANZADA(6L, "BUSQUEDA AVANZADA");

    private Long id;

    private String nombre;
    
    GrupoEnum(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }    
}
