@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
{
  "request": {
    "method": "POST",
    "url": "/payments"
  },
  "response": {
    "status": 200,
    "headers": { "Content-Type": "application/json" },
    "body": "{\"status\": \"approved\"}"
  }
}
