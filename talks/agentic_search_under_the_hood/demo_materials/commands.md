# Demo Commands

## Enabling private ip and setting your llm server address as trusted end point
```json
PUT _cluster/settings
{
    "persistent": {
        "plugins.ml_commons.only_run_on_ml_node": "false",
        "plugins.ml_commons.memory_feature_enabled": "true",
        "plugins.ml_commons.connector.private_ip_enabled": true,
        "plugins.ml_commons.trusted_connector_endpoints_regex": [
          "https://bedrock-runtime.us-east-1.amazonaws.com/model/us.anthropic.claude-sonnet-5/converse"
        ]                                                    
    }  
}
```

## Creating connector
```json
POST /_plugins/_ml/connectors/_create
{
  "name": "Bedrock Claude Sonnet 5",
  "description": "The connector to BedRock service for claude model",
  "version": 1,
  "protocol": "aws_sigv4",
  "parameters": {
    "region": "us-east-1",
    "service_name": "bedrock",
    "model": "us.anthropic.claude-sonnet-5",
    "response_filter": "$.output.message.content[0].text"
  },
  "credential": {
    "access_key": "your_access_key",
    "secret_key": "your_secret_key",
    "session_token": "your_session_token"
  },
  "actions": [{
    "action_type": "predict",
    "method": "POST",
    "url": "https://bedrock-runtime.${parameters.region}.amazonaws.com/model/${parameters.model}/converse",
    "headers": { "content-type": "application/json" },
    "request_body": "{\"messages\": [${parameters._chat_history:-}{\"role\":\"user\",\"content\":[{\"text\":\"${parameters.prompt:-}\"}]}${parameters._interactions:-}]${parameters.tool_configs:-}}"
  }]
}
```


## Registering model
```json
POST /_plugins/_ml/models/_register
{
    "name": "Bedrock Claude Instant model",
    "function_name": "remote",
    "description": "Bedrock Claude instant-v1 model",
    "connector_id": "JF4fe6AB7O8eU65CMRvD"
}
```

## Deploying the model
```json
POST /_plugins/_ml/models/KV4fe6AB7O8eU65CVRtM/_deploy
```

## Testing model with predict API
```json
POST /_plugins/_ml/models/KV4fe6AB7O8eU65CVRtM/_predict
{
  "parameters": {
    "prompt": "\n\nHuman: how are you? \n\nAssistant:"
  }
}
```

## Registering conversational agent
```json
POST _plugins/_ml/agents/_register
{
  "name": "Conversational agent",
  "type": "conversational",
  "description": "claude",
  "llm": {
    "model_id": "KV4fe6AB7O8eU65CVRtM",
    "parameters": {
      "tool_descriptions": "",
      "tool_names": ""
    }
  },
  "tools": [
    {
      "type": "QueryPlanningTool",
      "parameters": {
        "model_id": "KV4fe6AB7O8eU65CVRtM"
      },
      "include_output_in_agent_response": false
    },
    {
      "type": "SearchIndexTool",
      "description": "Searches an index using a query written in query domain-specific language (DSL)",
      "include_output_in_agent_response": false
    }
  ],
  "parameters": {
    "_llm_interface": "bedrock/converse/claude"
  },
  "memory": {
    "type": "conversation_index"
  }
}
```

## Testing agent
```json
POST /_plugins/_ml/agents/Ll4fe6AB7O8eU65CyBtq/_execute
{
    "parameters": {
        "question": "What is OpenSearch?",
        "verbose": true                 
    }                                                       
}
```


## Registering flow agent
```json
POST /_plugins/_ml/agents/_register
{
  "name": "Chatbot agent",
  "type": "flow",
  "description": "Root chatbot agent",
  "tools": [
    {
      "type": "AgentTool",
      "name": "LLMResponseGenerator",
      "parameters": {
        "agent_id": "Ll4fe6AB7O8eU65CyBtq"
      },
      "include_output_in_agent_response": true
    },
    {
      "type": "MLModelTool",
      "name": "QuestionSuggestor",
      "parameters": {
        "model_id": "KV4fe6AB7O8eU65CVRtM",
        "prompt": "Human:  You are an AI that only speaks JSON. Do not write normal text. Output should follow example JSON format: \n\n {\"response\": [\"question1\", \"question2\"]}\n\n. \n\nHuman:You will be given a chat history between OpenSearch Assistant and a Human.\nUse the context provided to generate follow up questions the Human would ask to the Assistant.\nThe Assistant can answer general questions about logs, traces and metrics.\nAssistant can access a set of tools listed below to answer questions given by the Human:\nQuestion suggestions generator tool\nHere's the chat history between the human and the Assistant.\n${parameters.question}\nUse the following steps to generate follow up questions Human may ask after the response of the Assistant:\nStep 1. Use the chat history to understand what human is trying to search and explore.\nStep 2. Understand what capabilities the assistant has with the set of tools it has access to.\nStep 3. Use the above context and generate follow up questions.Step4:You are an AI that only speaks JSON. Do not write normal text. Output should follow example JSON format: \n\n {\"response\": [\"question1\", \"question2\"]} \n \n----------------\n\nAssistant:"
      },
      "include_output_in_agent_response": true
    }
  ],
  "memory": {
    "type": "conversation_index"
  }
}
```

## Configuring a  chatbot agent in OpenSearch Dashboards
```json
PUT /.plugins-ml-config/_doc/os_chat
{
 "type":"os_chat_root_agent",
 "configuration":{
   "agent_id": "NV4ge6AB7O8eU65CQBt0"
 }
}
```
