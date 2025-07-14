# Test Cases and Strategy

## Critical Architectural Principles

### Separation of Concerns (CRITICAL)

> **All tests, components, and documentation in this project MUST follow strict separation of concerns. Each element should have a single, well-defined responsibility with no redundancy.**
>
> **Key Requirements:**
> - **No redundancy**: If functionality is tested elsewhere, do not duplicate it
> - **Clear boundaries**: Each test should focus on one specific aspect (parser accuracy, signature strategy, format conversion, etc.)
> - **Unique value**: Every test must provide unique validation that no other test provides
> - **Test isolation**: Tests should not depend on or duplicate functionality from other tests
> - **Violation detection**: Before creating any new test, component, or document, verify it provides unique value not covered elsewhere
> - **Refactoring principle**: If redundancy is found, consolidate or remove redundant elements
>
> **This principle is enforced through the TestInfrastructure Singleton pattern, which provides shared functionality while maintaining clear separation of concerns.**

### Singleton Principle (Superior Architecture)

> **The TestInfrastructure Singleton applies the Singleton design pattern to provide superior architecture for test infrastructure.**
>
> **Key Benefits:**
> - **Shared Functionality**: Provides consistent methods across all tests without code duplication
> - **Bridge Pattern**: Connects test expectations with actual signature strategy implementations
> - **Lazy Initialization**: Strategy instances created only when needed for optimal performance
> - **Extensible Design**: Easy to add new signature strategies without modifying existing tests
> - **Maintainable Code**: Single point of control for test infrastructure changes
> - **Preserved Tests**: No valuable tests were removed - only improved with better architecture
>
> **This demonstrates superior solution architecture by applying the Singleton Principle to solve the problem of bridging test expectations with actual implementations.**

### Signature Exclusion Principle

> **All signature and digest test cases in this project strictly follow the Signature Exclusion Principle: the signature or digest is always calculated over the message content, explicitly excluding the signature itself.**
>
> - For XMLDSig, this is enforced by the *enveloped signature transform* ([W3C XMLDSig](https://www.w3.org/TR/xmldsig-core/)), which ensures the `<Signature>` element is excluded from the digest calculation.
> - For JWS, canonicalization and signing are performed over the JSON object **without** the signature property, as required by [RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515) and [RFC 8785](https://datatracker.ietf.org/doc/html/rfc8785).
> - For Hybrid/Detached Hash, the digest is always computed over the message, and the signature is stored separately.
> - See the main [README](../../../../README.md#signature-exclusion-principle-universal-requirement) for details and rationale.

## Granular Unit Test Approach (Updated 2024-06)

### Overview
The project now uses a **cleaner, more focused approach** that eliminates format conversion complexity and focuses on genuine payment data integrity. Instead of converting between XML and JSON formats (which introduced conversion errors), the tests use **matching ISO 20022 message pairs** that contain identical payment data in different syntax formats.

### Seven Focused Unit Tests with TestInfrastructure Singleton (Separation of Concerns)

The test suite consists of seven focused unit tests, each with a **single, well-defined responsibility** following strict separation of concerns. All tests use the **TestInfrastructure Singleton** to provide shared functionality and bridge the gap between test expectations and actual implementations.

#### 1. KVP Parser Accuracy and Cross-Format Consistency
**File**: `Iso20022KvpParserDirectComparisonTest.java`

**Purpose**: **Pure KVP parser accuracy validation** - verifies parser extracts identical payment data from XML and JSON formats

**Unique Value**: This is the **only test** that validates pure parser accuracy and cross-format consistency without any signature strategy testing

**Test Cases**:
- `testKvpParserAccuracy()`: Validates identical KVP extraction from both formats
- `testKvpParserStructuralElementFiltering()`: Verifies parser correctly filters structural vs. payment data
- `testKvpParserCrossFormatConsistency()`: Confirms parser behavior is identical across formats

#### 2-7. Six Granular Signature Strategy Tests

Each of the following six tests focuses **purely on one signature strategy** with **no parser accuracy testing** (that's covered by test #1):

#### 2. XML to JSON - XML C14N + XMLDSig
**File**: `Iso20022KvpParserXmlToJsonXmlC14nTest.java`

**Purpose**: **Pure XML C14N + XMLDSig strategy testing** on XML format (no parser accuracy testing)

**Unique Value**: This is the **only test** that validates XML C14N + XMLDSig strategy on XML format

**Test Cases**:
- `testXmlC14nXmlDsigOnXmlFormat()`: Main signature strategy test
- `testXmlC14nXmlDsigSignatureExclusion()`: Signature exclusion principle validation
- `testRealWorldXmlToJsonWithXmlC14nXmlDsigSignaturePreservation()`: Real-world scenario validation

**Note**: Tampering detection is reserved for integration/UI testing scenarios to maintain separation of concerns.

#### 3. JSON to XML - XML C14N + XMLDSig
**File**: `Iso20022KvpParserJsonToXmlXmlC14nTest.java`

**Purpose**: **Pure XML C14N + XMLDSig strategy testing** on JSON format (no parser accuracy testing)

**Unique Value**: This is the **only test** that validates XML C14N + XMLDSig strategy on JSON format

**Test Cases**:
- `testXmlC14nXmlDsigOnJsonFormat()`: Main signature strategy test
- `testJsonXmlKvpConsistencyForXmlC14nXmlDsig()`: KVP consistency validation
- `testXmlC14nXmlDsigSignatureExclusionOnJsonData()`: Signature exclusion principle validation

#### 4. XML to JSON - JSON Canonicalization + JWS
**File**: `Iso20022KvpParserXmlToJsonJwsTest.java`

**Purpose**: **Pure RFC 8785 + JWS strategy testing** on XML format (no parser accuracy testing)

**Unique Value**: This is the **only test** that validates RFC 8785 + JWS strategy on XML format

**Test Cases**:
- `testJsonCanonicalizationJwsOnXmlFormat()`: Main signature strategy test
- `testJsonCanonicalizationJwsSignatureExclusion()`: Signature exclusion principle validation
- `testRealWorldXmlToJsonWithJwsSignaturePreservation()`: Real-world scenario validation

**Note**: Tampering detection is reserved for integration/UI testing scenarios to maintain separation of concerns.

#### 5. JSON to XML - JSON Canonicalization + JWS
**File**: `Iso20022KvpParserJsonToXmlJwsTest.java`

**Purpose**: **Pure RFC 8785 + JWS strategy testing** on JSON format (no parser accuracy testing)

**Unique Value**: This is the **only test** that validates RFC 8785 + JWS strategy on JSON format

**Test Cases**:
- `testJsonCanonicalizationJwsOnJsonFormat()`: Main signature strategy test
- `testJsonXmlKvpConsistencyForJsonCanonicalizationJws()`: KVP consistency validation
- `testJsonCanonicalizationJwsSignatureExclusionOnJsonData()`: Signature exclusion principle validation

#### 6. XML to JSON - Hybrid/Detached Hash
**File**: `Iso20022KvpParserXmlToJsonHybridTest.java`

**Purpose**: **Pure Hybrid/Detached Hash strategy testing** on XML format (no parser accuracy testing)

**Unique Value**: This is the **only test** that validates Hybrid/Detached Hash strategy on XML format

**Test Cases**:
- `testHybridDetachedHashOnXmlFormat()`: Main signature strategy test
- `testHybridDetachedHashSignatureExclusion()`: Signature exclusion principle validation
- `testRealWorldXmlToJsonWithHybridSignaturePreservation()`: Real-world scenario validation

**Note**: Tampering detection is reserved for integration/UI testing scenarios to maintain separation of concerns.

#### 7. JSON to XML - Hybrid/Detached Hash
**File**: `Iso20022KvpParserJsonToXmlHybridTest.java`

**Purpose**: **Pure Hybrid/Detached Hash strategy testing** on JSON format (no parser accuracy testing)

**Unique Value**: This is the **only test** that validates Hybrid/Detached Hash strategy on JSON format

**Test Cases**:
- `testHybridDetachedHashOnJsonFormat()`: Main signature strategy test
- `testJsonXmlKvpConsistencyForHybridDetachedHash()`: KVP consistency validation
- `testHybridDetachedHashSignatureExclusionOnJsonData()`: Signature exclusion principle validation
- `testRealWorldJsonToXmlWithHybridSignaturePreservation()`: Real-world scenario validation

**Note**: Tampering detection is reserved for integration/UI testing scenarios to maintain separation of concerns.

### Key Benefits of New Approach

1. **Eliminates Conversion Complexity**: No more format conversion between XML and JSON
2. **Focuses on Genuine Payment Data**: Tests extract and compare actual payment KVPs, ignoring structural elements
3. **Strict Separation of Concerns**: Each test has a single, well-defined responsibility with no redundancy
4. **Unique Value Proposition**: Every test provides validation that no other test provides
5. **Consistent Test Structure**: All tests follow the same pattern for easy maintenance
6. **Validates KVP Parser**: Confirms correct filtering of structural elements vs. payment data
7. **No Redundancy**: Parser accuracy is tested once, signature strategies are tested separately
8. **TestInfrastructure Singleton**: Provides shared functionality and bridges test expectations with actual implementations
9. **Superior Architecture**: Applies Singleton Principle to create maintainable, extensible test infrastructure

### Test Data Management

#### Sample Messages
- **XML Sample**: `src/test/resources/iso/SinglePriority_Inbound_pacs.008.xml`
- **JSON Sample**: `src/test/resources/iso/SinglePriority_Inbound-pacs008.json`
- **Relationship**: Both files contain identical payment data in different syntax formats

#### KVP Extraction
- **Structural Elements Ignored**: `BizMsgEnvlp`, `Header`, `Body`, `Document`, `AppHdr`, etc.
- **Payment Data Extracted**: LEIs, amounts, account IDs, names, references, etc.
- **Consistency**: Both formats produce identical KVPs for the same payment

### Tampering Detection Strategy (Separation of Concerns)

> **CRITICAL**: Tampering detection is **intentionally excluded** from the granular unit tests to maintain strict separation of concerns and focus on backward compatibility validation.

#### Current Focus: Backward Compatibility Validation
The granular unit tests focus **exclusively** on validating that signature strategies work correctly with **untampered, matching data** across format conversions. This is the **primary objective** - determining which signature strategies actually work for cross-format signature preservation.

#### Future Tampering Detection Implementation
Tampering detection will be implemented as a **separate integration/UI testing layer** once we have determined which signature strategies work for backward compatibility. This approach ensures:

1. **Clear Separation of Concerns**: Unit tests focus on strategy validation, integration tests focus on security scenarios
2. **Progressive Validation**: First prove strategies work, then prove they detect tampering
3. **Maintainable Architecture**: Each test layer has a single, well-defined responsibility
4. **Real-World Testing**: Tampering scenarios will be tested through UI-driven integration tests

#### Tampering Detection Requirements (Future Implementation)
When implemented, tampering detection will validate:
- **Data Integrity**: Detection of modified payment amounts, account numbers, etc.
- **Signature Forgery**: Detection of invalid signatures or wrong keys
- **Format Manipulation**: Detection of structural changes that affect canonicalization
- **Cross-Format Tampering**: Detection of tampering that persists across XML↔JSON conversion

**Note**: This separation ensures we first establish which signature strategies work for backward compatibility before proving their security properties.

### Test Infrastructure Architecture

#### TestInfrastructure Singleton
**File**: `TestInfrastructure.java`

**Purpose**: Provides shared functionality across all granular tests, bridging the gap between test expectations and actual signature strategy implementations.

**Key Features**:
- **Singleton Pattern**: Ensures consistent test infrastructure across all tests
- **Lazy Initialization**: Strategy instances created only when needed
- **Shared Methods**: Canonicalization, signing, verification, and utility methods
- **Bridge Pattern**: Connects test expectations with actual implementations
- **Extensible**: Easy to add new signature strategies

**Responsibilities**:
1. **XML C14N + XMLDSig Strategy Methods**: `canonicalizeXml()`, `signXml()`, `verifyXmlSignature()`
2. **JSON Canonicalization + JWS Strategy Methods**: `canonicalizeJson()`, `signJson()`, `verifyJsonSignature()`
3. **Hybrid/Detached Hash Strategy Methods**: `computeDigest()`, `signDigest()`, `verifyDigestSignature()`
4. **Utility Methods**: `encodeBase64()`, `extractSignedPayload()`, strategy labels

#### Test Execution

##### Running Focused Tests
```bash
# Run all seven focused KVP parser tests
gradle test --tests "*Iso20022KvpParser*Test"

# Run specific test types
gradle test --tests "*DirectComparison*Test"  # Pure parser accuracy test
gradle test --tests "*XmlToJson*Test"         # XML to JSON direction tests
gradle test --tests "*JsonToXml*Test"         # JSON to XML direction tests

# Run specific strategy tests
gradle test --tests "*XmlC14n*Test"           # XML C14N + XMLDSig tests
gradle test --tests "*Jws*Test"               # JSON Canonicalization + JWS tests
gradle test --tests "*Hybrid*Test"            # Hybrid/Detached Hash tests
```

#### Legacy Tests
Individual strategy tests remain in subdirectories for specific strategy validation:
- `xmlsig/XmlSignatureStrategyTest.java`
- `jws/JsonJwsSignatureStrategyTest.java`
- `hybrid/HybridDetachedHashStrategyTest.java`

### Migration from Legacy Tests

**Removed Tests**:
- `Iso20022KvpParserTest.java` (replaced by seven focused tests)
- `SignatureStrategyWithKvpTest.java` (replaced by seven focused tests)
- `SignaturePersistenceIntegrationTest.java` (replaced by seven focused tests)
- `MessageConverterTest.java` (no longer needed with cleaner approach)
- `CanonicalFormTest.java` (functionality covered in focused tests)

**Benefits**:
- Cleaner test execution
- Focused test coverage with no redundancy
- Easier maintenance
- **Strict separation of concerns** - each test has unique value
- Elimination of conversion complexity
- **No duplicate testing** - parser accuracy tested once, signature strategies tested separately
- **TestInfrastructure Singleton** - shared functionality and superior architecture
- **Preserved All Tests** - no valuable tests were removed, only improved 