package ar.edu.unlam.tallerweb1.dao;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import ar.edu.unlam.tallerweb1.modelo.Cuota;

@Repository("CuotaDao")
public class CuotaDaoImpl implements CuotaDao{
	
	@Inject
	private SessionFactory sessionFactory;

	@Override
	public void insertarCuota(List<Cuota> cuotas) {
		
		for (Cuota cuota : cuotas) {
			sessionFactory.getCurrentSession().save(cuota);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Cuota> consultarCuota(Long arefinanciar) {
		return (sessionFactory.getCurrentSession()
				.createCriteria(Cuota.class)
				.createAlias("prestamo", "prestamoj")
				.add(Restrictions.eq("prestamoj.idPrestamo", arefinanciar))
				.add(Restrictions.eq("estado", false))
				.list());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Cuota> consultarCuotaDelUltimoPrestamo() {
		return (sessionFactory.getCurrentSession()
				.createCriteria(Cuota.class)
				.createAlias("prestamo", "prestamoj")
				.add(Restrictions.isNull("prestamoj.estado"))
				.list());
	}
}
