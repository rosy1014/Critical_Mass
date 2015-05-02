// Created by Tingyu, April 18, 2015


// Before info a mass user is changed
// get the old event of the user and decrement the size
Parse.Cloud.beforeSave("MassUser", function (request) {

    var MassUser = Parse.Object.extend("MassUser");
    var queryUsers = new Parse.Query(MassUser);
    queryUsers.equalTo("user", request.user.id);
    queryUsers.first({
        success: function (massUser) {
            if (massUser != undefined) {
                var oldEvent = massUser.get("event");
                var queryEvents = new Parse.Query("MassEvent");
                queryEvents.get(oldEvent, {
                    success: function (massEvent) {
                        var size = massEvent.get("EventSize");
                        size--;
                        massEvent.set("EventSize", size);
                        massEvent.save();
                        console.log("Decremented size of event " + massEvent.get("locationName") + " to " + size);
                    },
                    error: function (error) {
                        console.error("No old event found in beforeSave!" + "Got an error " + error.code + " : " + error.message);
                    }
                });
            } else {
                console.log("sb else case");
            }
        },
        error: function (error) {
            console.error("No current user found in beforeSave! " + "Got an error " + error.code + " : " + "error.message");
        }
    });
    console.log("About to change mass user");

});



// After the info a mass user is changed
// get the new event of the mass user and increment the size
Parse.Cloud.afterSave("MassUser", function (request, response) {
    console.log("in afterSave");


    var eventID = request.object.get("event");

    var MassEvent = Parse.Object.extend("MassEvent");
    var query = new Parse.Query(MassEvent);
    console.log("event id for query is " + eventID);
    query.get(eventID, {
        success: function (massEvent) {

            if (massEvent != undefined) {

                console.log("request.object.id is " + request.object.id);
                var size = massEvent.get("EventSize");
                size++;
                massEvent.set("EventSize", size);
                massEvent.save();
                console.log("Incremented size of event " + massEvent.get("locationName") + " to " + size);


            } else {
                console.log("mass event undefined? " + massEvent);
            }

        },
        error: function (error) {
            console.error("No old event found in afterSave! " + "Got an error " + error.code + " : " + error.message);
        }
    });
});
