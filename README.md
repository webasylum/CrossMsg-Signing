# Payment Message Signature Strategies Test Suite

**Author:** Martin Sansone (martin@web-asylum.com)

## Overview

This repository provides a comprehensive, containerized test harness for evaluating signature strategies that ensure end-to-end integrity of ISO 20022 pacs.008 payment messages across format conversions (XML ↔ JSON) in a low-trust, multi-hop payment infrastructure.

## What's New (2024-06)
- **Containerized Development:** All builds, tests, and development tasks run inside Docker containers for consistency and reproducibility.
- **Gradle Build System:** Replaces Maven for faster, more flexible builds and test execution.
- **Strict Layer Separation:** Code is organized by infrastructure, service, script, and test layers, with dependency injection and constructor-based initialization.
- **Environment-Driven Configuration:** All configuration is via environment variables, loaded from `.env` files. No hardcoded values.
- **Comprehensive Testing:**
  - All unit and integration tests use JUnit 5, with a 5-minute timeout and resource cleanup.
  - Tests are run inside the container using Gradle (`./dev.ps1 test` or `./gradlew test`).
  - Test coverage includes canonicalization, signature persistence across format conversion, and negative cases (tampering, wrong key, etc.).
  - Test results and coverage reports are available in `build/reports/`.
- **Signature Strategies:**
  - XML C14N + XMLDSig (W3C C14N 1.1, Apache Santuario)
  - JSON Canonicalization (RFC 8785) + JWS (Nimbus JOSE + JWT)
  - Hybrid/Detached Hash (SHA-256, BouncyCastle/JCA)
- **Documentation Policy:** All code changes must be accompanied by documentation updates. Documentation is versioned and checked alongside code.

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
- **Gradle**: `./gradlew test` or use the provided `dev.ps1` script
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
   - Build changes: `.\dev.ps1 build`
   - Run tests: `.\dev.ps1 test`
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
   - Run tests in the container: `.\dev.ps1 test`
   - Use debug container for test debugging
   - Check test reports in `build/reports/tests`

3. **Building**
   - Use `.\dev.ps1 build` for consistent builds
   - Clean builds with `.\dev.ps1 clean`
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
  - `iso/SinglePriority_Inbound_pacs.008.xml`
  - `iso/SinglePriority_Outbound_pacs.008.xml`
- JSON sample:
  - `iso/SinglePriority_Inbound-pacs008.json`
- Schema conversion guide:
  - `iso/ISO 20022 - JSON Schema Draft 2020-12 generation (v20250321) (clean).docx`

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

### ISO Message Testing Strategy

#### Sample Message Usage
The ISO message samples are used to validate signature strategies across different scenarios:

1. **XML Signature Tests** (`xmlsig/XmlSignatureTest.java`):
   - Test signing of `SinglePriority_Inbound_pacs.008.xml`
   - Test signing of `SinglePriority_Outbound_pacs.008.xml`
   - Verify signatures after XML→JSON→XML conversion
   - Validate namespace preservation during signing
   - Test canonicalization with different XML structures

2. **JWS Signature Tests** (`jws/JwsSignatureTest.java`):
   - Test signing of converted JSON messages
   - Verify JWS signatures after JSON→XML→JSON conversion
   - Validate RFC 8785 canonicalization
   - Test signature verification with different key types
   - Verify header preservation during conversion

3. **Hybrid Signature Tests** (`hybrid/HybridSignatureTest.java`):
   - Test detached hash generation for both formats
   - Verify hash signatures after format conversion
   - Validate hash consistency across conversions
   - Test hybrid signature verification
   - Verify header placement in both formats

#### Test Scenarios

1. **Format Conversion Tests**:
   ```java
   @Test
   @Tag("integration")
   void testXmlToJsonConversion() {
       // Load XML sample
       // Convert to JSON
       // Verify structure matches JSON sample
       // Validate namespace handling
   }
   ```

2. **Signature Persistence Tests**:
   ```java
   @Test
   @Tag("integration")
   void testSignaturePersistence() {
       // Sign XML message
       // Convert to JSON
       // Convert back to XML
       // Verify signature remains valid
   }
   ```

3. **Canonicalization Tests**:
   ```java
   @Test
   @Tag("unit")
   void testXmlCanonicalization() {
       // Load XML sample
       // Apply C14N
       // Verify canonical form
       // Test signature generation
   }
   ```

#### Test Data Management

1. **Sample Loading**:
   - Samples are loaded from `iso/` directory
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

