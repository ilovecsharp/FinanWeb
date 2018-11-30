package ar.edu.unlam.tallerweb1.controladores;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.unlam.tallerweb1.modelo.Afiliado;
import ar.edu.unlam.tallerweb1.modelo.Cuota;
import ar.edu.unlam.tallerweb1.modelo.Prestamo;
import ar.edu.unlam.tallerweb1.servicios.ServicioAfiliado;
import ar.edu.unlam.tallerweb1.servicios.ServicioCuota;
import ar.edu.unlam.tallerweb1.servicios.ServicioPrestamo;
import ar.edu.unlam.tallerweb1.servicios.ServicioRefinanciar;

@Controller
public class ControladorPrestamo {
	
	@Inject
	private ServicioPrestamo servicioPrestamo;
	
	@Inject
	private ServicioCuota servicioCuota;
	
	@Inject
	private ServicioAfiliado servicioAfiliado;
	
	@Inject
	private ServicioRefinanciar servicioRefinanciar;
	
	@RequestMapping("/listarprestamos")
	public ModelAndView listarPrestamo() {
		ModelMap modelo = new ModelMap();
		
		List<Prestamo> prestamos= servicioPrestamo.consultarPrestamo();
		modelo.put("prestamos", prestamos);
		
		return new ModelAndView("listarprestamos",modelo);
	}
	
	@RequestMapping(path="/misprestamos")
	public ModelAndView misprestamos(Long dni, HttpServletRequest request) {
		ModelMap modelo = new ModelMap();
		Long dni0=(Long) request.getSession().getAttribute("dni");
		List<Prestamo> prestamos= servicioPrestamo.consultarPrestamo(dni0);
		modelo.put("prestamos", prestamos);

		
		return new ModelAndView("listarprestamos",modelo);
	}
	@RequestMapping(path = "/cancelarprestamo", method = RequestMethod.POST)
	public ModelAndView cancelarprestamo(Long idPrestamo2) {
		
		Prestamo miprestamo= servicioPrestamo.consultarUnPrestamo(idPrestamo2);

		miprestamo.setEstado("pagado");
		miprestamo.setCuotas(0);
		miprestamo.setInteres(0);
		miprestamo.setValor(0);
		
		servicioPrestamo.modificarPrestamo(miprestamo);

		return new ModelAndView("redirect:/misprestamos");
		
	}
	@RequestMapping(path = "/pagarcuota", method = RequestMethod.POST)
	public ModelAndView pagarcuota(Long idPrestamo1) {
		ModelMap modelo = new ModelMap();
//		prestamo con afiliado si no es nulo ahi si lo trae
		
		List<Cuota> cuotasnopagas=servicioCuota.consultarCuota(idPrestamo1);
		List<Cuota> cuotaspagas=servicioCuota.consultarCuotaPagada(idPrestamo1);
		
		Afiliado afiliado0= new Afiliado();
		
		afiliado0=servicioAfiliado.consultarAfiliado(idPrestamo1);
		
		
		
		modelo.put("afiliado", afiliado0);
		
		modelo.put("cuotaspagas", cuotaspagas);
		
		modelo.put("cuotasnopagas", cuotasnopagas);		

		return new ModelAndView("confirmarpagocuota",modelo);
		
	}

	@RequestMapping(path = "/totalapagarcuota", method=RequestMethod.POST)
	public void totalapagarcuota(@ModelAttribute("prestamo") Prestamo prestamo, HttpServletRequest request) {
		
	}
	@RequestMapping(path = "/crearprestamo", method=RequestMethod.POST)
	public ModelAndView crearPrestamo(@ModelAttribute("prestamo") Prestamo prestamo, HttpServletRequest request) {
		ModelMap modelo = new ModelMap();
		
		int cantcuotas=prestamo.getCuotas();
		
		double interesCuota=0;
		
		switch(cantcuotas) {
		case 6:interesCuota=2.0;
		break;
		case 12:interesCuota=4.0;
		break;
		case 24:interesCuota=8.0;
		break;
		case 32:interesCuota=16.0;
		break;
		case 72:interesCuota=32.0;
		break;
		default:{
			modelo.put("error", "Error en Cantidad Cuota");
			return new ModelAndView("crearprestamo", modelo);
		}
		}
		
		
		
		Prestamo nprestamo = prestamo;
		nprestamo.setInteres(cantcuotas*interesCuota);
		Long midni = (long) 123456789;
		Afiliado afiliado0=servicioAfiliado.consultarAfiliadoDni(midni);
		
		double montoMensual = nprestamo.getValor()/nprestamo.getCuotas();
		// calculamos el valor mensual de interes (el interes es igual para todos las cuotas)
		//double valorInteres = nprestamo.getValor() * nprestamo.getInteres();
		// capturamos la fecha actual
		double total = montoMensual + interesCuota;
		
		double sueldo=afiliado0.getSueldo();
		sueldo=sueldo*0.3;
		if(sueldo<total) {
			modelo.put("error", "Cada cuota excede el 30% del sueldo");
			return new ModelAndView("crearprestamo", modelo);
		}
		nprestamo.setDni(prestamo.getDni());
		
		nprestamo.setAfiliado(afiliado0);
		nprestamo.setEstado("pendiente");
		
		// calculamos el valor de la cuota mensual.
		
		
		Calendar fechven = Calendar.getInstance();
		
		List<Cuota> cuotas = new ArrayList<Cuota>();	
		
		
		for(int i=0; i<nprestamo.getCuotas(); i++){
			
			Cuota ncuota = new Cuota();
			fechven.add(Calendar.DAY_OF_YEAR, 30);
			ncuota.setMonto(montoMensual);
			ncuota.setInteres(interesCuota);
			ncuota.setMontoTotal(total);
			ncuota.setEstado(false);
			ncuota.setFechaDeVencimiento(fechven.getTime());
			ncuota.setPrestamo(nprestamo);	
			cuotas.add(ncuota);
			
		}
		servicioCuota.insertarCuota(cuotas);
		
		
		
		//servicioPrestamo.crearNuevoPrestamo(nprestamo);
		
		modelo.put("cuotas", cuotas);
		
		return new ModelAndView("redirect:/misprestamos");
	}
	
	@RequestMapping(path = "/refinanciar", method = RequestMethod.POST)
	public ModelAndView listaCuotasImpag(Long idPrestamo) {
		ModelMap modelo=new ModelMap();
		List<Cuota> impagas=servicioRefinanciar.consultaCuota(idPrestamo);
		Afiliado afiliado = servicioAfiliado.consultarAfiliado(idPrestamo);
		Double montoTotalARefinanciar = servicioRefinanciar.montoARefinanciar(idPrestamo);
		
		int cuotasRestante = impagas.size();
		
	    modelo.put("afiliado", afiliado);
	    modelo.put("idPrestamoRef", idPrestamo);
		modelo.put("cuotas", impagas);	
		modelo.put("MontoARefinanciar", montoTotalARefinanciar);
		modelo.put("cuotasRestante",cuotasRestante);
		return new ModelAndView("refinanciar",modelo);
	
	}

	@RequestMapping(path = "/hacer-refinanciacion", method = RequestMethod.POST)
	public ModelAndView refinanciarAlta(Long dni, Long idPrestamoRef, double newCapital, Integer cuotas, double interes) {
		ModelMap modelo = new ModelMap();
		
		servicioRefinanciar.refinanciar(dni, idPrestamoRef, newCapital, cuotas, interes);
		
		List<Cuota> nueCuotas = servicioCuota.consultarCuotaDelUltimoPrestamo();
		modelo.put("cuotas", nueCuotas);
		return new ModelAndView("listarcuotas",modelo);
	}
	
	// Lo uso solo para mostrar las cuotas del nuevo prestamo.
	@RequestMapping("/ultimoprestamo")
	public ModelAndView irAListarcuotasDeUltimoPrestamo() {

		ModelMap modelo=new ModelMap();
		List<Cuota> cuotasDelUltimoPrestamo = servicioCuota.consultarCuotaDelUltimoPrestamo();
		modelo.put("cuotas", cuotasDelUltimoPrestamo);
		
		return new ModelAndView("listarcuotas",modelo);
	}
	
	// si ingresa por la url "/refinanciar" sin pasar por los prestamos lo redirige al home.
	@RequestMapping("/refinanciar")
	public ModelAndView irAHome() {
			
		return new ModelAndView("home");
	}

	@RequestMapping(path = "/nuevoprestamo", method=RequestMethod.POST)
	public ModelAndView irANuevoPrestamo(@ModelAttribute("afiliado") Afiliado afiliado) {
		ModelMap modelo=new ModelMap();
		Afiliado miAfiliado = servicioAfiliado.consultarAfiliadoDni(afiliado.getDni());
		Prestamo prestamo = new Prestamo();
		List<Prestamo> prestamos = servicioPrestamo.consultarPrestamo(miAfiliado.getDni());
		
		double prestamoDisponible = servicioPrestamo.prestamoDisponible(afiliado);
		
		modelo.put("disponible", prestamoDisponible);
		modelo.put("prestamos", prestamos);
		modelo.put("afiliado", afiliado);
		modelo.put("prestamo", prestamo);
		return new ModelAndView("nuevoprestamo",modelo);
	}
	
	@RequestMapping(path = "/validar-nuevo-prestamo", method=RequestMethod.POST)
	public ModelAndView irValidarNuevoPrestamo(@ModelAttribute("afiliado") Afiliado afiliado, double valor, Integer cuotas) {
		
		ModelMap modelo=new ModelMap();
		
		if(valor <= servicioPrestamo.prestamoDisponible(afiliado)){
			return new ModelAndView("listarprestamos");
		}else{
			Afiliado miAfiliado = servicioAfiliado.consultarAfiliadoDni(afiliado.getDni());
			modelo.put("afiliado", miAfiliado);
			return new ModelAndView("nuevoprestamo",modelo);
		}
		
		
		
	}
		
	
	
}
