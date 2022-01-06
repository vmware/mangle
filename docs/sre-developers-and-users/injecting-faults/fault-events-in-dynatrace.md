# Fault Events in Dynatrace

If Dynatrace is your preferred metric provider and you are interested in publishing the fault events from Mangle to Dyntrace then while setting tags during fault injection, provide the entity ID of the endpoint / service being impacted by the fault as the tag value. Please refer to the screenshot below for an example of how to add these tags.

![Adding entity ids as Tags for Dynatrace integration](../../.gitbook/assets/Adding\_EntityId\_as\_tag.png)

Sending of fault injection event to Dynatrace will fail if entity ID specified in the tag is invalid. Fault injection events will appear in Dynatrace UI under the specified entity (endpoint /service being impacted) on providing the valid entity ID as value in the Tags section. Please refer the screenshot for the fault injection events.

![Fault Injection E](../../.gitbook/assets/FaultInjection\_Dynatrace\_Events.png)

{% hint style="info" %}
Entity ID of a service / entity can be retrieved from Dynatrace UI using the URL. Please navigate to entity page in Dynatrace and you will be able to find the entity ID in the URL.&#x20;


{% endhint %}

![Entity ID in Dynatrace entity page URL](../../.gitbook/assets/Url\_Entity\_Page.png)
