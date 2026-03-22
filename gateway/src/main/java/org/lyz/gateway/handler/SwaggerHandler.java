package org.lyz.gateway.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/swagger-resources")
public class SwaggerHandler {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<SwaggerResource>> swaggerResources() {
        List<SwaggerResource> resources = new ArrayList<>();
        
        resources.add(createResource("auth-service", "/auth/v3/api-docs", "1.0"));
        resources.add(createResource("system-service", "/system/v3/api-docs", "1.0"));
        resources.add(createResource("resource-service", "/resource/v3/api-docs", "1.0"));
        
        return Mono.just(resources);
    }

    private SwaggerResource createResource(String name, String location, String version) {
        SwaggerResource resource = new SwaggerResource();
        resource.setName(name);
        resource.setLocation(location);
        resource.setVersion(version);
        resource.setSwaggerVersion("3.0");
        return resource;
    }

    public static class SwaggerResource {
        private String name;
        private String location;
        private String version;
        private String swaggerVersion = "3.0";
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getSwaggerVersion() { return swaggerVersion; }
        public void setSwaggerVersion(String swaggerVersion) { this.swaggerVersion = swaggerVersion; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
