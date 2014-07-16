package com.crunch.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crunch.dao.CompanyDAO;
import com.crunch.pojo.Company;

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
	

}
