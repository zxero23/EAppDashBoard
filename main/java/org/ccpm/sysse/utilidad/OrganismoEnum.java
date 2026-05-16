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
public enum OrganismoEnum {

    SALUD_PUBLICA(3L,6000L, "SALUD PUBLICA"),
    CONTADURIA(97L,3223L, "CONTADURIA"),
    HACIENDA(1L,3000L, "HACIENDA");

    private Long codigo;
    
    private Long id;

    private String nombre;
    
    OrganismoEnum(Long id, Long codigo, String nombre) {
        this.id =id;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    
    
}
