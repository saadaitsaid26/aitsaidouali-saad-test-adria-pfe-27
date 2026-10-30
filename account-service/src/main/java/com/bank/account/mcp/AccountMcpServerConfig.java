// Configuration du serveur MCP : publie les @Tool d'AccountMcpTools au protocole MCP (P5)
package com.bank.account.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enregistre les outils de AccountMcpTools sur le serveur MCP Spring AI
 * (starter spring-ai-starter-mcp-server-webmvc, transport SSE/WebMVC).
 * Le transport et les endpoints sont fournis par l'auto-configuration :
 * handshake JSON-RPC sur GET /sse, messages sur POST /mcp/message.
 */
@Configuration(proxyBeanMethods = false)
public class AccountMcpServerConfig {

    @Bean
    public ToolCallbackProvider accountToolCallbackProvider(AccountMcpTools accountMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(accountMcpTools)
                .build();
    }
}
