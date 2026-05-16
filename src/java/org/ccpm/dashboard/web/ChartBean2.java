package org.ccpm.dashboard.web;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import org.ccpm.dashboard.entity.Indicador;
import org.ccpm.dashboard.entity.Metas;
import org.ccpm.dashboard.facade.ObjetivosFacadeLocal;
import org.ccpm.dashboard.facade.SituacionFacadeLocal;
import org.ccpm.dashboard.facade.IndicadorFacadeLocal;
import org.ccpm.dashboard.facade.MetasFacadeLocal;
import org.ccpm.util.diferenciaFecha;


@Named(ChartBean2.BEAN_NAME)
@RequestScoped
public class ChartBean2 implements Serializable {
    public static final String BEAN_NAME = "chartBean2";
    public String getBeanName() { return BEAN_NAME; }
   
    public static class Axis implements Serializable {
        private AxisType type;
        private int tickAngle;

        public AxisType getType() {
            return type;
        }

        public void setType(AxisType type) {
            this.type = type;
        }

        public int getTickAngle() {
            return tickAngle;
        }

        public void setTickAngle(int tickAngle) {
            this.tickAngle = tickAngle;
        }
    }

    public enum AxisType {
        CATEGORY
    }

    public static class CartesianSeries implements Serializable {
        public enum CartesianType {
            LINE
        }

        private CartesianType type;
        private String label;
        private String color;
        private final List<Object[]> values = new ArrayList<>();

        public void setType(CartesianType type) {
            this.type = type;
        }

        public CartesianType getType() {
            return type;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }

        public void add(String category, Number value) {
            values.add(new Object[]{category, value});
        }

        public List<Object[]> getValues() {
            return values;
        }
    }

    @EJB
    private ObjetivosFacadeLocal objetivosFacade;
    @EJB
    private SituacionFacadeLocal situacionFacade;
    @EJB
    private IndicadorFacadeLocal indicadorFacade;
    @EJB
    private MetasFacadeLocal metaFacade;
    
    
    
    private CartesianSeries barData = new CartesianSeries() ;
    private CartesianSeries barData1 = new CartesianSeries() ;
    private List<CartesianSeries> barData2=new ArrayList<CartesianSeries>();
    private CartesianSeries barData3 = new CartesianSeries() ; //de la meta
    private CartesianSeries barData4 = new CartesianSeries() ;
    
    
    
    
    
    private Long idMeta;
    private boolean mostrar=false;
    private String descripcion,leyenda,descripcion2,leyenda2;
    private boolean tieneIndicador=false;
    private String kml,kml1;
    private boolean existeIndicador=false;
    private double valorMeta=0;
    private double valorMetaBar4=0; //el valor del ultimo año fijado en la meta
    private double porcentajeMeta=0;
    private int ultimoValor=0;
    private boolean reduccion1=false;
    private String formatString = "<table class=\"jqplot-highlighter\"><tr><td>Fecha:</td><td>%d</td></tr> \n" +
            "<tr><td>Valor:</td><td>%s</td></tr></table>";
    private Date fechaInicio,fechaFinal;

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }
    
    
    
    
    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }
    
    

    public CartesianSeries getBarData4() {
        return barData4;
    }

    public double getValorMetaBar4() {
        return valorMetaBar4;
    }

    public void setValorMetaBar4(double valorMetaBar4) {
        this.valorMetaBar4 = valorMetaBar4;
    }
    
    

    public void setBarData4(CartesianSeries barData4) {
        this.barData4 = barData4;
    }

    
    
    
    public boolean isReduccion1() {
        return reduccion1;
    }

    public void setReduccion1(boolean reduccion1) {
        this.reduccion1 = reduccion1;
    }

    public List<CartesianSeries> getBarData2() {
        return barData2;
    }

    public int getUltimoValor() {
        return ultimoValor;
    }

    public void setUltimoValor(int ultimoValor) {
        this.ultimoValor = ultimoValor;
    }
    
    

    public double getPorcentajeMeta() {
        return porcentajeMeta;
    }

    public void setPorcentajeMeta(double porcentajeMeta) {
        this.porcentajeMeta = porcentajeMeta;
    }

    
    
    public double getValorMeta() {
        return valorMeta;
    }

    public void setValorMeta(double valorMeta) {
        
        this.valorMeta = valorMeta;
    }
    
    

    public void setBarData2(List<CartesianSeries> barData2) {
        this.barData2 = barData2;
    }

        
    public String getKml1() {
        return kml1;
    }

    public void setKml1(String kml1) {
        this.kml1 = kml1;
    }
    
    
    
    public String getKml() {
        return kml;
    }

    public boolean isExisteIndicador() {
        return existeIndicador;
    }

    public void setExisteIndicador(boolean existeIndicador) {
        this.existeIndicador = existeIndicador;
    }

    
    
    public void setKml(String kml) {
        this.kml = kml;
    }

    
    public String getDescripcion2() {
        return descripcion2;
    }

    public void setDescripcion2(String descripcion2) {
        this.descripcion2 = descripcion2;
    }

    public String getLeyenda2() {
        return leyenda2;
    }

    public void setLeyenda2(String leyenda2) {
        this.leyenda2 = leyenda2;
    }

    
    public boolean isTieneIndicador() {
        return tieneIndicador;
    }

    public void setTieneIndicador(boolean tieneIndicador) {
        this.tieneIndicador = tieneIndicador;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }
    

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
    public CartesianSeries getBarData1() {
        return barData1;
    }

    public void setBarData1(CartesianSeries barData1) {
        this.barData1 = barData1;
    }

    public Long getIdMeta() {
        return idMeta;
    }

    public void setIdMeta(Long idMeta) {
        this.idMeta = idMeta;
    }

    public boolean isMostrar() {
        return mostrar;
    }

    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
    }
    
    
    
    public List<CartesianSeries> getSelectItems(Long id) {  //carga datos de la tabla Resultados a los graficos con año y porcentaje 
        //this.barData.clear(); 
        //System.out.println("fucking id-->"+id);                                                        //como parámetros
        int puntero;
        double valorMeta4=0;
        diferenciaFecha diff=new diferenciaFecha();
        double porcentaje=0;
        barData.setType(CartesianSeries.CartesianType.LINE);
        //barData.setLabel("Porcentaje de Pobreza Anual");
        List <CartesianSeries> selectItems = new ArrayList <>();
        List <Indicador> lstIndicador = this.indicadorFacade.findAll(true,id);
        Metas lstMeta = this.metaFacade.find(id);
        this.setFechaInicio(lstMeta.getFechaInicio());
        this.setFechaFinal(lstMeta.getFechaFinal());
        Long año=diff.getDiffDates(lstMeta.getFechaInicio(),lstMeta.getFechaFinal(),0);
        this.setPorcentajeMeta((double)lstMeta.getCantidad());
        this.setReduccion1(lstMeta.isReduccion());
        if (!(lstIndicador.isEmpty())) {  //si el indicador no esta vacio
            this.setTieneIndicador(false);
            this.setExisteIndicador(true);
            int punteroValor=0;
            this.setDescripcion(lstIndicador.get(0).getDescripcion());
            this.setLeyenda(lstIndicador.get(0).getLeyenda());
            this.setKml(lstIndicador.get(0).getKml());
            int serieSize=lstIndicador.get(0).getSizeSerie();
               for (puntero=0;puntero<serieSize;puntero ++) {
                    Indicador orgAux=lstIndicador.get(0);
                        String[] Valor=orgAux.getValores();
                        String[] Serie=orgAux.getSerie();
                            barData.add(Serie[puntero],Double.parseDouble(Valor[punteroValor]));
                            if (puntero==0){
                                barData4.add(Serie[puntero],Double.parseDouble(Valor[punteroValor]));
                            }else{
                                if (puntero+1==serieSize){
                                    if (lstMeta.isReduccion()){
                                        barData4.add(Serie[puntero],Double.parseDouble(Valor[0])-(Double.valueOf(Valor[0])*(this.porcentajeMeta/100)));
                                        valorMeta4=Double.parseDouble(Valor[0])-(Double.valueOf(Valor[0])*(this.porcentajeMeta/100));
                                        
                                    }else{
                                        barData4.add(Serie[puntero],Double.parseDouble(Valor[0])+(Double.valueOf(Valor[0])*(this.porcentajeMeta/100)));
                                        valorMeta4=Double.parseDouble(Valor[0])+(Double.valueOf(Valor[0])*(this.porcentajeMeta/100));
                                        
                                        
                                    } //cambios de zanek, domingo 3 de marzo 2017
                                    this.setValorMeta(valorMeta4);
                                }
                            }
                        if (!Valor[puntero].equals("")){  //cargo el pocentaje de la meta en valor de indicador
                            if (lstMeta.isReduccion()){   // si Reduccion is true entonces meta es negativa o sino positiva
                                porcentaje=(double) Double.valueOf(Valor[Valor.length-3])-(Double.valueOf(Valor[Valor.length-3])*(this.porcentajeMeta/100));
                            }else{
                                porcentaje=(double) Double.valueOf(Valor[Valor.length-3])+(Double.valueOf(Valor[Valor.length-3])*(this.porcentajeMeta/100));    
                            }
                            //System.out.println("Valormeta-->"+valorMeta +"--vañpr-->"+ porcentajeMeta);
                            barData3.add(Serie[puntero],porcentaje);
                            this.setValorMeta(porcentaje);
                        }
              
                        
                    
                    //System.out.println("valor--->"+punteroValor+"--serie-->"+Serie[0]);
                    barData.setLabel(orgAux.getVariableIndicador()[0]);
                    barData3.setLabel("META");
                    barData3.setColor("green");
                    barData4.setLabel("META"); //nueva meta marzo 2017
                    barData4.setColor("green");
                    punteroValor=punteroValor+3;
                  
                    
            }
        ///////////// calcula los años de inicio y final y coloca el eje del valor de la meta  ////////////////    
        Calendar calendarInicio = Calendar.getInstance();
	calendarInicio.setTime(lstMeta.getFechaInicio());
        Calendar calendarFinal = Calendar.getInstance();
	calendarFinal.setTime(lstMeta.getFechaFinal());
	int diaInicio = calendarInicio.get(Calendar.DAY_OF_MONTH);
	int mesInicio = calendarInicio.get(Calendar.MONTH) + 1; // 0 Enero, 11 Diciembre
	int anioInicio = calendarInicio.get(Calendar.YEAR);
        int anioFinal = calendarFinal.get(Calendar.YEAR);
        int size=anioInicio+serieSize;
           
           while (size<=anioFinal){ 
              
                barData3.add(String.valueOf(size),this.valorMeta);
                
                barData4.add(String.valueOf(size),valorMeta4);
                    size =size+1;
                
           }
           
        ///////////fin del codigo del calculo de los años /////////////   
         
       
         
         barData2.add(barData);   
         barData2.add(barData4); //carga el objetivo de la meta aca iba barData3, cambio por el 4
                   
            
        }else{this.setExisteIndicador(false);}
        
        return selectItems;
        
    }
    
    
    
     
    private Axis barDemoDefaultAxis = new Axis() {{
        setTickAngle(-30);
    }};

    private Axis barDemoXAxis = new Axis() {{
        setType(AxisType.CATEGORY);
        
        
        
    }};
    
    private Axis barDemoYAxis = new Axis() {{
        setType(AxisType.CATEGORY);
    }};

    

   

    public CartesianSeries getBarData() {
        return barData;
    }

    public void setBarData(CartesianSeries barData) {
        this.barData = barData;
    }

    public Axis getBarDemoYAxis() {
        return barDemoYAxis;
    }

    public void setBarDemoYAxis(Axis barDemoYAxis) {
        this.barDemoYAxis = barDemoYAxis;
    }

    
    
    public Axis getBarDemoXAxis() {
        return barDemoXAxis;
    }

    public void setBarDemoXAxis(Axis barDemoXAxis) {
        this.barDemoXAxis = barDemoXAxis;
    }

    public Axis getBarDemoDefaultAxis() {
        return barDemoDefaultAxis;
    }

    public void setBarDemoDefaultAxis(Axis barDemoDefaultAxis) {
        this.barDemoDefaultAxis = barDemoDefaultAxis;
    }

    

   
}