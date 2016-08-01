package org.spideruci.cerebro.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseWriter {
	private static Connection c = null;

	private Map<Integer, Map<Integer, Integer>> stmt = new LinkedHashMap<>();
	private Map<String, Integer> source = new HashMap<>();
	private Map<String, Integer> aid = new HashMap<String, Integer>();

	private static int AuthorID = 0; 

	public DatabaseWriter(Connection c) {
		this.c = c;
	}
	
	private int getAuthorID(String author){

		int id;
		if(!aid.containsKey(author)){
			id = AuthorID++;
			aid.put(author, id);
		}
		else id = aid.get(author);
		return id;
	}
	
	
	public void createAuthorTable(){
		String authorTable="CREATE TABLE IF NOT EXISTS `SOURCE_LINE_AUTHOR` ( "
				+ "`AUTHOR_ID`		INTEGER,"
				+ "`AUTHOR`	    	TEXT"			
				+ ");";

		execute(authorTable);
		
		System.out.println("Create AUTHOR table");
	}

	public void insertAuthorTable(String author){
		String sql = "INSERT INTO AUTHOR "
				+"VALUES(?,?)";
		executePsmt(sql, getAuthorID(author), author);
	}
	
	public void createSourceLineAuthorTable(){
		String sourceLineAuthorTable="CREATE TABLE IF NOT EXISTS `SOURCE_LINE_AUTHOR` ( "
				+ "`SOURCE_ID`		INTEGER,"
				+ "`LINE_NUM`	    INTEGER,"
				+ "`AUTHOR_ID`	    INTEGER,"
				+ "`DATE`	    	TEXT"				
				+ ");";

		execute(sourceLineAuthorTable);
		
		System.out.println("Create Source_LINE_AUTHOR table");
	}

	public void insertSourceLineAuthorTable(int sourceId, int lineNum, String author, String date){
		System.out.println("Insert source " + sourceId + " line " + lineNum);
		String sql = "INSERT INTO SOURCE_LINE_AUTHOR "
				+"VALUES(?,?,?,?)";
		executePsmt(sql, sourceId, lineNum, author, date);
	}

	public void createSourceLineAuthorHistoryTable(){
		String sourceLineAuthorTable="CREATE TABLE IF NOT EXISTS `SOURCE_LINE_AUTHOR_HISTORY` ( "
				+ "`SOURCE_ID`		INTEGER,"
				+ "`LINE_NUM`	    INTEGER,"
				+ "`AUTHOR_ID`	    INTEGER,"
				+ "`DATE`	    	TEXT"
				+ ");";

		execute(sourceLineAuthorTable);
		
		System.out.println("Create Source_LINE_AUTHOR_HISTORY table");
	}

	public void insertSourceLineAuthorHistoryTable(int sourceId, int lineNum, String author){
		System.out.println("Insert source " + sourceId + " line " + lineNum + " author " + author);
		String sql = "INSERT INTO SOURCE_LINE_AUTHOR_HISTORY "
				+"VALUES(?,?,?)";
		executePsmt(sql, sourceId, lineNum, author);
	}
	
	
	protected void execute(String query){
		try {
			Statement s = c.createStatement();
			s.execute(query);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	protected void executePsmt(String sql, Object... args) {
		try{
			PreparedStatement psmt = c.prepareStatement(sql);
			for(int i=0; i<args.length; ++ i){
				psmt.setObject(i+1, args[i]);
			}
			psmt.executeUpdate();
			psmt.close();
		}catch(Exception e){
			System.out.println(sql);
			e.printStackTrace();
		}
	}
	
}
