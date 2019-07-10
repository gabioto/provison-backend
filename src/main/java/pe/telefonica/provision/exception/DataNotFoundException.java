package pe.telefonica.provision.exception;

import org.springframework.core.NestedRuntimeException;

public class DataNotFoundException extends NestedRuntimeException {
	
	private static final long serialVersionUID = -7679427088613925739L;

	public DataNotFoundException() {
		super("No se encontraron registros");
	}

}
