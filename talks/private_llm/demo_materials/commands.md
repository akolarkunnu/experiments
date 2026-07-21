# Demo Commands

## Enabling private ip and setting your llm server address as trusted end point
Replace your-private-llm-server.com and your-key with your own private llm server address and your openai key

```json
PUT _cluster/settings
{
  "persistent" : {
        "plugins.ml_commons.connector.private_ip_enabled": true,
        "plugins.ml_commons.trusted_connector_endpoints_regex": [
          "^https://your-private-llm-server.com/.*$"
        ]
  }
}
'''

## Registering model
```json
POST /_plugins/_ml/models/_register
{
  "name": "OpenSearchCon India 102",
  "function_name": "remote",
  "description": "test model",
  "connector": {                                                                      
      "name": "My openai connector: gpt-5 Test1",
      "description": "The connector to openai chat model",
      "version": 1,                                       
      "protocol": "http",
      "client_config": {
          "skip_ssl_verification": "false"
      },
      "parameters": {
          "model": "gpt-5"
      },                          
      "credential": {
          "openAI_key": "your-key"    
      },                                                                                         
      "actions": [
          {
              "action_type": "predict",
              "method": "POST",
              "url": "https://your-private-llm-server.com/v1/chat/completions",
              "headers": {                                                             
                  "Authorization": "Bearer ${credential.openAI_key}"
              },                                                    
              "request_body": "{ \"model\": \"${parameters.model}\", \"messages\": [{\"role\":\"developer\",\"content\":\"${parameters.system_prompt}\"},${parameters._chat_history:-}{\"role\":\"user\",\"content\":\"${parameters.user_prompt}\"}${parameters._interactions:-}], \"user\": \"abdulmun\", \"reasoning_effort\":\"minimal\"${parameters.tool_configs:-}}"
            }                                 
        ]                          
    } 
}
'''

## Testing model with predict API
```json
POST /_plugins/_ml/models/<model-id-created-above>/_predict
   {
     "parameters": {
       "user_prompt": "What is special about Mumbai?",
       "system_prompt": "You are a helpful assistant."
     }
   }
'''

## Registering agent
```json
POST /_plugins/_ml/agents/_register
{
  "name": "Agentic Search",
  "type": "conversational",
  "description": "Use this for Agentic Search",
  "llm": {
    "model_id": "<model-id-created-above>",
    "parameters": {
      "max_iteration": 15
    }
  },
  "memory": {
    "type": "conversation_index"
  },
  "parameters": {
    "_llm_interface": "openai/v1/chat/completions"
  },
  "tools": [
    {
      "type": "QueryPlanningTool",
      "name": "QueryPlanningTool",
      "description": "Generates an OpenSearch query DSL query from a natural language question"
    },
    {
      "type": "ListIndexTool",
      "name": "ListIndexTool",
      "description": "Use this tool to get OpenSearch index information: (health, status, index, uuid, primary count, replica count, docs.count, docs.deleted, store.size, primary.store.size)."
    },
    {
      "type": "IndexMappingTool",
      "name": "IndexMappingTool",
      "description": "Retrieves mapping and setting information for indexes in your cluster."
    }
  ],
  "app_type": "os_chat"
}
'''

## Testing agent
```json
POST /_plugins/_ml/agents/<agent-id-created-above>/_execute
{
  "parameters": {
      "question": "What is OpenSearch.",
      "system_prompt": "You are an OpenSearch expert.",
      "user_prompt": "${parameters.question}"
    }
}
'''

```json
POST /_plugins/_ml/agents/<agent-id-created-above>/_execute
{
  "parameters": {
      "question": "Can you explain about agentic search in OpenSearch, please give agent registration command also?",
      "system_prompt": "You are an OpenSearch expert.",
      "user_prompt": "${parameters.question}"
    }
}
'''
