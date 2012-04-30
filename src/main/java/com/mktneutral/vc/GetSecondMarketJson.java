package com.mktneutral.vc;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import org.json.JSONObject; 
import org.json.JSONArray;
import com.opensymphony.xwork2.ActionSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

public class GetSecondMarketJson extends ActionSupport implements ServletRequestAware {
   private static final long serialVersionUID = 1L;
   private HttpServletRequest request;

   private ResultSet rs;
   private Connection conn;
   private Statement stmt;
   private JSONObject outputJson;
   private static DecimalFormat dollarFormat = new DecimalFormat("$#,###");
   private static DecimalFormat visitorFormat = new DecimalFormat("#,###");
   private static DecimalFormat pctFormatter = new DecimalFormat("#,###.00%");
   private InputStream IStream;

   public void setServletRequest( HttpServletRequest _request) {
	request = _request;
   }

   public InputStream getIStream() {
       return IStream;
   }

   public String execute() throws Exception {
      if ( request.getParameter("queryName") == null ) {
          return ERROR;        
      }

      Class.forName("oracle.jdbc.OracleDriver");

      conn = DriverManager.getConnection("jdbc:oracle:thin:morningstar/uptime5@localhost:1521:XE");
      stmt = conn.createStatement();

      doQuery();
      outputJson = new JSONObject();
      buildJsonResponse();

      IStream = new StringBufferInputStream( outputJson.toString() );

      return SUCCESS;
   }

   public void doQuery() throws Exception {
       if ( request.getParameter("queryName").equals("startupQuery") ) {
	   rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM second_market_fact_table WHERE ( state='CA' ) ORDER BY last_funding_date DESC, last_funding_amount DESC) WHERE rownum<30");
       }
       else if ( request.getParameter("queryName").equals("scrollQuery") ) {
	   String startRow = request.getParameter("startRow");
	   String endRow = request.getParameter("endRow");
	   rs = stmt.executeQuery("SELECT * FROM (SELECT a.*, ROWNUM r FROM (SELECT * FROM second_market_fact_table WHERE ( state='CA' ) ORDER BY last_funding_date DESC, last_funding_amount DESC) a WHERE rownum<="+endRow+") WHERE r>="+startRow);
       }
   }

   public void buildJsonResponse() throws Exception {
      JSONArray jsonArray = new JSONArray();
      
      while ( rs.next() ) {
	  JSONObject jsonRecord = new JSONObject();
	  jsonRecord.put("companyName",rs.getString(1).trim());
	  jsonRecord.put("city",rs.getString(2).trim());
	  jsonRecord.put("state",rs.getString(3).trim());
	  if ( rs.getString(4) != null ) jsonRecord.put("secondMarketUrl",rs.getString(4));
	  jsonRecord.put("companyUrl",rs.getString(5));
	  jsonRecord.put("lastFundingDate",rs.getDate(6).toString());
	  jsonRecord.put("lastFundingAmount",dollarFormat.format(rs.getDouble(7)));
	  if ( rs.getDate(8) != null ) jsonRecord.put("maxMonth",rs.getDate(8).toString());
	  if ( rs.getDate(9) != null ) jsonRecord.put("minMonth",rs.getDate(9).toString());
	  jsonRecord.put("uniqueVisitors",visitorFormat.format(rs.getInt(10)));
	  jsonRecord.put("visitorGrowth",pctFormatter.format(rs.getDouble(11)));
	  jsonArray.put( jsonRecord );
      }
      rs.close();

      outputJson.put("records",jsonArray);
   }

}