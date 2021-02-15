import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RequestsComponent} from './requests.component';
import {ProcessedComponent} from './processed/processed.component';
import {ScheduledComponent} from './scheduled/scheduled.component';
import {EventsComponent} from '../../shared/events/events.component';

const routes: Routes = [
  {
    path: '', component: RequestsComponent, children: [
      {path: '', redirectTo: 'processed', pathMatch: 'prefix'},
      {path: 'processed', component: ProcessedComponent},
      {path: 'scheduled', component: ScheduledComponent},
      {path: 'events', component: EventsComponent}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class RequestsRoutingModule {
}
