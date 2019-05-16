# Requests and Reports

Request and report page provides a brief insight view of the task executed during fault execution, schedules and remediation action involved. Mangle creates tasks and execute it with different stages(NOT STARTED, IN_PROGRESS, COMPLETED, FAILED).

## Processed Requests

It provides brief description of the tasks executed by mangle.

#### Important fields of mangle tasks

1. Task Name: Name of the task created for any fault execution, schedule and remediation actions.
2. Endpoint Name: Name of the targeted endpoint during fault execution.
3. Task Type: Type of the task executed, eg: INJECTION, REMEDIATION
4. Task Description: You can get more details about fault, fault amount, endpoint targeted, targeted component on endpoint etc.
5. Triggers: Status, Start Time, End Time are the part of triggers in the task. Triggers is the list of execution for a task. The Processed Request data grid Status, Start Time and End Time are the values of latest trigger in that task.
6. Task Failed reason: In the status field you can find one exclamatory error icon. Take your mouse over it to get the details on task failure reason.

#### Extended view of mangle tasks
Click on ![](../.gitbook/assets/supportedactionsbutton.png) > Report to get more about task details.

#### Performing supported action on mangle tasks
Click on ![](../.gitbook/assets/supportedactionsbutton.png) to list all the supported action. Click on it to perform your action on the mangle task.

#### Refreshing the mangle task datagrid
Click on refresh icon to sync mangle task datagrid with current status.

## Scheduled Jobs

Scheduled Jobs datagrid list down all the schedules available on mangle.

#### Important fields of schedules:
1. ID: Contains id of the schedule.
2. Job Type: Type of the schedule , eg: CRON, SIMPLE
3. Scheduled At: Timing of the schedule, It shows the epoch time in millisecond if job type is SIMPLE and cron if job type is CRON.
4. Status: Status of the schedule . It may have values like: INITIALIZING, CANCELLED, SCHEDULED, FINISHED, PAUSED, SCHEDULE_FAILED

#### Triggers of each schedule:
Click on the ID link of each schedule to view all the triggers of that schedule.

#### Performing supported action on mangle schedules:
Click on ![](../.gitbook/assets/supportedactionsbutton.png) to list all the supported action. Click on it to perform your action on the mangle schedule.

#### Refreshing the schedule datagrid:
Click on refresh icon to sync mangle schedule datagrid with current status.

## LOGS:
Click on the Logs link to get the most current updated logs of mangle.
