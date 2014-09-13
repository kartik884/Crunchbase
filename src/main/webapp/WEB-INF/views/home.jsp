<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css"></link>
<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/crunchbase.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>	
<script src="${pageContext.request.contextPath}/resources/js/spin.min.js"></script>
	<title>Home</title>
</head>
<body>

<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
<div class="container-fluid">

<!-- navbar start -->
<nav class="navbar navbar-default" role="navigation">
  <div class="container-fluid">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="#">CrunchBase</a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
      
      
      <!-- <ul class="nav navbar-nav navbar-right">
        <li><a href="#">Dashboard</a></li>
        <li><a href="#">Setting</a></li>
        <li><a href="#">Profile</a></li>
        <li><a href="#">Help</a></li>
        
        
      </ul>
      <form class="navbar-form navbar-right" role="search">
        <div class="form-group">
          <input type="text" class="form-control" placeholder="Search">
        </div>
        <button type="submit" class="btn btn-default">Submit</button>
      </form> -->
    </div><!-- /.navbar-collapse -->
  </div><!-- /.container-fluid -->
</nav>

<!-- navbar end -->
	
</div>
</div>

<!-- Side bar code  -->

<div class="container-fluid">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li class="active"><a href="#">Overview</a></li>
            <li class="nav-header">
			  <a href="#" data-toggle="collapse" data-target="#userMenu">
			    Report <i class="icon-angle-down"></i>
			  </a>
			  <ul style="list-style: none;" class="collapse in" id="userMenu">
			    <li class="active" id="investorInfoPage"><a href="#">Investors </a></li>
			    <li class="active" id="acquisitionInfoPage"><a href="#">Acquisition </a></li>	
			    <li class="active" id="fundingRoundsInfoPage"><a href="#">Funding </a></li>				    
			  </ul>
			</li>  
			
			<li><a href="#">Analytics</a></li>
            <li><a href="#">Export</a></li>        
          </ul>
                              	                  
        </div>
        
        <!-- this div will load different data   -->
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">Dashboard</h1>
          <div class="maincontainer">  
          <div class="panel panel-default">			  
			  <div class="panel-heading" >Company Ranking</div>
			
			  <!-- Table -->
			  <table id="compRankTable" class="table table-hover">
			   <thead>
			   <tr>
			   <th>Company Name</th>
			   <th>Money Raised</th>
			   </tr>
			   </thead>
			   
			   <tbody id="compRankTableBody">
			   
			   </tbody>
			  </table>
			</div>  
			</div>
            
          </div>
          
          </div>
       </div>

<!-- Side bar code ends -->

<script type="text/javascript">

google.load("visualization", "1", {packages:["corechart"]});

 $(document).ready(function(){
	
	/* $('#compRankTable').ready(function(){
		alert("Getting the data ");
		var servicename ='company/top100company';
		$.get(servicename,"json",function(data){
			var obj = data;
			
			for(var i=0;i<obj.length;i++){
				var moneyraised = parseFloat(obj[i].total_money_raised.toString());
				
				$('#compRankTableBody').append(
						"<tr><td>"
						+ obj[i].name
						+ "</td>"+
						"<td>"+obj[i].total_money_raised
						+"</td></tr>");
			}
		});
	}); */
	
	
	$('#acquisitionInfoPage').on('click',function(){
		$('.page-header').text('Acquisition Information');
		$(".maincontainer").load('acquisition_Info', function(response, status, xhr){
	   		console.log("The load was made");	   			   
	   		
	   		if(status== "error"){
	   			console.log("xhr status "+xhr.status);
	   		}	   		
	   		
	   	// start drawing the average acquisition and top 10 acquirer
			getAvgAcquisition();
			getTop10Acquirer();
	   		
	   	});
			
		});
	
	
	//---------------------------------- Investor -----------------------// 
	$('#investorInfoPage').on('click',function(){
		$('.page-header').text('Investor Information');
		$(".maincontainer").load('investorInfo', function(response, status, xhr){
	   		console.log("The load was made");	   			   
	   		
	   		if(status== "error"){
	   			console.log("xhr status "+xhr.status);
	   		}	   		
	   		
	   	// start drawing the investor 
	   		getInvestorTypeData();
	   		getInvestorsBasedOnLocation();
	   		getFundRaisedPerYearPerRound();
	   		
	   	});
			
		});		
	
	}); 
 
 
 
 
 
 
 //-------------------------------- Acquirer search ----------------------------------/
 
 $(document).on('click','#acquisitionSubmit',function(){
 	var text = $('#acquisitionText').val();
 	getMatchingAcquirer(text);
 });
 
 function getMatchingAcquirer(text){
   	var serviceName = "acquisition/searchedacquirer?searchText="+text; 
   	
   	$.get(serviceName,"json",function(data){    		
   		fillAcquirerSearchedResult(data);    		    	
   	});      	
   }
 
   function fillAcquirerSearchedResult(data){    	
   	$('#searchedAcquisition_body').empty();
   	for(var i=0;i<data.length;i++){
   		$('#searchedAcquisition_body').append(
   				"<tr><td>"
   				+ data[i]
   				+ "</td>"+
   				"</tr>");
   	}
   	
   	} 
 
 
//-------------------------------------------- Acquirer Information --------------------------------------------

$(document).on('click','.searchedAcquisition_table tr',function(){
    	
    	var text = $(this).find('td').text();    	
		var serviceName = "acquisition/acquirerinfo?name="+text; 
      	
      	$.get(serviceName,"json",function(data){    		
      		fillAcquirerInfodd(data);    		    	
      	});
    	
    });
    
    
    var acquirerInfodata ;
    
    function fillAcquirerInfodd(data){
    	//fill the dropdown
    	
    	 acquirerInfodata=data;
    	 /*$("#acquisition_list_ul").empty();
   	 
	   	for(var i=0;i<data.length;i++){	   		
	   		$("#acquisition_list_ul").append('<li role="presentation"><a role="menuitem" tabindex="-1" >'+data[i].data.relationships.acquiree.items.name+'</a></li>');	
	   	} */
	   	
	   	$("#acquisition_list_body").empty();
	   	for(var i=0;i<data.length;i++){
	   		$('#acquisition_list_body').append(
	   				"<tr><td>"
	   				+ data[i].data.relationships.acquiree.items.name
	   				+ "</td>"+
	   				"</tr>");
	   	}
	   	
	   	//load and fill the acquisition info 
	   /* 	$("#acquisitionInfo").load('acquisition_Info', function(response, status, xhr){
	   		console.log("The load was made");
	   		if(status== "error"){
	   			alert("xhr status "+xhr.status);
	   		}	   		
	   	}); */
    }
    
    $(document).on('click', '#acquisition_list_body tr', function(){
        $(".btn:first-child").text($(this).text());
        $(".btn:first-child").append('<span class="caret"></span>');
        $(".btn:first-child").val($(this).text());
        
        fillAcquirerInfo($(this).text());
     });
    
    function fillAcquirerInfo(text){
    	
    	for(var i=0;i<acquirerInfodata.length;i++){
			if(acquirerInfodata[i].data.relationships.acquiree.items.name == text){
				$('#announced_on').val(acquirerInfodata[i].data.properties.announced_on);
				$('#price').val(acquirerInfodata[i].data.properties.price);
				$('#acquirer_name').val(acquirerInfodata[i].data.relationships.acquirer.items[0].name);
				$('#acquirer_type').val(acquirerInfodata[i].data.relationships.acquirer.items[0].type);
				$('#acquiree_name').val(acquirerInfodata[i].data.relationships.acquiree.items.name);
				$('#acquiree_type').val(acquirerInfodata[i].data.relationships.acquiree.items.type);
			}    		    		
    	}    	
    }


  //-------------------------------------------- Acquirer Information Ends--------------------------------------------
  
  //-------------------------------- avg acquisition ----------------------------------/   
 
 function getAvgAcquisition(){
 	var serviceName = "acquisition/avgacquisition";
 	
 	$.get(serviceName,"json",function(data){    		
 		drawAvgAcquisition(data);    		    	
 	});
 	
 }


 function drawAvgAcquisition(data){
 	    	
 	var chartAvgAcquisitionPerYear = new google.visualization.DataTable();
 	chartAvgAcquisitionPerYear.addColumn('number','Year');
 	chartAvgAcquisitionPerYear.addColumn('number','Avg ');
 	chartAvgAcquisitionPerYear.addRows(data.length);
 	
 	 	
 	for(var i=0;i<data.length;i++){
 		var j=0;    		
 		chartAvgAcquisitionPerYear.setValue(i,j,data[i]._id);
 		j++;
 		chartAvgAcquisitionPerYear.setValue(i,j,data[i].avg_acquisition);    		
 	}
 	
 	var options = {
	          title: 'Average Acquisition ',
	          hAxis: {title: 'Year',  titleTextStyle: {color: '#333'}, showTextEvery:1},
	          vAxis: {minValue: 0}
	        };
 	
 	var chart = new google.visualization.LineChart(document.getElementById('avgAcquisition'));
     chart.draw(chartAvgAcquisitionPerYear, options);
 }
 
 
 
 //-------------------------------- Top 10 acquirer ----------------------------------/
   
 
 function getTop10Acquirer(){
   	var serviceName = "acquisition/top10acquirer";
   	
   	$.get(serviceName,"json",function(data){    		
   		fillTop10Acquirer(data);    		    	
   	});
   	
   }
 
   function fillTop10Acquirer(data){    	
   	$('#top_10_acquirer_body').empty();
   	for(var i=0;i<data.length;i++)
   		$('#top_10_acquirer_body').append(
   				"<tr><td>"
   				+ data[i]._id
   				+ "</td>"+
   				"<td>"+ data[i].count
   				+"</td></tr>");	
   	}  
  
 //-------------------------------- Investor  ----------------------------------/  
 
//spinner config 
 var opts = {
  lines: 13, // The number of lines to draw
  length: 20, // The length of each line
  width: 10, // The line thickness
  radius: 30, // The radius of the inner circle
  corners: 1, // Corner roundness (0..1)
  rotate: 0, // The rotation offset
  direction: 1, // 1: clockwise, -1: counterclockwise
  color: '#000', // #rgb or #rrggbb or array of colors
  speed: 1, // Rounds per second
  trail: 60, // Afterglow percentage
  shadow: false, // Whether to render a shadow
  hwaccel: false, // Whether to use hardware acceleration
  className: 'spinner', // The CSS class to assign to the spinner
  zIndex: 2e9, // The z-index (defaults to 2000000000)
  top: '50%', // Top position relative to parent
  left: '50%' // Left position relative to parent
};
 var spinner;
 var noOfFundsPerYearTableObj;
   function getInvestorTypeData(){
   	var serviceName = "company/getinvestortype";    
   	var target = document.getElementById('chart_Investor_type');
   	spinner = new Spinner(opts).spin(target);
   	$.get(serviceName,"json",function(data){    	   		   	
   		noOfFundsPerYearTableObj = data;
   		investorTypedata = drawInvestorTypeData(data);
   		$('#no_of_funds_per_year_ul').ready(function(){
   			setInvestorTypeDropdown(data);});
   	}).done(function() {
   	    console .log( " investor type data fetched " );    
   	    spinner.stop();
   	  })
   	  .fail(function() {
   	    alert( "error" );
   	  });      	    	
   }
     
   function drawInvestorTypeData(data){
   	
   	var datalength=0;
   	for(var key in data)
		{    		
			if(data.hasOwnProperty(key)){
				datalength++;
			}
		}
   	
   	var chartinvesterTypedata = new google.visualization.DataTable();
   	chartinvesterTypedata.addColumn('string','investorType');
   	chartinvesterTypedata.addColumn('number','Moneyfunded');
   	chartinvesterTypedata.addRows(datalength);
   	
   	var i=0;    	
   	for(var key in data){
   		var j=0;
   		console.log(key +" -> "+data[key]);
   		chartinvesterTypedata.setValue(i,j,key);
   		var value = data[key];
   		var moneyRaised = value.moneyInvested;
   		j++;
   		chartinvesterTypedata.setValue(i,j,moneyRaised);
   		i++;
   	}
   	
   	var options = {
   	          title: 'Investors Funding',
   	          is3D: true,
   	        };
   	
   	var chart = new google.visualization.PieChart(document.getElementById('chart_Investor_type'));
       chart.draw(chartinvesterTypedata, options);
   }
   
   $('.dropdown').on('click',function(){
   	$('.dropdown-toggle').dropdown();	
   });
   
   
   function setInvestorTypeDropdown(data){    
   	 $("#no_of_funds_per_year_ul").empty();
   	 var index=0;
   	for(var key in data){
   		$("#no_of_funds_per_year_ul").append('<li role="presentation"><a role="menuitem" tabindex="-1" >'+key+'</a></li>');	
   	}   
   } 
   
   
   $(document).on('click', '#no_of_funds_per_year_ul li a', function(){
       $(".btn:first-child").text($(this).text());
       $(".btn:first-child").append('<span class="caret"></span>');
       $(".btn:first-child").val($(this).text());
       
       fillnoOfFundsPerYearTable($(this).text());
    });
   
   function fillnoOfFundsPerYearTable(investorType){
   	var investorInfo =  noOfFundsPerYearTableObj[investorType];
   	var tableInfo = investorInfo.yearNoofFunds;
   	$('#no_of_funds_per_year_body').empty();
   	for(var key in tableInfo){
   		$('#no_of_funds_per_year_body').append(
   				"<tr><td>"
   				+ key
   				+ "</td>"+
   				"<td>"+tableInfo[key].count
   				+"</td><td>"+tableInfo[key].moneyRaised
   				+"</td></tr>");	
   	}    	
   }
   
   
 //-------------------------------- Investor Location ----------------------------------/
 function getInvestorsBasedOnLocation(){
   	var serviceName = "company/getinvestorsLocation";
   	
   	$.get(serviceName,"json",function(data){    		
   		drawInvestorsLocation(data);    		    	
   	});
   	
   }
   
   function drawInvestorsLocation(data){
   	var datalength=0;
   	for(var key in data)
		{    		
			if(data.hasOwnProperty(key)){
				datalength++;
			}
		}
   	
   	var chartinvesterLocationdata = new google.visualization.DataTable();
   	chartinvesterLocationdata.addColumn('string','City');
   	chartinvesterLocationdata.addColumn('number','No Of Investors');
   	chartinvesterLocationdata.addRows(datalength);
   	
   	var i=0;    	
   	for(var key in data){
   		var j=0;    		
   		chartinvesterLocationdata.setValue(i,j,key);
   		var value = data[key];    		
   		j++;
   		chartinvesterLocationdata.setValue(i,j,value);
   		i++;
   	}
   	
   	var options = {
   	        region: 'world',
   	        displayMode: 'markers',
   	        colorAxis: {colors: ['green', 'blue']}
   	      };
   	
   	var chart = new google.visualization.GeoChart(document.getElementById('loaction_Investor'));
       chart.draw(chartinvesterLocationdata, options);
   }   
   
   //-------------------------------- Investors on location search ----------------------------------/
   
   $(document).on('click','#locationSearchSubmit',function(){
   	var text = $('#locationSearchText').val();
   	getMatchingLocation(text);
   });
   
   function getMatchingLocation(text){
     	var serviceName = "company/searchedlocation?searchText="+text; 
     	
     	$.get(serviceName,"json",function(data){    		
     		fillLocationSearchedResult(data);    		    	
     	});      	
     }
   
     function fillLocationSearchedResult(data){    	
     	$('#location_search_body').empty();
     	for(var i=0;i<data.length;i++){
     		$('#location_search_body').append(
     				"<tr><td>"
     				+ data[i].data.properties.name
     				+ "</td><td>"+data[i].data.relationships.offices.items[0].city
     				+ "</td></tr>");
     	}
     	
     	} 
     
     
     
     //------------------------------------------------------------- fund raised per year per round ----------------------------------------------------
     
     
     var fundPerYearPerRounddata;
    var rounds = [];
    var spinnerPerYearPerRound;
     
        function getFundRaisedPerYearPerRound(){
    	
    	var serviceName = "company/avgFundPerYearPerRound";
    	var target = document.getElementById('chart_div_round_year');
    	spinnerPerYearPerRound = new Spinner(opts).spin(target);
    	$.get(serviceName,"json",function(data){    
    		
    		//alert("data found "+data.length);
    		fundPerYearPerRounddata = data;
    		for(var i=0;i<data.length;i++){    			
    			rounds[i] = data[i].round;
    			//console.log(data[i]._id+" ");
    			//console.log("fundPerYearPerRounddata: "+fundPerYearPerRounddata);
    		}
    		
    		fillroundradiobtnlist(rounds);
    		
    	}).done(function() {
       	    console .log( " getFundRaisedPerYearPerRound type data fetched " );    
       	 spinnerPerYearPerRound.stop();
       	  });    	    
    }
    
           
        
    function fillroundradiobtnlist(roundsvar){
    	
    	for(var i=0;i<roundsvar.length;i++){    		
    		$('#roundradiobtn').append("<div class=\"radio\"><label> <input type=\"radio\"  name=\"roundListRadioGrp\" id='roundRadiobtn"+i+"' value='"+roundsvar[i]+"'>"+roundsvar[i]+"</label></div>");
    	}     	
    	$('#roundRadiobtn0').prop('checked',true);    	
    	$('#roundRadiobtn0').trigger("click");
    }    
    /* $("input[name=roundListRadioGrp]:radio").change(function () {
    	drawFundRaisedPerRoundPeryear();    	
    }); */
    
    $(document).on('click',"input[type='radio'][name='roundListRadioGrp']",function(){
    	
    	var selected = $("input[type='radio'][name='roundListRadioGrp']:checked");        	
    	if(selected.length > 0 )
			drawFundRaisedPerRoundPeryear(selected.val());
    	
    });
    
    function drawFundRaisedPerRoundPeryear(selectedround){    	    	
    	
    	var selFundPerYearPerRounddata = fundPerYearPerRounddata.filter(function(data){
    		return data.round == selectedround;
    	});
    	var hm = selFundPerYearPerRounddata[0].hmRoundFundRaised;
    	var hmlength=0;
    	for(var key in hm)
		{
    		var j=0;
			if(hm.hasOwnProperty(key)){
				hmlength++;
			}
		}    	
    	
    	var data3 = new google.visualization.DataTable();
    	data3.addColumn('number','Year');
    	data3.addColumn('number','AvgFundRaised');
    	data3.addRows(hmlength);
    	var i=0;
    	for(var key in hm)
		{
    		var j=0;
			if(hm.hasOwnProperty(key)){
				console.log(key +" -> "+hm[key]);
				data3.setValue(i,j,key);
	    		j++;
	    		data3.setValue(i,j,hm[key]);
	    		i++;
			}
		}    	    
    	var options = {
  	          title: 'Fund Raised Per Round Per Year',
  	          hAxis: {title: 'Year',  titleTextStyle: {color: '#333'}, showTextEvery:1},
  	          vAxis: {minValue: 0}
  	        };
    	
    	var chart = new google.visualization.LineChart(document.getElementById('chart_div_round_year'));
        chart.draw(data3, options);    	    	     
    }
    
    
     
   
   
</script>

</body>
</html>
