default: help

help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Available targets:"
	@grep -E '^[a-zA-Z0-9_-]+:' MakeFile | grep -v '^_' | sed 's/:.*//g' | sort | uniq | xargs -n1 echo " -"

argocd_install:
	@helm repo add argo https://argoproj.github.io/argo-helm
	@helm repo update
	@helm install argocd argo/argo-cd --version 7.8.11 --namespace argocd --create-namespace
	@echo "Waiting for ArgoCD to be ready..."
	@kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s
	@echo "ArgoCD is ready!"

argocd_password:
	@echo "ArgoCD password is:"
	@kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath='{.data.password}' | base64 --decode; echo

argocd_start:
	@kubectl port-forward svc/argocd-server -n argocd 8088:443 > /dev/null 2>&1 &
	@echo "ArgoCD is available at http://localhost:8088/"

postgres_deploy:
	@echo "Deploying PostgreSQL database..."
	@kubectl apply -f argocd/postgres.yaml
	@echo "PostgreSQL database deployed!"

openolat_deploy:
	@echo "Deploying ArgoCD application..."
	@kubectl apply -f argocd/openolat.yaml
	@echo "ArgoCD application deployed!"

openolat_shell:
	@POD=$$(kubectl get pod -n openolat -o jsonpath="{.items[0].metadata.name}"); \
	if [ -z "$$POD" ]; then \
		echo "No OpenOLAT pod is available."; \
	else \
		kubectl -n openolat exec -it $$POD -- /bin/bash || kubectl -n openolat exec -it $$POD -- /bin/sh; \
	fi

openolat_start:
	@kubectl port-forward services/openolat-svc -n openolat 8087:8080 > /dev/null 2>&1 &
	@echo "OpenOLAT is available at http://localhost:8087/"


registry_deploy:
	@echo "Deploying Registry ..."
	@kubectl apply -f argocd/registry.yaml
	@echo "Registry deployed!"

registry_open:
	@echo "Opening Registry ..."
	@kubectl port-forward -n zot services/zot-registry 5100:5000 > /dev/null 2>&1 &
	@echo "Registry is available at http://localhost:5100/"

registry_test:
	@echo "Testing Registry ..."
	@kubectl port-forward -n zot services/zot-registry 5100:5000 & \
	sleep 3 && \
	curl -s http://localhost:5100/v2/_catalog || echo "❌ Registry not responding"
	@echo "✅ Registry test complete (check output above)."