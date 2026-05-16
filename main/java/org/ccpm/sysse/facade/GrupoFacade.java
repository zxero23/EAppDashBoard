package org.ccpm.sysse.facade;

import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.ccpm.sysse.entity.ActionItem;
import org.ccpm.sysse.entity.Grupo;
import org.ccpm.sysse.entity.Menu;
import org.ccpm.sysse.utilidad.Utilidad;

@Stateless
public class GrupoFacade extends AbstractFacade<Grupo> implements GrupoFacadeLocal {
    @PersistenceContext(unitName = "EAppSysSE-ejbPU")
    private EntityManager em;
    @EJB
    private MenuFacadeLocal menuFacade;
    @EJB
    private ActionItemFacadeLocal actionFacade;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GrupoFacade() {
        super(Grupo.class);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> finGrupos(List <Long> gruposId) {
        String iDes = gruposId.toString().substring(1, gruposId.toString().length()-1);
        return em.createQuery("select object (g) from Grupo as g where g.id in (" + iDes +  " ) ").getResultList();
    }
    
    @Override
    public List <Menu> findAllMenu(Long idGrupo) {
        Query jpqlString = em.createQuery("SELECT OBJECT (m) FROM Grupo AS g, IN (g.menus) m where g.id = :p1 ORDER BY m.orden");
        jpqlString.setParameter("p1", idGrupo);
        return jpqlString.getResultList();
    }   
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> findAll(boolean estado) {
        Query consulta = em.createQuery("select object(o) from Grupo as o WHERE o.estado = :p1 ORDER BY o.nombre ASC");
        consulta.setParameter("p1", estado);
        return consulta.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> findAll(String nombreGrupo) {
        return em.createQuery("select object(o) FROM Grupo as o WHERE o.estado = true AND "+Utilidad.desacentuar("o.nombre")+" LIKE '%" + nombreGrupo + "%' ORDER BY o.nombre ").getResultList();
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List <Grupo> findAll(List <String> gruposId) {
        List<Grupo> listaGrupoRetorno = new ArrayList <Grupo>();
        for (String idGrupoAux : gruposId) {
            Grupo grupoAux = this.find(Long.parseLong(idGrupoAux));
            listaGrupoRetorno.add(grupoAux);
        }
        return listaGrupoRetorno;
    }
    
    @Override
    public void remove(Long idGrupo) throws Exception {
        try {
            Grupo grupo = this.find(idGrupo);
            grupo.setEstado(false);
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar el grupo.");
        }
    }
        
    @Override
    public void remove(Long idGrupo, List <Long> idAcciones) throws Exception {
        try {
            Grupo grupo = this.find(idGrupo);
            List <ActionItem> listAction = actionFacade.findAll(idAcciones);
            grupo.setAcciones(listAction);
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar borrar acciones del grupo.");
        }
    }

    @Override
    public void create(String nombreGrupo, String descripcionGrupo) throws Exception {
        try {
            Grupo grupo = new Grupo();
            grupo.setNombre(nombreGrupo.toUpperCase());
            grupo.setDescripcion(descripcionGrupo);
            grupo.setEstado(true);
            this.create(grupo);
        } catch (Exception ex) {
            throw new Exception("Error al intentar crear el grupo.");
        }
    }

    @Override
    public void edit(Long idGrupo, String nombreGrupo, String descripcionGrupo) throws Exception {
        try {
            Grupo grupo = this.find(idGrupo);
            grupo.setNombre(nombreGrupo.toUpperCase());
            grupo.setDescripcion(descripcionGrupo);
            grupo.setEstado(true);
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el grupo.");
        }
    }
        
    @Override
    public void edit(Long idGrupo, List <Long> idMenu) throws Exception {        
        try {
            Grupo grupo = this.find(idGrupo);
            List <Menu> listaMenu = menuFacade.findAll(idMenu);
            grupo.setMenus(listaMenu);
            this.edit(grupo);
        } catch (Exception e) {
            throw new Exception("Error al intentar editar el grupo.");
        }
    }
    
}
