import {Component, Inject, OnInit} from "@angular/core";
import {SettingService} from "../setting.service";
import {MessageConstants} from "src/app/common/message.constants";
import {ClrLoadingState} from "@clr/angular";
import {DOCUMENT} from "@angular/common";

@Component({
  selector: "app-logging",
  templateUrl: "./logging.component.html"
})
export class LoggingComponent implements OnInit {

  constructor(private settingService: SettingService, @Inject(DOCUMENT) document) {
  }

  public loggers: any;
  public levels: any = [];
  public loggerFormData: any = {"name": null, "configProp": {"configuredLevel": null, "effectiveLevel": null}};

  public alertMessage: string;
  public isErrorMessage: boolean;
  public loggerModal: boolean;

  public addEdit: string;

  public isLoading = true;
  public btnState: ClrLoadingState = ClrLoadingState.DEFAULT;

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
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public updateLogger(loggerFormVal) {
    this.isLoading = true;
    this.settingService.updateLogger(loggerFormVal).subscribe(
      res => {
        this.getLoggers();
        this.isErrorMessage = false;
        this.alertMessage = MessageConstants.LOGGER_UPDATE;
        this.isLoading = false;
      }, err => {
        this.getLoggers();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public downloadLogBundle() {
    this.btnState = ClrLoadingState.LOADING;
    this.settingService.downloadSupportBundle().subscribe(
      res => {
        this.btnState = ClrLoadingState.DEFAULT;
        const blob = new Blob([res], {
          type: "application/zip"
        });
        var downloadLink = document.createElement("a");
        downloadLink.href = window.URL.createObjectURL(blob);
        downloadLink.download = MessageConstants.MANGLE_SUPPORT_BUNDLE + new Date().toISOString() + MessageConstants.ZIP_EXT;
        document.body.appendChild(downloadLink);
        downloadLink.click();
        document.body.removeChild(downloadLink);
      }, err => {
        this.btnState = ClrLoadingState.DEFAULT;
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      }
    );
  }

}
