package org.ccpm.tablero.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serie temporal que mide una meta. Formato heredado de la v1 (lo sigue leyendo la app legacy):
 * serie = etiquetas de periodo, valores = tripletas por periodo donde solo la primera posicion es el dato.
 */
@Entity
@Table(name = "indicador")
public class Indicador {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String leyenda;
    public String descripcion;
    public String formula;
    public String kml;
    @Convert(converter = StringArrayConverter.class)
    public String[] serie;
    @Convert(converter = StringArrayConverter.class)
    public String[] valores;
    @Convert(converter = StringArrayConverter.class)
    public String[] variableIndicador;
    public int sizeSerie;
    public int sizeVariable;
    public boolean estado;
    @ManyToOne @JoinColumn(name = "metas_id")
    public Meta meta;

    public record Punto(String periodo, Double valor) {}

    public List<Punto> puntos() {
        List<Punto> out = new ArrayList<>();
        if (serie == null || valores == null) return out;
        int n = Math.min(sizeSerie, Math.min(serie.length, valores.length / 3));
        for (int i = 0; i < n; i++) out.add(new Punto(serie[i], parse(valores[i * 3])));
        return out;
    }

    public List<Double> valores() {
        return puntos().stream().map(Punto::valor).filter(v -> v != null).toList();
    }

    public void setPuntos(List<Punto> puntos) {
        serie = puntos.stream().map(Punto::periodo).toArray(String[]::new);
        valores = new String[puntos.size() * 3];
        for (int i = 0; i < puntos.size(); i++) {
            valores[i * 3] = String.valueOf(puntos.get(i).valor());
            valores[i * 3 + 1] = "0";
            valores[i * 3 + 2] = "0";
        }
        sizeSerie = puntos.size();
    }

    public String variable() {
        return variableIndicador != null && variableIndicador.length > 0 ? variableIndicador[0] : leyenda;
    }

    public void setVariable(String variable) {
        variableIndicador = new String[]{variable};
        sizeVariable = 1;
    }

    /**
     * % de cumplimiento con el ultimo valor. Aumento: ultimo/meta. Reduccion: cuanto se bajo desde el
     * primer valor respecto de lo que habia que bajar. Nunca negativo; puede pasar de 100.
     */
    public static Double avance(List<Double> valores, Integer meta, boolean reduccion) {
        if (valores.isEmpty() || meta == null) return null;
        double ultimo = valores.get(valores.size() - 1);
        double pct;
        if (!reduccion) {
            if (meta == 0) return null;
            pct = ultimo / meta * 100;
        } else {
            double base = valores.get(0);
            if (base <= meta) return ultimo <= meta ? 100.0 : 0.0;
            pct = (base - ultimo) / (base - meta) * 100;
        }
        return Math.max(0, Math.round(pct * 10) / 10.0);
    }

    private static Double parse(String s) {
        try {
            return s == null ? null : Double.valueOf(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
