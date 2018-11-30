package ar.edu.unlam.tallerweb1.dao;

import java.util.List;

import ar.edu.unlam.tallerweb1.modelo.Cuota;
import ar.edu.unlam.tallerweb1.modelo.Financiera;

public interface CuotaDao {
	void insertarCuota(List<Cuota> cuotas);
	
	List<Cuota> consultarCuota(Long arefinanciar);
	
	List<Cuota> consultarCuotaDelUltimoPrestamo();
 
	List<Cuota> consultarPorFinanciera(Financiera financiera);
	
	void modificarElCubierto(Cuota cuota);
	
	List<Cuota> consultarCuotaImpagas(Long idPrestamo);
}
