package org.ccpm.tablero.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "obras")
public class Obra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public Double latitud;
    public Double longitud;
    public LocalDateTime fechaInicio;
    public LocalDateTime fechaFinal;
    public String adjunto;
    public boolean estado;
    @ManyToOne @JoinColumn(name = "localidad_id")
    public Localidad localidad;
    @ManyToOne @JoinColumn(name = "organismo_id")
    public Organismo organismo;
    @ManyToOne @JoinColumn(name = "situacion_id")
    public Situacion situacion;
}
