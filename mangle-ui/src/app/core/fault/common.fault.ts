import { OnInit } from '@angular/core';

export class CommonFault implements OnInit {

    public clockRanges: any = [];
    public hourRanges: any = [];
    public dateRages: any = [];
    public dayRanges: any = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

    ngOnInit() {

    }

    constructor() {
        for (var i = 0; i < 60; i++) {
            this.clockRanges[i] = i;
            if (i < 24) {
                this.hourRanges[i] = i;
            }
            if (i < 31) {
                this.dateRages[i] = i + 1;
            }
        }
    }

    public getClockRanges() {
        return this.clockRanges;
    }

    public getHourRanges() {
        return this.hourRanges;
    }

    public getDayRanges() {
        return this.dayRanges;
    }

    public getDateRages() {
        return this.dateRages;
    }

    public getCronExpression(scheduleFormVal) {
        if (scheduleFormVal.cronType == "Minutes") {
            return scheduleFormVal.second + " " + scheduleFormVal.startat + "/" + scheduleFormVal.minute + " * * * ?";
        }
        if (scheduleFormVal.cronType == "Hourly") {
            return "0 " + scheduleFormVal.minute + " " + scheduleFormVal.startat + "/" + scheduleFormVal.hour + " * * ?";
        }
        if (scheduleFormVal.cronType == "Daily") {
            return "0 " + scheduleFormVal.minute + " " + scheduleFormVal.hour + " * * ?";
        }
        if (scheduleFormVal.cronType == "Weekly") {
            return "0 0 " + scheduleFormVal.hour + " ? * " + scheduleFormVal.day;
        }
        if (scheduleFormVal.cronType == "Monthly") {
            return "0 0 " + scheduleFormVal.hour + " " + scheduleFormVal.date + " * ?";
        }
    }

}