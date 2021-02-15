import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ResiliencyscoreComponent } from './resiliencyscore.component';
import { QueryComponent } from './query/query.component';
import { CalculateResiliencyScoreComponent } from './calculate-rscore/calculate-rscore.component';
import { ServiceComponent } from './service/service.component';

const routes: Routes = [
  {
    path: '', component: ResiliencyscoreComponent, children: [
      { path: 'calculaterscore', component: CalculateResiliencyScoreComponent },
      { path: 'service', component: ServiceComponent},
      { path: 'query', component: QueryComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class ResiliencyscoreRoutingModule {
}
