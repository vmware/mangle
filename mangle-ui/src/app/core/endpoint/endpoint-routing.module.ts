import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MachineComponent } from './machine/machine.component';
import { KubernetesComponent } from './kubernetes/kubernetes.component';
import { DockerComponent } from './docker/docker.component';
import { VcenterComponent } from './vcenter/vcenter.component';
import { AWSComponent } from './aws/aws.component';
import { AzureComponent } from './azure/azure.component';
import { EndpointCredentialsComponent } from './credentials/endpoint-credentials.component';
import { EndpointCertificatesComponent } from './certificates/endpoint-certificates.component';
import { EndpointGroupsComponent } from './groups/endpoint-groups.component';
import { VcenterAdapterComponent } from './vcenter-adapter/vcenter-adapter.component';
import { EndpointComponent } from './endpoint.component';
import { RedisProxyComponent } from './redis-proxy/redis-proxy.component';
import { DatabaseComponent } from './database/database.component';
import { EventsComponent } from '../../shared/events/events.component';

const routes: Routes = [
  {
    path: '', component: EndpointComponent, children: [
      { path: 'machine', component: MachineComponent },
      { path: 'kubernetes', component: KubernetesComponent },
      { path: 'docker', component: DockerComponent },
      { path: 'vcenter', component: VcenterComponent },
      { path: 'aws', component: AWSComponent },
      { path: 'azure', component: AzureComponent },
      { path: 'endpoint-credentials', component: EndpointCredentialsComponent },
      { path: 'endpoint-certificates', component: EndpointCertificatesComponent },
      { path: 'vcenter-adapter', component: VcenterAdapterComponent },
      { path: 'redis-proxy', component: RedisProxyComponent },
      { path: 'database', component: DatabaseComponent },
      { path: 'events', component: EventsComponent },
      { path: 'endpoint-groups', component: EndpointGroupsComponent },
      { path: '', redirectTo: 'machine', pathMatch: 'full' },
      { path: '**', redirectTo: 'machine' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class EndpointRoutingModule {
}
