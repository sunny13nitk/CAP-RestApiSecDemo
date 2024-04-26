package restapi.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cds.Result;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.db.userlogs.Userlog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import restapi.pojos.TY_NewLog;

@RestController
@RequestMapping("/logs")
@Slf4j
@RequiredArgsConstructor
public class LogsController
{

    private final PersistenceService ps;
    private final String logsTablePath = "db.userlogs.userlog"; // Table Path - HANA

    @GetMapping("/")
    public CollectionModel<EntityModel<Userlog>> readLogs()
    {
        CollectionModel<EntityModel<Userlog>> cM = null;
        List<EntityModel<Userlog>> logs = null;
        List<Userlog> logsList = null;

        // logsList = todoSrv.findByUsername(userName); GEt From DB
        // If Collection bound and not null
        logs = new ArrayList<EntityModel<Userlog>>();
        // logs.addAll( ) //list from DB

        cM = CollectionModel.of(logs);

        return cM;
    }

    @PostMapping("/")
    public ResponseEntity<EntityModel<Userlog>> createLog(@RequestBody TY_NewLog newLog)
    {
        EntityModel<Userlog> userlog = null;
        Result response = null;

        if (newLog != null && ps != null)
        {
            Map<String, Object> logEntity = new HashMap<>();
            logEntity.put("ID", UUID.randomUUID()); // ID
            logEntity.put("username", newLog.getUserName()); // User Name
            logEntity.put("timestamp", new Timestamp(System.currentTimeMillis())); // TimeStamp
            logEntity.put("endpoint", newLog.getEndPoint()); // Invocation EndPoint

            if (logEntity != null)
            {
                CqnInsert qLogInsert = Insert.into(this.logsTablePath).entry(logEntity);
                if (qLogInsert != null)
                {
                    log.info("LOG Insert Query Bound!");
                    Result result = ps.run(qLogInsert);
                    if (result.list().size() > 0)
                    {
                        log.info("# Log Successfully Inserted - " + result.rowCount());
                        response = result;
                        userlog = EntityModel.of(response.first(Userlog.class).get());
                    }
                }
            }
        }

        return new ResponseEntity<>(userlog, HttpStatus.OK);
    }
}
