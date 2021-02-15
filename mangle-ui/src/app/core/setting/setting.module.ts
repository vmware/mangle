import {NgModule} from '@angular/core';
import {SettingComponent} from './setting.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {ClarityModule} from '@clr/angular';
import {UsersComponent} from './users/users.component';
import {RolesComponent} from './roles/roles.component';
import {IdentityComponent} from './identity/identity.component';
import {LoggingComponent} from './loggers/logging.component';
import {IntegrationComponent} from './integration/integration.component';
import {ClusterComponent} from './cluster/cluster.component';
import {PluginsComponent} from './plugins/plugins.component';
import {LocalComponent} from './local/local.component';
import {SharedModule} from '../../shared/shared.module';
import {PasswordStrengthBarModule} from 'ng2-password-strength-bar';
import {NotifierComponent} from './integration/notifier/notifier.component';
import {ResiliencyScoreMetricConfigComponent} from './resiliencyscore-metric-config/resiliencyscore-metric-config.component';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {SettingRoutingModule} from './setting-routing.module';

@NgModule({
  declarations: [SettingComponent, UsersComponent, RolesComponent, IdentityComponent, LoggingComponent, IntegrationComponent,
    ClusterComponent, PluginsComponent, LocalComponent, NotifierComponent, ResiliencyScoreMetricConfigComponent],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    SharedModule,
    PasswordStrengthBarModule,
    CommonModule,
    RouterModule,
    SettingRoutingModule
  ]
})
export class SettingModule {
}
