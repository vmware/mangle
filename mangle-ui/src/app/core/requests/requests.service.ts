import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";
import { ServiceConstants } from 'src/app/common/service.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class RequestsService {

    constructor(private http: HttpClient) { }

    public getAllTasks(): Observable<any> {
        return this.http.get(ServiceConstants.TASKS);
    }

    public getAllTasksBasedOnIndex(filterOn): Observable<any> {
        return this.http.post(ServiceConstants.TASKS_PAGE, filterOn);
    }

    public getTaskById(id): Observable<any> {
        return this.http.get(ServiceConstants.TASKS + ServiceConstants.FILE_SEPARATOR + id);
    }

    public getAllScheduleJobs(): Observable<any> {
        return this.http.get(ServiceConstants.SCHEDULER);
    }

    public deleteScheduleOnly(id) {
        return this.http.delete(ServiceConstants.SCHEDULER + CommonConstants.QUESTION_MARK + CommonConstants.jobIds + CommonConstants.EQUALS_TO + id);
    }

    public deleteSchedule(id) {
        return this.http.delete(ServiceConstants.SCHEDULER + CommonConstants.QUESTION_MARK + CommonConstants.delete_associated_tasks + CommonConstants.EQUALS_TO + CommonConstants.true_var + CommonConstants.AND_OP + CommonConstants.jobIds + CommonConstants.EQUALS_TO + id);
    }

    public cancelSchedule(id) {
        return this.http.post(ServiceConstants.SCHEDULER_CANCEL + id, null);
    }

    public pauseSchedule(id) {
        return this.http.post(ServiceConstants.SCHEDULER_PAUSE + id, null);
    }

    public modifySchedule(scheduleTask) {
        return this.http.put(ServiceConstants.SCHEDULER, scheduleTask);
    }

    public resumeSchedule(id) {
        return this.http.post(ServiceConstants.SCHEDULER_RESUME + id, null);
    }

    public deleteTask(taskID) {
        return this.http.delete(ServiceConstants.TASKS + CommonConstants.QUESTION_MARK + CommonConstants.tasksIds + CommonConstants.EQUALS_TO + taskID);
    }

    public remediateFault(taskID) {
        return this.http.delete(ServiceConstants.FAULTS + taskID);
    }

    public rerunFault(taskID) {
        return this.http.post(ServiceConstants.FAULTS + taskID, null);
    }

    public getLogFile(): Observable<any> {
        return this.http.get(ServiceConstants.LOGFILE);
    }

    public getPluginDetails(pluginId): Observable<any> {
        return this.http.get(ServiceConstants.PLUGIN_DETAILS + CommonConstants.QUESTION_MARK + CommonConstants.pluginId + CommonConstants.EQUALS_TO + pluginId);
    }

}
