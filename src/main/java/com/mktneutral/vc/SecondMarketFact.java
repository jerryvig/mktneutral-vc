package com.mktneutral.vc;

import java.sql.Date;

public class SecondMarketFact {
    private String companyName;
    private String city;
    private String state;
    private String companyUrl;
    private Date lastFundingDate;
    private double lastFundingAmount;
    private Date minMonth;
    private Date maxMonth;
    private int uniqueVisitors;
    private double visitorGrowth;
    private String secondMarketUrl;
    private int version;

    public SecondMarketFact() {
	uniqueVisitors = 0;
        visitorGrowth = 0.0;
        version = 1;
    }

    public void setCompanyName( String _companyName ) { companyName = _companyName; }
    public String getCompanyName() { return companyName; }

    public void setCity( String _city ) { city = _city; }
    public String getCity() { return city; }

    public void setState( String _state ) { state = _state; }
    public String getState() { return state; }

    public void setCompanyUrl( String _url ) { companyUrl = _url; }
    public String getCompanyUrl() { return companyUrl; }

    public void setLastFundingDate( Date _date ) { lastFundingDate = _date; }
    public Date getLastFundingDate() { return lastFundingDate; }

    public void setLastFundingAmount( double _amt ) { lastFundingAmount = _amt; }
    public double getLastFundingAmount() { return lastFundingAmount; }

    public void setMinMonth( Date _date ) { minMonth = _date; }
    public Date getMinMonth() { return minMonth; }

    public void setMaxMonth( Date _date ) { maxMonth = _date; }
    public Date getMaxMonth() { return maxMonth; }

    public void setUniqueVisitors( int _visitors ) { uniqueVisitors = _visitors; }
    public int getUniqueVisitors() { return uniqueVisitors; }

    public void setVisitorGrowth( double _growth ) { visitorGrowth = _growth; }
    public double getVisitorGrowth() { return visitorGrowth; }
  
    public void setSecondMarketUrl( String _url ) { secondMarketUrl = _url; }
    public String getSecondMarketUrl() { return secondMarketUrl; }

    public void setVersion( int _version ) { version = _version; }
    public int getVersion() { return version; }
}