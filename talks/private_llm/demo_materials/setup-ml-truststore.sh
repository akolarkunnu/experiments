#!/usr/bin/env bash
# Builds ./cacerts-with-llm for opensearch-ml1 (JDK default truststore + your LLM cert).
#
# Usage:
#   ./setup-ml-truststore.sh /path/to/llm-server.crt
#   ./setup-ml-truststore.sh llm-server.crt llm-server   # optional alias (default: llm-server)
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

CERT_FILE="${1:-}"
ALIAS="${2:-llm-server}"
TRUSTSTORE="./cacerts-with-llm"
IMAGE="opensearchproject/opensearch:3.6.0"
STOREPASS="changeit"

if [[ -z "$CERT_FILE" || ! -f "$CERT_FILE" ]]; then
  echo "Usage: $0 /path/to/llm-server.crt [alias]"
  exit 1
fi

if [[ ! -f "$TRUSTSTORE" ]]; then
  echo "Copying default JDK cacerts from $IMAGE ..."
  podman run --rm "$IMAGE" cat /usr/share/opensearch/jdk/lib/security/cacerts > "$TRUSTSTORE"
fi

if command -v keytool >/dev/null 2>&1; then
  KEYTOOL=(keytool)
else
  echo "keytool not found on host; using keytool from $IMAGE ..."
  KEYTOOL=(docker run --rm -v "$SCRIPT_DIR:/work" -w /work "$IMAGE" /usr/share/opensearch/jdk/bin/keytool)
fi

echo "Importing cert into $TRUSTSTORE (alias: $ALIAS) ..."
"${KEYTOOL[@]}" -importcert -alias "$ALIAS" -file "$CERT_FILE" \
  -keystore "$TRUSTSTORE" -storepass "$STOREPASS" -noprompt

echo "Done. Restart ML node:"
echo "  docker compose restart opensearch-ml1"
