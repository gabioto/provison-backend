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
		//ConnectionString connectionString = new ConnectionString("mongodb://admin:SNOKIDFOQWHKSMXO@portal-ssl1346-52.bmix-dal-yp-b97ae098-6774-4b4d-8fee-2d41318ea29e.3558158292.composedb.com:23712,portal-ssl1226-53.bmix-dal-yp-b97ae098-6774-4b4d-8fee-2d41318ea29e.3558158292.composedb.com:23712/compose?authSource=admin&ssl=true");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
        
        return MongoClients.create(mongoClientSettings);
	}
}
