# Payment Message Signature Strategies Test Suite

**Author:** Martin Sansone (martin@web-asylum.com)

## 🤖 AI Development Environment Note

> **CRITICAL FOR AI ASSISTANTS:**
> 
> **Development Environment:** This project is developed on **Windows 11 with WSL2 Ubuntu, Docker Desktop, and CursorAI IDE integrated with containers**. 
> 
> **Key Environment Details:**
> - **OS:** Windows 11 with WSL2 Ubuntu
> - **Containerization:** Docker Desktop with container integration
> - **IDE:** CursorAI IDE connected to development containers
> - **Build System:** All builds, tests, and development tasks run inside Docker containers
> - **File System:** Windows filesystem accessed through WSL2
> - **Terminal:** Use Windows PowerShell or WSL2 bash for Docker commands
> 
> **AI Assistant Guidelines:**
> - Always assume Docker containerized development environment
> - Recommend Docker commands for Windows host (not inside containers)
> - Use `dev.ps1` script for container management
> - Consider WSL2 filesystem performance implications
> - Provide Windows-specific Docker Desktop troubleshooting when needed
> - Remember container networking and volume mounts for development
> 
> **Common Commands:**
> ```powershell
> # Windows host commands (recommended)
> .\dev.ps1 start    # Start development container
> .\dev.ps1 test     # Run tests in container
> .\dev.ps1 shell    # Open shell in container
> 
> # Docker commands from Windows host
> docker-compose up -d
> docker-compose logs -f
> ```
> 
> **⚠️ Important:** Never assume local Java/Gradle installation - everything runs in containers!

## Project Architecture Overview

This project consists of **two integrated components** designed to work together:

### 🎯 **Core Project: Iso20022KvpParser**
**Purpose**: Sophisticated parser that accurately identifies Key-Value Pairs (KVPs) within ISO 20022 XML and JSON payment messages and successfully signs them using three different cryptographic strategies.

**Key Features**:
- **Advanced KVP Extraction**: Identifies and extracts business data KVPs from complex ISO 20022 message structures
- **Three Signature Strategies**:
  1. **XML C14N + XMLDSig**: W3C XML Canonicalization 1.1 + XML Digital Signatures
  2. **RFC 8785 + JWS**: JSON Canonicalization (RFC 8785) + JSON Web Signatures
  3. **Hybrid/Detached Hash**: SHA-256 digest + asymmetric signature
- **Comprehensive Unit Tests**: Validates KVP extraction accuracy and signature strategy effectiveness
- **Cross-Format Compatibility**: Works with both XML and JSON ISO 20022 message formats

### 🌐 **WebUI Sub-Project: CrossMsg-Signing-WebUI**
**Purpose**: Web-based demonstration interface that showcases the full functionality of the core `Iso20022KvpParser`.

**Key Features**:
- **Interactive Message Editing**: Users can view and edit XML/JSON payment message examples
- **Real-Time Signature Testing**: Three buttons to test each signature strategy from the core project
- **Tampering Detection**: Validates whether canonical business data KVPs have been modified
- **Signature Validation**: Compares current message signatures against original core project signatures
- **Full Parser Integration**: Uses the actual `Iso20022KvpParser` from the core project (not a dummy implementation)

### 🔗 **Architectural Integration**
The WebUI sub-project is **intentionally included** in the same codebase as the core project because:

1. **Separation of Concerns**: Core parser logic is separate from UI presentation
2. **Singleton Principle**: Shared `Iso20022KvpParser` instance provides consistent behavior
3. **Real Implementation**: WebUI must use the actual sophisticated parser, not a simplified version
4. **Unified Testing**: Both components can be tested together in the same containerized environment

### 📁 **Project Structure**
```
TSG-CrossMsg-Signing/
├── src/                          # Core Iso20022KvpParser implementation
│   ├── main/java/com/tsg/crossmsg/signing/
│   │   ├── model/                # Core model classes
│   │   ├── parser/               # Iso20022KvpParser implementation
│   │   └── strategies/           # Three signature strategies
│   └── test/                     # Core project unit tests
├── CrossMsg-Signing-WebUI/       # WebUI demonstration sub-project
│   ├── webui-backend/            # Spring Boot backend for WebUI
│   ├── webui-frontend/           # React frontend for WebUI
│   └── nginx/                    # Reverse proxy configuration
├── docker-compose.yml            # Full application deployment (WebUI)
├── dev.ps1                       # Development script for core project
└── .devcontainer/                # VS Code Dev Container configuration
```

## Development Environment Setup

### 🚀 **Quick Start (Recommended)**

#### **Option 1: Core Project Development (Docker Compose)**
For working on the core `Iso20022KvpParser` and running unit tests:

1. **Prerequisites**:
   - Windows 11 with Docker Desktop installed and running
   - PowerShell or Command Prompt
   - Git for Windows

2. **Clone and Navigate**:
   ```powershell
   git clone <repository-url>
   cd TSG-CrossMsg-Signing
   ```

3. **Start Development Environment**:
   ```powershell
   .\dev.ps1 start
   ```

4. **Verify Setup**:
   ```powershell
   docker-compose ps                    # Should show dev container running
   docker-compose exec dev ls -la /app  # Should show project files
   docker-compose exec dev java -version # Should show Java 17
   docker-compose exec dev gradle --version # Should show Gradle 8.6
   ```

5. **Run Core Tests**:
   ```powershell
   .\dev.ps1 test
   # OR
   docker-compose exec dev gradle test --tests "*Iso20022KvpParser*Test"
   ```

6. **Verify Setup** (Optional):
   ```powershell
   .\verify-setup.ps1
   ```

6. **Access Development Shell**:
   ```powershell
   .\dev.ps1 shell
   # OR
   docker-compose exec dev bash
   ```

#### **Option 2: VS Code Dev Containers (Alternative)**
For IDE-integrated development:

1. **Open VS Code/Cursor IDE**
2. **Open the project folder**: `C:\Projects\TSG-CrossMsg-Signing`
3. **Open in Dev Container**:
   - Press `Ctrl+Shift+P`
   - Type: `Dev Containers: Open Folder in Container`
   - Wait for container to build
4. **Verify Setup**:
   ```bash
   ls -la /app                    # Should show project files
   java -version                  # Should show Java 17
   gradle --version               # Should show Gradle 8.6
   ```
5. **Run Core Tests**:
   ```bash
   gradle test --tests "*Iso20022KvpParser*Test"
   ```

#### **Option 2: Full Application Development (Docker Compose)**
For working on both core project and WebUI together:

1. **Navigate to WebUI directory**:
   ```powershell
   cd C:\Projects\TSG-CrossMsg-Signing\CrossMsg-Signing-WebUI
   ```
2. **Start full application**:
   ```powershell
   docker-compose up -d
   ```
3. **Access WebUI**: Open browser to `http://localhost:3000`
4. **Access Backend API**: `http://localhost:8080`

### 🔧 **Detailed Setup Instructions**

#### **Prerequisites**
- Windows 11 with Docker Desktop installed and running
- PowerShell or Command Prompt
- Git for Windows
- VS Code or Cursor IDE (optional, for IDE integration)

#### **Core Project Development**
```powershell
# Navigate to project root
cd C:\Projects\TSG-CrossMsg-Signing

# Start development environment
.\dev.ps1 start

# Verify container is running
docker-compose ps

# Run tests
.\dev.ps1 test

# Access development shell
.\dev.ps1 shell
```

#### **WebUI Development**
```powershell
# Navigate to WebUI sub-project
cd C:\Projects\TSG-CrossMsg-Signing\CrossMsg-Signing-WebUI

# Start the full application stack
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

#### **Full Stack Development**
```powershell
# From project root, start both core and WebUI
cd C:\Projects\TSG-CrossMsg-Signing

# Start WebUI (includes backend that uses core parser)
cd CrossMsg-Signing-WebUI
docker-compose up -d

# In another terminal, work on core project
cd C:\Projects\TSG-CrossMsg-Signing
# Use VS Code Dev Container for core development
```

### 🧪 **Testing Strategy**

#### **Core Project Tests**
```powershell
# Run all core project tests
.\dev.ps1 test
# OR
docker-compose exec dev gradle test

# Run specific KVP parser tests
docker-compose exec dev gradle test --tests "*Iso20022KvpParser*Test"

# Run signature strategy tests
docker-compose exec dev gradle test --tests "*XmlC14n*Test"
docker-compose exec dev gradle test --tests "*Jws*Test"
docker-compose exec dev gradle test --tests "*Hybrid*Test"

# Run tests with verbose output
docker-compose exec dev gradle test --info --console=plain
```

#### **WebUI Testing**
1. **Start WebUI**: `docker-compose up -d` (from WebUI directory)
2. **Open Browser**: Navigate to `http://localhost:3000`
3. **Test Functionality**:
   - View sample XML/JSON messages
   - Edit message values
   - Test signature strategies
   - Verify tampering detection

### 🔍 **Troubleshooting**

#### **Core Project Issues**
- **Container won't start**: 
  ```powershell
  # Clean up Docker state
  docker-compose down --volumes
  docker system prune -f
  .\dev.ps1 start
  ```
- **Mount errors**: Restart Docker Desktop and WSL2:
  ```powershell
  wsl --shutdown
  # Restart Docker Desktop from system tray
  .\dev.ps1 start
  ```
- **Tests failing**: Check container logs:
  ```powershell
  docker-compose logs dev
  docker-compose exec dev gradle test --info
  ```
- **Java/Gradle issues**: Container uses Java 17 and Gradle 8.6 (already configured)

#### **WebUI Issues**
- **Port conflicts**: Ensure ports 3000, 8080, 80, 443 are available
- **Container startup failures**: Check `docker-compose logs` for specific errors
- **Parser integration issues**: Verify WebUI backend is using core project classes

#### **Common Docker Issues**
- **WSL2 path problems**: Use explicit bind mounts in docker-compose.yml
- **Permission issues**: Ensure Docker Desktop has access to project directory
- **Resource limits**: Increase Docker Desktop memory allocation if needed

### 📚 **Documentation Index**

#### **Core Project Documentation**
- [Implementation Documentation](docs/implementation/README.md) - Detailed implementation guides
- [Architecture Documentation](docs/architecture/system-design.md) - System architecture and design
- [Security Model](docs/architecture/security-model.md) - Security implementation details
- [Quantum Transition](docs/architecture/quantum-transition.md) - Quantum-safe transition strategy

#### **Setup and Environment**
- [Development Environment Setup](docs/setup/development-environment-setup.md) - **COMPREHENSIVE:** Complete setup guide for core project and WebUI
- [AI Development Environment Config](docs/setup/ai-environment-config.md) - **CRITICAL:** AI assistant environment awareness
- [Quick Setup Reference](docs/setup/quick-setup-reference.md) - 5-minute setup guide
- [Windows Environment Setup](docs/setup/windows-environment-setup.md) - Complete Windows 11 + WSL2 + Docker setup
- [Development Environment](docs/setup/) - Environment configuration and troubleshooting

#### **WebUI Documentation**
- [WebUI README](README-WebUI.md) - WebUI-specific documentation
- [WebUI Setup Guide](CrossMsg-Signing-WebUI/README.md) - WebUI setup and configuration

> **Note:**
> All builds and tests for this project must be run inside Docker containers using the provided scripts (e.g., `dev.ps1`) or Docker Compose. The `.vscode/settings.json` file is configured for code navigation and editing only; it does not affect the actual build or runtime environment. For more details, see the [Windows Environment Setup Guide](docs/setup/windows-environment-setup.md).
> 
> **🤖 AI Assistant Note:** This project uses Windows 11 + WSL2 + Docker Desktop + CursorAI IDE. Always assume containerized development environment and recommend Docker-based commands. See [AI Development Environment Config](docs/setup/ai-environment-config.md) for complete guidelines.

> **⚠️ IMPORTANT: Extension Recommendation Warning**
> 
> When opening this project in CursorAI, you may see a notification asking:
> *"Do you want to install the recommended extensions from Anysphere and auchenberg for this repository?"*
> 
> **IGNORE THIS NOTIFICATION** - Do not install these recommended extensions. The project has been tested with an alternative extension build that works correctly for our needs. Installing the recommended extensions will cause compatibility issues and may break the project functionality. Only one version of these extensions is supported for this project, and it's already configured in the project settings.

### Implementation Details
- [Improvements Log](docs/implementation/improvements.md) - Chronological log of all improvements
- [XML Signature Implementation](docs/implementation/xml-signature/)
  - [Namespace Handling](docs/implementation/xml-signature/namespace-handling.md)
  - [Canonicalization](docs/implementation/xml-signature/canonicalization.md)
  - [Verification](docs/implementation/xml-signature/verification.md)
- [JWS Signature Implementation](docs/implementation/jws/strategy.md) - JWS (RFC 7515) + JSON Canonicalization (RFC 8785)
- [Hybrid/Detached Hash Implementation](docs/implementation/hybrid/strategy.md) - Hybrid/Detached Hash strategy
- [Testing Implementation](docs/implementation/testing/)
  - [Test Cases](docs/implementation/testing/test-cases.md)
  - [Test Data](docs/implementation/testing/test-data.md)
- [Architecture Details](docs/implementation/architecture/)
  - [Process Tracking](docs/implementation/architecture/process-tracking.md)
  - [Error Handling](docs/implementation/architecture/error-handling.md)

### ISO 20022 Documentation
- [Message Samples](src/test/resources/iso/) - Sample XML and JSON messages
- [Schema Conversion](src/test/resources/iso/ISO%2020022%20-%20JSON%20Schema%20Draft%202020-12%20generation%20(v20250321)%20(clean).docx)

## Overview

This repository provides a comprehensive, containerized test harness for evaluating signature strategies that ensure end-to-end integrity of ISO 20022 pacs.008 payment messages across format conversions (XML ↔ JSON) in a low-trust, multi-hop payment infrastructure.

## What's New (2024-06)
- **Containerized Development:** All builds, tests, and development tasks run inside Docker containers for consistency and reproducibility.
- **Gradle Build System:** Replaces Maven for faster, more flexible builds and test execution.
- **Strict Layer Separation:** Code is organized by infrastructure, service, script, and test layers, with dependency injection and constructor-based initialization.
- **Environment-Driven Configuration:** All configuration is via environment variables, loaded from `.env` files. No hardcoded values.
- **Comprehensive Testing:**
  - All unit and integration tests use JUnit 5, with a 5-minute timeout and resource cleanup.
  - Tests are run inside the container using Gradle (`.\dev.ps1 test` or `docker-compose exec dev gradle test`).
  - Test coverage includes canonicalization, signature persistence across format conversion, and negative cases (tampering, wrong key, etc.).
  - Test results and coverage reports are available in `build/reports/`.
- **Signature Strategies:**
  - XML C14N + XMLDSig (W3C C14N 1.1, Apache Santuario)
  - JSON Canonicalization (RFC 8785) + JWS (Nimbus JOSE + JWT)
  - Hybrid/Detached Hash (SHA-256, BouncyCastle/JCA)
- **Documentation Policy:** All code changes must be accompanied by documentation updates. Documentation is versioned and checked alongside code.

## ✅ **Phase 1 Status: Core Project Complete (July 2025)**

### **What's Working:**
- **✅ Core Development Environment**: Docker containerized development with Java 17 and Gradle 8.6
- **✅ Iso20022KvpParser**: Sophisticated parser with 1000+ lines and ISO e-Repository integration
- **✅ Three Signature Strategies**: All strategies implemented and tested successfully
- **✅ Comprehensive Test Suite**: 7 focused unit tests with real-world scenario validation
- **✅ Cross-Format Compatibility**: XML↔JSON signature preservation validated
- **✅ Signature Exclusion Principle**: All strategies correctly implemented
- **✅ Development Scripts**: `dev.ps1` script for easy container management

### **Test Results:**
- **✅ All Tests Passing**: 100% success rate across all signature strategies
- **✅ KVP Extraction**: 29 payment KVPs correctly extracted from both XML and JSON
- **✅ Signature Generation**: All three strategies successfully generate signatures
- **✅ Signature Verification**: All signatures validate correctly
- **✅ Cross-Format Testing**: XML↔JSON conversions preserve signature integrity

### **Ready for Phase 2:**
The core project is fully functional and ready for WebUI integration. All cryptographic operations work correctly, and the development environment is stable and reliable.

## Project Phases

### Phase 1: Classical Cryptography Implementation
- Implementation of current standards (RSA/ECDSA)
- Focus on immediate compatibility and performance
- Core signature strategies:
  1. XML C14N + XMLDSig
  2. JSON Canonicalization (RFC 8785) + JWS
  3. Hybrid/Detached Hash

### Phase 2: Hybrid Quantum-Safe Implementation
- Integration of quantum-safe algorithms alongside classical ones
- Support for hybrid signatures
- Enhanced security with quantum resistance
- Maintained backward compatibility

### Phase 3: Future Quantum-Safe Transition
- Documentation of transition strategy
- Analysis of quantum-safe algorithm candidates
- Implementation roadmap for pure quantum-safe signatures

## Documentation Standards

### Preservation Requirements
- **CRITICAL**: All documentation and code changes MUST preserve existing context and information
- Never remove or overwrite existing documentation without explicit approval
- When updating documentation:
  1. First review existing content thoroughly
  2. Preserve all existing information
  3. Add new information in a way that complements existing content
  4. Use append/merge strategies rather than replacement
  5. Maintain all historical context and examples
- Documentation changes require explicit approval before merging
- All AI-assisted changes must be reviewed for context preservation

### Architectural Requirements

#### Separation of Concerns (CRITICAL)
- **CRITICAL**: All code, tests, and documentation MUST follow strict separation of concerns
- Each component, test, or document should have a **single, well-defined responsibility**
- **No redundancy**: If functionality is tested elsewhere, do not duplicate it
- **Clear boundaries**: Each test should focus on one specific aspect (parser accuracy, signature strategy, format conversion, etc.)
- **Unique value**: Every test must provide unique validation that no other test provides
- **Test isolation**: Tests should not depend on or duplicate functionality from other tests
- **Documentation clarity**: Each document should address one specific concern or aspect
- **Code organization**: Classes and methods should have single, clear responsibilities
- **Violation detection**: Before creating any new test, component, or document, verify it provides unique value not covered elsewhere
- **Refactoring principle**: If redundancy is found, consolidate or remove redundant elements

#### Implementation Guidelines
- **Test design**: Each test class should focus on one specific concern (e.g., parser accuracy, signature verification, format conversion)
- **Component design**: Each class should have a single responsibility and clear interface
- **Documentation design**: Each document should address one specific aspect of the system
- **Validation**: Before implementation, verify the new element provides unique value
- **Review process**: All changes must be reviewed for separation of concerns compliance

#### Singleton Principle (Superior Architecture)
- **TestInfrastructure Singleton**: Applies Singleton design pattern to provide shared functionality while maintaining separation of concerns
- **Bridge Pattern**: Connects test expectations with actual signature strategy implementations
- **Lazy Initialization**: Strategy instances created only when needed for optimal performance
- **Extensible Design**: Easy to add new signature strategies without modifying existing tests
- **Preserved Tests**: No valuable tests were removed - only improved with better architecture
- **Superior Solution**: Demonstrates how to solve architectural problems using design patterns

### Project Structure
```
/
├── .vscode/                    # VS Code configuration
│   ├── launch.json            # Debug configuration
│   └── settings.json          # Java settings
├── docs/                      # Documentation
│   ├── architecture/          # Architecture documentation
│   │   ├── system-design.md
│   │   ├── security-model.md
│   │   └── quantum-transition.md
│   ├── implementation/        # Implementation documentation
│   │   ├── README.md
│   │   ├── improvements.md
│   │   ├── xml-signature/
│   │   ├── testing/
│   │   └── architecture/
│   └── templates/             # Documentation templates
│       └── documentation-template.md
├── gradle/                    # Gradle wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── img/                       # Project images and diagrams
├── iso/                       # ISO 20022 related files
│   ├── samples/              # Sample messages
│   │   ├── pacs.008.xml
│   │   └── pacs.008.json
│   └── ISO 20022 - JSON Schema Draft 2020-12 generation (v20250321) (clean).docx
├── puml/                      # PlantUML diagrams
├── scripts/                   # Utility scripts
│   └── healthcheck.sh        # Container health check
├── src/
│   ├── main/
│   │   └── java/com/tsg/crossmsg/signing/
│   │       └── model/        # Core model classes
│   └── test/
│       └── java/com/tsg/crossmsg/signing/
│           ├── hybrid/       # Hybrid signature tests
│           ├── jws/          # JWS signature tests
│           ├── xmlsig/       # XML signature tests
│           ├── BaseSigningTest.java
│           ├── MessageConverterTest.java
│           ├── ProjectSetupTest.java
│           └── SignatureStrategyTest.java
├── target/                    # Build output directory
├── .dockerignore             # Docker ignore rules
├── .gitignore               # Git ignore rules
├── build.gradle             # Gradle build configuration
├── dev.ps1                  # Development script
├── docker-compose.yml       # Docker compose configuration
├── Dockerfile              # Docker build configuration
├── gradle.properties       # Gradle properties
├── README.md              # Project documentation
└── settings.gradle        # Gradle settings
```

### Documentation Update Process
1. Each code change must include corresponding documentation updates
2. Documentation is versioned alongside code
3. Automated checks ensure documentation stays in sync
4. Regular documentation reviews as part of code review process

## Implementation Details

### Common Foundations

#### Message Model
- ISO 20022 pacs.008.001.09 payment (`<BizMsgEnvlp>` + `<Document><FIToFICstmrCdtTrf>` payload).
- Full BizMsgEnvlp wrapper must travel with signature container intact.

#### Format Conversion
- **XML → JSON:** deterministic mapping (strip prefixes, Ccy → `{"amt","Ccy"}`, elements → objects/arrays).
- **JSON → XML:** reverse mapping producing identical XML structure for canonicalization.

#### Key Concepts
- **Canonicalization:** process of transforming data to a canonical byte stream for stable hashing or signing.
- **Signature Container:** an element/property in the message header that carries the digital signature or digest.

## Signature Exclusion Principle (Universal Requirement)

> **For all digital signature strategies in this project, the signature or digest must always be calculated over the message content, explicitly excluding the signature itself.**
>
> - **XMLDSig:** Enforced by the *enveloped signature transform* ([W3C XMLDSig](https://www.w3.org/TR/xmldsig-core/)), which ensures the `<Signature>` element is excluded from the digest calculation.
> - **JWS (JSON Web Signature):** Canonicalization and signing are performed over the JSON object **without** the signature property, as required by [RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515) and [RFC 8785](https://datatracker.ietf.org/doc/html/rfc8785).
> - **Hybrid/Detached Hash:** The digest is always computed over the message, and the signature is stored separately.
>
> **If this rule is not followed, signature validation will always fail. This project enforces and audits this rule for every strategy.**

### Strategy 1: XML C14N + XMLDSig

#### Description
- Use W3C XML Canonicalization 1.1 to produce a stable byte stream of the XML payload.
- Generate an XML Digital Signature (`XMLDSig`) over those bytes.
- Embed the `<Signature>` element in the ISO 20022 AppHdr (`<Sgntr>` slot).

#### Process Flow
1. **Prepare XML**: ensure namespaces and whitespace conform to the spec.
2. **Canonicalize**: apply exclusive C14N 1.1 transform.
3. **Sign**: use Apache Santuario to generate `<Signature>` over the canonical bytes.
4. **Embed**: insert `<Signature>` into `<AppHdr>` of `BizMsgEnvlp`.
5. **Transmit**: deliver as XML or convert to JSON (leaving `<Signature>` untouched).
6. **Verify**:
   - Extract `<Signature>` from AppHdr.
   - If JSON, convert back to XML via deterministic mapping.
   - Canonicalize (C14N 1.1).
   - Use XMLDSig verifier with sender's public key.

#### Implementation Details
- **Libraries:** Apache Santuario (XMLSec), JUnit 5
- **Canonicalization Method URI:** `http://www.w3.org/2006/12/xml-c14n11`
- **Signature Algorithm:** `RSA_SHA256` or `ECDSA_SHA256`

> **Signature Exclusion Principle:** This strategy uses the enveloped signature transform to ensure the `<Signature>` element is excluded from the digest calculation, as required by the XMLDSig standard.

### Strategy 2: JSON Canonicalization (RFC 8785) + JWS

#### Description
- Serialize the full `BizMsgEnvlp` JSON object using RFC 8785 canonicalization (sorted keys, stripped whitespace).
- Generate a JSON Web Signature (JWS) over the canonical bytes.
- Carry the compact JWS string in a top-level `"Signature"` property of the JSON AppHdr.

#### Process Flow
1. **Prepare JSON:** ensure consistent field ordering, no extraneous whitespace.
2. **Canonicalize:** apply RFC 8785 to produce canonical UTF-8 bytes.
3. **Sign:** use Nimbus JOSE + JWT to create a JWS (e.g., `ES256` or `EdDSA`).
4. **Embed:** insert JWS compact serialization into `AppHdr.Signature`.
5. **Transmit:** deliver as JSON or convert to XML, mapping the `Signature` property to an XML element.
6. **Verify**:
   - Extract JWS from `Signature` property or XML `<Signature>` element.
   - Reconstruct JSON via XML→JSON mapping if needed.
   - Canonicalize (RFC 8785).
   - Verify JWS using the sender's public key.

#### Implementation Details
- **Libraries:** Nimbus JOSE + JWT, JUnit 5
- **Canonicalization Spec:** RFC 8785 JSON Canonicalization Scheme
- **JWS Algorithms:** `ES256`, `Ed25519`

> **Signature Exclusion Principle:** This strategy canonicalizes and signs the JSON object **without** the signature property, as required by JWS and RFC 8785.

### Strategy 3: Hybrid / Detached-Hash

#### Description
- Compute a deterministic digest (SHA-256 or HMAC-SHA256) over the canonical representation of the message (XML C14N or RFC 8785 JSON).
- Sign **only** the digest with an asymmetric key (RSA/ECDSA/EdDSA).
- Carry the signed digest (base64 or hex) in a lightweight header field (e.g., `AppHdr.MsgDgst` or JSON property).

#### Process Flow
1. **Canonicalize**: same as Strategy 1 or 2 to produce a stable byte stream.
2. **Digest**: compute SHA-256 hash (or HMAC if a shared secret is used).
3. **Sign Digest**: use private key to sign the hash.
4. **Embed**: place the signed digest string into a dedicated header slot.
5. **Transmit**: deliver as XML or JSON.
6. **Verify**:
   - Extract signed digest from header.
   - Re-canonicalize message.
   - Re-compute digest.
   - Verify digest signature with public key.

#### Implementation Details
- **Libraries:** Java `MessageDigest`, BouncyCastle or JCA for signatures, JUnit 5
- **Hash Algorithm:** `SHA-256`
- **Signature Algorithm:** `RSA_SHA256` or `ECDSA_SHA256`

> **Signature Exclusion Principle:** The digest is always computed over the message content, and the signature is stored separately. The signature is never included in the digest.

## Critical Finding: Cross-Format Signature Strategy Analysis

### **Key Discovery: Canonicalization Consistency Determines Cross-Format Success**

After comprehensive testing of all three signature strategies across XML↔JSON format conversions, a critical finding emerged:

**All three strategies in this project are limited to business data KVPs only** - they extract the same 29 payment fields using `Iso20022KvpParser` and hash only the business data, not the full document structure.

### **The Real Difference: Canonicalization Method Consistency**

| Strategy | Canonicalization Method | Cross-Format Behavior | Success Rate |
|----------|----------------------|---------------------|--------------|
| **XML C14N + XMLDSig** | Always uses XML C14N 1.1 | ✅ **WORKS** across formats | 100% |
| **RFC 8785 + JWS** | Always uses JSON RFC 8785 | ✅ **WORKS** across formats | 100% |
| **Hybrid/Detached Hash** | Format-specific (XML C14N for XML, RFC 8785 for JSON) | ❌ **FAILS** across formats | 0% |

### **Why XML C14N + XMLDSig and RFC 8785 + JWS Work:**

Both strategies use **consistent canonicalization** regardless of original format:

#### **XML C14N + XMLDSig Strategy:**
```java
// JSON→XML scenario:
Map<String, String> businessKvps = extractKvps(jsonMessage);
String xmlFormat = convertKvpsToXml(businessKvps);  // Convert to XML
String canonicalXml = XML_C14N_1_1(xmlFormat);      // Always use XML C14N
String hash = SHA256(canonicalXml);

// XML→JSON scenario:
Map<String, String> businessKvps = extractKvps(xmlMessage);
String xmlFormat = convertKvpsToXml(businessKvps);  // Already XML
String canonicalXml = XML_C14N_1_1(xmlFormat);      // Same XML C14N
String hash = SHA256(canonicalXml);

// Result: Same hash, same signature validation!
```

#### **RFC 8785 + JWS Strategy:**
```java
// XML→JSON scenario:
Map<String, String> businessKvps = extractKvps(xmlMessage);
String jsonFormat = convertKvpsToJson(businessKvps);  // Convert to JSON
String canonicalJson = RFC_8785(jsonFormat);          // Always use RFC 8785
String hash = SHA256(canonicalJson);

// JSON→XML scenario:
Map<String, String> businessKvps = extractKvps(jsonMessage);
String jsonFormat = convertKvpsToJson(businessKvps);  // Already JSON
String canonicalJson = RFC_8785(jsonFormat);          // Same RFC 8785
String hash = SHA256(canonicalJson);

// Result: Same hash, same signature validation!
```

### **Why Hybrid/Detached Hash Fails:**

The Hybrid strategy uses **format-specific canonicalization** without conversion:

```java
// JSON→XML scenario:
Map<String, String> businessKvps = extractKvps(jsonMessage);
String canonicalJson = RFC_8785(convertKvpsToJson(businessKvps));  // JSON canonicalization
String hash1 = SHA256(canonicalJson);

// XML→JSON scenario:
Map<String, String> businessKvps = extractKvps(xmlMessage);
String canonicalXml = XML_C14N_1_1(convertKvpsToXml(businessKvps));  // XML canonicalization
String hash2 = SHA256(canonicalXml);

// Result: Different hashes, signature validation fails!
```

### **Production Recommendation**

**For ISO 20022 cross-format message signing:**

1. **✅ Use XML C14N + XMLDSig** - Most reliable, established W3C standard
2. **✅ Use RFC 8785 + JWS** - Modern IETF standard, excellent for JSON-first workflows
3. **❌ Avoid Hybrid/Detached Hash** - Not suitable for cross-format scenarios

**The key insight:** Choose a strategy that uses **consistent canonicalization method** across all format conversions, not format-specific canonicalization.

## Architectural Insight: Parser Design Enables Standardized Cross-Format Behavior

### **Critical Role of Iso20022KvpParser**

The standardized cross-format behavior demonstrated by XML C14N + XMLDSig and RFC 8785 + JWS strategies is **fundamentally enabled** by the project's `Iso20022KvpParser` implementation.

### **Why Consistent Parser Design is Essential**

#### **1. Consistent Business Data Extraction**
```java
// All three strategies use the same parser
Iso20022KvpParser kvpParser = new Iso20022KvpParser();

// XML format
Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlMessage);

// JSON format  
Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonMessage);

// Result: Identical 29 KVPs regardless of format
assertEquals(xmlKvps, jsonKvps); // Always true
```

#### **2. Format-Agnostic Business Logic**
- **Same extraction logic** for both XML and JSON
- **Identical field mapping** using ISO 20022 glossary
- **Consistent key naming** across formats
- **Predictable output** regardless of input format

#### **3. Enables Canonicalization Strategy**
```java
// Strategy can choose canonicalization method independently
Map<String, String> businessKvps = kvpParser.extractKvps(message);

// XML C14N + XMLDSig: Always convert to XML
String xmlFormat = convertKvpsToXml(businessKvps);
String canonicalXml = XML_C14N_1_1(xmlFormat);

// RFC 8785 + JWS: Always convert to JSON  
String jsonFormat = convertKvpsToJson(businessKvps);
String canonicalJson = RFC_8785(jsonFormat);
```

### **Current Implementation Approach**

The project currently uses a **static parser implementation** with the following characteristics:

#### **1. ISO 20022 Glossary Integration**
```java
// Static mapping ensures consistency
private static final Map<String, String> ISO_20022_GLOSSARY = new HashMap<>();
static {
    ISO_20022_GLOSSARY.put("BizMsgIdr", "BusinessMessageIdentifier");
    ISO_20022_GLOSSARY.put("MsgDefIdr", "MessageDefinitionIdentifier");
    // ... comprehensive mapping
}
```

#### **2. Flexible Canonicalization Strategy**
```java
// Parser provides format-agnostic business data
Map<String, String> businessKvps = kvpParser.extractKvps(message);

// Strategy can apply any canonicalization method
if (strategy == XML_C14N) {
    return canonicalizeAsXml(businessKvps);
} else if (strategy == RFC_8785) {
    return canonicalizeAsJson(businessKvps);
}
```

#### **3. Testability and Validation**
```java
// Easy to test extraction consistency
@Test
void testCrossFormatExtraction() {
    Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
    Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
    
    // Should be identical for same business data
    assertEquals(xmlKvps, jsonKvps);
}
```

### **Future Enhancement Considerations**

The current static implementation provides a solid foundation, but future enhancements could include:

#### **1. Dynamic ISO e-Repository Integration**
- **Automatic field mapping** from ISO e-Repository
- **Support for new message types** without code changes
- **Real-time schema updates** as ISO standards evolve

#### **2. Enhanced Flexibility**
- **Runtime message type detection**
- **Automatic field extraction** for any ISO 20022 message
- **Dynamic canonicalization** based on message structure

#### **3. Implementation Considerations**
- **Performance optimization** for dynamic parsing
- **Caching strategies** for frequently used schemas
- **Fallback mechanisms** for offline operation

### **Conclusion**

The current `Iso20022KvpParser` design **directly enables** the successful cross-format signature validation demonstrated by XML C14N + XMLDSig and RFC 8785 + JWS strategies. The consistent extraction of business data KVPs provides the foundation for reliable cross-format canonicalization.

Future enhancements could expand this capability to handle the full ISO 20022 message universe dynamically, but the current implementation already provides the essential functionality needed for robust cross-format signature validation.

## Test Plan

### Directory Structure
```
/src/test/java
  /xmlsig  → XMLDSig tests
  /jws     → JSON Canonical + JWS tests
  /hybrid  → Detached-hash tests
/resources
  sample.xml
  sample.json
README.md
```

### Test Cases
For **each** strategy:
1. **Sign** sample XML
2. **Convert** signed XML → JSON, ensure header signature persists
3. **Verify** signature in JSON
4. **Convert** signed JSON → XML, ensure header signature persists
5. **Verify** signature in XML

### Tools & Commands
- **Gradle**: Use `gradle` command directly in CursorAI IDE Terminal
  - Run strategy tests: `gradle test --tests "*StrategyTest"`
  - Run all tests: `gradle test`
- **Docker**: Use `dev.ps1` script for building and running tests in containers
- **CursorAI**: Use provided Java classes and sample files to autotest conversion & verification

## Development Environment

### Prerequisites
- Windows 11 with Docker Desktop installed and running
- CursorAI IDE (recommended for development)
- Git for Windows

### Docker Configuration
This project is configured to run in Windows Docker Desktop containers. All build processes, tests, and development tasks are containerized to ensure consistency across development environments.

Key points:
- Uses Windows Docker Desktop for container management
- All Java/Gradle operations run inside containers
- No local Java or Gradle installation required
- Development script (`dev.ps1`) handles Docker operations
- Container configuration optimized for Windows filesystem

### IDE Integration
The project is set up for development in CursorAI IDE with:
- Docker Desktop integration
- Remote debugging support (port 5005)
- PowerShell script integration
- Gradle project recognition

### Docker-Based Development Setup

This project uses Docker for development to ensure consistency across all environments. The setup includes:

1. **Development Container**
   - Java 17 (Eclipse Temurin)
   - Gradle 8.6
   - PowerShell
   - All project dependencies

2. **Container Structure**
   - Multi-stage build for optimized production images
   - Development container with full tooling
   - Debug container with remote debugging support
   - Health checks for container monitoring

3. **Available Commands**
   ```powershell
   .\dev.ps1 start    # Start the development container
   .\dev.ps1 stop     # Stop the development container
   .\dev.ps1 build    # Build the project
   .\dev.ps1 test     # Run tests
   .\dev.ps1 clean    # Clean build files
   .\dev.ps1 shell    # Open a shell in the dev container
   .\dev.ps1 help     # Show all available commands
   ```

4. **Container Services**
   - `app`: Main application container
   - `app-debug`: Debug-enabled container (port 5005)
   - `dev`: Development container with full tooling

5. **Volume Management**
   - Source code mounted at `/app`
   - Gradle cache persisted in `gradle-cache` volume
   - Hot-reload support for development

6. **Remote Debugging**
   - Debug port: 5005
   - Connect your IDE to `localhost:5005`
   - Use `app-debug` service for debugging

### Getting Started

1. **Prerequisites**
   - Windows 11 with Docker Desktop installed
   - Docker Desktop configured for Windows containers
   - PowerShell 7+ (included in container)

2. **Initial Setup**
   ```powershell
   # Start the development environment
   .\dev.ps1 start

   # Build the project
   .\dev.ps1 build

   # Run tests
   .\dev.ps1 test
   ```

3. **Development Workflow**
   - Start the container: `.\dev.ps1 start`
   - Get a shell: `.\dev.ps1 shell`
   - Build changes: `gradle build`
   - Run strategy tests: `gradle test --tests "*StrategyTest"`
   - Stop when done: `.\dev.ps1 stop`

4. **Debugging**
   - Start debug container: `docker-compose up app-debug`
   - Connect IDE to `localhost:5005`
   - Set breakpoints in your IDE
   - Run tests or application to hit breakpoints

### Container Configuration

1. **Development Container (Dockerfile)**
   ```dockerfile
   FROM eclipse-temurin:17-jdk-windowsservercore-ltsc2022
   WORKDIR /app
   # ... (see Dockerfile for full configuration)
   ```

2. **Docker Compose Services**
   ```yaml
   services:
     app:
       build: 
         context: .
         target: builder
     app-debug:
       build: 
         context: .
         target: builder
     dev:
       build:
         context: .
         dockerfile: Dockerfile
   ```

3. **Volume Configuration**
   ```yaml
   volumes:
     gradle-cache:
       name: tsg-crossmsg-signing-gradle-cache
   ```

### Best Practices

1. **Development**
   - Always use the development container for coding
   - Keep the container running during development
   - Use the provided scripts for all operations
   - Commit the Gradle wrapper to version control

2. **Testing**
   - Run strategy tests: `gradle test --tests "*StrategyTest"`
   - Run all tests: `gradle test`
   - Use debug container for test debugging
   - Check test reports in `build/reports/tests`

3. **Building**
   - Use `gradle build` for consistent builds
   - Clean builds with `gradle clean`
   - Check build reports in `build/reports`

4. **Container Management**
   - Start/stop containers as needed
   - Use `docker-compose ps` to check status
   - Monitor container health with `docker-compose ps`

### Troubleshooting

1. **Common Issues**
   - Container won't start: Check Docker Desktop status
   - Build fails: Try `.\dev.ps1 clean` first
   - Tests fail: Check container logs
   - Debug not connecting: Verify port 5005 is free

2. **Container Logs**
   ```powershell
   # View container logs
   docker-compose logs dev

   # Follow logs
   docker-compose logs -f dev
   ```

3. **Resource Management**
   - Monitor container resources in Docker Desktop
   - Clean up unused containers: `docker-compose down`
   - Clear Gradle cache if needed: `docker volume rm tsg-crossmsg-signing-gradle-cache`

## Conventions & Terminology
- **Canonical Form**: the exact byte stream inputs to hash or signature
- **Signature Container**: header field or XML element carrying the signature
- **Signer**: originator; **Verifier**: any intermediary or final recipient

## References
- W3C C14N 1.1: https://www.w3.org/TR/xml-c14n11/
- JSON Canonicalization RFC 8785: https://datatracker.ietf.org/doc/html/rfc8785
- JWS (RFC 7515): https://datatracker.ietf.org/doc/html/rfc7515
- ISO 20022 AppHdr spec (head.001.001.02)

## Next Steps
1. Set up automated documentation checks
2. Implement Phase 1 signature strategies
3. Document implementation details
4. Begin Phase 2 hybrid implementation planning
5. Create comprehensive quantum transition documentation

### Message Format Conversion
The project includes ISO 20022 message samples and conversion documentation:
- XML samples:
  - `src/test/resources/iso/SinglePriority_Inbound_pacs.008.xml`
  - `src/test/resources/iso/SinglePriority_Outbound_pacs.008.xml`
- JSON sample:
  - `src/test/resources/iso/SinglePriority_Inbound-pacs008.json`
- Schema conversion guide:
  - `src/test/resources/iso/ISO 20022 - JSON Schema Draft 2020-12 generation (v20250321) (clean).docx`

The conversion process follows these key principles:
1. **XML to JSON**:
   - Strip XML prefixes
   - Convert currency elements to `{"amt","Ccy"}` format
   - Transform elements to objects/arrays
   - Preserve namespace information in JSON structure
   - Handle both inbound and outbound message formats

2. **JSON to XML**:
   - Reverse mapping to produce identical XML structure
   - Maintain canonicalization compatibility
   - Preserve namespace declarations
   - Ensure XML schema validation
   - Support bidirectional conversion

### Test Configuration
The project uses Gradle with comprehensive test configuration:
- JUnit 5 for test framework
- JaCoCo for code coverage
- Parallel test execution
- Detailed test reporting
- 5-minute test timeout
- Memory-optimized test execution

Test reports are available in:
- HTML: `build/reports/tests/html/index.html`
- XML: `build/reports/tests/xml/`
- Coverage: `build/reports/jacoco/html/index.html`

### Running Tests
To run tests in the CursorAI IDE Terminal:

#### Focused Unit Tests with TestInfrastructure Singleton (Recommended)
```bash
# Run all focused KVP parser tests with TestInfrastructure Singleton
gradle test --tests "*Iso20022KvpParser*Test"

# Run specific direction tests
gradle test --tests "*XmlToJson*Test"    # XML to JSON direction tests
gradle test --tests "*JsonToXml*Test"    # JSON to XML direction tests

# Run specific strategy tests
gradle test --tests "*XmlC14n*Test"      # XML C14N + XMLDSig tests
gradle test --tests "*Jws*Test"          # JSON Canonicalization + JWS tests
gradle test --tests "*Hybrid*Test"       # Hybrid/Detached Hash tests

# Run direct comparison test
gradle test --tests "*DirectComparison*Test"

# Run project setup test
gradle test --tests "*ProjectSetup*Test"
```

#### All Tests with Verbose Output
```bash
# Run all tests with maximum verbosity
gradle test --info --debug

# Run all tests with standard verbosity
gradle test --info
```

### ISO Message Testing Strategy

#### Focused Unit Test Approach with TestInfrastructure Singleton (Updated 2024-06)
The project now uses a **cleaner, more focused approach** that eliminates format conversion complexity and focuses on genuine payment data integrity. Instead of converting between XML and JSON formats (which introduced conversion errors), the tests use **matching ISO 20022 message pairs** that contain identical payment data in different syntax formats. The approach uses **TestInfrastructure Singleton** to provide superior architecture and shared functionality.

#### Seven Focused Unit Tests with TestInfrastructure Singleton
The test suite consists of seven focused unit tests, each testing one specific responsibility with no redundancy:

1. **XML to JSON - XML C14N + XMLDSig** (`Iso20022KvpParserXmlToJsonXmlC14nTest.java`):
   - Tests XML C14N 1.1 canonicalization on XML format
   - Tests XMLDSig signature generation and verification
   - Verifies KVP extraction and preservation
   - Validates signature exclusion principle

2. **JSON to XML - XML C14N + XMLDSig** (`Iso20022KvpParserJsonToXmlXmlC14nTest.java`):
   - Tests XML C14N + XMLDSig strategy on JSON-derived data
   - Verifies KVP consistency between JSON and XML formats
   - Tests signature exclusion principle on JSON data

3. **XML to JSON - JSON Canonicalization + JWS** (`Iso20022KvpParserXmlToJsonJwsTest.java`):
   - Tests RFC 8785 JSON canonicalization on XML-derived data
   - Tests JWS signature generation and verification
   - Verifies KVP extraction and preservation

4. **JSON to XML - JSON Canonicalization + JWS** (`Iso20022KvpParserJsonToXmlJwsTest.java`):
   - Tests RFC 8785 + JWS strategy on JSON format
   - Verifies KVP consistency between formats
   - Tests signature exclusion principle

5. **XML to JSON - Hybrid/Detached Hash** (`Iso20022KvpParserXmlToJsonHybridTest.java`):
   - Tests SHA-256 digest computation on XML format
   - Tests RSA signature generation and verification
   - Verifies detached hash signature principles

6. **JSON to XML - Hybrid/Detached Hash** (`Iso20022KvpParserJsonToXmlHybridTest.java`):
   - Tests Hybrid/Detached Hash strategy on JSON format
   - Verifies KVP consistency between formats
   - Tests signature exclusion principle

#### Key Benefits of New Approach

1. **Eliminates Conversion Complexity**: No more format conversion between XML and JSON
2. **Focuses on Genuine Payment Data**: Tests extract and compare actual payment KVPs, ignoring structural elements
3. **Strict Separation of Concerns**: Each test has a single, well-defined responsibility with no redundancy
4. **Consistent Test Structure**: All tests follow the same pattern for easy maintenance
5. **Validates KVP Parser**: Confirms correct filtering of structural elements vs. payment data
6. **TestInfrastructure Singleton**: Superior architecture using Singleton Principle for shared functionality
7. **Preserved All Tests**: No valuable tests were removed, only improved with better architecture

#### Test Scenarios

1. **KVP Extraction Tests**:
   ```java
   @Test
   @Tag("unit")
   void testKvpExtractionFromMatchingFiles() {
       // Load both XML and JSON samples directly
       // Extract KVPs from both formats
       // Verify identical payment data
       // Validate structural elements are ignored
   }
   ```

2. **Signature Strategy Tests**:
   ```java
   @Test
   @Tag("unit")
   void testSignatureStrategyOnFormat() {
       // Test specific signature strategy on format
       // Verify signature generation and verification
       // Validate KVP preservation through signing
   }
   ```

3. **Signature Exclusion Tests**:
   ```java
   @Test
   @Tag("unit")
   void testSignatureExclusionPrinciple() {
       // Verify signature is excluded from digest calculation
       // Validate signature exclusion principle compliance
   }
   ```

4. **Real-World Scenario Tests**:
   ```java
   @Test
   @Tag("unit")
   void testRealWorldScenario() {
       // Test complete flow: format → signature → conversion → validation
       // Verify signature preservation across format conversions
       // Validate backward compatibility for signature strategies
   }
   ```

**Note**: Tampering detection is reserved for integration/UI testing scenarios to maintain separation of concerns and focus on backward compatibility validation.

#### Project Setup Test
The following test validates basic project configuration:

1. **Project Setup Test** (`ProjectSetupTest.java`):
   - Validates key pair generation
   - Ensures cryptographic algorithms are available
   - Verifies basic project dependencies

#### Test Data Management

1. **Sample Loading**:
   - Samples are loaded from `src/test/resources/iso/` directory
   - Cached in memory for test performance
   - Validated against schema before use
   - Namespace declarations preserved

2. **Test Categories**:
   - `@Tag("unit")`: Individual component tests
   - `@Tag("integration")`: End-to-end flow tests
   - `@Tag("slow")`: Performance-intensive tests

3. **Test Resources**:
   - XML samples for inbound/outbound flows
   - JSON converted samples
   - Schema validation files
   - Test key pairs and certificates

4. **Validation Steps**:
   - Schema compliance
   - Namespace preservation
   - Signature validity
   - Format conversion accuracy
   - Header integrity

#### Test Execution

1. **Local Development**:
   ```powershell
   .\dev.ps1 test                    # Run all tests
   .\dev.ps1 test --tests *Xml*      # Run XML-specific tests
   .\dev.ps1 test --tests *Json*     # Run JSON-specific tests
   ```

2. **Docker Environment**:
   - Tests run in isolated containers
   - Consistent environment across platforms
   - Resource limits enforced
   - Reports accessible via volume mounts

3. **Continuous Integration**:
   - Automated test execution
   - Coverage reporting
   - Test result archiving
   - Performance monitoring

## Change History

### 1.2.0 - 2024-06-XX
- **Change**: Major test suite refactoring to implement cleaner, granular unit test approach with TestInfrastructure Singleton
- **Reason**: Eliminate format conversion complexity, focus on genuine payment data integrity, and apply superior architecture using Singleton Principle
- **Impact**: Seven focused unit tests with TestInfrastructure Singleton replace complex integration tests, cleaner test execution, preserved all valuable tests
- **Migration**: Use new focused tests (`*Iso20022KvpParser*Test`) with TestInfrastructure Singleton instead of legacy integration tests

### 1.1.0 - 2024-06-XX
- **Change**: Major update for containerized Gradle-based development, strict layer separation, and comprehensive test strategy
- **Reason**: Align documentation with new codebase and workflow
- **Impact**: All users and contributors must use the containerized workflow and follow updated test/documentation policies
- **Migration**: See updated README and docs for new workflow

### 1.0.0 - 2024-03-19
- **Change**: Initial system design documentation
- **Reason**: Project initialization
- **Impact**: Establishes baseline architecture
- **Migration**: N/A

## Auditability & Traceability

All signature strategies in this project produce detailed, auditable output files during signing and verification. **Every output file includes a `Strategy:` identifier** in its header, making it clear which cryptographic approach was used for each operation. This is critical for standards compliance, global interoperability, and for the ISO TSG committee to evaluate and recommend solutions.

### Example Output Header

```
Process ID: 3
Timestamp: 20250608_110344
Operation: after_id_set
Strategy: XMLDSig (C14N 1.1 + XMLDSig)
Canonicalized Content:
---------------------
... (canonicalized XML here) ...
```

Or for JSON/JWS:
```
Process ID: 7
Timestamp: 20250608_110344
Operation: signing
Strategy: JWS (RFC 8785 Canonical JSON + JWS)
Key-Value Pairs:
----------------
... (key-value pairs here) ...
```

Or for Hybrid/Detached Hash:
```
Process ID: 12
Timestamp: 20250608_110344
Operation: digest
Strategy: Hybrid (Detached Hash + Signature)
Digest:
-------
... (digest value here) ...
```

### Why This Matters
- **Auditability:** Every output is traceable to the cryptographic method used.
- **Interoperability:** Facilitates cross-strategy and cross-format validation.
- **Standards Compliance:** Enables ISO TSG and other bodies to evaluate, compare, and recommend strategies for real-world, global use.
- **Integration Testing:** End-to-end and integration tests always include the correct strategy label in their outputs.

### Strategy Labels Used
- `XMLDSig (C14N 1.1 + XMLDSig)`
- `JWS (RFC 8785 Canonical JSON + JWS)`
- `Hybrid (Detached Hash + Signature)`

**All strategy classes implement a `getStrategyLabel()` method, ensuring this pattern is consistent and future-proof.**

> **Critical Security Principle: Signature Exclusion**
>
> For **all digital signature strategies**, the signature or digest must always be calculated over the message content **excluding the signature itself**. This is a universal requirement:
> - For **XMLDSig**, this is enforced by using the *enveloped signature transform* (`http://www.w3.org/2000/09/xmldsig#enveloped-signature`), which ensures the `<Signature>` element is excluded from the digest calculation.
> - For **JWS (JSON Web Signature)**, the canonicalization and signing are performed over the JSON object **without** the signature property.
> - For **Hybrid/Detached Hash**, the digest is always computed over the message, and the signature is stored separately.
>
> **If this rule is not followed, signature validation will always fail.** This project enforces this rule in all strategies and makes it auditable in every output.

> **Project Focus & Philosophy**
>
> This repository is a **test harness and standards exploration tool**—not a production system. Its purpose is to rigorously evaluate, compare, and provide auditable evidence for digital signature strategies that can guarantee end-to-end integrity of payment messages as they traverse multiple agents and are converted between different syntax formats (such as XML and JSON). This is especially critical in scenarios where there is **no central authority** responsible for message security, as is increasingly the case in global, cross-border, and open banking environments.
>
> **Why does this matter?**
>
> - The ISO community and the global payments industry are moving toward **interoperable message syntax types** (XML, JSON, etc.).
> - There is a critical gap: how can we ensure that payment messages remain untampered and trustworthy as they are converted and relayed between independent agents, without relying on a single trusted intermediary?
> - This project provides a **transparent, auditable, and comparative framework** for testing and proving which cryptographic strategies (XMLDSig, JWS, Hybrid, etc.) actually work in these real-world, cross-format, multi-hop scenarios.
> - The outputs—canonicalized content, key-value pairs, digests, and validation results—are all labeled, logged, and designed for **maximum auditability and standards evaluation**.
> - **All strategies strictly enforce the rule that the signature/digest is calculated over the message content excluding the signature itself.**
> - The ultimate goal is to provide the **evidence and methodology** needed for the ISO TSG and other standards bodies to recommend a globally approved, interoperable, and efficient approach to payment message integrity.
>
> **This project is not about building a production system.** It is about providing the proof, transparency, and insight needed to close a crucial standards gap and enable trust, interoperability, and development ROI for the next generation of cross-border payments.

