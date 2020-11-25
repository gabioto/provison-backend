package pe.telefonica.provision.repository.impl;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Order saveOrder(Order order) {
		return mongoOperations.insert(order);
	}

	@Override
	public void updateOrder(String idOrder, Update update) {
		mongoOperations.updateFirst(new Query(Criteria.where("idOrder").is(new ObjectId(idOrder))), update,
				Order.class);
	}

	@Override
	public Order getOrderByAtisCode(String atisCode) {
		return getOrdersByAtisCode(atisCode, null, null);
	}

	@Override
	public Order getOrderBySaleCode(String saleCode) {
		return getOrdersBySaleCode(saleCode, null, null);
	}

	@Override
	public Order getOrdersByCmsCode(String cmsCode, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("cmsRequest").is(cmsCode);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.findOne(new Query(criteria).with(new Sort(Direction.DESC, "registerLocalDate")),
				Order.class);
	}

	@Override
	public Order getOrdersByAtisCode(String atisCode, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("atisOrder").is(atisCode);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.findOne(new Query(criteria).with(new Sort(Direction.DESC, "registerLocalDate")),
				Order.class);
	}

	@Override
	public Order getOrdersBySaleCode(String saleCode, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("code").is(saleCode);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.findOne(new Query(criteria).with(new Sort(Direction.DESC, "registerLocalDate")),
				Order.class);
	}

	@Override
	public Order getOrdersByPhone(String publicId, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("phone").is(publicId);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.findOne(new Query(criteria).with(new Sort(Direction.DESC, "registerLocalDate")),
				Order.class);
	}

	private Criteria validateFilterBetweenDates(Criteria criteria, LocalDateTime startDate, LocalDateTime endDate) {

		if (startDate != null && endDate != null) {
			criteria.andOperator(Criteria.where("registerDate").gte(startDate),
					Criteria.where("registerDate").lte(endDate));
		}

		return criteria;
	}
}
