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
    void avance() {
        assertEquals(84.0, Indicador.avance(List.of(40.0, 95.0, 210.0), 250, false));
        // reduccion: de 12% a 8% con meta 7% -> se bajo 4 de 5
        assertEquals(80.0, Indicador.avance(List.of(12.0, 11.0, 8.0), 7, true));
        assertEquals(0.0, Indicador.avance(List.of(10.0, 14.0), 7, true));
        assertNull(Indicador.avance(List.of(), 10, false));
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
