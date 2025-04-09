pipeline {
  agent { label 'agentA && docker && helm' }

  environment {
    IMAGE_NAME = "openolat"
    LATEST_TAG = "latest"
    BRANCH_NAME = "${env.BRANCH_NAME ?: 'dev'}"
    ZOT_HOST = "zot"
    ZOT_PORT = "5000"
    ZOT_REGISTRY = "${ZOT_HOST}:${ZOT_PORT}"
    ZOT_OCI_URL = "oci://${ZOT_REGISTRY}/openolat"
    DOCKERFILE_NAME = "Dockerfile.release"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Read Version from olat.properties') {
      steps {
        script {
          def version = sh(script: "grep '^build.version' src/main/resources/serviceconfig/olat.properties | cut -d'=' -f2", returnStdout: true).trim()
          env.VERSION_TAG = version
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "docker build -f ${DOCKERFILE_NAME} -t ${IMAGE_NAME}:${VERSION_TAG} ."
      }
    }

    stage('Push to Local Zot Registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'zot-local', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASS')]) {
          sh """
            echo \$REGISTRY_PASS | docker login ${ZOT_REGISTRY} -u \$REGISTRY_USER --password-stdin
            docker tag ${IMAGE_NAME}:${VERSION_TAG} ${ZOT_REGISTRY}/openolat:${VERSION_TAG}
            docker tag ${IMAGE_NAME}:${VERSION_TAG} ${ZOT_REGISTRY}/openolat:${BRANCH_NAME}-${LATEST_TAG}
            docker push ${ZOT_REGISTRY}/openolat:${VERSION_TAG}
            docker push ${ZOT_REGISTRY}/openolat:${BRANCH_NAME}-${LATEST_TAG}
          """
        }
      }
    }

    stage('Helm Lint & Package') {
      steps {
        sh """
          curl -s https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
          helm lint openolat
          helm package openolat
          mv *.tgz chart.tgz
        """
      }
    }

    stage('Push Helm Chart to Zot (OCI)') {
      steps {
        sh "helm push chart.tgz ${ZOT_OCI_URL}"
      }
    }
  }
}