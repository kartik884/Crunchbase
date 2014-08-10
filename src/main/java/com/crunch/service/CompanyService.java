package com.crunch.service;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crunch.dao.CompanyDAO;
import com.crunch.pojo.Company;
import com.crunch.pojo.FundRaisedPerYearPerRound;

@Service
public class CompanyService {
	
	@Autowired 
	CompanyDAO compDao;
	
	public CompanyService() throws Exception{
		// TODO Auto-generated constructor stub		
		
	}
	
	public List<Company> getTopCompanyService(int limit)
	{
		return compDao.getTopCompanies(limit);
	}
	
	//chart average fund raised per year 
	public JSONArray getAvgFundRaisedPerYear()
	{
		return compDao.getFundRaisedByYear();
	}
	
	public ArrayList<FundRaisedPerYearPerRound> getAvgFundRaisedPerYearPerRound()
	{
		return compDao.getFundRaisedPerYearPerRound();
	}
	

}
