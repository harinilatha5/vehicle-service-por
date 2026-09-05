pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')   // Jenkins credential ID (username+password)
        SSH_CREDENTIALS       = 'server-ssh-key'                 // Jenkins SSH credential ID
        SERVER_USER           = 'ubuntu'
        SERVER_HOST           = '13.201.167.97'                  // your new EC2 public IP, no trailing spaces
        DB_NAME               = 'vehicleportal'
        DB_USERNAME           = 'root'
        DB_CREDENTIALS        = credentials('db-password')       // Jenkins secret text credential ID
        IMAGE_NAME            = 'vehicle-service-portal'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    env.FULL_IMAGE = "${DOCKERHUB_CREDENTIALS_USR}/${IMAGE_NAME}"
                }
                sh "docker build -t ${FULL_IMAGE}:latest -t ${FULL_IMAGE}:${BUILD_NUMBER} ."
            }
        }

        stage('Docker Push') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    echo "$DOCKERHUB_CREDENTIALS_PSW" | docker login -u "$DOCKERHUB_CREDENTIALS_USR" --password-stdin
                    docker push "$FULL_IMAGE:latest"
                    docker push "$FULL_IMAGE:${BUILD_NUMBER}"
                '''
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sshagent(credentials: [SSH_CREDENTIALS]) {
                    withCredentials([string(credentialsId: 'db-password', variable: 'DB_PASSWORD_SECRET')]) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no "$SERVER_USER@$SERVER_HOST" bash -s -- \
                                "$FULL_IMAGE" "$DB_NAME" "$DB_USERNAME" "$DB_PASSWORD_SECRET" <<'ENDSSH'
                                set -e
                                IMAGE="$1"
                                DBNAME="$2"
                                DBUSER="$3"
                                DBPASS="$4"

                                docker network inspect vehicleportal-net >/dev/null 2>&1 || \
                                    docker network create vehicleportal-net

                                if [ ! "$(docker ps -aq -f name=^vehicleportal-mysql$)" ]; then
                                    docker run -d --name vehicleportal-mysql \
                                        --network vehicleportal-net \
                                        --restart unless-stopped \
                                        -e MYSQL_ROOT_PASSWORD="$DBPASS" \
                                        -e MYSQL_DATABASE="$DBNAME" \
                                        mysql:8.0
                                fi

                                docker pull "$IMAGE:latest"
                                docker stop vehicleportal-app || true
                                docker rm vehicleportal-app || true

                                docker run -d --name vehicleportal-app \
                                    --network vehicleportal-net \
                                    --restart unless-stopped \
                                    -p 9090:8080 \
                                    -e SPRING_PROFILES_ACTIVE=mysql \
                                    -e DB_HOST=vehicleportal-mysql \
                                    -e DB_PORT=3306 \
                                    -e DB_NAME="$DBNAME" \
                                    -e DB_USERNAME="$DBUSER" \
                                    -e DB_PASSWORD="$DBPASS" \
                                    "$IMAGE:latest"
ENDSSH
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Build #${BUILD_NUMBER} deployed successfully."
        }
        failure {
            echo "Build #${BUILD_NUMBER} failed. Check the console log."
        }
        always {
            sh 'docker logout || true'
        }
    }
}
