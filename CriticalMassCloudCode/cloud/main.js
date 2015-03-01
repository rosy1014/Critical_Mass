
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

/*
 * Update a user's location if the location has changed 
 * differs from the last location by more than 0.01 km
 * https://www.parse.com/questions/handling-duplicate-records-in-parse
 * Handling Duplicates
 */

 Parse.Cloud.define("updateUserLocation", function(request, response) {
 	Parse.Cloud.useMasterKey();

  var user = request.params.user;
  //
 	var query = new Parse.Query("MassUser");
 	var currentLocation = Parse.GeoPoint.current;
 	query.equalTo("objectId", request.params.objectId);
 	query.first({
    success: function(result) {
      if(currentLocation.kilometersTo(result.location) > 0.01) {
        result.set("location", currentLocation);
      }
      return result.save();
      console.log("Updated the user's location.");
      response.success("Updated the user's location");
    }, 
    error: function(result, error) {
      console.log("request.. internal unusual failure: " + error.code + " " + error.message);
      response.error("Failed to update the user's location.");
    return;
  }
 	});
 });

/*
// Check the if the oldLocation is very close to the current location.
 Parse.Cloud.beforeSave("MassUser", function(request, response) {
  var oldLocation = request.object.get("location");
  var currentLocation = userObject.get("location");
  //Replace with a distance interval
  if (!oldLocation.equalTo(currentLocation)) {
  	request.object.set("location", currentLocation);
  }
  response.success();
});

 Parse.Cloud.beforeSave("MassUser", function(request, response) {
  if (request.object.get("location") != null) {
    response.error("you cannot give less than one star");
  } else if (request.object.get("stars") > 5) {
    response.error("you cannot give more than five stars");
  } else {
    response.success();
  }
});
*/