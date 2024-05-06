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
    endpoint  : String(50);

}


@Capabilities.Insertable: false
@Capabilities.Deletable : false
@Capabilities.Updatable : false
entity apiSignUps : cuid {
    consumer  : String(200);
    signedAt  : Timestamp;
    validTill : Timestamp;
    isActive  : Boolean;

}
