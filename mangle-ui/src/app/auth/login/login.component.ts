import { Component, OnInit, NgZone } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { ClrLoadingState } from '@clr/angular';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService, private route: ActivatedRoute, private router: Router, private ngZone: NgZone) { }

  public errorFlag = false;
  public alertMessage: string;

  public authSources: any;
  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  ngOnInit() {
    this.getAuthSources();
  }

  public login(loginData) {
    this.errorFlag = false;
    this.submitBtnState = ClrLoadingState.LOADING;
    this.authService.login(loginData).subscribe(
      res => {
        if (res.status === 200) {
          this.ngZone.run(() => this.router.navigateByUrl('core')).then();
          //this.ngZone.run(() => this.router.navigateByUrl('')).then();
          //this.router.navigateByUrl('');
          this.submitBtnState = ClrLoadingState.DEFAULT;
        }
      }, err => {
        if (err.error) {
          this.alertMessage = 'Login unsuccessful. ' + err.error.message;
        } else {
          this.alertMessage = 'Invalid username or password.';
        }
        this.errorFlag = true;
        this.submitBtnState = ClrLoadingState.DEFAULT;
      });
  }

  public getAuthSources() {
    this.authService.getAuthSources().subscribe(
      res => {
        this.authSources = res._embedded.stringList;
      });
  }

}
