import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class RequestsService {

    constructor(private http: HttpClient) { }

    public getAllTasks(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/tasks');
    }

    public getTaskById(id): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/tasks/' + id);
    }

    public getAllScheduleJobs(): Observable<any> {
        return this.http.get('/mangle-services/rest/api/v1/scheduler');
    }

    public deleteSchedule(id) {
        return this.http.delete('/mangle-services/rest/api/v1/scheduler?delete-associated-tasks=true&taskIds=' + id);
    }

    public cancelSchedule(id) {
        return this.http.post('/mangle-services/rest/api/v1/scheduler/cancel/' + id, null);
    }

    public pauseSchedule(id) {
        return this.http.post('/mangle-services/rest/api/v1/scheduler/pause/' + id, null);
    }

    public resumeSchedule(id) {
        return this.http.post('/mangle-services/rest/api/v1/scheduler/resume/' + id, null);
    }

    public deleteTask(taskID) {
        return this.http.delete('/mangle-services/rest/api/v1/tasks?tasksIds=' + taskID);
    }

    public remediateFault(taskID) {
        return this.http.delete('/mangle-services/rest/api/v1/faults/' + taskID);
    }

    public getLogFile(): Observable<any> {
        return this.http.get('/mangle-services/application/logfile');
    }

}
