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
import { AWSComponent } from './core/endpoint/aws/aws.component';
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
import { PacketDelayComponent } from './core/fault/infra/network/packet-delay/packet-delay.component';
import { PacketDropComponent } from './core/fault/infra/network/packet-drop/packet-drop.component';
import { PacketDuplicateComponent } from './core/fault/infra/network/packet-duplicate/packet-duplicate.component';
import { PacketCorruptionComponent } from './core/fault/infra/network/packet-corruption/packet-corruption.component';
import { FilehandlerComponent } from './core/fault/application/filehandler/filehandler.component';
import { ThreadLeakComponent } from './core/fault/application/threadleak/threadleak.component';
import { FilehandlerInfraComponent } from './core/fault/infra/filehandler/filehandler.component';
import { JavaMethodLatencyComponent } from './core/fault/application/javamethodlatency/javamethodlatency.component';
import { SpringServiceLatencyComponent } from './core/fault/application/springservicelatency/springservicelatency.component';
import { SpringServiceExceptionComponent } from './core/fault/application/springserviceexception/springserviceexception.component';
import { KillJVMComponent } from './core/fault/application/killjvm/killjvm.component';
import { SimulateJavaExceptionComponent } from './core/fault/application/simulatejavaexception/simulatejavaexception.component';
import { EndpointCredentialsComponent } from './core/endpoint/credentials/endpoint-credentials.component';
import { EndpointCertificatesComponent } from './core/endpoint/certificates/endpoint-certificates.component';
import { LogLevelComponent } from './setting/loggers/log-level.component';
import { IntegrationComponent } from './setting/integration/integration.component';
import { UnavailableComponent } from './pages/unavailable.component';
import { AuthGuardService } from './auth-guard.service';
import { ClusterComponent } from './setting/cluster/cluster.component';
import { EventsComponent } from './shared/events/events.component';
import { PluginsComponent } from './setting/plugins/plugins.component';
import { CustomFaultComponent } from './core/fault/custom/custom-fault.component';
import { AwsEC2StateComponent } from './core/fault/infra/aws/ec2/state/aws-ec2-state.component';
import { AwsEC2NetworkComponent } from './core/fault/infra/aws/ec2/network/aws-ec2-network.component';
import { DiskSpaceComponent } from './core/fault/infra/diskspace/diskspace.component';
import { KernelPanicComponent } from './core/fault/infra/kernelpanic/kernelpanic.component';
import { K8SServiceUnavailableComponent } from './core/fault/infra/k8s/service-unavailable/k8s-service-unavailable.component';

const routes: Routes = [
	{ path: '', redirectTo: 'core/home', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent, canActivate: [ConfigGuardService] },
	{ path: 'config', component: ConfigComponent },
	{ path: 'unavailable', component: UnavailableComponent },
	{
		path: 'core',
		component: CoreComponent,
		children: [
			{ path: '', redirectTo: 'home', pathMatch: 'full' },
			{ path: 'home', component: HomeComponent, canActivate: [AuthGuardService] },
			{
				path: 'endpoint',
				component: EndpointComponent,
				children: [
					{ path: '', redirectTo: 'machine', pathMatch: 'full' },
					{ path: 'machine', component: MachineComponent },
					{ path: 'kubernetes', component: KubernetesComponent },
					{ path: 'docker', component: DockerComponent },
					{ path: 'vcenter', component: VcenterComponent },
					{ path: 'aws', component: AWSComponent },
					{ path: 'endpoint-credentials', component: EndpointCredentialsComponent },
					{ path: 'endpoint-certificates', component: EndpointCertificatesComponent },
					{ path: 'events', component: EventsComponent }
				]
			},
			{
				path: 'fault',
				component: FaultComponent,
				children: [
					{ path: 'cpu', component: CpuComponent },
					{ path: 'cpu-infra', component: CpuInfraComponent },
					{ path: 'memory', component: MemoryComponent },
					{ path: 'memory-infra', component: MemoryInfraComponent },
					{ path: 'diskio-infra', component: DiskioInfraComponent },
					{ path: 'killprocess', component: KillprocessComponent },
					{ path: 'docker-state-change', component: DockerStateChangeComponent },
					{ path: 'delete-k8s-resource', component: DeleteK8SResourceComponent },
					{ path: 'k8s-resource-not-ready', component: K8SResourceNotReadyComponent },
					{ path: 'k8s-service-unavailable', component: K8SServiceUnavailableComponent},
					{ path: 'vcenter-disk', component: VcenterDiskComponent },
					{ path: 'vcenter-nic', component: VcenterNicComponent },
					{ path: 'vcenter-state', component: VcenterStateComponent },
					{ path: 'aws-ec2-state', component: AwsEC2StateComponent },
					{ path: 'aws-ec2-network', component: AwsEC2NetworkComponent },
					{ path: 'custom-fault', component: CustomFaultComponent },
					{ path: 'packet-delay', component: PacketDelayComponent },
					{ path: 'packet-drop', component: PacketDropComponent },
					{ path: 'packet-duplicate', component: PacketDuplicateComponent },
					{ path: 'packet-corruption', component: PacketCorruptionComponent },
					{ path: 'filehandler-leak', component: FilehandlerComponent },
					{ path: 'filehandler-leak-infra', component: FilehandlerInfraComponent },
					{ path: 'thread-leak', component: ThreadLeakComponent },
					{ path: 'java-method-latency', component: JavaMethodLatencyComponent },
					{ path: 'spring-service-latency', component: SpringServiceLatencyComponent },
					{ path: 'spring-service-exception', component: SpringServiceExceptionComponent },
					{ path: 'kill-jvm', component: KillJVMComponent },
					{ path: 'simulate-java-exception', component: SimulateJavaExceptionComponent },
					{ path: 'disk-space', component: DiskSpaceComponent },
					{ path: 'kernel-panic', component: KernelPanicComponent }
				]
			},
			{
				path: 'requests',
				component: RequestsComponent,
				children: [
					{ path: '', redirectTo: 'processed', pathMatch: 'full' },
					{ path: 'processed', component: ProcessedComponent },
					{ path: 'scheduled', component: ScheduledComponent },
					{ path: 'events', component: EventsComponent }
				]
			},
			{
				path: 'setting',
				component: SettingComponent,
				children: [
					{ path: '', redirectTo: 'identity', pathMatch: 'full' },
					{ path: 'identity', component: IdentityComponent },
					{ path: 'roles', component: RolesComponent },
					{ path: 'users', component: UsersComponent },
					{ path: 'log-levels', component: LogLevelComponent },
					{ path: 'integration', component: IntegrationComponent },
					{ path: 'cluster', component: ClusterComponent },
					{ path: 'plugins', component: PluginsComponent },
					{ path: 'events', component: EventsComponent }
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
