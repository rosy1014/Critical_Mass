// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});
 
//static var EVENT_RADIUS = 0.2;
/*
 * Update a user's location if the location has changed 
 * differs from the last location by more than 0.01 km
 * https://www.parse.com/questions/handling-duplicate-records-in-parse
 * Handling Duplicates
 */
 
 Parse.Cloud.define("updateEventSize", function(request, status) {
    Parse.Cloud.useMasterKey();

    var query = new Parse.Query("MassEvent");
    query.each( function(event){
    	console.log(event.get("objectId"));
    	query2 = new Parse.Query("MassUser");
    		query2.withinKilometers("location", event.get('location'), 0.2);
    		query2.count({
    			success: function(count){
    				event.set("EventSize", count);
    				

    				status.success("updated sizes of event " + event.id + "to size " + count);

    			//event.save();
    				//status.success("new event size is " + count);
    			},
    			error: function(error){
    				//status.error("failed to updateEventSize");
    				alert("Error: " + error.code + " failed to updateEventSize "+ event.get('objectId'));
    			}
    		});
    		event.save();

    	}).then(function(){
    		status.success("Updated sizes of event");
    	}, function(error){
    		status.error("failed to update event size, Error: " + error.code);

    });
});

 Parse.Cloud.define("updateEventSize2", function(request, status) {
    Parse.Cloud.useMasterKey();

    var query = new Parse.Query("MassEvent");
    //query.equalTo("objectId", "jHgaxJAyMd");
    query.find({
    	success: function(events){
    		var ids = " ";
    		for(var i = 0; i < events.length; i++){
    		// // 	query2 = new Parse.Query("MassUser");
    		// // 	query2.withinKilometers("location", events[i].get("location"), 0.2);
    		// // 	query2.count({
    		// // 		success:function(count){
    		// // 			event[i].set("EventSize", count);
    		// // 		});
    		// // },
    		// 	})
				var objId = events[i].id;
				ids = ids + objId + " ";
				events[i].set("EventSize", 40);
				events[i].save();
    			query2 = new Parse.Query("MassEvent");
    			query2.equalTo("objectId", objId);
    			query2.first({
    				success:function(mEvent){
    					mEvent.set("EventSize", 30);
    					mEvent.save();
    				},
    				error: function(){
    					console.log(error.code);
    				}
    			});
    		// 	ids = ids + "old event size " + events[i].get("EventSize") + " ";
    		// 	events[i].set("EventSize", 100);
    		// 	events[i].save();
    		// 	ids = ids + events[i].id + " " + events[i].get("EventSize") + " ";
    		// 	ids = ids + "old event size " + events[i].get("EventSize") + " ";
    		// 	events[i].set("EventSize", 100);
    		// 	events[i].save();
    		// 	ids = ids + events[i].id + " " + events[i].get("EventSize") + " ";

    		// event.set("EventSize", 1);
    		// event.save()	
    		// ids = ids + event.get("EventSize");
    	}
    		
    		status.success("updated event size, with " + ids + " events ");
    	},
    	error: function(error){
    		status.error("Oops, error "+ error.code);
    	}
    });
});


