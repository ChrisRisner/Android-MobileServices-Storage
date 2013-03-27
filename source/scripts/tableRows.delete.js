var azure = require('azure');

function del(id, user, request) {
    
    var accountName = 'accountname';
    var accountKey = 'accountkey';
    var host = accountName + '.table.core.windows.net';
    var tableService = azure.createTableService(accountName, accountKey, host);
    
    tableService.deleteEntity(request.parameters.tableName, 
                             {
                                PartitionKey : request.parameters.partitionKey
                                , RowKey : request.parameters.rowKey
                             }
                             , function (error) {
        if (!error) {
            request.respond(200);
        } else {
            request.respond(500);
        }
    });
}