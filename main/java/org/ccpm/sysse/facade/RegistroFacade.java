package org.ccpm.sysse.facade;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.ccpm.sysse.entity.Registro;

@Stateless
public class RegistroFacade extends AbstractFacade<Registro> implements RegistroFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RegistroFacade() {
        super(Registro.class);
    }
    
    @Override
    public void create(String nombre, String organsimo, 
        String telefono, String email) throws Exception {
        try {
            Registro registroAux = new Registro();
            registroAux.setNombre(nombre.toUpperCase());
            registroAux.setOrganismo(organsimo.toUpperCase());
            registroAux.setTelefono(telefono);
            registroAux.setEmail(email);
            registroAux.setEstado(true);
            this.create(registroAux);
        } catch (Exception e) {
            throw new Exception("Error al intentar crear la solicitud.");
        }
    }
}
