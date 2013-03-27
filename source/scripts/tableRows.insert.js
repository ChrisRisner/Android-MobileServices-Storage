var azure = require('azure');

function insert(item, user, request) {
    
    var accountName = 'accountname';
    var accountKey = 'accountkey';
    var host = accountName + '.table.core.windows.net';
    var tableService = azure.createTableService(accountName, accountKey, host);
    
    tableService.insertEntity(request.parameters.table, item, function (error) {
        if (!error) {
            request.respond(200, item);
        } else {
            request.respond(500);
        }
    });
}