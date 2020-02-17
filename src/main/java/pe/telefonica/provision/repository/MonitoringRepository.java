package pe.telefonica.provision.repository;

import java.time.LocalDateTime;

public interface MonitoringRepository {
	
	public long getQuantityRegisterByStatus(LocalDateTime dateStart, LocalDateTime dateEnd, String status);
	
}
