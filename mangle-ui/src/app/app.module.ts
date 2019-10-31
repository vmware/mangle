import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { InterceptorService } from "./interceptor.service";
import { ConfigGuardService } from './config-guard.service';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ClarityModule } from '@clr/angular';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ConfigModule } from './config/config.module';
import { AuthModule } from './auth/auth.module';
import { CoreModule } from './core/core.module';
import { HomeModule } from './home/home.module';
import { SettingModule } from './setting/setting.module';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { PagesModule } from './pages/pages.module';
import { AuthGuardService } from './auth-guard.service';
import { SharedModule } from './shared/shared.module';
import { PrivilegeGuardService } from './privilege-guard.service';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppRoutingModule,
    ClarityModule,
    BrowserAnimationsModule,
    ConfigModule,
    AuthModule,
    CoreModule,
    HomeModule,
    PagesModule,
    SettingModule,
    SharedModule
  ],
  providers: [
    ConfigGuardService,
    AuthGuardService,
    PrivilegeGuardService,
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
export class AppModule { }
