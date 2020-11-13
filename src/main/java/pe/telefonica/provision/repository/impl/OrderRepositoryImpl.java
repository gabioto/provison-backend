package pe.telefonica.provision.repository.impl;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order getOrderBySaleCode(String saleCode) {
		// TODO Auto-generated method stub
		return null;
	}

}
