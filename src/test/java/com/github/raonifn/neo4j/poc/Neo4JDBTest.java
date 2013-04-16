package com.github.raonifn.neo4j.poc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class Neo4JDBTest {

	private static Neo4JDB neo4jdb;
	private static List<Long> allIds;

	@BeforeClass
	public static void init() {
		String tmp = System.getProperty("java.io.tmpdir");
		String path = tmp + "/neo4j/teste";
		delete(new File(path));

		neo4jdb = new Neo4JDB(path);
		createData(1000);

	}

	private static void createData(int amount) {
		long beforeTime = System.currentTimeMillis();

		int lineSize = 100;
		int corredores = 10;

		allIds = new ArrayList<Long>(amount);
		List<Node> nodes = new ArrayList<Node>(amount);
		Transaction tx = neo4jdb.getDb().beginTx();
		try {

			for (int index = 0; index < amount; index++) {
				Node node = neo4jdb.createShelfNode("Shelf" + index);
				long shelf = node.getId();
				allIds.add(shelf);
				nodes.add(node);

				if (index == 0) {
					continue;
				}

				// before
				if (index % lineSize > 0) {
					Node before = nodes.get(index - 1);
					node.createRelationshipTo(before, Neo4JDB.ShelfRelations.CLOSE);
				}

				// corridor
				if (index % corredores == 0 && index > lineSize) {
					Node corridor = nodes.get(index - lineSize);
					node.createRelationshipTo(corridor, Neo4JDB.ShelfRelations.CLOSE);
				}

				// front
				int corridorNumber = (index / lineSize);
				if (corridorNumber % 2 == 1) {
					Node corridor = nodes.get(index - lineSize);
					node.createRelationshipTo(corridor, Neo4JDB.ShelfRelations.CLOSE);
				}
			}

			tx.success();
		} finally {
			tx.finish();
		}
		long after = System.currentTimeMillis();
		System.out.println(after - beforeTime);
	}

	@AfterClass
	public static void after() {
		neo4jdb.shutdown();
	}

	private static void delete(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f);
			}
			return;
		}
		file.delete();
	}

	@Test
	public void test1() {
		Long shelf1 = allIds.get(0);
		Long shelf2 = allIds.get(1);

		assertEquals(1, neo4jdb.distance(shelf1, shelf2));
	}

	@Test
	public void test2() {
		Long shelf1 = allIds.get(0);
		Long shelf4 = allIds.get(3);
		assertEquals(3, neo4jdb.distance(shelf1, shelf4));
	}
	
	@Test
	public void testFront() {
		Long shelf2 = allIds.get(1);
		Long shelf102 = allIds.get(101);
		assertEquals(1, neo4jdb.distance(shelf2, shelf102));
	}
	
	@Test
	public void test5() {
		Long shelf109 = allIds.get(108);
		Long shelf209 = allIds.get(208);
		assertEquals(5, neo4jdb.distance(shelf109, shelf209));
	}


	@Test
	public void test3() {
		Long shelf1 = allIds.get(0);
		Long shelf230 = allIds.get(229);
		assertEquals(31, neo4jdb.distance(shelf1, shelf230));
	}

	@Test
	public void testGetNext() {
		Long shelf1 = allIds.get(0);
		Long shelf3 = allIds.get(2);
		Long shelf103 = allIds.get(102);

		assertEquals(shelf3, neo4jdb.next(shelf1, shelf3, shelf103));
	}

	@Test
	public void testGetNextALot() {
		Long shelf1 = allIds.get(0);
		Long shelf3 = allIds.get(2);
		Long shelf103 = allIds.get(102);

		assertEquals(shelf3, neo4jdb.next(shelf1, shelf3, shelf103));
	}

	@Test
	public void testGetNextALot2() {
		Long shelf1 = allIds.get(0);
		Long shelf3 = allIds.get(2);
		Long shelf230 = allIds.get(229);

		assertEquals(shelf3, neo4jdb.next(shelf1, shelf3, shelf230));
	}
}
