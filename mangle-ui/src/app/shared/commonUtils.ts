import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CommonUtils {

  constructor() { }

  public mergeTwoMaps(map1, map2): any {
    let mergedMap = {};
    for (const [key, value] of Object.entries(map1)) {
      mergedMap[key] = value;
    }
    for (const [key, value] of Object.entries(map2)) {
      mergedMap[key] = value;
    }
    return mergedMap;
  }
  public getTagsData(injectionTagsData, endpointTagsData): any {
    let tagsData = {};
    if (injectionTagsData == null && endpointTagsData == null) {
      return tagsData;
    } else if (injectionTagsData == null && endpointTagsData != null) {
      tagsData = endpointTagsData;
    } else if (injectionTagsData != null && endpointTagsData == null) {
      tagsData = injectionTagsData;
    } else if (injectionTagsData != null && endpointTagsData != null) {
      tagsData = this.mergeTwoMaps(injectionTagsData, endpointTagsData);
    }
    return tagsData;
  }

}
