pipeline {

  environment {
    devRegistry = 'ghcr.io/datakaveri/auditing-server-dev'
    deplRegistry = 'ghcr.io/datakaveri/auditing-server-depl'
    testRegistry = 'ghcr.io/datakaveri/auditing-server-test:latest'
    registryUri = 'https://ghcr.io'
    registryCredential = 'datakaveri-ghcr'
    GIT_HASH = GIT_COMMIT.take(7)
  }

  agent { 
    node {
      label 'slave1' 
    }
  }

  stages {
    
    stage('Build images') {
      steps{
        script {
          devImage = docker.build( devRegistry, "-f ./docker/dev.dockerfile .")
          deplImage = docker.build( deplRegistry, "-f ./docker/depl.dockerfile .")
          testImage = docker.build( testRegistry, "-f ./docker/test.dockerfile .")
        }
      }
    }

    stage('Unit Tests and Code Coverage Test'){
      steps{
        script{
          sh 'docker compose -f docker-compose.test.yml up test'
        }
        xunit (
          thresholds: [ skipped(failureThreshold: '1'), failed(failureThreshold: '0') ],
          tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
        )
        jacoco classPattern: 'target/classes', execPattern: 'target/jacoco.exec', sourcePattern: 'src/main/java', exclusionPattern:'iudx/resource/server/apiserver/ApiServerVerticle.class,**/*VertxEBProxy.class,**/Constants.class,**/*VertxProxyHandler.class,**/*Verticle.class,iudx/resource/server/database/archives/DatabaseService.class,iudx/resource/server/database/async/AsyncService.class,iudx/resource/server/database/latest/LatestDataService.class,iudx/resource/server/deploy/*.class,iudx/resource/server/database/postgres/PostgresService.class,iudx/resource/server/apiserver/ManagementRestApi.class,iudx/resource/server/apiserver/AdminRestApi.class,iudx/resource/server/apiserver/AsyncRestApi.class,iudx/resource/server/callback/CallbackService.class,**/JwtDataConverter.class,**/EncryptionService.class,**/EsResponseFormatter.class,**/AbstractEsSearchResponseFormatter.class'
      }
      post{
      always {
        recordIssues(
          enabledForFailure: true,
          blameDisabled: true,
          forensicsDisabled: true,
          qualityGates: [[threshold:0, type: 'TOTAL', unstable: false]],
          tool: checkStyle(pattern: 'target/checkstyle-result.xml')
        )
        recordIssues(
          enabledForFailure: true,
          blameDisabled: true,
          forensicsDisabled: true,
          qualityGates: [[threshold:5, type: 'TOTAL', unstable: false]],
          tool: pmdParser(pattern: 'target/pmd.xml')
        )
      }
        failure{
          script{
            sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
          }
          error "Test failure. Stopping pipeline execution!"
        }
        cleanup{
          script{
            sh 'sudo rm -rf target/'
          }
        }        
      }
    }

    stage('Continuous Deployment') {
      when {
        allOf {
          anyOf {
            changeset "docker/**"
            changeset "docs/**"
            changeset "pom.xml"
            changeset "src/main/**"
            triggeredBy cause: 'UserIdCause'
          }
          expression {
            return env.GIT_BRANCH == 'origin/master';
          }
        }
      }
      stages {
        stage('Push Images') {
          steps {
            script {
              docker.withRegistry( registryUri, registryCredential ) {
                devImage.push("5.5.0-alpha-${env.GIT_HASH}")
                deplImage.push("5.5.0-alpha-${env.GIT_HASH}")
              }
            }
          }
        }
        stage('Docker Swarm deployment') {
          steps {
            script {
              sh "ssh azureuser@docker-swarm 'docker service update auditing_auditing --image ghcr.io/datakaveri/auditing-server-depl:5.5.0-aplha-${env.GIT_HASH}'"
              sh 'sleep 10'
            }
          }
          post{
            failure{
              error "Failed to deploy image in Docker Swarm"
            }
          }          
        }
      }
    }
  }
  post{
    failure{
      script{
        if (env.GIT_BRANCH == 'origin/master')
        emailext recipientProviders: [buildUser(), developers()], to: '$RS_RECIPIENTS, $DEFAULT_RECIPIENTS', subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!', body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
Check console output at $BUILD_URL to view the results.'''
      }
    }
  }
}
