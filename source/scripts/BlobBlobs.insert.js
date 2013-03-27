var azure = require('azure');
var qs = require('querystring');

function insert(item, user, request) {
   
    var accountName = 'accountname';
    var accountKey = 'accountkey';
    var host = accountName + '.blob.core.windows.net';
    var blobService = azure.createBlobService(accountName, accountKey, host);
    
    var sharedAccessPolicy = { 
        AccessPolicy: {
            Permissions: 'rw', //Read and Write permissions
            Expiry: minutesFromNow(5) 
        }
    };
    
    var sasUrl = blobService.generateSharedAccessSignature(request.parameters.containerName,
                    request.parameters.blobName, sharedAccessPolicy);

    var sasQueryString = { 'sasUrl' : sasUrl.baseUrl + sasUrl.path + '?' + qs.stringify(sasUrl.queryString) };                    

    request.respond(200, sasQueryString);
}

function formatDate(date) { 
    var raw = date.toJSON(); 
    // Blob service does not like milliseconds on the end of the time so strip 
    return raw.substr(0, raw.lastIndexOf('.')) + 'Z'; 
} 

function minutesFromNow(minutes) {
    var date = new Date()
  date.setMinutes(date.getMinutes() + minutes);
  return date;
}