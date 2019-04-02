import { NgModule } from '@angular/core';
import { Routes, RouterModule, PreloadAllModules } from '@angular/router';

import { ConfigGuardService } from './config-guard.service';
import { ConfigComponent } from './config/config.component';
import { CoreComponent } from './core/core.component';
import { HomeComponent } from './home/home.component';
import { SettingComponent } from './setting/setting.component';
import { LoginComponent } from './auth/login/login.component';
import { PasswordComponent } from './auth/password/password.component';
import { EndpointComponent } from './core/endpoint/endpoint.component';
import { UsersComponent } from './setting/users/users.component';
import { RolesComponent } from './setting/roles/roles.component';
import { IdentityComponent } from './setting/identity/identity.component';
import { MachineComponent } from './core/endpoint/machine/machine.component';
import { KubernetesComponent } from './core/endpoint/kubernetes/kubernetes.component';
import { DockerComponent } from './core/endpoint/docker/docker.component';
import { VcenterComponent } from './core/endpoint/vcenter/vcenter.component';
import { LocalComponent } from './setting/local/local.component';
import { FaultComponent } from './core/fault/fault.component';
import { RequestsComponent } from './core/requests/requests.component';
import { ProcessedComponent } from './core/requests/processed/processed.component';
import { ScheduledComponent } from './core/requests/scheduled/scheduled.component';
import { CpuComponent } from './core/fault/application/cpu/cpu.component';
import { CpuInfraComponent } from './core/fault/infra/cpu/cpu.component';
import { MemoryComponent } from './core/fault/application/memory/memory.component';
import { MemoryInfraComponent } from './core/fault/infra/memory/memory.component';
import { DiskioInfraComponent } from './core/fault/infra/diskio/diskio.component';
import { KillprocessComponent } from './core/fault/infra/killprocess/killprocess.component';
import { DockerStateChangeComponent } from './core/fault/infra/docker/state/docker-state-change.component';
import { DeleteK8SResourceComponent } from './core/fault/infra/k8s/delete/delete-k8s-resource.component';
import { K8SResourceNotReadyComponent } from './core/fault/infra/k8s/ready/k8s-resource-not-ready.component';
import { VcenterDiskComponent } from './core/fault/infra/vcenter/disk/vcenter-disk.component';
import { VcenterNicComponent } from './core/fault/infra/vcenter/nic/vcenter-nic.component';
import { VcenterStateComponent } from './core/fault/infra/vcenter/state/vcenter-state.component';
import { EndpointCredentialsComponent } from './core/endpoint/credentials/endpoint-credentials.component';
import { LogLevelComponent } from './setting/loggers/log-level.component';
import { IntegrationComponent } from './setting/integration/integration.component';

const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent, canActivate: [ ConfigGuardService ] },
	{ path: 'config', component: ConfigComponent },
	{
    path: 'core',
		component: CoreComponent,
		children: [
			{ path: '', redirectTo: 'home', pathMatch: 'full' },
			{ path: 'home', component: HomeComponent },
			{ 
				path: 'endpoint',
				component: EndpointComponent,
				children: [
					{ path: '', redirectTo: 'machine', pathMatch: 'full' },
					{ path: 'machine', component: MachineComponent },
					{ path: 'kubernetes', component: KubernetesComponent },
					{ path: 'docker', component: DockerComponent },
					{ path: 'vcenter', component: VcenterComponent },
					{ path: 'endpoint-credentials', component: EndpointCredentialsComponent }
				]
			},
			{ 
				path: 'fault',
				component: FaultComponent,
				children: [
					{ path: '', redirectTo: 'cpu', pathMatch: 'full' },
					{ path: 'cpu', component: CpuComponent },
					{ path: 'cpu-infra', component: CpuInfraComponent },
					{ path: 'memory', component: MemoryComponent },
					{ path: 'memory-infra', component: MemoryInfraComponent },
					{ path: 'diskio-infra', component: DiskioInfraComponent },
					{ path: 'killprocess', component: KillprocessComponent },
					{ path: 'docker-state-change', component: DockerStateChangeComponent },
					{ path: 'delete-k8s-resource', component: DeleteK8SResourceComponent },
					{ path: 'k8s-resource-not-ready', component: K8SResourceNotReadyComponent },
					{ path: 'vcenter-disk', component: VcenterDiskComponent },
					{ path: 'vcenter-nic', component: VcenterNicComponent },
					{ path: 'vcenter-state', component: VcenterStateComponent }
				]
			},
			{ 
				path: 'requests',
				component: RequestsComponent,
				children: [
					{ path: '', redirectTo: 'processed', pathMatch: 'full' },
					{ path: 'processed', component: ProcessedComponent },
					{ path: 'scheduled', component: ScheduledComponent }
				]
			},
			{
				path: 'setting',
				component: SettingComponent,
				children: [
					{ path: '', redirectTo: 'local', pathMatch: 'full' },
					{ path: 'identity', component: IdentityComponent },
					{ path: 'roles', component: RolesComponent },
					{ path: 'users', component: UsersComponent },
					{ path: 'local', component: LocalComponent },
					{ path: 'log-levels', component: LogLevelComponent },
					{ path: 'integration', component: IntegrationComponent}
				]
			},
			{ path: 'password', component: PasswordComponent }
		]
    }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule { }
