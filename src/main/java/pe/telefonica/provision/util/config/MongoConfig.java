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
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

}
