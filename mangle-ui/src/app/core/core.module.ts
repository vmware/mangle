import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CoreComponent} from './core.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {ClarityModule} from '@clr/angular';
import {SharedModule} from '../shared/shared.module';
import {CoreRoutingModule} from './core-routing.module';
import {PasswordComponent} from './password/password.component';
import {PasswordStrengthBarModule} from 'ng2-password-strength-bar';
import {HomeComponent} from './home/home.component';

@NgModule({
  declarations: [CoreComponent, PasswordComponent, HomeComponent],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    SharedModule,
    CoreRoutingModule,
    PasswordStrengthBarModule
  ]
})
export class CoreModule {
}
