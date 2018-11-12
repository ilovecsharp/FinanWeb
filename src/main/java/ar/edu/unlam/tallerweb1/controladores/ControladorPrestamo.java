package ar.edu.unlam.tallerweb1.controladores;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.unlam.tallerweb1.modelo.Cuota;
import ar.edu.unlam.tallerweb1.modelo.Prestamo;
import ar.edu.unlam.tallerweb1.servicios.ServicioPrestamo;

@Controller
public class ControladorPrestamo {
	
	@Inject
	private ServicioPrestamo servicioPrestamo;
	
	@RequestMapping("/listarprestamos")
	public ModelAndView listarPrestamo() {
		ModelMap modelo = new ModelMap();
		
		List<Prestamo> prestamos= servicioPrestamo.consultarPrestamo();
		modelo.put("prestamos", prestamos);
		
		return new ModelAndView("listarprestamos",modelo);
	}
	
	@RequestMapping("/nuevoprestamo")
	public ModelAndView nuevoPrestamo() {
		ModelMap modelo = new ModelMap();
		
		Prestamo prestamo = new Prestamo();
		modelo.put("prestamo", prestamo);
		return new ModelAndView("crearprestamo", modelo);		
	}
	
	@RequestMapping(path = "/crearprestamo", method=RequestMethod.POST)
	public ModelAndView crearPrestamo(@ModelAttribute("prestamo") Prestamo prestamo, HttpServletRequest request) {
		ModelMap modelo = new ModelMap();
		
		Prestamo nprestamo = prestamo;
		
		Cuota ncuota = new Cuota();
		
		// calculamos el valor de la cuota mensual.
		double montoMensual = nprestamo.getValor()/nprestamo.getCuotas();
		// calculamos el valor mensual de interes (el interes es igual para todos las cuotas)
		double valorInteres = nprestamo.getValor() * (nprestamo.getInteres()/100);
		// capturamos la fecha actual
		double total = montoMensual + valorInteres;
		Date fecha = new Date();
		 
		ncuota.setMonto(montoMensual);
		ncuota.setInteres(valorInteres);
		ncuota.setMontoTotal(total);
		ncuota.setEstado(false);
		ncuota.setFechaDeVencimiento(fecha);
		
		List<Cuota> cuotas = new ArrayList<Cuota>();
		
		for(int i=0; i<nprestamo.getCuotas(); i++){
			cuotas.add(ncuota);
		}
		
		modelo.put("cuotas", cuotas);
		
		return new ModelAndView("realizarpagoafinan", modelo);		
	}
	
	
}
