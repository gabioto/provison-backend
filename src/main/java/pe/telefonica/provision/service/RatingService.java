package pe.telefonica.provision.service;

import pe.telefonica.provision.controller.request.rating.GetRatingRequest;
import pe.telefonica.provision.controller.request.rating.SetRatingRequest;
import pe.telefonica.provision.dto.ProvisionDetailTrazaDto;
import pe.telefonica.provision.model.rating.Rating;

public interface RatingService {

	public Rating getRatingByKey(GetRatingRequest request);

	public ProvisionDetailTrazaDto setRating(SetRatingRequest request);
}
