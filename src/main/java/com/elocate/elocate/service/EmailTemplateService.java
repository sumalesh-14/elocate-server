package com.elocate.elocate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service for loading and processing email templates
 */
@Service
@Slf4j
public class EmailTemplateService {

    private static final String TEMPLATE_PATH = "email-templates/";

    /**
     * Load and process email template with variables
     * 
     * @param templateName Name of the template file (without .html extension)
     * @param variables Map of variables to replace in template
     * @return Processed HTML content
     */
    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            String template = loadTemplate(templateName);
            return replaceVariables(template, variables);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            // Return a simple fallback message
            return createFallbackEmail(variables);
        }
    }

    /**
     * Load template from resources
     */
    private String loadTemplate(String templateName) throws IOException {
        String fileName = templateName.endsWith(".html") ? templateName : templateName + ".html";
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH + fileName);
        
        if (!resource.exists()) {
            throw new IOException("Template not found: " + fileName);
        }
        
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Replace variables in template
     * Supports both {{variable}} and {{#if variable}}...{{/if}} syntax
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;
        
        // Replace simple variables {{variable}}
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        // Handle conditional blocks {{#if variable}}...{{/if}}
        result = processConditionals(result, variables);
        
        return result;
    }

    /**
     * Process conditional blocks in template
     */
    private String processConditionals(String template, Map<String, Object> variables) {
        String result = template;
        
        // Find all {{#if variable}}...{{/if}} blocks
        int startIndex = 0;
        while ((startIndex = result.indexOf("{{#if ", startIndex)) != -1) {
            int endIfIndex = result.indexOf("{{/if}}", startIndex);
            if (endIfIndex == -1) break;
            
            // Extract variable name
            int varStart = startIndex + 6; // length of "{{#if "
            int varEnd = result.indexOf("}}", varStart);
            if (varEnd == -1) break;
            
            String varName = result.substring(varStart, varEnd).trim();
            
            // Get the content between {{#if}} and {{/if}}
            int contentStart = varEnd + 2; // after "}}"
            String content = result.substring(contentStart, endIfIndex);
            
            // Check if variable exists and is not null/empty
            Object value = variables.get(varName);
            boolean shouldInclude = value != null && 
                                   !value.toString().isEmpty() && 
                                   !"false".equalsIgnoreCase(value.toString());
            
            // Replace the entire block
            String block = result.substring(startIndex, endIfIndex + 7); // include {{/if}}
            result = result.replace(block, shouldInclude ? content : "");
            
            // Don't increment startIndex to handle nested conditions
        }
        
        return result;
    }

    /**
     * Create a simple fallback email when template loading fails
     */
    private String createFallbackEmail(Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; padding: 20px;'>");
        sb.append("<h2>ELocate Notification</h2>");
        sb.append("<div style='background: #f8f9fa; padding: 15px; border-radius: 4px;'>");
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            sb.append("<p><strong>").append(entry.getKey()).append(":</strong> ")
              .append(entry.getValue()).append("</p>");
        }
        
        sb.append("</div>");
        sb.append("<p style='color: #666; font-size: 12px; margin-top: 20px;'>");
        sb.append("ELocate - E-Waste Recycling Platform</p>");
        sb.append("</body></html>");
        
        return sb.toString();
    }

    /**
     * Check if template exists
     */
    public boolean templateExists(String templateName) {
        try {
            String fileName = templateName.endsWith(".html") ? templateName : templateName + ".html";
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH + fileName);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }
}
