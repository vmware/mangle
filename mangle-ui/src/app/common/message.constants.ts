export class MessageConstants {

    public static readonly TEST_CONNECTION = "Test connection successful.";
    public static readonly PASSWORD_OR_KEY_REQUIRED = "Password or private key is required.";
    public static readonly QUESTION_MARK = " ?";
    public static readonly MANGLE_SUPPORT_BUNDLE = "mangle-support-bundle-";
    public static readonly ZIP_EXT = ".zip";
    public static readonly RESOURCE_LABEL_REQUIRED = "At least one resource label is required.";

    public static readonly AUTH_FAILED = "Invalid username or password.";
    public static readonly PASSWORD_UPDATED = "Password updated successfully.";
    public static readonly PASSWORD_MISMATCH = "Retype password mismatch. Please try again.";
    public static readonly PASSWORD_UPDATE_FAILED = "Failed to update password.";
    public static readonly OLD_PASSWORD_WRONG = MessageConstants.PASSWORD_UPDATE_FAILED + " Please enter the correct old password.";

    public static readonly ENDPOINT_ADD = " endpoint created successfully.";
    public static readonly ENDPOINT_UPDATE = " endpoint updated successfully.";
    public static readonly ENDPOINT_DELETE = " The selected endpoint(s) deleted successfully.";
    public static readonly ENDPOINT_DISABLE = " The selected endpoint(s) disabled successfully.";
    public static readonly ENDPOINT_ENABLE = " The selected endpoint(s) enabled successfully.";
    public static readonly NO_ENDPOINT_SELECTED = " No endpoint(s) is selected";

    public static readonly ENDPOINT_GROUP_ADD = " endpoint group created successfully.";
    public static readonly ENDPOINT_GROUP_UPDATE = " endpoint group updated successfully.";
    public static readonly ENDPOINT_GROUP_DELETE = " The selected endpoint group(s) deleted successfully.";
    public static readonly ENDPOINT_GROUP_DISABLE = " The selected endpoint group(s) disabled successfully.";
    public static readonly ENDPOINT_GROUP_ENABLE = " The selected endpoint group(s) enabled successfully.";
    public static readonly NO_ENDPOINT_GROUP_SELECTED = " No endpoint group(s) is selected";
    public static readonly NOT_SELECTED_MINIMUM_ENDPOINTS = " Please select atleast two endpoints to form a group";

    public static readonly CREDENTIAL_ADD = " credential created successfully.";
    public static readonly CREDENTIAL_UPDATE = " credential updated successfully.";
    public static readonly CREDENTIAL_DELETE = " The selected credential(s) deleted successfully.";
    public static readonly NO_CREDENTIAL_SELECTED = " No credential(s) selected";

    public static readonly CERTIFICATES_ADD = " certificates uploaded successfully.";
    public static readonly CERTIFICATES_UPDATE = " certificates updated successfully.";
    public static readonly CERTIFICATES_DELETE = " certificates deleted successfully.";

    public static readonly IDENTITY_ADD = " identity source registered successfully.";
    public static readonly IDENTITY_UPDATE = " identity source updated successfully.";
    public static readonly IDENTITY_DELETE = " identity source deleted successfully.";

    public static readonly LOGGER_UPDATE = "Logger updated successfully.";
    public static readonly CLUSTER_UPDATE = "Cluster updated successfully.";

    public static readonly ROLE_ADD = " role created successfully.";
    public static readonly ROLE_UPDATE = " role updated successfully.";
    public static readonly ROLE_DELETE = " role deleted successfully.";
    public static readonly MIN_PRIVILEGE_REQUIRED = "Minimum one privilege is required.";

    public static readonly USER_ADD = " user created successfully.";
    public static readonly USER_UPDATE = " user updated successfully.";
    public static readonly USER_DELETE = " user deleted successfully.";

    public static readonly METRIC_PROVIDER_ADD = " metric provider registered successfully.";
    public static readonly METRIC_PROVIDER_UPDATE = " metric provider updated successfully.";
    public static readonly METRIC_PROVIDER_DELETE = " metric provider deleted successfully.";
    public static readonly METRIC_PROVIDER_STATUS_UPDATE = " metric provider updated as active.";
    public static readonly METRIC_PROVIDER_DiSABLED = " metric provider is disabled.";
    public static readonly METRIC_PROVIDER_COLLECTION_UPDATE = "Updated metric collection status successfully.";

    public static readonly TASK_DELETE = " The selected task(s) deleted successfully.";
    public static readonly REMEDIATION_TASK_TRIGGERED = " remediation task triggered successfully.";
    public static readonly FAULT_TRIGGERED = "Fault re-triggered successfully.";
    public static readonly REMEDIATION_RE_TRIGGERED = "Remediation of fault re-triggered successfully.";
    public static readonly NO_TASK_SELECTED = " No Task(s) selected";

    public static readonly SCHEDULE_DELETE = " schedule deletion request submitted successfully.";
    public static readonly SCHEDULE_CANCEL = " schedule cancellation request submitted successfully.";
    public static readonly SCHEDULE_PAUSE = " schedule pause request submitted successfully.";
    public static readonly SCHEDULE_RESUME = " schedule resume request submitted successfully.";
    public static readonly SCHEDULE_MODIFY = " schedule modify request submitted successfully.";

    public static readonly DELETE_CONFIRM = "Are you sure you want to delete ";
    public static readonly DISABLE_CONFIRM = "Are you sure you want to disable ";
    public static readonly ENABLE_CONFIRM = "Are you sure you want to enable ";
    public static readonly REMEDIATE_CONFIRM = "Are you sure you want to remediate ";
    public static readonly RERUN_FAULT_CONFIRM = "Are you sure you want to rerun the fault ";
    public static readonly DELETE_SCHEDULE_CONFIRM = "Deleting associated tasks. " + MessageConstants.DELETE_CONFIRM;
    public static readonly CANCEL_CONFIRM = "Are you sure you want to cancel ";
    public static readonly PAUSE_CONFIRM = "Are you sure you want to pause ";
    public static readonly RESUME_CONFIRM = "Are you sure you want to resume ";
    public static readonly MODIFY_CONFIRM = "Are you sure you want to modify ";

    public static readonly PLUGIN_UPLOADED = "Plugin uploaded successfully.";
    public static readonly PLUGIN_ACTION_MESSAGE = " action on plugin: ";
    public static readonly PLUGIN_ACTION_COMPLETED = " completed successfully.";
    public static readonly PLUGIN_DELETE = " plugin deleted successfully.";
    public static readonly PLUGIN_UPGRADED_ERR = "Can't edit and Re-trigger since the plugin is upgraded.";
    public static readonly PLUGIN_UNAVAILABLE = "Plugin unavailable.";

    public static readonly DISK_FILL_CONFIRM = "Are you sure you want to fill root partition more than 90%";
    public static readonly ENABLE_AUTO_REFRESH = "ENABLE AUTO REFRESH";
    public static readonly DISABLE_AUTO_REFRESH = "DISABLE AUTO REFRESH";
    public static readonly NOTIFICATION_ADD = " notification created successfully.";
    public static readonly NOTIFICATION_UPDATE = " notification updated successfully.";
    public static readonly NO_NOTIFICATION_SELECTED = " No notification(s) is selected";
    public static readonly NOTIFICATION_DELETE = " The selected notification(s) deleted successfully.";
    public static readonly NOTIFICATION_DISABLE = " The selected notification(s) disabled successfully.";
    public static readonly NOTIFICATION_ENABLE = " The selected notification(s) enabled successfully.";

    public static readonly RESILIENCY_SCORE_METRIC_CONFIG = " ResiliencyScore Metric Configuration ";
    public static readonly RESILIENCY_SCORE_METRIC_CONFIG_ADD = MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG + "created successfully.";
    public static readonly RESILIENCY_SCORE_METRIC_CONFIG_UPDATE_SUCCESSFUL = MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG + "updated successfully.";
    public static readonly RESILIENCY_SCORE_METRIC_CONFIG_DELETE = " The selected " + MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG +  "deleted successfully.";
    public static readonly RESILIENCY_SCORE_METRIC_CONFIG_NOT_SELECTED = MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG + " not selected ";
    public static readonly RESILIENCY_SCORE = "Resiliency score "
    public static readonly RESILIENCY_SCORE_SUBMITTED = MessageConstants.RESILIENCY_SCORE + " calculating submitted";
    public static readonly RESILINECY_SCORE_SUBMITTED_FOR_SERVICE = MessageConstants.RESILIENCY_SCORE_SUBMITTED + " for the service: ";
    public static readonly QUERY = " Query ";
    public static readonly QUERY_CREATED = MessageConstants.QUERY + " created successfully";
    public static readonly QUERY_UPDATED = MessageConstants.QUERY + " updated successfully";
    public static readonly QUERY_DELETED = MessageConstants.QUERY + " deleted successfully";
    public static readonly QUERY_NOT_SELECTED = MessageConstants.QUERY + " not selected";

    public static readonly SERVICE = " Service ";
    public static readonly SERVICE_CREATED = MessageConstants.SERVICE + " created successfully";
    public static readonly SERVICE_UPDATED = MessageConstants.SERVICE + " updated successfully";
    public static readonly SERVICE_DELETED = MessageConstants.SERVICE + " deleted successfully";
    public static readonly SERVICE_NOT_SELECTED = MessageConstants.SERVICE + " not selected";
}