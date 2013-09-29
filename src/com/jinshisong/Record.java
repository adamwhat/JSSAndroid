package com.jinshisong;

import java.io.Serializable;

public class Record implements Serializable{
	boolean success = false;
	String serverURL;
	String method;
	Order currentOrder;
	String value;

	
	public Record(String serverURL, String method, Order currentOrder, String value)
	{
		this.serverURL = serverURL;
		this.method = method;
		this.currentOrder = currentOrder;
		this.value = value;
	}
}
