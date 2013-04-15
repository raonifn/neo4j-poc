package com.github.raonifn.neo4j.poc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Neo4JDB {

	private final GraphDatabaseService db;

	public Neo4JDB(String dbName) {
		String tmp = System.getProperty("java.io.tmpdir");
		db = new GraphDatabaseFactory().newEmbeddedDatabase(tmp + "/neo4j/" + dbName);
	}

	public void shutdown() {
		db.shutdown();
	}

	public static void main(String[] args) {
		final Neo4JDB neo4jdb = new Neo4JDB("teste");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				neo4jdb.shutdown();
			}
		});
	}
}
