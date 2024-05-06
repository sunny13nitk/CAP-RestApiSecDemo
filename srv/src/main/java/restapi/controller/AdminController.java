package restapi.controller;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cds.gen.db.userlogs.ApiSignUps;
import cds.gen.db.userlogs.Userlog;
import lombok.extern.slf4j.Slf4j;
import restapi.pojos.TY_NewLog;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController
{
    @PostMapping("/signups")
    public ResponseEntity<EntityModel<ApiSignUps>> createLog(@RequestBody TY_NewLog newLog)
    {
        return null;
    }
}
