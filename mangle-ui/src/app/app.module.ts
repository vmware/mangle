import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {InterceptorService} from "./interceptor.service";
import {ConfigGuardService} from "./shared/guards/config-guard.service";
import {AppComponent} from "./app.component";
import {ClarityModule} from "@clr/angular";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HashLocationStrategy, LocationStrategy} from "@angular/common";
import {PagesModule} from "./pages/pages.module";
import {AuthGuardService} from "./shared/guards/auth-guard.service";
import {PrivilegeGuardService} from "./privilege-guard.service";
import {ConfigComponent} from "./config/config.component";
import {PasswordStrengthBarModule} from "ng2-password-strength-bar";
import {RouterModule} from "@angular/router";
import {AppRoutingModule} from "./app-routing.module";
import {LoginGuardService} from "./shared/guards/login-guard.service";
import {UnavailableGuardService} from "./shared/guards/unavailable-guard.service";

@NgModule({
  declarations: [
    AppComponent, ConfigComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    BrowserAnimationsModule,
    PagesModule,
    PasswordStrengthBarModule,
    RouterModule,
    AppRoutingModule
  ],
  providers: [
    ConfigGuardService,
    AuthGuardService,
    LoginGuardService,
    PrivilegeGuardService,
    UnavailableGuardService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: InterceptorService,
      multi: true
    },
    {
      provide: LocationStrategy,
      useClass: HashLocationStrategy
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
