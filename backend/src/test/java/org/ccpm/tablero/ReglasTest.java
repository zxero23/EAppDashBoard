package org.ccpm.tablero;

import org.ccpm.tablero.modelo.Indicador;
import org.ccpm.tablero.modelo.StringArrayConverter;
import org.ccpm.tablero.seguridad.ClaveEncoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReglasTest {

    @Test
    void metaEsPorcentajeSobreElPrimerValor() {
        // aumentar 50% desde 100 -> meta 150; con 120 se logro 20 de 50
        assertEquals(150.0, Indicador.valorMeta(List.of(100.0, 110.0, 120.0), 50, false));
        assertEquals(40.0, Indicador.avance(List.of(100.0, 110.0, 120.0), 50, false));
        // reducir 25% desde 12 -> meta 9; llegar a 9 es 100%
        assertEquals(9.0, Indicador.valorMeta(List.of(12.0, 11.0, 9.0), 25, true));
        assertEquals(100.0, Indicador.avance(List.of(12.0, 11.0, 9.0), 25, true));
        // empeoro en vez de reducir -> 0, nunca negativo
        assertEquals(0.0, Indicador.avance(List.of(10.0, 14.0), 30, true));
        assertNull(Indicador.avance(List.of(), 10, false));
        assertNull(Indicador.avance(List.of(5.0), 0, false));
    }

    @Test
    void indicadorGuardaTripletasComoLaV1() {
        Indicador i = new Indicador();
        i.setPuntos(List.of(new Indicador.Punto("T1", 5.0), new Indicador.Punto("T2", 7.5)));
        assertArrayEquals(new String[]{"5.0", "0", "0", "7.5", "0", "0"}, i.valores);
        assertEquals(2, i.sizeSerie);
        assertEquals(List.of(5.0, 7.5), i.valores());

        StringArrayConverter c = new StringArrayConverter();
        assertArrayEquals(i.serie, c.convertToEntityAttribute(c.convertToDatabaseColumn(i.serie)));
    }

    @Test
    void converterRechazaOtrasClases() throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream o = new ObjectOutputStream(b)) {
            o.writeObject(new ArrayList<>(List.of("x")));
        }
        assertThrows(IllegalStateException.class, () -> new StringArrayConverter().convertToEntityAttribute(b.toByteArray()));
    }

    @Test
    void clavesLegacyYBcrypt() {
        ClaveEncoder e = new ClaveEncoder(true);
        assertTrue(e.matches("Secreto", "secreto"));   // la v1 guardaba en minusculas
        assertFalse(e.matches("otra", "secreto"));
        assertTrue(e.upgradeEncoding("secreto"));
        String hash = e.encode("secreto");
        assertTrue(e.matches("secreto", hash));
        assertFalse(e.upgradeEncoding(hash));
        assertFalse(new ClaveEncoder(false).upgradeEncoding("secreto"));
    }
}
