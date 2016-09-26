package com.jin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {


	@Autowired
	protected MongoProperties mongoProperties;
	
	
	@Override
	protected String getDatabaseName() {
		
		return mongoProperties.getDatabase();
	}

	@Override
	public MongoClient mongo() throws Exception {
		
		MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
		builder.connectionsPerHost(10).connectTimeout(3);
		
		MongoClientURI mongoUri = new MongoClientURI(mongoProperties.getUri(),builder);
		
		MongoClient mongo = new MongoClient(mongoUri);
		
		return mongo;
	}
	
	@Override
	public MongoDbFactory mongoDbFactory() throws Exception {
		SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongo(), getDatabaseName());
		simpleMongoDbFactory.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		return simpleMongoDbFactory;
	}
	
}
