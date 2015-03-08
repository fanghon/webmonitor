$( document ).ready(function() {
  displayLoadingGIF();
  loadStatisticsData();
  loadReportsData();
  stopLoadingGIF();
});


function loadStatisticsData()
{
		var scoreZone = $("#scoreZone");
		var percentage = $("#scoreZone span");
		var porcentageValue = ((jsonDATA.statistics[1].value / (jsonDATA.statistics[0].value))) * 100;
		percentage.html(porcentageValue.toFixed(0) + "%");
		if(porcentageValue >= 90)
		{
			scoreZone.addClass( "greenBorder" );
		}
		else if(porcentageValue < 90 && porcentageValue > 50)
		{
			scoreZone.addClass( "yellowBorder" );
		}
		else
		{
			scoreZone.addClass( "redBorder" );
		}
		
		//Content Type TABLE
		jQuery("#contentTypeList").jqGrid({
		data: jsonDATA.statistics,
		datatype: "local",
		height: "auto",
		colNames:['','',''],
	   	colModel:[
	   		{name:'topic',index:'topic', width:400, sorttype:"string", },
	   		{name:'value',index:'value', width:50, sorttype:"string", align : "right"},
	   		{name:'type',index:'type', sorttype:"string"}
	   	],
	   	viewrecords: true,
	   	grouping:true,
	   	sortname: 'topic',
	   	hidegrid: false,
	   	sortorder: "asc",
	   	groupingView : {
	   		groupField : ['type'],
	   		groupColumnShow : [false],
	   		groupText : ['<b>{0}</b>'],
	   		groupOrder: ['desc'],
	   		groupDataSorted : true
	   	},
	   	caption: "Global Statistics"
	});
		
}

function loadReportsData()
{
	var Reports = jsonDATA.reports;
	var comboBox = $("#pageSelector");
	var options = $('#pageSelector').attr('options');
	
	
	Reports.forEach(function(entry) {
    	options[options.length] = new Option(entry.id, entry.id, false, false);
	});
	
	comboBox.change(function () {
	  jQuery("#reportsDetails").jqGrid('GridUnload');
	  var tempScrollTop = $(window).scrollTop();

	  displayReportTable(Reports[this.selectedIndex].id, Reports[this.selectedIndex].Links);
	  $(window).scrollTop(tempScrollTop);
	});
	
	displayReportTable(Reports[0].id, Reports[0].Links);
}

function displayReportTable(name, jsonSection)
{
	//Content Type TABLE
		jQuery("#reportsDetails").jqGrid({
		data: jsonSection,
		datatype: "local",
		height: "auto",
		colNames:['URL','Content-Type', 'Status', 'Type'],
	   	colModel:[
	   		{name:'url',index:'url', width:620, sorttype:"string", align : "left"},
	   		{name:'contentType',index:'contentType', width:100, sorttype:"string", align : "right"},
	   		{name:'status',index:'status', width:240, sorttype:"string", align : "right"},	   		
	   		{name:'type',index:'type', width:0, sorttype:"string"}
	   	],
	   	viewrecords: true,
	   	grouping:true,
	   	sortname: 'topic',
	   	sortorder: "asc",
	   	groupingView : {
	   		groupField : ['type'],
	   		groupColumnShow : [false],
	   		groupText : ['<b>{0}</b>'],
	   		groupOrder: ['desc'],
	   		groupDataSorted : true
	   	},
	   	hidegrid: false,
	   	caption: " Link information for: " + name
	});
}


function displayLoadingGIF()
{
	
	var loadingDiv = $("#loading");
	var left = (  loadingDiv.parent().width() / 2   ) - (loadingDiv.width() / 2 );
	var top = (  loadingDiv.parent().height/ 2   ) - (loadingDiv.height() / 2 );
	loadingDiv.css({top:top,left:left});
    loadingDiv.show();
}

function stopLoadingGIF()
{
	
	var loadingDiv = $("#loading");
	loadingDiv.hide();
}
