import { QUERIES } from '@angular/core/src/render3/interfaces/view';

export class ServiceConstants {

    public static readonly URL_PREFIX = "/mangle-services/rest/api/v1/";
    public static readonly URL_PREFIX_V2 = "/mangle-services/rest/api/v2/";
    public static readonly LOGOUT = "/mangle-services/logout";
    public static readonly LOGFILE = "/mangle-services/application/logfile";
    public static readonly FILE_SEPARATOR = "/";
    public static readonly LOGGERS = "/mangle-services/application/loggers";
    public static readonly APPLICATION_INFO = "/mangle-services/application/info";
    public static readonly APPLICATION_HEALTH = "/mangle-services/application/health";
    public static readonly APPLICATION_SUPPORT_BUNDLE = "/mangle-services/application/zip";

    public static readonly ENDPOINTS = ServiceConstants.URL_PREFIX + "endpoints";
    public static readonly ENDPOINTS_ENABLE = ServiceConstants.URL_PREFIX + "endpoints/enable";
    public static readonly ENDPOINTS_V2 = ServiceConstants.URL_PREFIX_V2 + "endpoints";
    public static readonly ENDPOINTS_TYPE = ServiceConstants.URL_PREFIX + "endpoints/type";
    public static readonly ENDPOINTS_CREDENTIALS = ServiceConstants.URL_PREFIX + "endpoints/credentials";
    public static readonly ENDPOINTS_CREDENTIALS_K8S = ServiceConstants.URL_PREFIX + "endpoints/credentials/k8s";
    public static readonly ENDPOINTS_CREDENTIALS_REMOTE_MACHINE = ServiceConstants.URL_PREFIX + "endpoints/credentials/remotemachine";
    public static readonly ENDPOINTS_CREDENTIALS_VCENTER = ServiceConstants.URL_PREFIX + "endpoints/credentials/vcenter";
    public static readonly ENDPOINTS_CREDENTIALS_AWS = ServiceConstants.URL_PREFIX + "endpoints/credentials/aws";
    public static readonly ENDPOINTS_CREDENTIALS_AZURE = ServiceConstants.URL_PREFIX + "endpoints/credentials/azure";
    public static readonly ENDPOINTS_CERTIFICATES = ServiceConstants.URL_PREFIX + "endpoints/certificates";
    public static readonly ENDPOINTS_CERTIFICATES_DOCKER = ServiceConstants.URL_PREFIX + "endpoints/certificates/docker";
    public static readonly TEST_ENDPOINTS = ServiceConstants.URL_PREFIX + "endpoints/testEndpoint";
    public static readonly ENDPOINTS_DOCKER_CONTAINER = ServiceConstants.ENDPOINTS + "/docker/containers"
    public static readonly ENDPOINTS_K8S_RESOURCES = ServiceConstants.ENDPOINTS + "/k8s/resources";
    public static readonly ENDPOINTS_CREDENTIALS_DATABASE = ServiceConstants.URL_PREFIX + "endpoints/credentials/database";
    public static readonly ENDPOINTS_K8S_READY_NODES = ServiceConstants.ENDPOINTS + "/k8s/nodes/ready";

    public static readonly USER_MANAGEMENT_USER = ServiceConstants.URL_PREFIX + "user-management/user";
    public static readonly USER_MANAGEMENT_PASSWORD = ServiceConstants.URL_PREFIX + "user-management/password";
    public static readonly USER_MANAGEMENT_USERS = ServiceConstants.URL_PREFIX + "user-management/users";
    public static readonly USER_MANAGEMENT_USERS_ADMIN = ServiceConstants.URL_PREFIX + "user-management/users/admin";
    public static readonly USER_MANAGEMENT_USERS_LOGIN = ServiceConstants.URL_PREFIX + "user-management/login";

    public static readonly ROLE_MANAGEMENT_ROLES = ServiceConstants.URL_PREFIX + "role-management/roles";
    public static readonly ROLE_MANAGEMENT_PRIVILEGES = ServiceConstants.URL_PREFIX + "role-management/privileges";

    public static readonly AUTH_PROVIDER_MANAGEMENT_DOMAINS = ServiceConstants.URL_PREFIX + "auth-provider-management/domains";
    public static readonly AUTH_PROVIDER_MANAGEMENT_PROVIDERS = ServiceConstants.URL_PREFIX + "auth-provider-management/ad-auth-providers";
    public static readonly AUTH_PROVIDER_TEST_CONNECTION = ServiceConstants.URL_PREFIX + "auth-provider-management/test-connection";
    public static readonly AUTH_PROVIDER_MANAGEMENT_PROVIDERS_V2 = ServiceConstants.URL_PREFIX_V2 + "auth-provider-management/ad-auth-providers";

    public static readonly FAULTS = ServiceConstants.URL_PREFIX + "faults/";
    public static readonly FAULTS_CPU = ServiceConstants.URL_PREFIX + "faults/cpu";
    public static readonly FAULTS_MEMORY = ServiceConstants.URL_PREFIX + "faults/memory";
    public static readonly FAULTS_DISKIO = ServiceConstants.URL_PREFIX + "faults/diskIO";
    public static readonly FAULTS_KILL_PROCESS = ServiceConstants.URL_PREFIX + "faults/kill-process";
    public static readonly FAULTS_STOP_SERVICE = ServiceConstants.URL_PREFIX + "faults/stop-service";
    public static readonly FAULTS_DOCKER = ServiceConstants.URL_PREFIX + "faults/docker";
    public static readonly FAULTS_K8S_DELETE_RESOURCE = ServiceConstants.URL_PREFIX + "faults/k8s/delete-resource";
    public static readonly FAULTS_DRAIN_K8S_NODE = ServiceConstants.URL_PREFIX + "faults/k8s/drain-node";
    public static readonly FAULTS_K8S_RESOURCE_NOT_READY = ServiceConstants.URL_PREFIX + "faults/k8s/resource-not-ready";
    public static readonly FAULTS_K8S_SERVICE_UNAVAILABLE = ServiceConstants.URL_PREFIX + "faults/k8s/service-unavailable";
    public static readonly FAULTS_VCENTER_VM_DISK = ServiceConstants.URL_PREFIX + "faults/vcenter/disk";
    public static readonly FAULTS_VCENTER_VM_NIC = ServiceConstants.URL_PREFIX + "faults/vcenter/nic";
    public static readonly FAULTS_VCENTER_VM_STATE = ServiceConstants.URL_PREFIX + "faults/vcenter/state";
    public static readonly FAULTS_VCENTER_HOST_STATE = ServiceConstants.URL_PREFIX + "faults/vcenter/host";
    public static readonly PLUGINS_CUSTOM_FAULT = ServiceConstants.URL_PREFIX + "plugins/custom-fault";
    public static readonly FAULTS_NETWORK = ServiceConstants.URL_PREFIX + "faults/network-fault";
    public static readonly FAULTS_FILE_HANDLER_LEAK = ServiceConstants.URL_PREFIX + "faults/filehandler-leak";
    public static readonly FAULTS_THREAD_LEAK = ServiceConstants.URL_PREFIX + "faults/thread-leak";
    public static readonly FAULTS_AWS_EC2_STATE = ServiceConstants.URL_PREFIX + "faults/aws/ec2/state";
    public static readonly FAULTS_AWS_EC2_NETWORK = ServiceConstants.URL_PREFIX + "faults/aws/ec2/network";
    public static readonly FAULTS_AWS_EC2_STORAGE = ServiceConstants.URL_PREFIX + "faults/aws/ec2/storage";
    public static readonly FAULTS_AWS_RDS = ServiceConstants.URL_PREFIX + "faults/aws/rds";
    public static readonly FAULTS_AZURE_VM_STATE = ServiceConstants.URL_PREFIX + "faults/azure/virtualmachine/state";
    public static readonly FAULTS_AZURE_VM_STORAGE = ServiceConstants.URL_PREFIX + "faults/azure/virtualmachine/storage";
    public static readonly FAULTS_AZURE_VM_NETWORK = ServiceConstants.URL_PREFIX + "faults/azure/virtualmachine/network";
    public static readonly FAULTS_JAVA_METHOD_LATENCY = ServiceConstants.URL_PREFIX + "faults/java-method-latency";
    public static readonly FAULTS_SPRING_SERVICE_LATENCY = ServiceConstants.URL_PREFIX + "faults/spring-service-latency";
    public static readonly FAULTS_SPRING_SERVICE_EXCEPTION = ServiceConstants.URL_PREFIX + "faults/spring-service-exception";
    public static readonly FAULTS_KILL_JVM = ServiceConstants.URL_PREFIX + "faults/kill-jvm";
    public static readonly FAULTS_SIMULATE_JAVA_EXCEPTION = ServiceConstants.URL_PREFIX + "faults/simulate-java-exception";
    public static readonly DB_CONNECTION_LEAK_URL = ServiceConstants.FAULTS + 'db-connection-leak';
    public static readonly DB_TRANSACTION_ERROR_URL = ServiceConstants.FAULTS + 'db-transaction-error';
    public static readonly DB_TRANSACTION_ERROR_CODES_URL = ServiceConstants.FAULTS + 'db-transaction-error-code?databaseType=';
    public static readonly DB_TRANSACTION_LATENCY_URL = ServiceConstants.FAULTS + 'db-transaction-latency';
    public static readonly REDIS_DELAY_URL = ServiceConstants.FAULTS + "redis/delay";
    public static readonly REDIS_RETURN_ERROR_URL = ServiceConstants.FAULTS + "redis/return-error";
    public static readonly REDIS_RETURN_EMPTY_URL = ServiceConstants.FAULTS + "redis/return-empty";
    public static readonly REDIS_DROP_CONNECTION_URL = ServiceConstants.FAULTS + "redis/drop-connection";
    public static readonly NETWORK_PARTITION_URL = ServiceConstants.FAULTS + "network-partition";

    public static readonly TASKS = ServiceConstants.URL_PREFIX + "tasks";
    public static readonly TASKS_PAGE = ServiceConstants.URL_PREFIX + "tasks/pagination";
    public static readonly SCHEDULER = ServiceConstants.URL_PREFIX + "scheduler";
    public static readonly SCHEDULER_CANCEL = ServiceConstants.URL_PREFIX + "scheduler/cancel/";
    public static readonly SCHEDULER_PAUSE = ServiceConstants.URL_PREFIX + "scheduler/pause/";
    public static readonly SCHEDULER_RESUME = ServiceConstants.URL_PREFIX + "scheduler/resume/";

    public static readonly METRIC_PROVIDERS = ServiceConstants.URL_PREFIX + "metric-providers";
    public static readonly METRIC_PROVIDERS_STATUS = ServiceConstants.URL_PREFIX + "metric-providers/status";
    public static readonly METRIC_PROVIDERS_COLLECTION_STATUS = ServiceConstants.URL_PREFIX + "metric-providers/mangle-metrics-collection-status";
    public static readonly METRIC_PROVIDERS_TEST_CONNECTION = ServiceConstants.URL_PREFIX + "metric-providers/test-connection";

    public static readonly CLUSTER_CONFIG = ServiceConstants.URL_PREFIX + "cluster-config";
    public static readonly CLUSTER_QUORUM_UPDATE = ServiceConstants.URL_PREFIX + "cluster-config/quorum";
    public static readonly CLUSTER_DEPLOYMENT_MODE_UPDATE = ServiceConstants.CLUSTER_CONFIG;
    public static readonly EVENTS = ServiceConstants.URL_PREFIX + "events";

    public static readonly PLUGINS = ServiceConstants.URL_PREFIX + "plugins";
    public static readonly PLUGIN_DETAILS = ServiceConstants.URL_PREFIX + "plugins/plugin-details";
    public static readonly PLUGIN_FILES = ServiceConstants.URL_PREFIX + "plugins/files";
    public static readonly PLUGIN_REQ_JSON = ServiceConstants.URL_PREFIX + "plugins/request-json";
    public static readonly PLUGIN_CUSTOM_FAULT = ServiceConstants.URL_PREFIX + "plugins/custom-fault";
    public static readonly FAULTS_DISK_SPACE = ServiceConstants.URL_PREFIX + "faults/disk-space";
    public static readonly KERNEL_PANIC_FAULT_URL = ServiceConstants.URL_PREFIX + "faults/kernel-panic";
    public static readonly NOTIFICATION_URL = ServiceConstants.URL_PREFIX + "notifier";
    public static readonly TEST_NOTIFICATION = ServiceConstants.NOTIFICATION_URL + "/testConnection";
    public static readonly NOTIFICATION_ENABLE_URL = ServiceConstants.NOTIFICATION_URL + "/enable";
    public static readonly CLOCK_SKEW_FAULT_URL = ServiceConstants.URL_PREFIX + "faults/clockSkew";

    public static readonly VCENTER_ADAPTER_DETAILS_URL = ServiceConstants.URL_PREFIX + "vcenter-adapter-details";
    public static readonly VCENTER_ADAPTER_DETAILS_TEST_CONNECTION_URL = ServiceConstants.VCENTER_ADAPTER_DETAILS_URL + "/test-connection";

    public static readonly RESILIENCY_SCORE_METRIC_CONFIG_URL = ServiceConstants.URL_PREFIX + "resiliencyscore/config";

    public static readonly RESILIENCY_SCORE = ServiceConstants.URL_PREFIX + "resiliencyscore/";
    public static readonly SERVICES = ServiceConstants.RESILIENCY_SCORE + "service";
    public static readonly CALCULATE_RESILIENCY_SCORE = ServiceConstants.RESILIENCY_SCORE + "calculate";
    public static readonly QUERIES = ServiceConstants.RESILIENCY_SCORE + "query";
    public static readonly QUERY_DELETE = ServiceConstants.QUERIES + "/deleteByName";
    public static readonly SERVICE_DELETE = ServiceConstants.SERVICES + "/deleteByName?serviceName";
}
