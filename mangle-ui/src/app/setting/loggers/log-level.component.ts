import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-log-level',
    templateUrl: './log-level.component.html',
    styleUrls: ['./log-level.component.css']
})
export class LogLevelComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public loggers: any;
    public levels: any = [];
    public loggerFormData: any = { "name": null, "configProp": { "configuredLevel": null, "effectiveLevel": null } };

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public addEdit: string;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getLoggers();
    }

    public populateLoggerForm(loggerData: any) {
        this.loggerFormData.name = loggerData.key;
        this.loggerFormData.configProp = loggerData.value;
    }

    public getLoggers() {
        this.isLoading = true;
        this.settingService.getLoggers().subscribe(
            res => {
                this.loggers = res.loggers;
                this.levels = res.levels;
                this.isLoading = false;
            }, err => {
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateLogger(loggerFormVal) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.updateLogger(loggerFormVal).subscribe(
            res => {
                this.getLoggers();
                this.alertMessage = MessageConstants.LOGGER_UPDATE;
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getLoggers();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

}
