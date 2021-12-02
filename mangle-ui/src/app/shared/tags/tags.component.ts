import { Component, Input, Output, EventEmitter, SystemJsNgModuleLoader, OnInit } from '@angular/core';
import { SharedService } from 'src/app/shared/shared.service';
import { CommonConstants } from 'src/app/common/common.constants';

@Component({
  selector: 'app-tags',
  templateUrl: './tags.component.html'
})
export class TagsComponent implements OnInit {

  constructor(private sharedService: SharedService) { }
  ngOnInit() {
    this.getActiveMetricProvider();
  }

  @Input() tagsModal: boolean;
  @Input() tagsData: any;
  @Output() updateTagsModal = new EventEmitter<boolean>();
  public isDynatraceMetricProviderActive: boolean;

  public updateTags(tagsVal) {
    if (this.isDynatraceMetricProviderActive == true) {
      tagsVal.tagKey = tagsVal.tagKey + CommonConstants.HYPHEN + CommonConstants.DYNATRACE_ARTIFACT;
    }
    this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
  }

  public getActiveMetricProvider() {
    this.isDynatraceMetricProviderActive = false;
    this.sharedService.getActiveMetricProvider().subscribe(
      res => {
        res = res.content;
        if (res.length > 0) {
          if (res[0].metricProviderType == CommonConstants.DYNATRACE) {
            this.isDynatraceMetricProviderActive = true;
          }
        }
      }, err => {
        console.error(err.error.description);
      });
  }


  public closeTagsModal() {
    this.tagsModal = false;
    this.updateTagsModal.emit(false);
  }

}

