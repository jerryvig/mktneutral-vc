<%@ page import="java.sql.DriverManager" %><%@ page import="java.sql.Connection" %><%@ page import="java.sql.Statement" %><%@ page import="java.sql.ResultSet" %><%@ page import="org.json.JSONObject" %><%@ page import="java.text.DecimalFormat" %><%@ page import="org.json.JSONArray" %><%

if ( request.getParameter("queryName") != null ) {
   Class.forName("oracle.jdbc.OracleDriver");
   Connection conn = DriverManager.getConnection("jdbc:oracle:thin:morningstar/uptime5@localhost:1521:XE");
   Statement stmt = conn.createStatement();
  
   DecimalFormat dollarFormat = new DecimalFormat("$#,###");
   DecimalFormat visitorFormat = new DecimalFormat("#,###");
   DecimalFormat pctFormatter = new DecimalFormat("#,###.00%");

   ResultSet rs = null;
   if ( request.getParameter("queryName").equals("startupQuery") ) {
      rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM second_market_fact_table WHERE ( state='CA' ) ORDER BY last_funding_date DESC, last_funding_amount DESC) WHERE rownum<30");
   }
   else if ( request.getParameter("queryName").equals("scrollQuery") ) {
     String startRow = request.getParameter("startRow");
     String endRow = request.getParameter("endRow");
     rs = stmt.executeQuery("SELECT * FROM (SELECT a.*, ROWNUM r FROM (SELECT * FROM second_market_fact_table WHERE ( state='CA' ) ORDER BY last_funding_date DESC, last_funding_amount DESC) a WHERE rownum<="+endRow+") WHERE r>="+startRow);
   }

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

    JSONObject records = new JSONObject();
    records.put("records",jsonArray);
    out.print( records.toString() );

   conn.close();
} %>