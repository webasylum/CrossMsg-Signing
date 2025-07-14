# ISO 20022 Schema Documentation

## Overview
This directory contains documentation related to ISO 20022 message schemas and their implementation in the project.

## Contents
- Schema conversion rules and mappings
- XML to JSON transformation specifications
- Message format validation rules
- Namespace handling documentation

## Related Files
- `src/main/java/com/tsg/crossmsg/signing/config/ConversionRules.java` - Core conversion rules
- `src/test/resources/iso/` - Test message samples
- `src/test/resources/iso/ISO 20022 - JSON Schema Draft 2020-12 generation (v20250321) (clean).docx` - Schema generation guide

## Schema Structure
The project implements the following ISO 20022 message types:
- pacs.008.001.09 (Customer Credit Transfer)
- head.001.001.02 (Business Application Header)

## Namespace Handling
- XML namespace: `urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09`
- AppHdr namespace: `urn:iso:std:iso:20022:tech:xsd:head.001.001.02`

## Conversion Rules
1. XML to JSON:
   - Strip XML prefixes
   - Convert currency elements to `{"amt","Ccy"}` format
   - Transform elements to objects/arrays
   - Preserve namespace information

2. JSON to XML:
   - Reverse mapping to produce identical XML structure
   - Maintain canonicalization compatibility
   - Preserve namespace declarations
   - Ensure XML schema validation 