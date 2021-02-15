import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SettingComponent} from './setting.component';
import {IdentityComponent} from './identity/identity.component';
import {RolesComponent} from './roles/roles.component';
import {UsersComponent} from './users/users.component';
import {LoggingComponent} from './loggers/logging.component';
import {IntegrationComponent} from './integration/integration.component';
import {NotifierComponent} from './integration/notifier/notifier.component';
import {ClusterComponent} from './cluster/cluster.component';
import {PluginsComponent} from './plugins/plugins.component';
import {EventsComponent} from '../../shared/events/events.component';
import {ResiliencyScoreMetricConfigComponent} from './resiliencyscore-metric-config/resiliencyscore-metric-config.component';


const routes: Routes = [
  {
    path: '',
    component: SettingComponent,
    children: [
      {path: '', redirectTo: 'identity', pathMatch: 'full'},
      {path: 'identity', component: IdentityComponent},
      {path: 'roles', component: RolesComponent},
      {path: 'users', component: UsersComponent},
      {path: 'logging', component: LoggingComponent},
      {path: 'integration', component: IntegrationComponent},
      {path: 'notifier', component: NotifierComponent},
      {path: 'cluster', component: ClusterComponent},
      {path: 'plugins', component: PluginsComponent},
      {path: 'events', component: EventsComponent},
      {path: 'resiliencyscore-metric-config', component: ResiliencyScoreMetricConfigComponent},
      {path: '**', redirectTo: 'identity'}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class SettingRoutingModule {
}
