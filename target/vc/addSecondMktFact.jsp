<%@ page import="java.sql.DriverManager" %><%@ page import="java.sql.Connection" %><%@ page import="java.sql.PreparedStatement" %><%@ page import="java.sql.ResultSet" %><%@ page import="org.json.JSONObject" %><%@ page import="java.text.DecimalFormat" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="org.json.JSONArray" %><%@ page import="java.util.Locale" %><%@ page import="java.sql.Statement" %><%

if ( request.getParameter("companyName") != null 
     && request.getParameter("city") != null 
     && request.getParameter("state") != null
     && request.getParameter("companyUrl") != null
     && request.getParameter("lastFundingDate") != null
     && request.getParameter("lastFundingAmount") != null
     && request.getParameter("minDate") != null 
     && request.getParameter("maxDate") != null ) {


   Class.forName("oracle.jdbc.OracleDriver");
   Connection conn = DriverManager.getConnection("jdbc:oracle:thin:morningstar/uptime5@localhost:1521:XE");
   Statement stmt = conn.createStatement();
   PreparedStatement prepStmt = conn.prepareStatement("INSERT INTO second_market_fact_table ( company_name, city, state, second_market_url, company_url, last_funding_date, last_funding_amount, max_month, min_month, unique_visitors, visitor_growth, version ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

   SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd",Locale.US);

   String companyName = request.getParameter("companyName"); 
   String city = request.getParameter("city");
   String state = request.getParameter("state");
   String companyUrl = request.getParameter("companyUrl");
   java.sql.Date lastFundingDate = new java.sql.Date( dateFmt.parse(request.getParameter("lastFundingDate")).getTime() );
   double lastFundingAmount = Double.parseDouble(request.getParameter("lastFundingAmount").replace(",",""));
   java.sql.Date minMonth = new java.sql.Date( dateFmt.parse(request.getParameter("minDate")).getTime() );
   java.sql.Date maxMonth = new java.sql.Date( dateFmt.parse(request.getParameter("maxDate")).getTime() );
   int uniqueVisitors = 0;
   double visitorGrowth = 0.0;
   String secondMarketUrl = "";
   int version = 1;

   if ( request.getParameter("uniqueVisitors") != null ) {
     uniqueVisitors = Integer.parseInt(request.getParameter("uniqueVisitors").replace(",",""));
   }
   if ( request.getParameter("visitorGrowth") != null ) {
     visitorGrowth = Double.parseDouble(request.getParameter("visitorGrowth").replace(",",""));
   }
   if ( request.getParameter("secondMarketUrl") != null ) {
     secondMarketUrl = request.getParameter("secondMarketUrl");
   }

   prepStmt.setString(1,companyName);
   prepStmt.setString(2,city);
   prepStmt.setString(3,state);
   prepStmt.setString(4,secondMarketUrl);
   prepStmt.setString(5,companyUrl);
   prepStmt.setDate(6,lastFundingDate);
   prepStmt.setDouble(7,lastFundingAmount);
   prepStmt.setDate(8,minMonth);
   prepStmt.setDate(9,maxMonth);
   prepStmt.setInt(10,uniqueVisitors);
   prepStmt.setDouble(11,visitorGrowth);
   prepStmt.setInt(12,version);
   
   JSONArray jsonArray = new JSONArray();
   JSONObject records = new JSONObject();   

   try {
     prepStmt.executeUpdate();
     prepStmt.clearParameters();

     DecimalFormat dollarFmt = new DecimalFormat("$#,###");
     DecimalFormat vFmt = new DecimalFormat("#,###");
     DecimalFormat pctFmt = new DecimalFormat("#,###.00%");

     ResultSet rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM second_market_fact_table WHERE company_name='"+companyName+"' ORDER BY id DESC) WHERE rownum<2");
     while ( rs.next() ) {
       JSONObject jsonObj = new JSONObject();
       jsonObj.put("companyName",rs.getString(1).trim());
       jsonObj.put("city",rs.getString(2).trim());
       jsonObj.put("state",rs.getString(3).trim());
       if ( rs.getString(4) != null ) jsonObj.put("secondMarketUrl",rs.getString(4).trim());
       jsonObj.put("companyUrl",rs.getString(5).trim());
       jsonObj.put("lastFundingDate",rs.getDate(6).toString());
       jsonObj.put("lastFundingAmount",dollarFmt.format(rs.getDouble(7)));
       if ( rs.getDate(8) != null ) jsonObj.put("maxMonth",rs.getDate(8));
       if ( rs.getDate(9) != null ) jsonObj.put("minMonth",rs.getDate(9));
       jsonObj.put("uniqueVisitors",vFmt.format(rs.getInt(10)));
       jsonObj.put("visitorGrowth",pctFmt.format(rs.getDouble(11)));
       jsonArray.put(jsonObj);
     }
     rs.close();

     records.put("status","success");
     records.put("records",jsonArray);
     out.print(records.toString());
   } catch ( Exception e ) {
      records.put("status","insert_query_failed");
      records.put("records",jsonArray);
      out.print(records.toString());
   }
   conn.close();
}
else {
  JSONArray jsonArray = new JSONArray();
  JSONObject records = new JSONObject();
  records.put("status","missing_get_parameter");
  records.put("records",jsonArray);
  out.print(records.toString());
} %>