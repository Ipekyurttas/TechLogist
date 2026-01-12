pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
    }

    environment {
        PATH = "/usr/local/bin:${env.PATH}"
    }

    tools {
        maven 'maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Ipekyurttas/TechLogist.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test -Dtest=org.tech.techlogist.unit.**.*Test'
            }
        }

        stage('Integration Tests') {
            steps {
                sh 'mvn test -Dtest=org.tech.techlogist.integration.**.*IT'
            }
        }

        stage('Start App with Docker') {
            steps {
                sh '''
                docker-compose down || true
                docker-compose up -d --build app
                echo "App container başlatıldı."
                '''
            }
        }

        stage('Selenium UI Tests') {
            steps {
                sh "mvn test -Dtest=org.tech.techlogist.selenium.**.*Test"
            }
        }
    }

    post {
        always {
            sh "docker-compose down || true"
        }
        success {
            echo "Tüm testler başarıyla tamamlandı!"
        }
        failure {
            echo "Testlerde hata oluştu!"
        }
    }
}
