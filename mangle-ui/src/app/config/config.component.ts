import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfigService } from './config.service';
import { ClrLoadingState } from '@clr/angular';
import { MessageConstants } from '../common/message.constants';

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html'
})
export class ConfigComponent implements OnInit {

  public errorFlag = false;
  public alertMessage: string;
  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public name: string = "admin@mangle.local";
  public oldPassword: string;
  public userFormData: any = { "name": this.name, "oldPassword": null, "password": null, "rePassword": null };
  public barLabel: string = '';
  public barColors = ['#DD2C00', '#FF6D00', '#FFD600', '#AEEA00', '#00C853'];
  public baseColor = '#DDD';
  public strengthLabels = ['(Useless)', '(Weak)', '(Normal)', '(Strong)', '(Great!)'];

  constructor(private configService: ConfigService, private router: Router) { }

  ngOnInit() {
  }

  public updatePassword(userFormValue) {
    this.errorFlag = false;
    this.submitBtnState = ClrLoadingState.LOADING;
    if (userFormValue.password != userFormValue.rePassword) {
      this.alertMessage = MessageConstants.PASSWORD_MISMATCH;
      this.errorFlag = true;
      this.submitBtnState = ClrLoadingState.DEFAULT;
    } else {
      this.oldPassword = userFormValue.oldPassword;
      delete userFormValue["oldPassword"];
      delete userFormValue["rePassword"];
      userFormValue.roleNames = ["ROLE_ADMIN"];
      this.configService.updateLocalUserConfig(userFormValue, this.oldPassword).subscribe(
        res => {
          if (res.status === 200) {
            this.router.navigateByUrl('login');
            this.submitBtnState = ClrLoadingState.DEFAULT;
          } else {
            this.alertMessage = MessageConstants.PASSWORD_UPDATE_FAILED;
            this.errorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
          }
        }, err => {
          if (err.status == 401) {
            this.alertMessage = MessageConstants.OLD_PASSWORD_WRONG;
            this.errorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
          }
        });
    }
  }

}
