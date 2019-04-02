import { Component, OnInit } from '@angular/core';
import { RequestsService } from '../requests.service';
import { Router } from '@angular/router';
import { ClrDatagridSortOrder } from '@clr/angular';

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

  public deleteSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    this.isLoadingScheduledJobs = true;
    if (confirm('Associated tasks will also be deleted. Are you sure you want to delete schedule: ' + scheduleTask.id + '?')) {
      this.requestsService.deleteSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + ' schedule deleted successfully!';
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
        });
    } else {
      // Do nothing!
    }
  }

  public cancelSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    this.isLoadingScheduledJobs = true;
    if (confirm('Are you sure you want to delete schedule: ' + scheduleTask.id + '?')) {
      this.requestsService.cancelSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + ' schedule canceled successfully!';
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
        });
    } else {
      // Do nothing!
    }
  }

  public pauseSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    this.isLoadingScheduledJobs = true;
    if (confirm('Are you sure you want to delete schedule: ' + scheduleTask.id + '?')) {
      this.requestsService.pauseSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + ' schedule paused successfully!';
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
        });
    } else {
      // Do nothing!
    }
  }

  public resumeSchedule(scheduleTask) {
    this.errorFlag = false;
    this.successFlag = false;
    this.isLoadingScheduledJobs = true;
    if (confirm('Are you sure you want to delete schedule: ' + scheduleTask.id + '?')) {
      this.requestsService.resumeSchedule(scheduleTask.id).subscribe(
        res => {
          this.getAllScheduleJobs();
          this.alertMessage = scheduleTask.id + ' schedule resumed successfully!';
          this.successFlag = true;
          this.isLoadingScheduledJobs = false;
        }, err => {
          this.getAllScheduleJobs();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoadingScheduledJobs = false;
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
