package gg.sep.securityrobot.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.Getter;

import gg.sep.securityrobot.config.models.MongoDBConfig;

/**
 * Wrapper around a MongoDB client.
 */
public class MongoWrapper {

    @Getter private final MongoClient mongoClient;
    @Getter private final MongoDBConfig mongoDBConfig;

    /**
     * Crate a new MongoDB client and wrapper from the specified MongoDB configuration.
     * @param mongoDBConfig MongoDB configuration.
     */
    public MongoWrapper(final MongoDBConfig mongoDBConfig) {
        this.mongoDBConfig = mongoDBConfig;

        final ServerAddress address = new ServerAddress(mongoDBConfig.getHost(), mongoDBConfig.getPort());
        final MongoCredential creds = MongoCredential.createCredential(
            mongoDBConfig.getUser(),
            mongoDBConfig.getDatabase(),
            mongoDBConfig.getPassword().toCharArray()
        );

        final MongoClientOptions options = MongoClientOptions.builder()
            .sslEnabled(false)
            .build();
        this.mongoClient = new MongoClient(address, creds, options);
    }
}
