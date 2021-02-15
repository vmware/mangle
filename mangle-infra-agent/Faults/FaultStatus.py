from enum import Enum


class FaultStatus(Enum):
    NOT_STARTED = 1
    INITIALIZING = 2
    IN_PROGRESS = 3
    COMPLETED = 4
    REMEDIATION_FAILED = 5
    INJECTION_FAILED = 6