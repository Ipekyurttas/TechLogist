pipeline {
    agent any

    environment {
        APP_CONTAINER = "techlogist_app"
        DB_CONTAINER  = "techlogist_db"
    }

    stages {
        stage('Sistemi Hazırla (Docker Build)') {
            steps {
                echo 'Docker imajları oluşturuluyor ve sistem ayağa kaldırılıyor...'
                sh 'docker-compose down'
                sh 'docker-compose up -d --build'

                echo 'Uygulamanın hazır olması bekleniyor...'
                sleep 15
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Birim testleri (Unit Tests) çalıştırılıyor...'
                sh 'mvn test -Dtest=org.tech.techlogist.unit.**'
            }
            post {
                always {
                    junit 'target/surefire-reports/TEST-org.tech.techlogist.unit.*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo 'Entegrasyon testleri (Integration Tests) çalıştırılıyor...'
                sh 'mvn test -Dtest=org.tech.techlogist.integration.**'
            }
            post {
                always {
                    junit 'target/surefire-reports/TEST-org.tech.techlogist.integration.*.xml'
                }
            }
        }

        stage('Selenium UI Tests') {
            steps {
                echo 'Selenium arayüz testleri çalıştırılıyor...'
                sh 'mvn test -Dtest=org.tech.techlogist.selenium.TechLogistUITest'
            }
            post {
                always {
                    junit 'target/surefire-reports/TEST-org.tech.techlogist.selenium.TechLogistUITest.xml'
                }
            }
        }
    }

    post {
        always {
            echo 'Temizlik yapılıyor...'
            sh 'docker-compose down'
        }
        success {
            echo 'Tüm testler başarıyla tamamlandı!'
        }
        failure {
            echo 'Pipeline başarısız oldu. Lütfen test raporlarını ve logları kontrol edin.'
        }
    }
}