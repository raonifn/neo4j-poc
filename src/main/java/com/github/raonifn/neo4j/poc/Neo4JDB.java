package com.github.raonifn.neo4j.poc;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.Traversal;

public class Neo4JDB {

	private final GraphDatabaseService db;

	public static enum ShelfRelations implements RelationshipType {
		CLOSE;
	}

	public Neo4JDB(String path) {
		db = new GraphDatabaseFactory().newEmbeddedDatabase(path);
	}

	public void shutdown() {
		db.shutdown();
	}

	public GraphDatabaseService getDb() {
		return db;
	}

	public long createShelf(String shelfName, long... relations) {
		Transaction tx = db.beginTx();
		try {
			Node node = createShelfNode(shelfName, relations);
			tx.success();
			return node.getId();
		} finally {
			tx.finish();
		}
	}

	public Node createShelfNode(String shelfName, long... relations) {
		Node node = db.createNode();
		node.setProperty("name", shelfName);

		for (long relation : relations) {
			createRelationship(node, relation);
		}
		return node;
	}

	public void createRelationship(long node1, long node2) {
		Transaction tx = db.beginTx();
		try {
			Node dbNode1 = db.getNodeById(node1);
			createRelationship(dbNode1, node2);

			tx.success();
		} finally {
			tx.finish();
		}
	}

	public int distance(long node1, long node2) {
		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				Traversal.expanderForTypes(ShelfRelations.CLOSE, Direction.BOTH), 100);
		Node dbNode1 = db.getNodeById(node1);
		Node dbNode2 = db.getNodeById(node2);

		Path path = finder.findSinglePath(dbNode1, dbNode2);

		return path.length();
	}

	private void createRelationship(Node node1, long node2) {
		Node dbNode2 = db.getNodeById(node2);
		node1.createRelationshipTo(dbNode2, ShelfRelations.CLOSE);
	}

	public Long next(long node1, long... nodes) {
		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				Traversal.expanderForTypes(ShelfRelations.CLOSE, Direction.BOTH), 100);
		Node dbNode1 = db.getNodeById(node1);

		int min = 9999;
		long minId = 0;
		for (long node : nodes) {
			Node dbNode2 = db.getNodeById(node);
			Path path = finder.findSinglePath(dbNode1, dbNode2);
			if (path.length() < min) {
				minId = dbNode2.getId();
				min = path.length();
			}
		}

		return minId;

	}
}
