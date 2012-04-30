package com.mktneutral.vc;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.json.JSONArray;
import java.util.Locale;
import java.sql.Statement;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import com.opensymphony.xwork2.ActionSupport;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts2.interceptor.ServletRequestAware;

public class AddSecondMarketFact extends ActionSupport implements ServletRequestAware {
  private HttpServletRequest request;
  private Connection conn;
  private Statement stmt;
  private PreparedStatement prepStmt;
  private SecondMarketFact secondMarketFact;
  private InputStream inputStream;
  private JSONObject jsonOutput;

  public String execute() throws Exception {
      /*  if ( request.getParameter("companyName") == null
	  || request.getParameter("city") == null
	  || request.getParameter("state") == null
	  || request.getParameter("companyUrl") == null
	  || request.getParameter("lastFundingDate") == null
	  || request.getParameter("lastFundingAmount") == null
	  || request.getParameter("minDate") == null
	  || request.getParameter("maxDate") == null ) { return SUCCESS; } */

     Class.forName("oracle.jdbc.OracleDriver");
     conn = DriverManager.getConnection("jdbc:oracle:thin:morningstar/uptime5@localhost:1521:XE");
     stmt = conn.createStatement();
     prepStmt = conn.prepareStatement("INSERT INTO second_market_fact_table ( company_name, city, state, second_market_url, company_url, last_funding_date, last_funding_amount, max_month, min_month, unique_visitors, visitor_growth, version ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");   

     setSecondMarketFact();
     insertSecondMarketFact();
     sendJson();
     
     conn.close();

     inputStream = new StringBufferInputStream( jsonOutput.toString() );
     return SUCCESS;
  }

  public void setServletRequest( HttpServletRequest _request ) { request = _request; }

  public void setSecondMarketFact() throws Exception {
     secondMarketFact = new SecondMarketFact();

     SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
     
     secondMarketFact.setCompanyName( request.getParameter("companyName") );
     secondMarketFact.setCity( request.getParameter("city") );
     secondMarketFact.setState( request.getParameter("state") );
     secondMarketFact.setCompanyUrl( request.getParameter("companyUrl") );
     secondMarketFact.setLastFundingDate( new java.sql.Date( dateFmt.parse(request.getParameter("lastFundingDate")).getTime() ) );
     secondMarketFact.setLastFundingAmount( Double.parseDouble(request.getParameter("lastFundingAmount").replace(",","")) );
     secondMarketFact.setMinMonth( new java.sql.Date( dateFmt.parse(request.getParameter("minDate")).getTime() ) );
     secondMarketFact.setMaxMonth( new java.sql.Date( dateFmt.parse(request.getParameter("maxDate")).getTime() ) );
     
     if ( request.getParameter("uniqueVisitors") != null ) {
	 secondMarketFact.setUniqueVisitors( Integer.parseInt(request.getParameter("uniqueVisitors").replace(",","")) );
     }
     else {
         secondMarketFact.setUniqueVisitors( 0 );
     }

     if ( request.getParameter("visitorGrowth") != null ) {
	 secondMarketFact.setVisitorGrowth( Double.parseDouble(request.getParameter("visitorGrowth").replace(",","")) );
     }
     else {
         secondMarketFact.setVisitorGrowth( 0.0 );
     }

     if ( request.getParameter("secondMarketUrl") != null ) {
	 secondMarketFact.setSecondMarketUrl( request.getParameter("secondMarketUrl") );
     }
     else {
         secondMarketFact.setSecondMarketUrl("");
     }
  }

  public void insertSecondMarketFact() throws Exception {
     prepStmt.setString(1,secondMarketFact.getCompanyName());
     prepStmt.setString(2,secondMarketFact.getCity());
     prepStmt.setString(3,secondMarketFact.getState());
     prepStmt.setString(4,secondMarketFact.getSecondMarketUrl());
     prepStmt.setString(5,secondMarketFact.getCompanyUrl());
     prepStmt.setDate(6,secondMarketFact.getLastFundingDate());
     prepStmt.setDouble(7,secondMarketFact.getLastFundingAmount());
     prepStmt.setDate(8,secondMarketFact.getMinMonth());
     prepStmt.setDate(9,secondMarketFact.getMaxMonth());
     prepStmt.setInt(10,secondMarketFact.getUniqueVisitors());
     prepStmt.setDouble(11,secondMarketFact.getVisitorGrowth());
     prepStmt.setInt(12,secondMarketFact.getVersion());

     prepStmt.executeUpdate();
  }

  public void sendJson() throws Exception {
     DecimalFormat dollarFmt = new DecimalFormat("$#,###");
     DecimalFormat vFmt = new DecimalFormat("#,###");
     DecimalFormat pctFmt = new DecimalFormat("#,###.00%");

     jsonOutput = new JSONObject();
     JSONArray recordsArray = new JSONArray();
     try {
	ResultSet rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM second_market_fact_table WHERE company_name='"+secondMarketFact.getCompanyName()+"' ORDER BY id DESC) WHERE rownum<2");

        while ( rs.next() ) {
	    JSONObject obj = new JSONObject();
            obj.put("companyName",rs.getString(1).trim());
            obj.put("city",rs.getString(2).trim());
	    obj.put("state",rs.getString(3).trim());
	    if ( rs.getString(4) != null ) obj.put("secondMarketUrl",rs.getString(4).trim());
	    obj.put("companyUrl",rs.getString(5).trim());
	    obj.put("lastFundingDate",rs.getDate(6).toString());
	    obj.put("lastFundingAmount",dollarFmt.format(rs.getDouble(7)));
	    if ( rs.getDate(8) != null ) obj.put("maxMonth",rs.getDate(8));
	    if ( rs.getDate(9) != null ) obj.put("minMonth",rs.getDate(9));
	    obj.put("uniqueVisitors",vFmt.format(rs.getInt(10)));
	    obj.put("visitorGrowth",pctFmt.format(rs.getDouble(11)));
            recordsArray.put(obj);
        }
        rs.close();

        jsonOutput.put("status","success");
        jsonOutput.put("records",recordsArray);

     } catch ( SQLException sqle ) {
	 jsonOutput.put("status","failed"+sqle.getMessage());
         jsonOutput.put("records",recordsArray);
     }
  }

  public InputStream getInputStream() { return inputStream; }
}