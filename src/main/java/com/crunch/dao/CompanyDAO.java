package com.crunch.dao;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.crunch.config.MongoConfig;
import com.crunch.pojo.Company;
import com.crunch.pojo.FundRaisedPerYearPerRound;
import com.crunch.pojo.InvestorsInfo;
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
	static HashMap<String, InvestorsInfo> invInfo = null;
	
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
	}
	
	public static DBObject findDocumentById(DBCollection collection,String id) {
	    BasicDBObject query = new BasicDBObject();
	    query.put("_id", new ObjectId(id));
	    DBObject dbObj = collection.findOne(query);
	    return dbObj;
	}
	
	
	public JSONArray getFundRaisedByYear()
	{		
		/*String jsoncommand = "{aggregate:\"company\", pipeline:[{$project:{\"funding_rounds.raised_amount\":1,\"funding_rounds.funded_year\":1}}," +
				"{$unwind:\"$funding_rounds\"},{$group:{_id:\"$funding_rounds.funded_year\",avg_money_raised_yearly:{$avg:\"$funding_rounds.raised_amount\"}}}," +
				"{$sort:{_id:1}}]}";*/
		
		String jsoncommand = "{aggregate: \"fundingrounds\",pipeline: [{$group: {_id: \"$data.properties.announced_on_year\"," +
				"avg_money_raised: {$avg: \"$data.properties.money_raised_usd\"}}},{$sort: {_id: 1}}]}"; 
		
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
		/*String jsonCommand = "{aggregate:\"company\", pipeline:[{$match:{\"funding_rounds\":{$not:{$size:0}}}}," +
				"{$project:{\"funding_rounds\":1}},{$unwind:\"$funding_rounds\"},{$group:{_id:'$funding_rounds.round_code',roundcount:{$sum:1}," +
				"funding_round_details:{$push:{fundingYear:\"$funding_rounds.funded_year\",raisedAmount:\"$funding_rounds.raised_amount\"}}}}]}";*/
		
		String jsonCommand = "{aggregate:\"fundingrounds\",pipeline:[{$group:{_id:\"$data.properties.funding_type\",roundcount:{$sum:1}," +
				"funding_round_details:{$push:{fundingYear:\"$data.properties.announced_on_year\"," +
				"raisedAmount:\"$data.properties.money_raised_usd\"}}}}]}";
		
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
			}
			/*ObjectWriter  ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			System.out.println("fundperyearperroundlist TO JSON String --> "+ow.writeValueAsString(fundperyearperroundlist));*/
			
		}catch(Exception pex){
			pex.printStackTrace();
		}
		return fundperyearperroundlist;	
	}
	
	public HashMap<String, InvestorsInfo> getTypesOfInvestors(){
		
		if(invInfo!= null)
		{
			return invInfo;
		}
		String jsonCommand = "{aggregate:\"company\", pipeline:[" +
				"{$match:{\"data.properties.number_of_investments\":{$gt:0}}}," +
				"{$project:{\"data.properties\":1,\"data.relationships.investments\":1}}," +
				"{$group:{_id:\"$data.properties.primary_role\",list:{$push:{data:\"$data\"}},count:{$sum:1}}}]}";
		
		CommandResult cmdresult = mongoOperations.executeCommand(jsonCommand);
		JSONParser parser;
		JSONObject jsonobj;
		JSONArray jsonarray =  null;
		JSONArray investersList=null;
		
		HashMap<String, InvestorsInfo> hm = new HashMap<String, InvestorsInfo>();
		
		try
		{			
			parser = new JSONParser(); 
			jsonobj =  (JSONObject)parser.parse(cmdresult.toString());		
			jsonarray = (JSONArray)jsonobj.get("result");
					
			//loop to iterate over the types of startup
			for(int i=0;i<jsonarray.size();i++){
				JSONObject temp = (JSONObject) jsonarray.get(i);
				String type = temp.get("_id").toString();
				investersList =  (JSONArray) temp.get("list");				
				InvestorsInfo invInfo = new InvestorsInfo();
				HashMap<Integer, Integer> yearNoofFunds = new HashMap<Integer, Integer>();
				ArrayList<String> fundingRoundsArrList = new ArrayList<String>();
				System.out.println("Type "+type);
				
				//loop over investments made by a particular type of investor
				for(int j=0;j<investersList.size();j++){
					JSONObject investerInfoObj = (JSONObject)investersList.get(j);										
					invInfo.setType(type);
					invInfo.setOrganizationName(((JSONObject)((JSONObject)investerInfoObj.get("data")).get("properties")).get("name").toString());
					
					JSONObject relationShipobj = (JSONObject)(((JSONObject)investerInfoObj.get("data")).get("relationships"));					
					if(relationShipobj.containsKey("investments"))
					{
						//System.out.println("investment item");
						JSONObject investmentObj = (JSONObject) relationShipobj.get("investments"); 
						JSONArray itemArrItems = (JSONArray) investmentObj.get("items");
						String fundingRounds[] = new String[itemArrItems.size()];
						String investedIn[] = new String[itemArrItems.size()];
						int fundRoundindex=0;
						int investedInIndex=0;
						for(int k=0;k<itemArrItems.size();k++){
							
							JSONObject itemObj = (JSONObject) itemArrItems.get(k);
							try{
								if(itemObj.containsKey("funding_round"))
								{
									if(((JSONObject)itemObj.get("funding_round")).get("path")!=null){
										String tmpfundround = ((JSONObject)itemObj.get("funding_round")).get("path").toString().split("/")[1];
										fundingRounds[fundRoundindex++] = tmpfundround;	
										fundingRoundsArrList.add(tmpfundround);
										if(((JSONObject)itemObj.get("invested_in")).get("path")!=null)
										investedIn[investedInIndex++] = ((JSONObject)itemObj.get("invested_in")).get("path").toString().split("/")[1];	
									}									
								}
							}catch(NullPointerException nex){
								System.out.println("itemobj "+itemObj);
								nex.printStackTrace();
							}																			
						}											
						getTotalMoneyInvestedAndFundedOrganization(invInfo,fundingRounds,yearNoofFunds);
					}			
				}
				invInfo.setFundingRoundPaths(fundingRoundsArrList);
				Map<Integer, Integer> tmpmap = (Map)yearNoofFunds;
				Map<Integer, Integer> temptreemap = new TreeMap<Integer, Integer>(tmpmap);
				invInfo.setYearNoofFunds(temptreemap);
				hm.put(type, invInfo);				
			}
			
			
			ObjectWriter  ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			System.out.println("HM TO JSON String --> "+ow.writeValueAsString(hm));
			//System.out.println("aggregation result: "+cmdresult.toString());
		}
		catch(Exception pex){
			pex.printStackTrace();
		}
		invInfo = hm;
		return hm;
	}
	
	
	public void getTotalMoneyInvestedAndFundedOrganization(InvestorsInfo invinfo,String[] fundingRoundPaths,HashMap<Integer, Integer> yearNoOfFunds){
		
		JSONParser parser=new JSONParser();
		DB db = MongoHandler.initializemongo();
		DBCollection collection = db.getCollection("fundingrounds");
		BasicDBObject whereQuery = new BasicDBObject();
		long totalmoney=invinfo.getMoneyInvested();
		String[]  investortype = new String[fundingRoundPaths.length];		
		ArrayList<String> organizationInfo = invinfo.getOrganizationsInvestedIn();
		
		for(int i=0;i<fundingRoundPaths.length;i++)
		{
			whereQuery.put("data.uuid", fundingRoundPaths[i]);
			DBCursor cursor = collection.find(whereQuery);			
			while(cursor.hasNext())
			{
				try {
					JSONObject jobj = (JSONObject) parser.parse(cursor.next().toString());	
					JSONObject dataobj = (JSONObject) jobj.get("data");
					JSONObject propObj = (JSONObject) dataobj.get("properties");
					if(propObj.get("money_raised_usd") != null){
						totalmoney += (Long) propObj.get("money_raised_usd");
					}	
					
					//fill yearNoOfFunds
					if(propObj.get("announced_on_year") != null){
						int year = Integer.parseInt(propObj.get("announced_on_year").toString()) ;
						if(yearNoOfFunds.containsKey(year)){
							int count = yearNoOfFunds.get(year);
							count++;
							yearNoOfFunds.put(year, count);
						}else{
							yearNoOfFunds.put(year, 1);
						}						
					}
					
					JSONObject relationshipsObj = (JSONObject) dataobj.get("relationships");
					
					if(relationshipsObj.containsKey("funded_organization")){
						JSONObject fundedOrganization = (JSONObject) relationshipsObj.get("funded_organization");
						JSONArray fundedOrganizationArr = (JSONArray) fundedOrganization.get("items");
						
						for(int j=0;j<fundedOrganizationArr.size();j++){
							JSONObject item = (JSONObject) fundedOrganizationArr.get(j);
							organizationInfo.add(item.get("path").toString().split("/")[1]);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}catch(NullPointerException nex){
					nex.printStackTrace();
				}
			}			
		}
		
		invinfo.setMoneyInvested(totalmoney);				
	}

	
	public TreeMap<String, Integer> getInvestorsBasedOnLocation(){
		
		String jsonCommand = "{aggregate:\"company\",pipeline:[" +
				"{$match:{\"data.properties.number_of_investments\":{$gt:0},\"data.relationships.offices\":{$exists:true}}}," +
				"{$project:{\"data.relationships\":1,\"data.properties\":1}}," +
				"{$unwind:\"$data.relationships.offices.items\"}," +
				"{$project:{\"data.properties\":1,\"data.relationships\":1}}," +
				"{$group:{_id:\"$data.relationships.offices.items.city\",data:{$push:\"$data\"},count:{$sum:1}}}," +
				"{$sort:{count:-1}}]}";
		
		CommandResult cmdresult = mongoOperations.executeCommand(jsonCommand);
		JSONParser parser;
		JSONObject jsonobj;
		JSONArray jsonarr;
		TreeMap<String, Integer> hm = new TreeMap<String, Integer>();
		try{
			parser = new JSONParser(); 
			jsonobj =  (JSONObject)parser.parse(cmdresult.toString());
			jsonarr = (JSONArray)jsonobj.get("result");
			
			for(int i=0;i<jsonarr.size();i++){
				JSONObject jobj = (JSONObject) jsonarr.get(i);
				if(!jobj.isEmpty() && jobj.get("_id")!=null){
					try{
						hm.put(jobj.get("_id").toString(), Integer.parseInt(jobj.get("count").toString()));
					}catch(NullPointerException nex){						
						nex.printStackTrace();
					}					
				}
				ObjectWriter  ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				System.out.println("HM TO JSON String --> "+ow.writeValueAsString(hm));
			}				
		}catch(Exception pex ){
			pex.printStackTrace();
		}
		
		return hm;		
	}
	
	
	public void getAcquisitionInfo(){
		
		
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
		//c.getFundRaisedPerYearPerRound();
		//c.getTypesOfInvestors();
		c.getInvestorsBasedOnLocation();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
