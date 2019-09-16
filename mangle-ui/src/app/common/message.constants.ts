export class MessageConstants {

    public static readonly TEST_CONNECTION = "Test connection successful.";
    public static readonly PASSWORD_OR_KEY_REQUIRED = "Password or private key is required.";
    public static readonly QUESTION_MARK = " ?";
    public static readonly RESOURCE_LABEL_REQUIRED = "At least one resource label is required.";

    public static readonly AUTH_FAILED = "Invalid username or password.";
    public static readonly PASSWORD_UPDATED = "Password updated successfully.";
    public static readonly PASSWORD_MISMATCH = "Retype password mismatch. Please try again.";
    public static readonly PASSWORD_UPDATE_FAILED = "Failed to update password.";
    public static readonly OLD_PASSWORD_WRONG = MessageConstants.PASSWORD_UPDATE_FAILED + " Please enter the correct old password.";

    public static readonly ENDPOINT_ADD = " endpoint created successfully.";
    public static readonly ENDPOINT_UPDATE = " endpoint updated successfully.";
    public static readonly ENDPOINT_DELETE = " endpoint deleted successfully.";

    public static readonly CREDENTIAL_ADD = " credential created successfully.";
    public static readonly CREDENTIAL_UPDATE = " credential updated successfully.";
    public static readonly CREDENTIAL_DELETE = " credential deleted successfully.";

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
    public static readonly METRIC_PROVIDER_COLLECTION_UPDATE = "Updated metric collection status successfully.";

    public static readonly TASK_DELETE = " task deleted successfully.";
    public static readonly REMEDIATION_TASK_TRIGGERED = " remediation task triggered successfully.";
    public static readonly FAULT_TRIGGERED = "Fault re-triggered successfully.";
    public static readonly REMEDIATION_RE_TRIGGERED = "Remediation of fault re-triggered successfully.";

    public static readonly SCHEDULE_DELETE = " schedule deletion request submitted successfully.";
    public static readonly SCHEDULE_CANCEL = " schedule cancellation request submitted successfully.";
    public static readonly SCHEDULE_PAUSE = " schedule pause request submitted successfully.";
    public static readonly SCHEDULE_RESUME = " schedule resume request submitted successfully.";

    public static readonly DELETE_CONFIRM = "Are you sure you want to delete ";
    public static readonly REMEDIATE_CONFIRM = "Are you sure you want to remediate ";
    public static readonly RERUN_FAULT_CONFIRM = "Are you sure you want to rerun the fault ";
    public static readonly DELETE_SCHEDULE_CONFIRM = "Deleting associated tasks. " + MessageConstants.DELETE_CONFIRM;
    public static readonly CANCEL_CONFIRM = "Are you sure you want to cancel ";
    public static readonly PAUSE_CONFIRM = "Are you sure you want to pause ";
    public static readonly RESUME_CONFIRM = "Are you sure you want to resume ";

    public static readonly PLUGIN_UPLOADED = "Plugin uploaded successfully.";
    public static readonly PLUGIN_ACTION_MESSAGE = " action on plugin: ";
    public static readonly PLUGIN_ACTION_COMPLETED = " completed successfully.";
    public static readonly PLUGIN_DELETE = " plugin deleted successfully.";
    public static readonly PLUGIN_UPGRADED_ERR = "Can't edit and Re-trigger since the plugin is upgraded.";
    public static readonly PLUGIN_UNAVAILABLE = "Plugin unavailable.";

}