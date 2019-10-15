package pe.telefonica.provision.util.exception;

import org.springframework.core.NestedRuntimeException;

public class ServerNotFoundException extends NestedRuntimeException {
	private static final long serialVersionUID = -6679427088613925739L;
	public ServerNotFoundException(String message) {
		super(message);
	}
	
}
