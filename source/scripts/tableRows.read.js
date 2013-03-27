var azure = require('azure');

function read(query, user, request) {
    
    var accountName = 'accountname';
    var accountKey = 'accountkey';
    var host = accountName + '.table.core.windows.net';
    var tableService = azure.createTableService(accountName, accountKey, host);
    
    var tq = azure.TableQuery
        .select()
        .from(request.parameters.table);

    tableService.queryEntities(tq, function (error, rows) {
        if (error) {
            request.respond(500, error);
        } else {
            request.respond(200, rows)
        }
    });
}