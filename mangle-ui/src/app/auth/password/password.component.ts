import { Component, OnInit, NgZone } from '@angular/core';
import { CoreService } from 'src/app/core/core.service';
import { SettingService } from 'src/app/setting/setting.service';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-password',
    templateUrl: './password.component.html',
    styleUrls: ['./password.component.css']
})
export class PasswordComponent implements OnInit {

    constructor(private coreService: CoreService, private settingService: SettingService, private authService: AuthService, private router: Router, private ngZone: NgZone) { }

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;
    public loginModal: boolean = false;

    public passwordFormData: any = { "username": null, "password": null };

    ngOnInit() {
        this.getMyDetails();
    }

    public getMyDetails() {
        this.coreService.getMyDetails().subscribe(
            res => {
                this.passwordFormData.username = res.name;
            });
    }

    public updatePassword(passwordFormValue) {
        this.errorFlag = false;
        this.successFlag = false;
        this.settingService.updateLocalUser(passwordFormValue).subscribe(
            res => {
                this.alertMessage = 'Password updated successfully!';
                this.successFlag = true;
                this.authService.logout();
                this.loginModal = true;
            }, err => {
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            });
    }

    public loginAgain() {
        this.ngZone.run(() => this.router.navigateByUrl('login')).then();
        //this.router.navigateByUrl('login');
    }

}
