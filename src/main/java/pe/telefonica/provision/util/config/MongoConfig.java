package pe.telefonica.provision.util.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Override
	protected String getDatabaseName() {
		//return System.getenv("TDP_DATA_MONGODB_DATABASE");
		return "compose";
	}

	@Override
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString("mongodb://admin:JOQIORBFYKYVSJTH@portal-ssl1348-50.bmix-dal-yp-a589d8bf-2208-4290-880f-9a48911af4e3.3558158292.composedb.com:23906,portal-ssl1272-51.bmix-dal-yp-a589d8bf-2208-4290-880f-9a48911af4e3.3558158292.composedb.com:23906/compose?authSource=admin&ssl=true");
		//ConnectionString connectionString = new ConnectionString(System.getenv("TDP_DATA_MONGODB_URI"));
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

}
