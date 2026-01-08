pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    stages {
        stage('Sistemi Hazırla') {
            steps {
                echo 'Docker ortamı hazırlanıyor'
                sh '''
                    set -x
                    docker version
                    docker compose down || true
                    docker compose up -d --build
                    docker compose ps
                    sleep 20
                '''
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Unit testler çalıştırılıyor'
                sh '''
                    set -x
                    mvn -e test -Dtest=org.tech.techlogist.unit.*
                '''
            }
        }

        stage('Integration Tests') {
            steps {
                echo 'Integration testler çalıştırılıyor'
                sh '''
                    set -x
                    mvn -e test -Dtest=org.tech.techlogist.integration.*
                '''
            }
        }

        stage('Selenium UI Tests') {
            steps {
                echo 'Selenium testleri çalıştırılıyor'
                sh '''
                    set -x
                    mvn -e test -Dtest=org.tech.techlogist.selenium.*
                '''
            }
        }
    }

    post {
        always {
            echo 'Containerlar kapatılıyor'
            sh 'docker compose down'
        }
        failure {
            echo 'Hata sonrası container logları'
            sh 'docker compose logs --tail=100'
        }
    }
}
