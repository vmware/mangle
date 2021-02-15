import {Component, OnInit} from "@angular/core";
import {SettingService} from "../setting.service";

@Component({
  selector: "app-local",
  templateUrl: "./local.component.html"
})
export class LocalComponent implements OnInit {

  constructor(private settingService: SettingService) {
  }

  public alertMessage: string;
  public isErrorMessage: boolean;
  public localUserModal: boolean;

  public addEdit: string;

  public localUserFormData;
  public localUserList: any;

  public isLoading: boolean = true;

  ngOnInit() {
    this.getLocalUserList();
  }

  public populateLocalUserForm(localUserData: any) {
    this.localUserFormData = localUserData;
  }

  public getLocalUserList() {
    this.isLoading = true;
    this.settingService.getLocalUserList().subscribe(
      res => {
        this.localUserList = res.content;
        this.isLoading = false;
      });
  }

  public addOrUpdateLocalUser(localUserFormValue) {
    if (localUserFormValue.id == null) {
      localUserFormValue.username = localUserFormValue.username + "@mangle.local";
      this.addLocalUser(localUserFormValue);
    } else {
      this.updateLocalUser(localUserFormValue);
    }
  }

  public addLocalUser(localUserFormValue) {
    delete localUserFormValue["id"];
    this.isLoading = true;
    this.settingService.addLocalUser(localUserFormValue).subscribe(
      res => {
        this.getLocalUserList();
        this.isErrorMessage = false;
        this.alertMessage = "User added successfully!";
        this.isLoading = false;
      }, err => {
        this.getLocalUserList();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public updateLocalUser(localUserFormValue) {
    this.isLoading = true;
    this.settingService.updateLocalUser(localUserFormValue).subscribe(
      res => {
        this.getLocalUserList();
        this.isErrorMessage = false;
        this.alertMessage = "User updated successfully!";
        this.isLoading = false;
      }, err => {
        this.getLocalUserList();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public deleteLocalUser(localUser) {
    if (confirm("Do you want to delete: " + localUser.username + " user ?")) {
      this.settingService.deleteLocalUser(localUser.username).subscribe(
        res => {
          this.getLocalUserList();
          this.isErrorMessage = false;
          this.alertMessage = localUser.username + " user deleted successfully!";
          this.isLoading = false;
        }, err => {
          this.getLocalUserList();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }

}
