import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {ClarityModule} from '@clr/angular';
import {AuthService} from './auth.service';
import {LoginComponent} from './login/login.component';
import {AuthRoutingModule} from './auth-routing.module';

@NgModule({
  declarations: [LoginComponent],
  imports: [
    FormsModule,
    HttpClientModule,
    ClarityModule,
    AuthRoutingModule,
    CommonModule
  ],
  providers: [
    AuthService
  ]
})
export class AuthModule {
}
