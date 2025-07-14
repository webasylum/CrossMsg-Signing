#!/bin/bash

# Generate Test Keys and Certificates for ISO 20022 Signature Strategy Testing
# This script generates all required keys and certificates for testing purposes only

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_KEYS_DIR="$SCRIPT_DIR/../test-keys"

echo "🔐 Generating test keys and certificates for ISO 20022 signature strategy testing..."

# Create test-keys directory if it doesn't exist
mkdir -p "$TEST_KEYS_DIR"

# Function to check if OpenSSL is available
check_openssl() {
    if ! command -v openssl &> /dev/null; then
        echo "❌ Error: OpenSSL is required but not installed."
        echo "Please install OpenSSL and try again."
        exit 1
    fi
}

# Function to generate RSA key pair and certificate
generate_rsa_keys() {
    echo "📝 Generating RSA 2048-bit key pair and certificate..."
    
    # Generate RSA private key
    openssl genrsa -out "$TEST_KEYS_DIR/rsa_private.pem" 2048
    
    # Generate RSA public key
    openssl rsa -in "$TEST_KEYS_DIR/rsa_private.pem" -pubout -out "$TEST_KEYS_DIR/rsa_public.pem"
    
    # Generate self-signed certificate for XMLDSig testing
    openssl req -new -x509 -key "$TEST_KEYS_DIR/rsa_private.pem" \
        -out "$TEST_KEYS_DIR/rsa_cert.pem" \
        -days 365 \
        -subj "/C=US/ST=Test/L=Test/O=ISO20022-Test/OU=Testing/CN=Test-Certificate"
    
    echo "✅ RSA keys and certificate generated"
}

# Function to generate EC key pair
generate_ec_keys() {
    echo "📝 Generating EC (P-256) key pair..."
    
    # Generate EC private key
    openssl ecparam -genkey -name prime256v1 -out "$TEST_KEYS_DIR/ec_private.pem"
    
    # Generate EC public key
    openssl ec -in "$TEST_KEYS_DIR/ec_private.pem" -pubout -out "$TEST_KEYS_DIR/ec_public.pem"
    
    echo "✅ EC keys generated"
}

# Function to generate Ed25519 key pair
generate_ed25519_keys() {
    echo "📝 Generating Ed25519 key pair..."
    
    # Generate Ed25519 private key
    openssl genpkey -algorithm Ed25519 -out "$TEST_KEYS_DIR/ed25519_private.pem"
    
    # Generate Ed25519 public key
    openssl pkey -in "$TEST_KEYS_DIR/ed25519_private.pem" -pubout -out "$TEST_KEYS_DIR/ed25519_public.pem"
    
    echo "✅ Ed25519 keys generated"
}

# Function to set proper permissions
set_permissions() {
    echo "🔒 Setting proper file permissions..."
    
    # Set restrictive permissions on private keys
    chmod 600 "$TEST_KEYS_DIR"/*private*.pem
    chmod 600 "$TEST_KEYS_DIR"/*.key
    
    # Set readable permissions on public keys and certificates
    chmod 644 "$TEST_KEYS_DIR"/*public*.pem
    chmod 644 "$TEST_KEYS_DIR"/*cert*.pem
    
    echo "✅ File permissions set"
}

# Function to create README
create_readme() {
    echo "📖 Creating test-keys README..."
    
    cat > "$TEST_KEYS_DIR/README.md" << 'EOF'
# Test Keys and Certificates

This directory contains test keys and certificates for ISO 20022 signature strategy testing.

## ⚠️ IMPORTANT SECURITY NOTICE

**These keys are for TESTING PURPOSES ONLY. Never use these keys in production environments.**

## Generated Files

- `rsa_private.pem` - RSA 2048-bit private key (for XMLDSig, JWS, Hybrid strategies)
- `rsa_public.pem` - RSA 2048-bit public key
- `rsa_cert.pem` - Self-signed X.509 certificate (for XMLDSig)
- `ec_private.pem` - EC P-256 private key (for JWS ES256)
- `ec_public.pem` - EC P-256 public key
- `ed25519_private.pem` - Ed25519 private key (for JWS EdDSA)
- `ed25519_public.pem` - Ed25519 public key

## Regenerating Keys

To regenerate all test keys and certificates, run:

```bash
./scripts/generate-test-keys.sh
```

## Usage in Tests

The TestInfrastructure automatically loads these keys when running tests. No manual key management is required.

## Git Ignore

Private keys (*.pem, *.key) are automatically ignored by git to prevent accidental commits.
EOF

    echo "✅ README created"
}

# Main execution
main() {
    check_openssl
    
    echo "🎯 Starting key generation process..."
    
    generate_rsa_keys
    generate_ec_keys
    generate_ed25519_keys
    set_permissions
    create_readme
    
    echo ""
    echo "🎉 All test keys and certificates generated successfully!"
    echo "📁 Keys are stored in: $TEST_KEYS_DIR"
    echo "🔒 Private keys have restricted permissions"
    echo "📖 See $TEST_KEYS_DIR/README.md for usage information"
    echo ""
    echo "⚠️  Remember: These keys are for TESTING ONLY!"
}

# Run main function
main "$@" 