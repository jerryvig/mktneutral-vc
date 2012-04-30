package com.mktneutral.vc.scrape;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class GetSecondMarketIconUrls {
   private static HtmlUnitDriver driver;
   private static BufferedWriter writer;

   public static void main( String[] args ) {
     try {
	Class.forName("oracle.jdbc.OracleDriver");   
     } catch ( ClassNotFoundException cnfe ) { cnfe.printStackTrace(); }

     Connection conn = null;
     Statement stmt = null;

     try {
	conn = DriverManager.getConnection("jdbc:oracle:thin:morningstar/uptime5@localhost:1521:XE");
        stmt = conn.createStatement();
     }
     catch ( SQLException sqle ) { sqle.printStackTrace(); }
 
     ArrayList<String> urlList = new ArrayList<String>();
    
     try {
       ResultSet rs = stmt.executeQuery("SELECT DISTINCT second_market_url FROM second_market_fact_table WHERE second_market_url IS NOT NULL ORDER BY second_market_url ASC");
       while ( rs.next() ) {
	  urlList.add( rs.getString(1).trim() );
       }
     } catch ( SQLException sqle ) { sqle.printStackTrace(); }

     driver = new HtmlUnitDriver();
     try {
       writer = new BufferedWriter( new FileWriter("/tmp/SecondMarketIconUrls.csv") );
     } catch ( IOException ioe ) { ioe.printStackTrace(); }
       
     for ( String url : urlList ) {
        scrapeUrl( url );
        try {
          Thread.sleep( 300 );
        } catch ( InterruptedException ie ) { ie.printStackTrace(); }
     }

     driver.close();
     
     try {
       writer.close();
     } catch ( IOException ioe ) { ioe.printStackTrace(); }
   }

   public static void scrapeUrl( String _url ) {
       driver.get( _url );

       try {
         WebElement iconDiv = driver.findElementByXPath("//div[@class='sm-icon-border']");
         WebElement iconImg = iconDiv.findElement( By.tagName("img") );
      
         System.out.println( _url + ", " + iconImg.getAttribute("src") ); 
	 writer.write( "\"" + _url + "\",\"" + iconImg.getAttribute("src") + "\"\n" );
       } catch ( IOException ioe ) { ioe.printStackTrace(); }
       catch ( NoSuchElementException nsee ) { nsee.printStackTrace(); }
   }
}