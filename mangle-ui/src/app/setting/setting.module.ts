import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingComponent } from './setting.component';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AppRoutingModule } from '../app-routing.module';
import { UsersComponent } from './users/users.component';
import { RolesComponent } from './roles/roles.component';
import { IdentityComponent } from './identity/identity.component';
import { LogLevelComponent } from './loggers/log-level.component';
import { IntegrationComponent } from './integration/integration.component';
import { ClusterComponent } from './cluster/cluster.component';
import { PluginsComponent } from './plugins/plugins.component';
import { LocalComponent } from './local/local.component';
import { SharedModule } from '../shared/shared.module';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';

@NgModule({
  declarations: [SettingComponent, UsersComponent, RolesComponent, IdentityComponent, LogLevelComponent, IntegrationComponent, ClusterComponent, PluginsComponent, LocalComponent],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    AppRoutingModule,
    CommonModule,
    SharedModule,
    PasswordStrengthBarModule
  ]
})
export class SettingModule { }
