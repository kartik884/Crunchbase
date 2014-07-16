package com.crunch.dao;


import java.util.List;
import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.stereotype.Repository;

import com.crunch.config.MongoConfig;
import com.crunch.pojo.Company;
import com.crunch.util.MongoHandler;
import com.mongodb.BasicDBObject;
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
	
	//THIS WAS TO CHECK AND CORRECT THE TOTAL_MONEY_RAISED 
	public static void main(String[] args)
	{
		MongoConfig m = new MongoConfig();
		CompanyDAO c;
		try {
			c = new CompanyDAO();
			c.mongoOperations = m.mongoOperations();
			
		
		for(Company com: c.getTopCompanies(100))
		{
			System.out.println(com.toString());			
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
