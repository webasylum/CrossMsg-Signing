# CrossMsg-Signing WebUI Restoration Plan

## Current State Analysis

### ✅ What's Working (Preserve These)
1. **Frontend React Application**
   - MessageEditor component with XML/JSON tabs
   - SignaturePanel with strategy buttons
   - ResultViewer for displaying KVP comparisons
   - TamperAlert for security notifications
   - Proper state management and API integration

2. **Backend Spring Boot Application**
   - MessageController with all endpoints
   - SignatureService with KVP comparison logic
   - Proper CORS configuration
   - Rate limiting implementation
   - Logging service

3. **Infrastructure**
   - Docker Compose configuration
   - Nginx reverse proxy setup
   - Health check endpoints
   - Security configurations

### ❌ What Needs to be Fixed (Integration Points)

1. **Parser Integration Issue**
   - **Current**: WebUI has simplified `Iso20022KvpParser` (basic implementation)
   - **Target**: Integrate main project's sophisticated parser (1000+ lines with ISO e-Repository)
   - **Impact**: KVP extraction accuracy and consistency

2. **Signature Strategy Integration**
   - **Current**: Mock signature implementations
   - **Target**: Real signature strategies from main project
   - **Impact**: Actual signature generation and validation

3. **Test Infrastructure**
   - **Current**: Basic tests
   - **Target**: Comprehensive test suite from main project
   - **Impact**: Reliability and validation

## Step-by-Step Restoration Process

### Phase 1: Parser Integration (Priority 1)
1. **Backup Current Working State**
   ```bash
   # Create backup of current working state
   cp -r CrossMsg-Signing-WebUI CrossMsg-Signing-WebUI-working-backup-$(date +%Y%m%d-%H%M%S)
   ```

2. **Replace WebUI Parser with Main Project Parser**
   - Copy `src/main/java/com/tsg/crossmsg/signing/model/Iso20022KvpParser.java` from main project
   - Update package imports in WebUI
   - Ensure all dependencies are included in `build.gradle`

3. **Update SignatureService**
   - Integrate main project's signature strategies
   - Replace mock implementations with real ones
   - Maintain existing API contracts

### Phase 2: Strategy Integration (Priority 2)
1. **Copy Signature Strategies**
   - `XmlC14nSignatureStrategy`
   - `Rfc8785SignatureStrategy` 
   - `HybridSignatureStrategy`

2. **Update Dependencies**
   - Add missing dependencies to `build.gradle`
   - Ensure all required libraries are available

### Phase 3: Test Integration (Priority 3)
1. **Copy Test Infrastructure**
   - `TestInfrastructure` singleton
   - `TestKeyManager` utilities
   - Comprehensive test cases

2. **Update Test Configuration**
   - Ensure test resources are properly located
   - Update test paths and configurations

### Phase 4: Validation & Testing (Priority 4)
1. **End-to-End Testing**
   - Test KVP comparison functionality
   - Verify signature generation
   - Validate frontend-backend integration

2. **Performance Testing**
   - Ensure no regression in response times
   - Validate rate limiting still works

## Critical Success Factors

1. **Preserve Working Frontend**: Don't break the React UI that's working
2. **Maintain API Contracts**: Keep existing endpoints working
3. **Incremental Integration**: Test each phase before proceeding
4. **Rollback Capability**: Keep backups for each phase

## Environment Context
- **OS**: Windows 11 WSL2 Ubuntu
- **Development**: Docker Desktop with connected CursorAI IDE
- **Container**: Dev container environment
- **Architecture**: Microservices with nginx reverse proxy

## Validation Checklist

### Pre-Restoration
- [ ] Frontend loads and displays messages
- [ ] Backend responds to health checks
- [ ] KVP comparison endpoint works (basic)
- [ ] Docker services start successfully

### Post-Restoration
- [ ] Parser extracts same KVPs as main project
- [ ] Signature strategies work correctly
- [ ] Frontend displays accurate results
- [ ] All tests pass
- [ ] Performance is acceptable

## Risk Mitigation

1. **Incremental Approach**: Test each component after integration
2. **Feature Flags**: Use configuration to enable/disable new features
3. **Monitoring**: Add logging to track integration success
4. **Rollback Plan**: Keep working state backups

## Timeline Estimate

- **Phase 1**: 2-3 hours (Parser integration)
- **Phase 2**: 1-2 hours (Strategy integration)  
- **Phase 3**: 1-2 hours (Test integration)
- **Phase 4**: 1-2 hours (Validation)
- **Total**: 5-9 hours with testing

## Success Criteria

1. **Functional**: KVP comparison works with main project's accuracy
2. **Performance**: No degradation in response times
3. **Reliability**: All existing functionality preserved
4. **Maintainability**: Code follows main project patterns 