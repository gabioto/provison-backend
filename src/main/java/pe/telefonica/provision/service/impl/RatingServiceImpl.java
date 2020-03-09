package pe.telefonica.provision.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.request.rating.GetRatingRequest;
import pe.telefonica.provision.controller.request.rating.SetRatingRequest;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.rating.Rating;
import pe.telefonica.provision.service.RatingService;

import pe.telefonica.provision.repository.RatingRepository;
import pe.telefonica.provision.repository.ProvisionRepository;

@Service
public class RatingServiceImpl implements RatingService {

	@Autowired
	private RatingRepository ratingRepository;

	@Autowired
	private ProvisionRepository provisionRepository;

	@Override
	public Rating getRatingByKey(GetRatingRequest request) {
		return ratingRepository.getRatingByKeyName(request.getKeyName());
	}

	@Override
	public Provision setRating(SetRatingRequest request) {

		Provision provision = provisionRepository.getProvisionByIdNotFilter(request.getProvisionId());
		if (provision != null) {
			Update update = new Update();

			List<Rating> ratingList = provision.getRating();
			Rating rating = new Rating();

			rating.setKeyName(request.getKeyName());
			rating.setTitle(request.getTitle());
			rating.setQuestion(request.getQuestion());
			rating.setRating(request.getRating());
			rating.setAnswer(request.getAnswer());

			ratingList.add(rating);

			update.set("rating", ratingList);
			
			provision.setRating(ratingList);

			boolean updated = provisionRepository.updateProvision(provision, update);

			return updated ? provision : null;

		}
		return null;
	}

}
