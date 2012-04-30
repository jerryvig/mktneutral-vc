package com.mktneutral.user.action;

import com.opensymphony.xwork2.ActionSupport;

public class LoginAction extends ActionSupport {
    private String username;
    private String password;

    public String getPassword() {
	return password;
    }

    public void setPassword(String _password) {
        password = _password;
    }

    public String getUsername(){
	return username;
    }

    public void setUsername(String _username) {
	username = _username;
    }

    //business logic
    public String execute() {
	return SUCCESS;
    }

    public void validate() {
	if ( "".equals(getUsername()) ) {
	    addFieldError( "username.required", getText("username.reqd") );
        }
        if ( "".equals(getPassword()) ) {
	    addFieldError( "password.required", getText("password.reqd") );
        }
    }
}