@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
{
  "request": {
    "method": "POST",
    "urlPattern": "/tickets/[^/]+/close"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "{\"status\": \"closed\"}"
  }
}
