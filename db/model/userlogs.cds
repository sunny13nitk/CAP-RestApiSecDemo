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
    apiKey    : UUID;
    consumer  : String(200);
    signedBy  : String(50);
    signedAt  : Timestamp;
    updatedAt : Timestamp;
    updatedBy : String(50);
    validTill : Timestamp;
    isActive  : Boolean;

}

@Capabilities.Insertable: false
@Capabilities.Deletable : false
@Capabilities.Updatable : false
entity srvSignUps : cuid {
    apiKey                : UUID;
    consumer              : String(200);
    xsappname             : String(200);
    clientId              : String(200);
    sourceScopes          : String;
    isScopeCheckMandatory : Boolean;
    failMessage           : String;
    signedBy              : String(50);
    signedAt              : Timestamp;
    updatedAt             : Timestamp;
    updatedBy             : String(50);
    validTill             : Timestamp;
    isActive              : Boolean;

}
