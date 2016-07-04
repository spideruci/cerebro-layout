package org.spideruci.cerebro.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseReader {
	private static Connection c = null;
	private int stmtSize;
	private int testcaseSize;
	private Map<Integer, Map<Integer, Integer>> stmt = new LinkedHashMap<>();
	private Map<Integer, Double> suspicious = new HashMap<>();
	private Map<Integer, Double> confidence = new HashMap<>();
	private Map<String, Integer> source = new HashMap<>();

	public DatabaseReader(Connection c){
		this.c = c;
	}
	
	public Map<Integer, Map<Integer, Integer>> getStmtMap(){
		return stmt;
	}
	
	public Map<Integer, Double> getSuspiciousMap(){
		return suspicious;
	}
	
	public Map<Integer, Double> getConfidenceMap(){
		return confidence;
	}
	
	public Map<String, Integer> getSourceMap(){
		return source;
	}
	
	public void getSTMT(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * FROM STMT";
			ResultSet rs = s.executeQuery(sql);

			System.out.println("Getting the STMT table");
						
			while(rs.next()){
				int stmtID = rs.getInt("STMT_ID");
				int sourceID = rs.getInt("SOURCE_ID");
				int lineNum = rs.getInt("LINE_NUM");
				
				Map<Integer, Integer> temp = stmt.get(sourceID);

				if(temp == null){
					temp = new HashMap<>();
				}
				
				temp.put(lineNum, stmtID);

				stmt.put(sourceID, temp);
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	public void getSuspicious(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * FROM SUSPICIOUS";
			ResultSet rs = s.executeQuery(sql);

			System.out.println("Getting the SUSPICIOUS table");
			
//			rs.getFetchSize()
			
			while(rs.next()){
				int stmtID = rs.getInt("STMT_ID");
				double suspiciousValue = rs.getDouble("SUSPICIOUS");
				
				suspicious.put(stmtID, suspiciousValue);
								
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getConfidence(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * FROM CONFIDENCE";
			ResultSet rs = s.executeQuery(sql);

			System.out.println("Getting the CONFIDENCE table");
			
//			rs.getFetchSize()
			
			while(rs.next()){
				int stmtID = rs.getInt("STMT_ID");
				double confidenceValue = rs.getDouble("CONFIDENCE");
				
				confidence.put(stmtID, confidenceValue);
								
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getSource(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * FROM SOURCE";
			ResultSet rs = s.executeQuery(sql);

			System.out.println("Getting the SOURCE table");
			
//			rs.getFetchSize()
			
			while(rs.next()){
				int sourceID = rs.getInt("SOURCE_ID");
				String fqn = rs.getString("FQN");
				
				source.put(fqn, sourceID);
								
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
