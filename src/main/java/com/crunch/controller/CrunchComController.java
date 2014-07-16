package com.crunch.controller;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crunch.dao.CompanyDAO;
import com.crunch.pojo.Company;
import com.crunch.service.CompanyService;

@Controller
@RequestMapping(value="/company")
public class CrunchComController {
	
	@Autowired
	CompanyService compService;
	
	@RequestMapping(value="/top100company",method=RequestMethod.GET)
	public @ResponseBody List<Company> getTop100Companies()
	{		
		return compService.getTopCompanyService(100);
				
	}

}
