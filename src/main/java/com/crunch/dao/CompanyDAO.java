package com.crunch.dao;


import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.stereotype.Repository;

import com.crunch.config.MongoConfig;
import com.crunch.pojo.Company;
import com.crunch.pojo.FundRaisedPerYearPerRound;
import com.crunch.util.MongoHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Repository
public class CompanyDAO {
	MongoOperations mongoOperations;
	MongoConfig mongoConfig;
	
	public CompanyDAO() throws Exception{		
			mongoConfig = new MongoConfig();		
			mongoOperations = mongoConfig.mongoOperations();		
	}
	
	public List<Company> getTopCompanies(int limit)
	{
		//31835
		//To partial fill DAO with the selected fields just add the field to the query 
		Query q = new Query();		
		Sort s = new Sort(Direction.DESC,"total_money_raised");
		q.fields().include("name");
		q.fields().include("total_money_raised");
		q.with(s);		
		q.limit(limit);
		List<Company> comdaolist;
		comdaolist = mongoOperations.find(q, Company.class);
		
		return comdaolist;
		
	}
	
	//used to correct the field total_money_raised the company collection
	public static void correctTotalRaisedMoney()
	{
		
		ArrayList<String> objectids = new ArrayList<String>();
		
		//initialize mongo connection
		DB db = MongoHandler.initializemongo();
		DBCollection collection = db.getCollection("company");			
		DBCursor cursor = collection.find();
		
		collection.createIndex(new BasicDBObject("total_money_raised",-1));
		cursor.sort(new BasicDBObject("total_money_raised",-1));
		int count =0;
		
		//get all the ids
		while(cursor.hasNext() && count<208)
		{
			DBObject dbobj = (DBObject) cursor.next();
			objectids.add(dbobj.get("_id").toString());
			count++;
			
		}						
		cursor.close();
//		
//		//iterate over the collection
		count=0;
		for(String id:objectids)
		{
			count++;
			DBObject dbobject = findDocumentById(collection,id);
			
			String name = dbobject.get("name").toString();
			System.out.println("Cursor name: "+name);
		
			BasicDBObject newobject = new BasicDBObject();
			String totalMoney="";
			if(dbobject.get("total_money_raised") != null)
				totalMoney = dbobject.get("total_money_raised").toString();

			
			//inserting
			if(totalMoney.length()>1 || totalMoney != null )
			{
				System.out.println("total money: "+totalMoney);
				float totalmoney = MongoHandler.correctTotRaisedMoney(totalMoney);
								
				newobject.append("$set", new BasicDBObject().append("total_money_raised", totalmoney));
								
				BasicDBObject searchQuery = new BasicDBObject().append("_id",new ObjectId(id));//to update duplicate entries 
				//BasicDBObject searchQuery = new BasicDBObject().append("name",name);
				collection.update(searchQuery, newobject);
				
				System.out.println("count: "+count);
				System.out.println();			
			}			
		}
		
		
//		while(cursor.hasNext() && count < 300)
//		{
//			count++;
//			DBObject dbobject = (DBObject)cursor.next();
//			
//			String name = dbobject.get("name").toString();
//			System.out.println("Cursor name: "+name);
//			
//			BasicDBObject newobject = new BasicDBObject();
//			String totalMoney="";
//			if(dbobject.get("total_money_raised") != null)
//				totalMoney = dbobject.get("total_money_raised").toString();
//
//			
//			//inserting
//			if(totalMoney.length()>1 || totalMoney != null )
//			{
//				System.out.println("total money: "+totalMoney);
//				float totalmoney = MongoHandler.correctTotRaisedMoney(totalMoney);
//								
//				newobject.append("$set", new BasicDBObject().append("total_money_raised", totalmoney));
//				BasicDBObject searchQuery = new BasicDBObject().append("name",name);
//				collection.update(searchQuery, newobject);
//								
//				System.out.println("count: "+count);
//				System.out.println();			
//			}			
//		}
//		cursor.close();

	}
	
	public static DBObject findDocumentById(DBCollection collection,String id) {
	    BasicDBObject query = new BasicDBObject();
	    query.put("_id", new ObjectId(id));
	    DBObject dbObj = collection.findOne(query);
	    return dbObj;
	}
	
	
	public JSONArray getFundRaisedByYear()
	{
		/*class FundYearly{
			int _id;
			double moneyraised;
		}
		
		AggregationOperation project = Aggregation.project("funding_rounds.raised_amount").and("funding_rounds.funded_year");
		AggregationOperation unwind = Aggregation.unwind("funding_rounds");
		AggregationOperation group = Aggregation.group("funding_rounds.funded_year").avg("funding_rounds.raised_amount").as("avg_money_raised_yearly");
		AggregationOperation sort = Aggregation.sort(Sort.Direction.ASC, "_id");
		
		Aggregation aggregation = Aggregation.newAggregation(project,unwind,group,sort);
		//Aggregation aggregation = Aggregation.newAggregation(project);
		AggregationResults<FundYearly> aggResult = mongoOperations.aggregate(aggregation, "company", FundYearly.class);
		List<FundYearly> temp = aggResult.getMappedResults();
		System.out.println("temp: "+temp.get(1));*/
		
		String jsoncommand = "{aggregate:\"company\", pipeline:[{$project:{\"funding_rounds.raised_amount\":1,\"funding_rounds.funded_year\":1}}," +
				"{$unwind:\"$funding_rounds\"},{$group:{_id:\"$funding_rounds.funded_year\",avg_money_raised_yearly:{$avg:\"$funding_rounds.raised_amount\"}}}," +
				"{$sort:{_id:1}}]}";
		
		CommandResult cmdresult = mongoOperations.executeCommand(jsoncommand);
		JSONParser parser;
		JSONObject jsonobj;
		JSONArray jsonarray =  null;
		
		try
		{
			
			parser = new JSONParser(); 
			jsonobj =  (JSONObject)parser.parse(cmdresult.toString());
			
			jsonarray = (JSONArray)jsonobj.get("result");
			System.out.println("aggregation result: "+cmdresult.toString());
		}
		catch(Exception pex){
			pex.printStackTrace();
		}
		
		
		return jsonarray;
		
	}
	
	
	public ArrayList<FundRaisedPerYearPerRound> getFundRaisedPerYearPerRound(){
		String jsonCommand = "{aggregate:\"company\", pipeline:[{$match:{\"funding_rounds\":{$not:{$size:0}}}}," +
				"{$project:{\"funding_rounds\":1}},{$unwind:\"$funding_rounds\"},{$group:{_id:'$funding_rounds.round_code',roundcount:{$sum:1}," +
				"funding_round_details:{$push:{fundingYear:\"$funding_rounds.funded_year\",raisedAmount:\"$funding_rounds.raised_amount\"}}}}]}";
		
		CommandResult cmdresult = mongoOperations.executeCommand(jsonCommand);
		
		JSONParser parser;
		JSONObject jsonobj;
		JSONArray jsonarray =  null;
		ArrayList<FundRaisedPerYearPerRound> fundperyearperroundlist = new ArrayList<FundRaisedPerYearPerRound>();
		try{
			parser = new JSONParser(); 
			jsonobj =  (JSONObject)parser.parse(cmdresult.toString());
			jsonarray = (JSONArray)jsonobj.get("result");
			
			HashMap<Integer, Double> year_fundraised ;
			
			for(int i=0;i<jsonarray.size();i++)
			{
				JSONObject tempobj = (JSONObject)jsonarray.get(i);
				FundRaisedPerYearPerRound frpypr  = new FundRaisedPerYearPerRound();
				frpypr.setRound(tempobj.get("_id").toString());
				JSONArray fundingrounddetails = (JSONArray) tempobj.get("funding_round_details");
				year_fundraised = new HashMap<Integer, Double>();
				
				for(int j=0;j<fundingrounddetails.size();j++)
				{
					JSONObject tempfundraiseddetobj = (JSONObject) fundingrounddetails.get(j);
					int year =  Integer.parseInt(tempfundraiseddetobj.get("fundingYear").toString());
					double moneyraised=0.0;
					if(tempfundraiseddetobj.get("raisedAmount") != null){
						
						moneyraised = Double.parseDouble(tempfundraiseddetobj.get("raisedAmount").toString());	
					}
					
					
					if(year_fundraised.containsKey(year)){
						double tmpmoneyraised = year_fundraised.get(year);
						tmpmoneyraised+=moneyraised;
						year_fundraised.put(year, tmpmoneyraised);
					}else{						
						year_fundraised.put(year, moneyraised);						
					}				
					
				}
				
				frpypr.setHmRoundFundRaised(year_fundraised);
				fundperyearperroundlist.add(frpypr);
				
				//System.out.println("fundingrounddetails: "+fundingrounddetails);				
			}
			
		}catch(Exception pex){
			pex.printStackTrace();
		}
		return fundperyearperroundlist;	
	}
	
	
	//THIS WAS TO CHECK AND CORRECT THE TOTAL_MONEY_RAISED 
	public static void main(String[] args)
	{
		MongoConfig m = new MongoConfig();
		CompanyDAO c;
		try {
			c = new CompanyDAO();
			c.mongoOperations = m.mongoOperations();
			
		
		/*for(Company com: c.getTopCompanies(100))
		{
			System.out.println(com.toString());			
		}*/
			
		//c.getFundRaisedByYear();
		c.getFundRaisedPerYearPerRound();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
