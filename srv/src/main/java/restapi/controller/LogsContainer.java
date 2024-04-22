package restapi.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cds.gen.db.userlogs.Userlog;

@RestController
@RequestMapping("/logs")
public class LogsContainer
{
    @GetMapping("/")
    public CollectionModel<EntityModel<Userlog>> readLogs()
    {
        CollectionModel<EntityModel<Userlog>> cM = null;
        List<EntityModel<Userlog>> logs = null;
        List<Userlog> logsList = null;

        // logsList = todoSrv.findByUsername(userName);  GEt From DB
        //If Collection bound and not null
        logs = new ArrayList<EntityModel<Userlog>>();
        // logs.addAll( ) //list from DB

        cM = CollectionModel.of(logs);

        return cM;
    }
}
