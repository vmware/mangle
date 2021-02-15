from Faults import FaultStatus


class FaultInfo:
    def __init__(self,fault_args, fault_name):
        print("Initializing fault info")
        self.fault_args = fault_args
        self.fault_name = fault_name
        self._status = FaultStatus.FaultStatus.NOT_STARTED.name
        self._activity = []

    @property
    def status(self):
        return self._status

    @status.setter
    def status(self, value):
        print("status set to ",value)
        self._status = value

    @property
    def activity(self):
        return self._activity

    @activity.setter
    def activity(self, value1):
        print("Activity set to ",value1)
        self._activity = value1
