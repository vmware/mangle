import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../app-routing.module';
import { LoginComponent } from './login/login.component';
import { PasswordComponent } from './password/password.component';
import { AuthService } from './auth.service';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';

@NgModule({
  declarations: [LoginComponent, PasswordComponent],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    HttpClientModule,
    ClarityModule,
    AppRoutingModule,
    CommonModule,
    PasswordStrengthBarModule
  ],
  providers: [
    AuthService
  ]
})
export class AuthModule { }
