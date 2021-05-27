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
		//return "dbProvision";

	}

	@Override
	public MongoClient mongoClient() {
		
		ConnectionString connectionString = new ConnectionString(System.getenv("TDP_DATA_MONGODB_URI"));
		//ConnectionString connectionString = new ConnectionString("mongodb://account-trazabilidad-cert:o3383SBK8f5V7KnEfwff774zyAt704fIpiZ9da9OQ5Momp7zDeoiMLRwNefyWsQlUFKlq7CXH4ytqIQnHG8tUQ==@account-trazabilidad-cert.mongo.cosmos.azure.com:10255/?ssl=true&retrywrites=false&replicaSet=globaldb&maxIdleTimeMS=120000&appName=@account-trazabilidad-cert@");
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

}
