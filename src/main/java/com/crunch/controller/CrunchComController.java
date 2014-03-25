package com.crunch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.crunch.dao.CompanyDAO;
import com.crunch.service.CompanyService;

@Controller
public class CrunchComController {
	
	@Autowired
	CompanyService compService;

}
