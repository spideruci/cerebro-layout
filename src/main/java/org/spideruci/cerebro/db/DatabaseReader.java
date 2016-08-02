package org.spideruci.cerebro.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import scala.Array;

public class DatabaseReader {
	private static Connection c = null;
	private List<String> authorList = new ArrayList<>();
	private Map<Integer, Map<Integer, Integer>> stmt = new LinkedHashMap<>();
	private Map<Integer, Map<Integer, String>> authorInfo = new LinkedHashMap<>();
	private Map<Integer, Double> suspicious = new HashMap<>();
	private Map<Integer, Double> confidence = new HashMap<>();
	private Map<String, Integer> source = new HashMap<>();
	private Map<Integer, Integer> sourceLine = new HashMap<>();

	public DatabaseReader(Connection c){
		this.c = c;
	}
	
	public Map<Integer, Map<Integer, Integer>> getStmtMap(){
		return stmt;
	}
	
	public Map<Integer, Map<Integer, String>> getAuthorMap(){
		return authorInfo;
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
	
	public Map<Integer, Integer> getSourceLineMap(){
		return sourceLine;
	}
	
	public int getAuthorCount(){
		return authorList.size();
	}
	
	public List<String> getAuthorList(){
		return authorList;
	}
	
	public void getSTMT(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * FROM STMT";
			ResultSet rs = s.executeQuery(sql);

//			System.out.println("Getting the STMT table");
						
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

//			System.out.println("Getting the SUSPICIOUS table");
						
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

//			System.out.println("Getting the CONFIDENCE table");
						
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

//			System.out.println("Getting the SOURCE table");
						
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
	
	public void getSourceLineAuthor(){
		Statement s;
		try {
			s = c.createStatement();
			String sql = "SELECT * FROM SOURCE_LINE_AUTHOR";
			ResultSet rs = s.executeQuery(sql);

//			System.out.println("Getting the SOURCE_LINE_AUTHOR table");
						
			while(rs.next()){
				int sourceID = rs.getInt("SOURCE_ID");
				int lineNum = rs.getInt("LINE_NUM");
				String authorship = rs.getString("AUTHOR");
				
				Map<Integer, String> temp = authorInfo.get(sourceID);

				if(temp == null){
					temp = new HashMap<>();
				}
				
				temp.put(lineNum, authorship);

				authorInfo.put(sourceID, temp);
												
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getAuthor(){
		Statement s;
		
		try {
			s = c.createStatement();
			String sql = "SELECT DISTINCT AUTHOR FROM SOURCE_LINE_AUTHOR";
			ResultSet rs = s.executeQuery(sql);
			
//			System.out.println("Getting the SOURCE_LINE_AUTHOR table");

			while(rs.next()){
				String authorship = rs.getString("AUTHOR");
				
				authorList.add(authorship);	
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void getSourceLineNum(){
		Statement s;
		
		try {
			s = c.createStatement();
			String sql = "SELECT SOURCE_ID, LINE_NUM FROM SOURCE_LINE_AUTHOR";
			ResultSet rs = s.executeQuery(sql);
			
//			System.out.println("Getting the SOURCE_LINE_AUTHOR table");
			
			while(rs.next()){
				int sourceId = rs.getInt("SOURCE_ID");
				int lineNum = rs.getInt("LINE_NUM");
				
				if(sourceLine.get(sourceId) == null)
					sourceLine.put(sourceId, lineNum);
				
				else if(sourceLine.get(sourceId) < lineNum)
					sourceLine.put(sourceId, lineNum);
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Map<Integer, List<Integer>> getSourceLineByAuthor(String author){
		Statement s;
		Map<Integer, List<Integer>> sourceLineByAuthor = new HashMap<>();

		try {
			s = c.createStatement();

			String sql = "SELECT * FROM SOURCE_LINE_AUTHOR_HISTORY WHERE "
					+"AUTHOR LIKE \"%" + author + "%\"";

			ResultSet rs = s.executeQuery(sql);
			
//			System.out.println("Getting the SOURCE_LINE_AUTHOR table");
			
			while(rs.next()){
				int sourceId = rs.getInt("SOURCE_ID");
				int lineNum = rs.getInt("LINE_NUM");
				
				if(sourceLineByAuthor.get(sourceId) == null)
					sourceLineByAuthor.put(sourceId, new ArrayList<Integer>());
				
				sourceLineByAuthor.get(sourceId).add(lineNum);
			}
			
			s.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sourceLineByAuthor;
		
	}
	
	
	protected ResultSet executePsmt(String sql, Object... args) {
		ResultSet rs = null;
		
		try{
			PreparedStatement psmt = c.prepareStatement(sql);
			for(int i=0; i<args.length; ++ i){
				psmt.setObject(i+1, args[i]);
			}
			rs = psmt.executeQuery();			
			psmt.close();
		}catch(Exception e){
			System.out.println(sql);
			e.printStackTrace();
		}
		
		return rs;
	}
	
}
