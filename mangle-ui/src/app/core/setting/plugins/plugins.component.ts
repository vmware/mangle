import {Component, OnInit} from "@angular/core";
import {SettingService} from "../setting.service";
import {MessageConstants} from "src/app/common/message.constants";

@Component({
  selector: "app-plugins",
  templateUrl: "./plugins.component.html"
})
export class PluginsComponent implements OnInit {

  constructor(private settingService: SettingService) {
  }

  public pluginList: any;
  public pluginListCopy: any;

  public pluginFileToUpload: any;

  public alertMessage: string;
  public isErrorMessage: boolean;
  public pluginUploadModal: boolean;

  public isLoading: boolean = true;

  public pluginInfo: any = {
    "pluginAction": null,
    "pluginName": null
  };

  ngOnInit() {
    this.getPluginDetails();
  }

  public getPluginDetails() {
    this.isLoading = true;
    this.settingService.getPluginDetails().subscribe(
      res => {
        this.pluginList = res.content;
        this.isLoading = false;
      }, err => {
        this.pluginList = [];
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public getPluginFiles() {
    this.isLoading = true;
    this.settingService.getPluginFiles().subscribe(
      res => {
        this.pluginListCopy = JSON.parse(JSON.stringify(this.pluginList));
        for (var file_i = 0; file_i < res.length; file_i++) {
          var count_match = 0;
          for (var plugin_i = 0; plugin_i < this.pluginListCopy.length; plugin_i++) {
            if (res[file_i].indexOf(this.pluginListCopy[plugin_i].pluginName) > -1) {
              break;
            }
            count_match++;
          }
          if (count_match == this.pluginListCopy.length) {
            this.pluginList.push({"pluginName": res[file_i]});
          }
        }
        this.isLoading = false;
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public getPluginFile(fileToUploadEvent) {
    this.pluginFileToUpload = fileToUploadEvent.target.files[0];
  }

  public uploadCustomPlugin() {
    this.isLoading = true;
    this.settingService.uploadPlugin(this.pluginFileToUpload).subscribe(
      res => {
        this.performPluginAction("LOAD", this.pluginFileToUpload.name);
        this.isErrorMessage = false;
        this.alertMessage = MessageConstants.PLUGIN_UPLOADED;
        this.getPluginDetails();
        this.isLoading = false;
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.getPluginDetails();
        this.isLoading = false;
      }
    );
  }

  public removePlugin(pluginDetailData) {
    if (confirm(MessageConstants.DELETE_CONFIRM + pluginDetailData.pluginId + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.settingService.deletePlugin(pluginDetailData.pluginId).subscribe(
        res => {
          this.isErrorMessage = false;
          this.alertMessage = pluginDetailData.pluginId + MessageConstants.PLUGIN_DELETE;
          this.getPluginDetails();
          this.isLoading = false;
        }, err => {
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.getPluginDetails();
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }

  public deletePluginFile(pluginDetailData) {
    if (confirm(MessageConstants.DELETE_CONFIRM + pluginDetailData.pluginName + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.settingService.detelePluginFile(pluginDetailData.pluginName).subscribe(
        res => {
          this.isErrorMessage = false;
          this.alertMessage = pluginDetailData.pluginName + MessageConstants.PLUGIN_DELETE;
          this.getPluginDetails();
          this.isLoading = false;
        }, err => {
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.getPluginDetails();
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }

  public performPluginAction(pluginAction, pluginName) {
    this.isLoading = true;
    this.pluginInfo.pluginAction = pluginAction;
    this.pluginInfo.pluginName = pluginName;
    this.settingService.performPluginAction(this.pluginInfo).subscribe(
      res => {
        this.isErrorMessage = false;
        this.alertMessage = pluginAction + MessageConstants.PLUGIN_ACTION_MESSAGE + pluginName + MessageConstants.PLUGIN_ACTION_COMPLETED;
        this.getPluginDetails();
        this.isLoading = false;
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.getPluginDetails();
        this.isLoading = false;
      }
    );
  }

}
