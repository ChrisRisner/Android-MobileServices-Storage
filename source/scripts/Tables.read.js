var azure = require('azure');

function read(query, user, request) {
    
    var accountName = 'accountname';
    var accountKey = 'accountkey';
    var host = accountName + '.table.core.windows.net';
    var tableService = azure.createTableService(accountName, accountKey, host);
    
    tableService.queryTables(function (error, tables) {
        if (error) {
            request.respond(500, error);
        } else {
            request.respond(200, tables);
        }
    });
}