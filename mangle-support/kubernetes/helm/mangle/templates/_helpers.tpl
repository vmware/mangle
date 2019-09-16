{{- define "set-resource-limits" }}
resources:
 limits:
   cpu: {{ .Values.limits.cpu }}
   memory: {{ .Values.limits.memory }}
{{- end }}