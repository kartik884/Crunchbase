package com.crunch.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Company {

	@Id
	private String id;
	
	String name;
	String permalink;
	String homepageurl;
	String founded_year;
	String founded_month;
	String founded_day;
	int noOfEmp;
	float total_money_raised;
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getTotal_money_raised() {
		return total_money_raised;
	}
	public void setTotal_money_raised(float total_money_raised) {
		this.total_money_raised = total_money_raised;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPermalink() {
		return permalink;
	}
	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}
	public String getHomepageurl() {
		return homepageurl;
	}
	public void setHomepageurl(String homepageurl) {
		this.homepageurl = homepageurl;
	}
	public String getFounded_year() {
		return founded_year;
	}
	public void setFounded_year(String founded_year) {
		this.founded_year = founded_year;
	}
	public String getFounded_month() {
		return founded_month;
	}
	public void setFounded_month(String founded_month) {
		this.founded_month = founded_month;
	}
	public String getFounded_day() {
		return founded_day;
	}
	public void setFounded_day(String founded_day) {
		this.founded_day = founded_day;
	}
	public int getNoOfEmp() {
		return noOfEmp;
	}
	public void setNoOfEmp(int noOfEmp) {
		this.noOfEmp = noOfEmp;
	}
	
	@Override
	public String toString()
	{
		return "name: "+this.getName()+"  Total money raised: "+this.getTotal_money_raised() ;
	}
	
}
