package org.monkey.mmq.web.controller;

import org.monkey.mmq.config.modules.acl.AclModule;
import org.monkey.mmq.config.modules.acl.AclParam;
import org.monkey.mmq.core.consistency.model.ResponsePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @ClassName:AclController
 * @Auther: Solley
 * @Description: ACL
 * @Date: 2022/7/31 21:41
 * @Version: v1.0
 */
@RestController
@RequestMapping("/v1/api/acl")
public class AclController {
    @Autowired
    AclModule aclModule;

    @GetMapping("/pageAcl")
    public ResponsePage<AclParam> listAllAclParams(@RequestParam int pageNo,
                                                   @RequestParam int pageSize,
                                                   @RequestParam(required = false, defaultValue = "") String clientId,
                                                   @RequestParam(required = false, defaultValue = "") String username,
                                                   @RequestParam(required = false, defaultValue = "") String ip) {
        List<AclParam> aclParams = aclModule.listAllAclParams();
        return new ResponsePage<>(pageSize, pageNo,
                aclParams.size() / pageSize,
                aclParams.size(),
                aclParams.stream().filter(x -> x.getClientId().contains(clientId))
                        .filter(x -> x.getUsername().contains(username))
                        .filter(x -> x.getIpaddr().contains(ip))
                        .skip(pageNo - 1).limit(pageSize).collect(Collectors.toList()));
    }

    @PostMapping
    public void addAclParam(@RequestBody AclParam aclParam) {
        aclModule.addAclParam(aclParam);
    }

    @DeleteMapping
    public void deleteAclParam(String id) {
        aclModule.deleteAclParam(id);
    }

    @PutMapping
    public void updateAclParam(@RequestBody AclParam aclParam) {
        aclModule.updateAclParam(aclParam);
    }
}
