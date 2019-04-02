import { Component, OnInit, NgZone } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { SettingService } from '../setting/setting.service';
import { Router } from '@angular/router';
import { ConfigService } from './config.service';
import { ClrLoadingState } from '@clr/angular';

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {

  public errorFlag = false;
  public alertMessage: string;
  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public username: string = "admin@mangle.local";
  public oldPassword: string;
  public userFormData: any = { "username": this.username, "oldPassword": null, "password": null, "rePassword": null };

  constructor(private authService: AuthService, private settingService: SettingService, private configService: ConfigService, private router: Router, private ngZone: NgZone) { }

  ngOnInit() {
  }

  public updatePassword(userFormValue) {
    this.errorFlag = false;
    this.submitBtnState = ClrLoadingState.LOADING;
    if (userFormValue.password != userFormValue.rePassword) {
      this.alertMessage = "Retype password mismatch, Please try again.";
      this.errorFlag = true;
      this.submitBtnState = ClrLoadingState.DEFAULT;
    } else {
      this.oldPassword = userFormValue.oldPassword;
      delete userFormValue["oldPassword"];
      delete userFormValue["rePassword"];
      this.settingService.updateLocalUserConfig(userFormValue, this.oldPassword).subscribe(
        res => {
          if (res.status === 200) {
            this.setConfigStatus();
            this.submitBtnState = ClrLoadingState.DEFAULT;
            this.authService.logout();
            this.ngZone.run(() => this.router.navigateByUrl('login')).then();
          } else {
            this.alertMessage = 'Password was not updated.';
            this.errorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
          }
        }, err => {
          if (err.status == 401) {
            this.alertMessage = 'Password was not updated, Please enter the correct old password.';
            this.errorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
          }
        });
    }
  }

  public setConfigStatus() {
    return this.configService.setConfigStatus().subscribe();
  }

}
