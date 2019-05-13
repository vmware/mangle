export class ServiceConstants {

    public static readonly URL_PREFIX = "/mangle-services/rest/api/v1/";
    public static readonly LOGOUT = "/mangle-services/logout";
    public static readonly LOGFILE = "/mangle-services/application/logfile";
    public static readonly FILE_SEPARATOR = "/";
    public static readonly LOGGERS = "/mangle-services/application/loggers";

    public static readonly ENDPOINTS = ServiceConstants.URL_PREFIX + "endpoints";
    public static readonly ENDPOINTS_TYPE = ServiceConstants.URL_PREFIX + "endpoints/type";
    public static readonly ENDPOINTS_CREDENTIALS = ServiceConstants.URL_PREFIX + "endpoints/credentials";
    public static readonly ENDPOINTS_CREDENTIALS_K8S = ServiceConstants.URL_PREFIX + "endpoints/credentials/k8s";
    public static readonly ENDPOINTS_CREDENTIALS_REMOTE_MACHINE = ServiceConstants.URL_PREFIX + "endpoints/credentials/remotemachine";
    public static readonly ENDPOINTS_CREDENTIALS_VCENTER = ServiceConstants.URL_PREFIX + "endpoints/credentials/vcenter";
    public static readonly ENDPOINTS_CERTIFICATES = ServiceConstants.URL_PREFIX + "endpoints/certificates";
    public static readonly ENDPOINTS_CERTIFICATES_DOCKER = ServiceConstants.URL_PREFIX + "endpoints/certificates/docker";
    public static readonly TEST_ENDPOINTS = ServiceConstants.URL_PREFIX + "endpoints/testEndpoint";

    public static readonly USER_MANAGEMENT_USER = ServiceConstants.URL_PREFIX + "user-management/user";
    public static readonly USER_MANAGEMENT_PASSWORD = ServiceConstants.URL_PREFIX + "user-management/password";
    public static readonly USER_MANAGEMENT_USERS = ServiceConstants.URL_PREFIX + "user-management/users";
    public static readonly USER_MANAGEMENT_USERS_ADMIN = ServiceConstants.URL_PREFIX + "user-management/users/admin";

    public static readonly ROLE_MANAGEMENT_ROLES = ServiceConstants.URL_PREFIX + "role-management/roles";
    public static readonly ROLE_MANAGEMENT_PRIVILEGES = ServiceConstants.URL_PREFIX + "role-management/privileges";

    public static readonly AUTH_PROVIDER_MANAGEMENT_DOMAINS = ServiceConstants.URL_PREFIX + "auth-provider-management/domains";
    public static readonly AUTH_PROVIDER_MANAGEMENT_PROVIDERS = ServiceConstants.URL_PREFIX + "auth-provider-management/ad-auth-providers";

    public static readonly FAULTS = ServiceConstants.URL_PREFIX + "faults/";
    public static readonly FAULTS_CPU = ServiceConstants.URL_PREFIX + "faults/cpu";
    public static readonly FAULTS_MEMORY = ServiceConstants.URL_PREFIX + "faults/memory";
    public static readonly FAULTS_DISKIO = ServiceConstants.URL_PREFIX + "faults/diskIO";
    public static readonly FAULTS_KILL_PROCESS = ServiceConstants.URL_PREFIX + "faults/kill-process";
    public static readonly FAULTS_DOCKER = ServiceConstants.URL_PREFIX + "faults/docker";
    public static readonly FAULTS_K8S_DELETE_RESOURCE = ServiceConstants.URL_PREFIX + "faults/k8s/delete-resource";
    public static readonly FAULTS_K8S_RESOURCE_NOT_READY = ServiceConstants.URL_PREFIX + "faults/k8s/resource-not-ready";
    public static readonly FAULTS_VCENTER_DISK = ServiceConstants.URL_PREFIX + "faults/vcenter/disk";
    public static readonly FAULTS_VCENTER_NIC = ServiceConstants.URL_PREFIX + "faults/vcenter/nic";
    public static readonly FAULTS_VCENTER_STATE = ServiceConstants.URL_PREFIX + "faults/vcenter/state";

    public static readonly TASKS = ServiceConstants.URL_PREFIX + "tasks";
    public static readonly SCHEDULER = ServiceConstants.URL_PREFIX + "scheduler";
    public static readonly SCHEDULER_CANCEL = ServiceConstants.URL_PREFIX + "scheduler/cancel/";
    public static readonly SCHEDULER_PAUSE = ServiceConstants.URL_PREFIX + "scheduler/pause/";
    public static readonly SCHEDULER_RESUME = ServiceConstants.URL_PREFIX + "scheduler/resume/";

    public static readonly METRIC_PROVIDERS = ServiceConstants.URL_PREFIX + "metric-providers";
    public static readonly METRIC_PROVIDERS_STATUS = ServiceConstants.URL_PREFIX + "metric-providers/status";
    public static readonly METRIC_PROVIDERS_COLLECTION_STATUS = ServiceConstants.URL_PREFIX + "metric-providers/mangle-metrics-collection-status";
    public static readonly METRIC_PROVIDERS_TEST_CONNECTION = ServiceConstants.URL_PREFIX + "metric-providers/test-connection";

}