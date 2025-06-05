# AI Agent Instructions Guide

## File Inclusion Format

When instructing the AI agent via CursorAI chat panel, use the following format:

```
<file_inclusion>
<file>
path: [relative path to file]
content: [file content or specific sections]
</file>
</file_inclusion>
```

## Required Information

1. **File Context**
   - Always specify the file path
   - Include relevant file content
   - Mention any dependencies
   - **Specify if the file is part of a containerized, test-driven workflow.**

2. **Change Instructions**
   - Be specific about what to change
   - Reference existing content
   - Specify preservation requirements
   - **Describe any test or containerization context relevant to the change.**

3. **Documentation Updates**
   - Reference the documentation template
   - Include change history
   - Maintain cross-references
   - **Ensure documentation reflects the containerized, test-driven workflow.**

## Example Instructions

```
Please update the system design document with the following changes:

<file_inclusion>
<file>
path: docs/architecture/system-design.md
content: [relevant sections]
</file>
</file_inclusion>

Changes required:
1. Add new section about [topic]
2. Update implementation details
3. Preserve existing content
4. Update change history
5. **Describe any test or containerization context.**
```

## Best Practices

1. **Always Include**
   - File path
   - Current content
   - Change requirements
   - Documentation updates
   - **Test and containerization context**

2. **Avoid**
   - Vague instructions
   - Missing file context
   - Incomplete change history

3. **Documentation Rules**
   - Follow template structure
   - Maintain version history
   - Preserve existing content
   - Update cross-references
   - **Reflect the containerized, test-driven workflow.**

## Template Usage

When requesting documentation updates, reference:

```
<file_inclusion>
<file>
path: docs/templates/documentation-template.md
content: [relevant template sections]
</file>
</file_inclusion>
```

## Change History Format

```
### [Version] - YYYY-MM-DD
- **Change**: [Description]
- **Reason**: [Justification]
- **Impact**: [Effect]
- **Migration**: [Path]
``` 