import {Component, OnInit} from "@angular/core";
import {ClrLoadingState} from "@clr/angular";
import {NotifierService} from "./notifier.service";
import {MessageConstants} from "src/app/common/message.constants";

@Component({
  selector: "app-notifier",
  templateUrl: "./notifier.component.html"
})
export class NotifierComponent implements OnInit {

  public channelModal: boolean;
  public notifications: any;
  public isLoading = true;
  public slackDatagrid = false;
  public slackForm = false;
  public disableSubmit = true;
  public testSlackBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public alertMessage: string;
  public isErrorMessage: boolean;
  public selectedSlacks: any = [];
  public channelsData: any = [];
  public addEdit: string;
  public enableSlack: boolean = true;
  public hideEnableDisableButtons: boolean = false;
  public notificationData: any;
  public notifierTypes: any = ["SLACK"];
  public slackType: boolean = true;

  public testAlertMessage: String;
  public testSuccessFlag: boolean;
  public testErrorFlag: boolean;

  constructor(private slackService: NotifierService) {

  }

  ngOnInit() {
    this.getNotificationInfo();
  }

  public getNotificationInfo() {
    this.testAlertMessage = null;
    this.testErrorFlag = false;
    this.testSuccessFlag = false;
    this.isLoading = true;
    this.slackService.getNotificationInfo().subscribe(
      res => {
        if (res.code) {
          this.notifications = [];
          this.isLoading = false;
        } else {
          this.notifications = res;
          this.isLoading = false;
        }
      }, err => {
        this.notifications = [];
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public populateSlackForm(notifi: any) {
    if (notifi.slackInfo != null && notifi.slackInfo.channels != null) {
      this.channelsData = notifi.slackInfo.channels;
    } else {
      this.channelsData = [];
    }
    this.disableSubmit = true;
    this.notificationData = notifi;
  }

  public addOrUpdateNotification(notifi: any, action: string) {
    if (this.channelsData !== []) {
      notifi.slackInfo.channels = this.channelsData;
      this.channelsData = [];
    }
    this.testSlackBtnState = ClrLoadingState.DEFAULT;
    if (action === "Add") {
      this.addNotification(notifi);
    } else {
      this.updateNotification(notifi);
    }
  }

  public addNotification(slackinfo: any) {
    this.isLoading = true;
    this.slackService.addNotification(slackinfo).subscribe(
      res => {
        this.getNotificationInfo();
        this.isErrorMessage = false;
        this.alertMessage = slackinfo.name + MessageConstants.NOTIFICATION_ADD;
        this.isLoading = false;
      }, err => {
        this.getNotificationInfo();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public updateNotification(slackinfo: any) {
    this.isLoading = true;
    this.slackService.updateNotification(slackinfo).subscribe(
      res => {
        this.getNotificationInfo();
        this.isErrorMessage = false;
        this.alertMessage = slackinfo.name + MessageConstants.NOTIFICATION_UPDATE;
        this.isLoading = false;
      }, err => {
        this.getNotificationInfo();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public updateChannel(chVal: any) {
    this.channelsData.push(chVal.name);
  }

  public removeChannel(chaname: string) {
    const index: number = this.channelsData.indexOf(chaname, 0);
    if (index > -1) {
      this.channelsData.splice(index, 1);
    }
  }

  public testNotificationConnection(isFormValid: any, notifi: any) {
    this.testAlertMessage = null;
    this.testErrorFlag = false;
    this.testSuccessFlag = false;
    if (this.channelsData !== []) {
      notifi.slackInfo.channels = [];
      notifi.slackInfo.channels = this.channelsData;
    }
    if (isFormValid) {
      this.testSlackBtnState = ClrLoadingState.LOADING;
      this.slackService.testNotificationConnection(notifi).subscribe(
        res => {
          if (res.code) {
            this.testSlackBtnState = ClrLoadingState.ERROR;
            this.testErrorFlag = true;
            this.testAlertMessage = res.description;
          } else {
            this.testSlackBtnState = ClrLoadingState.SUCCESS;
            this.disableSubmit = false;
            this.testSuccessFlag = true;
            this.testAlertMessage = MessageConstants.TEST_CONNECTION;
          }
        }, err => {
          this.testSlackBtnState = ClrLoadingState.ERROR;
          this.testErrorFlag = true;
          this.testAlertMessage = err.error.description;
          if (this.testAlertMessage === undefined) {
            this.testAlertMessage = err.error.error;
          }
        });
    }
  }

  public deleteSlack(slackinfos: any) {
    const selectedSlackNames = [];
    if (slackinfos !== undefined) {
      for (let i = 0; i < slackinfos.length; i++) {
        selectedSlackNames.push(slackinfos[i].name);
      }
    } else {
      this.alertMessage = MessageConstants.NO_NOTIFICATION_SELECTED;
    }
    if (confirm(MessageConstants.DELETE_CONFIRM + selectedSlackNames + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.slackService.deleteNotification(selectedSlackNames).subscribe(
        res => {
          this.getNotificationInfo();
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.NOTIFICATION_DELETE;
          this.isLoading = false;
        }, err => {
          this.getNotificationInfo();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    }
  }

  public updateDisableButton() {
    this.slackType = true;
    if (this.selectedSlacks !== undefined) {
      let flag = false;
      let count = 0;
      for (let i = 0; i < this.selectedSlacks.length; i++) {
        if ((this.selectedSlacks[i].enable == null || this.selectedSlacks[i].enable)) {
          flag = true;
          count++;
        } else {
          flag = false;
        }
        if (this.selectedSlacks[i].notifierType !== "SLACK") {
          this.slackType = false;
        }
      }
      this.enableSlack = flag;
      if (count === 0 || count === this.selectedSlacks.length) {
        this.hideEnableDisableButtons = false;
      } else {
        this.hideEnableDisableButtons = true;
      }
    }
  }

  public enableSlacks(slackinfos: any, enableFlag: boolean) {
    const selectedSlackNames = [];
    if (slackinfos !== undefined) {
      for (let i = 0; i < slackinfos.length; i++) {
        selectedSlackNames.push(slackinfos[i].name);
      }
    } else {
      this.alertMessage = MessageConstants.NO_NOTIFICATION_SELECTED;
    }
    let confimationMessage = MessageConstants.DISABLE_CONFIRM;
    if (enableFlag) {
      confimationMessage = MessageConstants.ENABLE_CONFIRM;
    }
    if (confirm(confimationMessage + selectedSlackNames + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.slackService.enableNotification(selectedSlackNames, enableFlag).subscribe(
        res => {
          this.getNotificationInfo();
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.NOTIFICATION_DISABLE;
          if (enableFlag) {
            this.alertMessage = MessageConstants.NOTIFICATION_ENABLE;
          }
          this.isLoading = false;
        }, err => {
          this.getNotificationInfo();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    }
  }
}
