@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
{
  "request": {
    "method": "GET",
    "urlPattern": "/inventory/out-of-stock-.*"
  },
  "response": {
    "status": 200,
    "headers": { "Content-Type": "application/json" },
    "body": "{\"available\": false}"
  }
}
