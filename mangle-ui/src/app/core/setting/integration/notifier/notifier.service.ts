import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ServiceConstants } from 'src/app/common/service.constants';
import { Observable } from 'rxjs';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class NotifierService {

    constructor(private http: HttpClient) { }

    public getNotificationInfo(): Observable<any> {
        return this.http.get(ServiceConstants.NOTIFICATION_URL);
    }

    public addNotification(slack: any): Observable<any> {
        return this.http.post(ServiceConstants.NOTIFICATION_URL, slack);
    }

    public updateNotification(slack: any): Observable<any> {
        return this.http.put(ServiceConstants.NOTIFICATION_URL, slack);
    }

    public testNotificationConnection(slack: any): Observable<any> {
        return this.http.post(ServiceConstants.TEST_NOTIFICATION, slack);
    }

    public deleteNotification(names: any): Observable<any> {
        return this.http.delete(ServiceConstants.NOTIFICATION_URL
            + CommonConstants.QUESTION_MARK + CommonConstants.NOTIFICATION_NAMES + CommonConstants.EQUALS_TO + names);
    }

    public enableNotification(names: any, enableFlag: boolean): Observable<any> {
        return this.http.put(ServiceConstants.NOTIFICATION_ENABLE_URL + CommonConstants.QUESTION_MARK
            + 'enable' + CommonConstants.EQUALS_TO + enableFlag + CommonConstants.AND_OP
            + CommonConstants.NOTIFICATION_NAMES + CommonConstants.EQUALS_TO + names, {});
    }
}
