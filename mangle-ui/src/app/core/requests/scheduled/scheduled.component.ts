import { Component, OnInit } from '@angular/core';
import { RequestsService } from '../requests.service';
import { Router } from '@angular/router';
import { ClrDatagridSortOrder } from '@clr/angular';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
  selector: 'app-scheduled',
  templateUrl: './scheduled.component.html',
  styleUrls: ['./scheduled.component.css']
})
export class ScheduledComponent implements OnInit {

  constructor(private requestsService: RequestsService, private router: Router) {

  }

  public errorFlag = false;
  public successFlag = false;
  public alertMessage: string;

  public scheduledJobs: any = [];
  public scheduledTaskDetails: any = {};

  public isLoadingScheduledJobs: boolean = false;
  public isLoadingScheduledTaskDetails: boolean = false;

  public startTimeDesc = ClrDatagridSortOrder.DESC;

  ngOnInit() {
    this.getAllScheduleJobs();
  }

  public getAllScheduleJobs() {
    this.isLoadingScheduledJobs = true;
    this.requestsService.getAllScheduleJobs().subscribe(
      res => {
        if (res.code) {
          this.scheduledJobs = [];
          this.isLoadingScheduledJobs = false;
        } else {
          this.scheduledJobs = res;
          this.isLoadingScheduledJobs = false;
        }
      }, err => {
        this.scheduledJobs = [];
        this.alertMessage = err.error.description;
        this.errorFlag = true;
        this.isLoadingScheduledJobs = false;
      }
    );
  }

  public deleteScheduleOnly(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.DELETE_CONFIRM + scheduleTask.id + MessageConstants.QUESTION_MARK)) {
      this.requestsService.deleteScheduleOnly(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + MessageConstants.SCHEDULE_DELETE;
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public deleteSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.DELETE_SCHEDULE_CONFIRM + scheduleTask.id + MessageConstants.QUESTION_MARK)) {
      this.requestsService.deleteSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + MessageConstants.SCHEDULE_DELETE;
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public cancelSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.CANCEL_CONFIRM + scheduleTask.id + MessageConstants.QUESTION_MARK)) {
      this.requestsService.cancelSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + MessageConstants.SCHEDULE_CANCEL;
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public pauseSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.PAUSE_CONFIRM + scheduleTask.id + MessageConstants.QUESTION_MARK)) {
      this.requestsService.pauseSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + MessageConstants.SCHEDULE_PAUSE;
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public resumeSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.RESUME_CONFIRM + scheduleTask.id + MessageConstants.QUESTION_MARK)) {
      this.requestsService.resumeSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + MessageConstants.SCHEDULE_RESUME;
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public getScheduledTaskDetails(scheduleTask) {
    this.isLoadingScheduledTaskDetails = true;
    this.requestsService.getTaskById(scheduleTask.id).subscribe(
      res => {
        if (res.code) {
          this.alertMessage = res.description;
          this.errorFlag = true;
          this.isLoadingScheduledTaskDetails = false;
        } else {
          this.scheduledTaskDetails = res;
          if (res.triggers != null) {
            for (var i = 0; i < res.triggers.length; i++) {
              res.triggers[i].startTime = new Date(res.triggers[i].startTime);
              if (res.triggers[i].endTime != null) {
                res.triggers[i].endTime = new Date(res.triggers[i].endTime);
              }
            }
          }
          this.isLoadingScheduledTaskDetails = false;
        }
      }, err => {
        this.alertMessage = err.error.description;
        this.errorFlag = true;
        this.isLoadingScheduledTaskDetails = false;
      }
    );
  }

}
