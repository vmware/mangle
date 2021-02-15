import {Component, OnInit} from "@angular/core";
import {SettingService} from "../setting.service";
import {MessageConstants} from "src/app/common/message.constants";
import {ClrLoadingState} from "@clr/angular";

@Component({
  selector: "app-identity",
  templateUrl: "./identity.component.html"
})
export class IdentityComponent implements OnInit {

  constructor(private settingService: SettingService) {
  }

  public identities: any;
  public identityFormData: any;

  public alertMessage: string;
  public addEdit: string;
  public isErrorMessage: boolean;

  public testAlertMessage: String;
  public testSuccessFlag: boolean;
  public testErrorFlag: boolean;
  public identitySource: boolean;

  public isLoading = true;
  public testADBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public disableSubmit = true;

  ngOnInit() {
    this.getIdentities();
  }

  public populateIdentityForm(identityData: any) {
    this.testErrorFlag = false;
    this.testSuccessFlag = false;
    this.disableSubmit = true;
    this.identityFormData = identityData;
    this.identityFormData.adUserPassword = "";
  }

  public getIdentities() {
    this.isLoading = true;
    this.settingService.getIdentities().subscribe(
      res => {
        if (res.content != null) {
          this.identities = res.content;
          this.isLoading = false;
        } else {
          this.identities = [];
          this.isLoading = false;
        }
      }, err => {
        this.identities = [];
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public addOrUpdateIdentitySource(identitySourceFormData) {
    if (identitySourceFormData.id == null) {
      this.addIdentitySource(identitySourceFormData);
    } else {
      this.updateIdentitySource(identitySourceFormData);
    }
  }

  public addIdentitySource(identitySourceFormData) {
    delete identitySourceFormData["id"];
    this.isLoading = true;
    this.settingService.addIdentitySource(identitySourceFormData).subscribe(
      res => {
        this.getIdentities();
        this.isErrorMessage = false;
        this.alertMessage = identitySourceFormData.adDomain + MessageConstants.IDENTITY_ADD;
        this.isLoading = false;
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.getIdentities();
        this.isLoading = false;
      });
  }

  public updateIdentitySource(identitySourceFormData) {
    this.isLoading = true;
    this.settingService.updateIdentitySource(identitySourceFormData).subscribe(
      res => {
        this.getIdentities();
        this.isErrorMessage = false;
        this.alertMessage = identitySourceFormData.adDomain + MessageConstants.IDENTITY_UPDATE;
        this.isLoading = false;
      }, err => {
        this.getIdentities();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public deleteIdentity(identity) {
    if (confirm(MessageConstants.DELETE_CONFIRM + identity.adDomain + MessageConstants.QUESTION_MARK)) {
      this.settingService.deleteIdentity(identity.adDomain).subscribe(
        res => {
          this.getIdentities();
          this.isErrorMessage = false;
          this.alertMessage = identity.adDomain + MessageConstants.IDENTITY_DELETE;
          this.isLoading = false;
        }, err => {
          this.getIdentities();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }

  public testADConnection(isFormValid, endpoint) {
    this.testAlertMessage = null;
    this.testSuccessFlag = false;
    this.testErrorFlag = false;
    if (isFormValid) {
      this.testADBtnState = ClrLoadingState.LOADING;
      this.settingService.testADConnection(endpoint).subscribe(
        res => {
          if (res.code) {
            this.testADBtnState = ClrLoadingState.ERROR;
            this.testErrorFlag = true;
            this.testSuccessFlag = false;
            this.testAlertMessage = res.description;
          } else {
            this.testADBtnState = ClrLoadingState.SUCCESS;
            this.disableSubmit = false;
            this.testErrorFlag = false;
            this.testSuccessFlag = true;
            this.testAlertMessage = MessageConstants.TEST_CONNECTION;
          }
        }, err => {
          this.testADBtnState = ClrLoadingState.ERROR;
          this.disableSubmit = true;
          this.testErrorFlag = true;
          this.testSuccessFlag = false;
          this.testAlertMessage = err.error.description;
        });
    }
  }

}
