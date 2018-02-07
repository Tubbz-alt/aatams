$(function()
{
    var dataSource = contextPath + '/tag/lookupBySerialNumber';
    $("#tag\\.serialNumber").autocomplete({source:dataSource});
    
    // If known serial number, fill in tag properties
    $("#tag\\.serialNumber").autocomplete(
    {
    	select: function(event, ui)
    	{
    		updateTagFields(ui.item);
    	}
    });
});

function updateTagFields(tag)
{
	$("#tag\\.project\\.id").val(tag.project.id);
	$("#tag\\.model\\.id").val(tag.model.id);
	$("#tag\\.codeMap\\.id").val(tag.codeMap.id);
	$("#tag\\.expectedLifeTimeDays").val(tag.expectedLifeTimeDays);
	$("#tag\\.status\\.id").val(tag.status.id);
}

// Enable/disable slope/intercept/unit based on transmitter type.
$(function ()
{
	setSensorFieldsEnabled();
	$("#transmitterType\\.id").change(function() 
	{
		setSensorFieldsEnabled();
	});
});

function setSensorFieldsEnabled()
{
	var sensorFields = ["slope", "intercept", "unit"];
	$.each(sensorFields, function(index, fieldSelector)
	{
		if (['PINGER','RANGE_TEST'].indexOf($("#transmitterType\\.id option:selected").text()) > -1 )
		{
			$("#" + fieldSelector).attr("disabled", "disabled");
			$("#" +fieldSelector).attr("placeholder", "not applicable");
            $("label[for=" + fieldSelector + "]").removeClass("compulsory");

        }
		else
		{
			$("#" + fieldSelector).removeAttr("disabled");
			$("#" + fieldSelector).removeAttr("placeholder");
            $("label[for=" + fieldSelector + "]").addClass("compulsory");
        }
	});
}