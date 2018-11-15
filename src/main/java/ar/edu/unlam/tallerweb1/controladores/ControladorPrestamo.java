package ar.edu.unlam.tallerweb1.controladores;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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
	
	@RequestMapping(path = "/crearprestamo", method = RequestMethod.POST)
	public ModelAndView crearPrestamo(@ModelAttribute ("prestamo") Prestamo prestamo) {
		ModelMap modelo = new ModelMap();
		Prestamo nuevoPrestamo = new Prestamo();
		
		modelo.put("cuota", nuevoPrestamo.getCuota());
		modelo.put("valor", nuevoPrestamo.getValor());
		modelo.put("interes", nuevoPrestamo.getInteres());
		
		return new ModelAndView("crearprestamo", modelo);		
	}
}
