package com.crunch.util;

import java.net.UnknownHostException;
import java.util.ArrayList;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class MongoHandler {
	
	public static DB initializemongo()
	{
		Mongo mongo=null;
		try{
			mongo = new Mongo("localhost",27017);
		}catch(UnknownHostException uhe){
			uhe.printStackTrace();
		}
		DB db = mongo.getDB("crunchV2");
		
		return db;		
	}
	
	//
	public static void insertintodb(String jsonString, DBCollection collection)
	{		 
		DBObject dbobject = (DBObject) JSON.parse(jsonString);
		
		//correct total money
		String totalmoney = (String) dbobject.get("total_money_raised");
		//System.out.println("totalmoney -> "+totalmoney);
		correctTotRaisedMoney(totalmoney);
		
		//collection.insert(dbobject);		
	}

	public static float correctTotRaisedMoney(String totalMoney)
	{
		char[] totalmoneychar = totalMoney.toCharArray();
		int firstDigitChar=0;
		boolean isdollar;
		char currCode=' ';
		//if it only contains digits
		if(totalMoney.matches("^[0-9]+\\.?[0-9]*$"))
		{
			return Float.parseFloat(totalMoney);			
		}
		
		if(totalMoney.length()<3)
		{
			System.out.println("Total raised money "+"0");
			return 0.0f;
		}
				
		isdollar = (totalmoneychar[0] == '$')?true:false;			
		if(!Character.isDigit(totalmoneychar[totalmoneychar.length-1]))
				currCode = totalmoneychar[totalmoneychar.length-1];
		
		while(!Character.isDigit(totalmoneychar[firstDigitChar]))
		{			
			firstDigitChar++;
		}
		
		//		
		ArrayList<Character> totalMoneylist = new ArrayList<Character>();
		for(int i=firstDigitChar;i<totalmoneychar.length-1;i++)
		{
			char temp = totalmoneychar[i];
			if(Character.isDigit(temp)) 
				totalMoneylist.add(temp);
			else if(temp=='.')
				totalMoneylist.add(temp);
			else
				totalMoneylist.add('0');									
		}
		
		String totalmoneystring="";
		//
		for(int i=0;i<totalMoneylist.size();i++)
		{			
			totalmoneystring+=totalMoneylist.get(i);
		}
		
		float totalraisedmoney=0.0f;
		if(currCode == 'k'|| currCode == 'K')
		{
			 totalraisedmoney = Float.parseFloat(totalmoneystring);
			 totalraisedmoney*=1000;//10 raise to 3
		}
		else if(currCode == 'm'|| currCode == 'M')
		{
						
			totalraisedmoney = Float.parseFloat(totalmoneystring);
			 totalraisedmoney*=1000000;//10 raise to 6
		}
		else if(currCode == 'b'|| currCode == 'B')
		{
			totalraisedmoney = Float.parseFloat(totalmoneystring);
			 totalraisedmoney*=1000000000; //10 raise to 9
		}
		else
		{
			totalraisedmoney = Float.parseFloat(totalmoneystring);
		}
				
		System.out.println("total raised money -> "+totalraisedmoney);
		
		return totalraisedmoney;
	}
}
