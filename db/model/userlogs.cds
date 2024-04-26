using {cuid} from '@sap/cds/common';

namespace db.userlogs;


/*
--- Cannot be inserted Directly via OData - Managed via Logging Event
*/
@Capabilities.Insertable: false
@Capabilities.Updatable : false
entity userlog : cuid {
    username  : String(50);
    timestamp : Timestamp;
    endpoint : String(50);

}
