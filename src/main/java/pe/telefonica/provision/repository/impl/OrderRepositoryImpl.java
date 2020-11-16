package pe.telefonica.provision.repository.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
		return mongoOperations.findOne(new Query(Criteria.where("atisOrder").is(atisCode)), Order.class);
	}

	@Override
	public Order getOrderBySaleCode(String saleCode) {
		return mongoOperations.findOne(new Query(Criteria.where("code").is(saleCode)), Order.class);
	}

	@Override
	public List<Order> getOrdersByAtisCode(String atisCode, LocalDateTime startDate, LocalDateTime endDate) {

		Query query = null;

		if (startDate != null && endDate != null) {
			query = new Query(Criteria.where("atisOrder").is(atisCode));
		} else {
			query = new Query(Criteria.where("atisOrder").is(atisCode).andOperator(
					Criteria.where("registerDate").gte(startDate), Criteria.where("registerDate").lte(endDate)));
		}

		return mongoOperations.find(query, Order.class);
	}

	@Override
	public List<Order> getOrdersBySaleCode(String saleCode, LocalDateTime startDate, LocalDateTime endDate) {

		Query query = null;

		if (startDate != null && endDate != null) {
			query = new Query(Criteria.where("code").is(saleCode));
		} else {
			query = new Query(Criteria.where("code").is(saleCode).andOperator(
					Criteria.where("registerDate").gte(startDate), Criteria.where("registerDate").lte(endDate)));
		}

		return mongoOperations.find(query, Order.class);
	}

}
