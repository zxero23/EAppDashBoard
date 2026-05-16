/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package org.ccpm.sysse.utilidad;

/**
 *
 * @author matia
 */
public enum EstadoEnum {
    
    INICIADO(1L, "INICIADO"),
    ENVIADO(2L, "ENVIADO"),
    ACEPTADO(3L, "ACEPTADO"),
    RECHAZADO(4L, "RECHAZADO"),
    ARCHIVADO(5L, "ARCHIVADO"),
    EDITADO(6L, "EDITADO"),
    REENVIADO(7L, "REENVIADO"),
    ELIMINADO(8L, "ELIMINADO"),
    EDITADO_CORREGIDO(9L, "EDITADO (CORREGIDO POSTERIOR)");
    
    private Long id;

    private String nombre;
    
    EstadoEnum(Long id, String nombre) {
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
