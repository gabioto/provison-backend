package pe.telefonica.provision.repository.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

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
import pe.telefonica.provision.util.constants.Constants;

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

		return mongoOperations.findOne(new Query(criteria), Order.class);
	}

	@Override
	public Order getOrdersByAtisCode(String atisCode, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("atisOrder").is(atisCode);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.findOne(new Query(criteria), Order.class);
	}

	@Override
	public Order getOrdersBySaleCode(String saleCode, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("code").is(saleCode);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.findOne(new Query(criteria), Order.class);
	}

	@Override
	public List<Order> getOrdersByPhone(String publicId, LocalDateTime startDate, LocalDateTime endDate) {

		Criteria criteria = Criteria.where("phone").is(publicId);
		criteria = validateFilterBetweenDates(criteria, startDate, endDate);

		return mongoOperations.find(new Query(criteria), Order.class);
	}

	@Override
	public List<Order> getOrdersToNotify() {

		return mongoOperations.find(getNotificationQueryCriteria(), Order.class);
	}

	@Override
	public void updateFlagDateNotify() {

		Update update = new Update();
		update.set("notifications.finalizadoSendNotify", true);
		update.set("notifications.finalizadoSendDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));

		this.mongoOperations.updateMulti(getNotificationQueryCriteria(), update, Order.class);
	}

	private Query getNotificationQueryCriteria() {

		Criteria criteria = Criteria.where("statusOrderCode").is("FI")
				.andOperator(Criteria.where("commercialOpAtis").is("ALTA MIGRACION"),
						Criteria.where("commercialOpAtis").is("ALTA MIGRACION TRASLADO CIS"),
						Criteria.where("commercialOpAtis").is("ALTA MIGRACION TRASLADO SIS"),
						Criteria.where("commercialOpAtis").is("SUSPENSION APC"),
						Criteria.where("commercialOpAtis").is("ALTA MIGRACION DE P/S"),
						Criteria.where("commercialOpAtis").is("MIGRACION"),
						Criteria.where("commercialOpAtis").is("MIGRACION CON CAMBIO DE CUENTA"),
						Criteria.where("commercialOpAtis").is("MIGRACION CON TRASLADO CIS"),
						Criteria.where("commercialOpAtis").is("MIGRACION CON TRASLADO SIS"),
						Criteria.where("commercialOpAtis").is("MIGRACION DE CABECERA"),
						Criteria.where("commercialOpAtis").is("MODIFICACION"),
						Criteria.where("commercialOpAtis").is("MODIFICACION DE CARACTERISTICAS DE P/S"))
				.and("notifications").ne(null).and("notifications.finalizadoSendNotify").is(false);

		return new Query(criteria).limit(15).with(new Sort(Direction.ASC, "_id"));
	}

	private Criteria validateFilterBetweenDates(Criteria criteria, LocalDateTime startDate, LocalDateTime endDate) {

		if (startDate != null && endDate != null) {
			criteria.andOperator(Criteria.where("registerDate").gte(startDate),
					Criteria.where("registerDate").lte(endDate));
		}

		return criteria;
	}

}
