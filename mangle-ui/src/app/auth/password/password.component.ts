import { Component, OnInit } from '@angular/core';
import { SettingService } from 'src/app/setting/setting.service';
import { Router } from '@angular/router';
import { MessageConstants } from 'src/app/common/message.constants';
import { ClrLoadingState } from '@clr/angular';

@Component({
    selector: 'app-password',
    templateUrl: './password.component.html'
})
export class PasswordComponent implements OnInit {

    constructor(private settingService: SettingService, private router: Router) { }

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public loginModal: boolean = false;

    public barLabel: string = '';
    public barColors = ['#DD2C00', '#FF6D00', '#FFD600', '#AEEA00', '#00C853'];
    public baseColor = '#DDD';
    public strengthLabels = ['(Useless)', '(Weak)', '(Normal)', '(Strong)', '(Great!)'];

    public passwordFormData: any = {
        "currentPassword": null,
        "newPassword": null,
        "rNewpassword": null
    };

    ngOnInit() { }

    public updatePassword(passwordFormValue) {
        this.submitBtnState = ClrLoadingState.LOADING;
        this.errorFlag = false;
        this.successFlag = false;
        if (passwordFormValue.newPassword != passwordFormValue.rNewpassword) {
            this.alertMessage = MessageConstants.PASSWORD_MISMATCH;
            this.errorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
        } else {
            delete passwordFormValue["rNewpassword"];
            this.settingService.updatePassword(passwordFormValue).subscribe(
                res => {
                    this.alertMessage = MessageConstants.PASSWORD_UPDATED;
                    this.successFlag = true;
                    this.loginModal = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                }, err => {
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                });
        }
    }

    public loginAgain() {
        this.router.navigateByUrl('login');
    }

}
