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
		return System.getenv("TDP_DATA_MONGODB_DATABASE");
		//return "compose";
	}

	@Override
	public MongoClient mongoClient() {
			
		ConnectionString connectionString = new ConnectionString(System.getenv("TDP_DATA_MONGODB_URI"));
		//ConnectionString connectionString = new ConnectionString("mongodb://account-trazabilidad-provision-cert:KiJ8oByP0ZMaIglR7Kzno3z2ZzIQllKNe47eJ7Lj0NZLPvlFxUgYbOCJZxRcbPn4pBN2es5awd3Ak6XxpqVvuw==@account-trazabilidad-provision-cert.mongo.cosmos.azure.com:10255/?ssl=true&replicaSet=globaldb&retrywrites=false&maxIdleTimeMS=120000&appName=@account-trazabilidad-provision-cert@");
		
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

}
