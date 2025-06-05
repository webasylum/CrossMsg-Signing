# Quantum-Safe Transition Strategy

## Overview
This document outlines the strategy for transitioning to quantum-safe algorithms in the ISO 20022 message signing system.

## Content

### Current State
- Classical cryptography (RSA/ECDSA)
- XML and JSON message formats
- Multiple signature strategies
- **Test harness and codebase are designed to support hybrid and quantum-safe algorithms as they are added.**

### Transition Phases

#### Phase 1: Classical Implementation
- Current standards (RSA/ECDSA)
- Focus on compatibility
- Performance optimization

#### Phase 2: Hybrid Implementation
- Integration of quantum-safe algorithms
- Support for both classical and quantum-safe signatures
- Backward compatibility
- **Containerized test harness and modular codebase enable rapid integration and testing of new algorithms.**

#### Phase 3: Full Quantum-Safe Implementation
- Pure quantum-safe algorithms
- Complete transition from classical cryptography
- Performance optimization for quantum-safe operations

### Quantum-Safe Algorithm Candidates

#### Digital Signatures
1. CRYSTALS-Dilithium
   - Lattice-based
   - NIST PQC finalist
   - Good performance characteristics

2. Falcon
   - Lattice-based
   - Compact signatures
   - Efficient verification

3. SPHINCS+
   - Hash-based
   - Conservative security
   - Larger signature size

#### Hash Functions
1. SHA-3 (Keccak)
   - Current standard
   - Good quantum resistance
   - Well-studied

2. BLAKE3
   - Modern design
   - High performance
   - Strong security properties

## Implementation Details

### Performance Impact
- Signature size increase
- Generation time
- Verification overhead

### Compatibility
- Message format changes
- Signature container modifications
- Backward compatibility requirements

### Security
- Key management
- Algorithm security
- Implementation security

## Usage Examples
[To be added with implementation]

## Configuration
[To be added with implementation]

## Dependencies
- Quantum-safe cryptography libraries
- Classical cryptography libraries
- Testing frameworks
- Performance monitoring tools
- **Containerized test harness and Gradle-based workflow**

## Change History

### 1.1.0 - 2024-06-XX
- **Change**: Confirmed readiness for hybrid/quantum-safe integration and test harness extensibility
- **Reason**: Align documentation with new codebase and workflow
- **Impact**: All users and contributors must use the containerized workflow and follow updated test/documentation policies
- **Migration**: See updated README and docs for new workflow

### 1.0.0 - 2024-03-19
- **Change**: Initial quantum transition strategy
- **Reason**: Project initialization
- **Impact**: Establishes quantum-safe roadmap
- **Migration**: N/A

## Deprecated Features
None at this time

## Future Considerations
- Algorithm standardization
- Performance optimization
- Implementation complexity
- Compatibility issues

## References
- NIST PQC Project
- CRYSTALS-Dilithium Specification
- Falcon Specification
- SPHINCS+ Specification

---
**Note**: This document follows the [Documentation Preservation Policy](./documentation-policy.md). All changes must maintain existing content and include proper version history. 