/*
 * Copyright (c) 2010 Dmitry Astapov <dastapov@gmail.com>
 * Distributed under BSD License
 */
package ua.astapov.jira.plugins.hierarchicalid;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.util.EasyList;
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HierarchicalIDCFType extends CalculatedCFType implements SortableCustomField, MultipleCustomFieldType, MultipleSettableCustomFieldType 
{
    private static final Logger log = Logger.getLogger(HierarchicalIDCFType.class);

	private final OptionsManager optionsManager;

    public HierarchicalIDCFType(OptionsManager optionsManager)
    {
		this.optionsManager = optionsManager;
    }

    public String getStringFromSingularObject(Object value)
    {
        return value != null ? value.toString() : Boolean.FALSE.toString();
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        if (string != null)
        {
            return (string);
        }
        else
        {
            return Boolean.FALSE.toString();
        }
    }

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
    	// Get a list of stored options for the field
    	Options options = optionsManager.getOptions(field.getRelevantConfig(issue));

    	String linkTypeName = ((Option) options.getRootOptions().get(0)).getValue();
        log.debug("linkTypeName: " + linkTypeName);

        // incidents linked to this one
        List<IssueLink> outwardLinks = ComponentManager.getInstance().getIssueLinkManager().getOutwardLinks(issue.getId());
 
        String hierarchicalId = issue.getKey();
        
        for (int i = 0; i < outwardLinks.size(); i++)
        {
          IssueLink link = (IssueLink) outwardLinks.get(i);
          String lTypeName = link.getIssueLinkType().getName();
          log.debug("Examining link with type name " + lTypeName);
          if ( lTypeName.equals(linkTypeName) ) {
            log.debug("Link found.");

            // the key of the parent issue
            String parentKey = link.getDestinationObject().getKey();
            log.debug("Key of the parent issue " + parentKey);

            hierarchicalId = parentKey + "." + stripProject(hierarchicalId);
          }
        }
        return hierarchicalId;

    }
    
    private String stripProject(String key)
    {
    	return key.substring(key.lastIndexOf('-')+1);
    }
    
    public List getConfigurationItemTypes()
    {
    	return EasyList.build(new SettableOptionsConfigItem(this, optionsManager));
    }
    
    public Options getOptions(FieldConfig config, JiraContextNode jiraContextNode)
    {
    	return this.optionsManager.getOptions(config);
    }

	public Set getIssueIdsWithValue(CustomField arg0, Option arg1) {
		// TODO Auto-generated method stub
		return new HashSet();
	}

	public void removeValue(CustomField arg0, Issue arg1, Option arg2) {
		// TODO Auto-generated method stub
	}
}
