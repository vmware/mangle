import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../auth.service";
import {ClrLoadingState} from "@clr/angular";
import {MessageConstants} from "src/app/common/message.constants";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.css"]
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService, private route: ActivatedRoute, private router: Router) {
  }

  public errorFlag = false;
  public alertMessage: string;

  public authSources: any;
  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public showPassword = false;

  public authData = {
    "authSource": null,
    "username": null,
    "password": null
  };

  ngOnInit() {
    this.getAuthSources();
  }

  public login(loginData) {
    this.errorFlag = false;
    this.submitBtnState = ClrLoadingState.LOADING;
    this.authService.login(loginData).subscribe(
      res => {
        if (res.status === 200) {
          this.router.navigateByUrl("core");
          this.submitBtnState = ClrLoadingState.DEFAULT;
        }
      }, err => {
        if (err.status === 403) {
          this.alertMessage = err.statusText;
        } else {
          if (err.error) {
            this.alertMessage = err.error.description;
          } else {
            this.alertMessage = MessageConstants.AUTH_FAILED;
          }
        }
        this.errorFlag = true;
        this.submitBtnState = ClrLoadingState.DEFAULT;
      });
  }

  public getAuthSources() {
    this.authService.getAuthSources().subscribe(
      res => {
        this.authSources = res.content;
      });
  }

  public negateShowPassword() {
    this.showPassword = !this.showPassword;
  }

}
