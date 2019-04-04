import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class FaultService {

    constructor(private http: HttpClient) { }

    public executeCpuFault(faultData) {
        this.deleteUserIfNoUser(faultData);
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/cpu', faultData);
    }

    public executeMemoryFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/memory', faultData);
    }

    public executeDiskIOFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/diskIO', faultData);
    }

    public executeKillProcessFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/kill-process', faultData);
    }

    public executeDockerStateChangeFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/docker', faultData);
    }

    public executeK8SDeleteResourceFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/k8s/delete-resource', faultData);
    }

    public executeK8SResourceNotReadyFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/k8s/resource-not-ready', faultData);
    }

    public executeVcenterDiskFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/vcenter/disk', faultData);
    }

    public executeVcenterNicFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/vcenter/nic', faultData);
    }

    public executeVcenterStateFault(faultData) {
        this.deleteScheduleIfNotSchedule(faultData);
        return this.http.post('/mangle-services/rest/api/v1/faults/vcenter/state', faultData);
    }

    public deleteScheduleIfNotSchedule(faultData) {
        if (faultData.schedule.cronExpression == "" || faultData.schedule.cronExpression == null) {
            delete faultData["schedule"];
        }
    }

    public deleteUserIfNoUser(faultData) {
        if (!(faultData.jvmProperties === undefined)) {
            if (faultData.jvmProperties.user == "" || faultData.jvmProperties.user == null) {
                delete faultData.jvmProperties["user"];
            }
        }
    }

}
