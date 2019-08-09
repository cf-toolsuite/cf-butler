package io.pivotal.cfapp.domain.event;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

public enum EventType {
    APP_CRASH("app.crash"),
    AUDIT_APP_COPY_BITS("audit.app.copy-bits"),
    AUDIT_APP_CREATE("audit.app.create"),
    AUDIT_APP_DELETE_REQUEST("audit.app.delete-request"),
    AUDIT_APP_DROPLET_MAPPED("audit.app.droplet.mapped"),
    AUDIT_APP_MAP_ROUTE("audit.app.map-route"),
    AUDIT_APP_PACKAGE_CREATE("audit.app.package.create"),
    AUDIT_APP_PACKAGE_DELETE("audit.app.package.delete"),
    AUDIT_APP_PACKAGE_DOWNLOAD("audit.app.package.download"),
    AUDIT_APP_PACKAGE_UPLOAD("audit.app.package.upload"),
    AUDIT_APP_RESTAGE("audit.app.restage"),
    AUDIT_APP_SSH_AUTHORIZED("audit.app.ssh-authorized"),
    AUDIT_APP_SSH_UNAUTHORIZED("audit.app.ssh-unauthorized"),
    AUDIT_APP_START("audit.app.start"),
    AUDIT_APP_STOP("audit.app.stop"),
    AUDIT_APP_UNMAP_ROUTE("audit.app.unmap-route"),
    AUDIT_APP_UPDATE("audit.app.update"),
    AUDIT_APP_UPLOAD_BITS("audit.app.upload-bits"),
    AUDIT_ORGANIZATION_CREATE("audit.organization.create"),
    AUDIT_ORGANIZATION_DELETE_REQUEST("audit.organization.delete-request"),
    AUDIT_ORGANIZATION_UPDATE("audit.organization.update"),
    AUDIT_ROUTE_CREATE("audit.route.create"),
    AUDIT_ROUTE_DELETE_REQUEST("audit.route.delete-request"),
    AUDIT_ROUTE_UPDATE("audit.route.update"),
    AUDIT_SERVICE_CREATE("audit.service.create"),
    AUDIT_SERVICE_DELETE("audit.service.delete"),
    AUDIT_SERVICE_UPDATE("audit.service.update"),
    AUDIT_SERVICE_BINDING_CREATE("audit.service_binding.create"),
    AUDIT_SERVICE_BINDING_DELETE("audit.service_binding.delete"),
    AUDIT_SERVICE_BROKER_CREATE("audit.service_broker.create"),
    AUDIT_SERVICE_BROKER_DELETE("audit.service_broker.delete"),
    AUDIT_SERVICE_BROKER_UPDATE("audit.service_broker.update"),
    AUDIT_SERVICE_DASHBOARD_CLIENT_CREATE("audit.service_dashboard_client.create"),
    AUDIT_SERVICE_DASHBOARD_CLIENT_DELETE("audit.service_dashboard_client.delete"),
    AUDIT_SERVICE_INSTANCE_BIND_ROUTE("audit.service_instance.bind_route"),
    AUDIT_SERVICE_INSTANCE_CREATE("audit.service_instance.create"),
    AUDIT_SERVICE_INSTANCE_DELETE("audit.service_instance.delete"),
    AUDIT_SERVICE_INSTANCE_UNBIND_ROUTE("audit.service_instance.unbind_route"),
    AUDIT_SERVICE_INSTANCE_UPDATE("audit.service_instance.update"),
    AUDIT_SERVICE_KEY_CREATE("audit.service_key.create"),
    AUDIT_SERVICE_KEY_DELETE("audit.service_key.delete"),
    AUDIT_SERVICE_PLAN_CREATE("audit.service_plan.create"),
    AUDIT_SERVICE_PLAN_DELETE("audit.service_plan.delete"),
    AUDIT_SERVICE_PLAN_UPDATE("audit.service_plan.update"),
    AUDIT_SERVICE_PLAN_VISIBILITY_CREATE("audit.service_plan_visibility.create"),
    AUDIT_SERVICE_PLAN_VISIBILITY_DELETE("audit.service_plan_visibility.delete"),
    AUDIT_SERVICE_PLAN_VISIBILITY_UPDATE("audit.service_plan_visibility.update"),
    AUDIT_SPACE_CREATE("audit.space.create"),
    AUDIT_SPACE_DELETE_REQUEST("audit.space.delete-request"),
    AUDIT_SPACE_UPDATE("audit.space.update"),
    AUDIT_USER_PROVIDED_SERVICE_INSTANCE_CREATE("audit.user_provided_service_instance.create"),
    AUDIT_USER_PROVIDED_SERVICE_INSTANCE_DELETE("audit.user_provided_service_instance.delete"),
    AUDIT_USER_PROVIDED_SERVICE_INSTANCE_UPDATE("audit.user_provided_service_instance.update"),
    AUDIT_USER_SPACE_AUDITOR_ADD("audit.user.space_auditor_add"),
    AUDIT_USER_SPACE_AUDITOR_REMOVE("audit.user.space_auditor_remove"),
    AUDIT_USER_SPACE_MANAGER_ADD("audit.user.space_manager_add"),
    AUDIT_USER_SPACE_MANAGER_REMOVE("audit.user.space_manager_remove"),
    AUDIT_USER_SPACE_DEVELOPER_ADD("audit.user.space_developer_add"),
    AUDIT_USER_SPACE_DEVELOPER_REMOVE("audit.user.space_developer_remove"),
    AUDIT_USER_ORGANIZATION_AUDITOR_ADD("audit.user.organization_auditor_add"),
    AUDIT_USER_ORGANIZATION_AUDITOR_REMOVE("audit.user.organization_auditor_remove"),
    AUDIT_USER_ORGANIZATION_BILLING_MANAGER_ADD("audit.user.organization_billing_manager_add"),
    AUDIT_USER_ORGANIZATION_BILLING_MANAGER_REMOVE("audit.user.organization_billing_manager_remove"),
    AUDIT_USER_ORGANIZATION_MANAGER_ADD("audit.user.organization_manager_add"),
    AUDIT_USER_ORGANIZATION_MANAGER_REMOVE("audit.user.organization_manager_remove"),
    AUDIT_USER_ORGANIZATION_USER_ADD("audit.user.organization_user_add"),
    AUDIT_USER_ORGANIZATION_USER_REMOVE("audit.user.organization_user_remove"),
    BLOB_REMOVE_ORPHAN("blob.remove_orphan"),
    AUDIT_APP_BUILD_CREATE("audit.app.build.create"),
    AUDIT_APP_DROPLET_CREATE("audit.app.droplet.create"),
    AUDIT_APP_DROPLET_DELETE("audit.app.droplet.delete"),
    AUDIT_APP_DROPLET_DOWNLOAD("audit.app.droplet.download"),
    AUDIT_APP_PROCESS_CRASH("audit.app.process.crash"),
    AUDIT_APP_PROCESS_CREATE("audit.app.process.create"),
    AUDIT_APP_PROCESS_DELETE("audit.app.process.delete"),
    AUDIT_APP_PROCESS_SCALE("audit.app.process.scale"),
    AUDIT_APP_PROCESS_TERMINATE_INSTANCE("audit.app.process.terminate_instance"),
    AUDIT_APP_PROCESS_UPDATE("audit.app.process.update"),
    AUDIT_APP_TASK_CANCEL("audit.app.task.cancel"),
    AUDIT_APP_TASK_CREATE("audit.app.task.create"),
    AUDIT_SERVICE_INSTANCE_SHARE("audit.service_instance.share"),
    AUDIT_SERVICE_INSTANCE_UNSHARE("audit.service_instance.unshare");

    private final String id;

    EventType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static EventType from(String id) {
        EventType result = null;
        List<EventType> candidates = Arrays.asList(EventType.values()).stream().filter(et -> et.getId().equalsIgnoreCase(id)).collect(Collectors.toList());
        if (candidates != null && candidates.size() == 1) {
            result = candidates.get(0);
        }
        Assert.isTrue(result != null, "Not a valid event type identifier");
        return result;
    }
}