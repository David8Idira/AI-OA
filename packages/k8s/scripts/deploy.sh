#!/bin/bash
set -e

NAMESPACE="ai-oa"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "AI-OA Kubernetes Deployment Script"
echo "=========================================="

# Check kubectl
if ! command -v kubectl &> /dev/null; then
    echo "Error: kubectl not found"
    exit 1
fi

# Create namespace
echo "[1/6] Creating namespace..."
kubectl apply -f "$K8S_DIR/base/namespace.yaml"

# Apply base resources
echo "[2/6] Applying base resources (ConfigMap, Secret, MySQL, Redis)..."
kubectl apply -f "$K8S_DIR/base/configmap.yaml"
kubectl apply -f "$K8S_DIR/base/secret.yaml"
kubectl apply -f "$K8S_DIR/base/mysql.yaml"

# Wait for MySQL
echo "[3/6] Waiting for MySQL to be ready..."
kubectl wait --for=condition=available deployment/mysql -n "$NAMESPACE" --timeout=300s

# Apply services
echo "[4/6] Applying backend services..."
for svc in gateway system workflow knowledge ai asset attendance hr license ocr reimburse report im; do
    echo "  - Applying aioa-$svc..."
    kubectl apply -f "$K8S_DIR/services/$svc.yaml"
done

# Apply frontend
echo "[5/6] Applying frontend..."
kubectl apply -f "$K8S_DIR/frontend/deployment.yaml"

# Apply ingress
echo "[6/6] Applying ingress..."
kubectl apply -f "$K8S_DIR/ingress/ingress.yaml"

# Verify deployments
echo ""
echo "=========================================="
echo "Deployment Summary"
echo "=========================================="
echo "Namespace: $NAMESPACE"
echo ""
echo "Pods:"
kubectl get pods -n "$NAMESPACE"
echo ""
echo "Services:"
kubectl get svc -n "$NAMESPACE"
echo ""
echo "Ingress:"
kubectl get ingress -n "$NAMESPACE"
echo ""
echo "HPAs:"
kubectl get hpa -n "$NAMESPACE"

echo ""
echo "=========================================="
echo "Deployment completed!"
echo "=========================================="