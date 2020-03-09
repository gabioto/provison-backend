package pe.telefonica.provision.repository;

import pe.telefonica.provision.model.rating.Rating;

public interface RatingRepository {
	
	public Rating getRatingByKeyName(String keyName);
}
