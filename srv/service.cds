using {db.userlogs as logs } from '../db/model/userlogs'; 

service UserLogsService  
{
    @readonly entity Logs as projection on logs.userlog;
}
