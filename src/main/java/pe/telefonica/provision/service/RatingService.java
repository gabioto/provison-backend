package pe.telefonica.provision.service;

import pe.telefonica.provision.controller.request.rating.GetRatingRequest;
import pe.telefonica.provision.controller.request.rating.SetRatingRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.rating.Rating;

public interface RatingService {

	public Rating getRatingByKey(GetRatingRequest request);

	public Provision setRating(SetRatingRequest request);
}
