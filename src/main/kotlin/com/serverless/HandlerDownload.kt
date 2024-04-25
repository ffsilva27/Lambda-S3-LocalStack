package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.io.IOException
import java.net.URI
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception

class HandlerDownload(
    var s3Client: S3Client
): RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    constructor() : this(S3Client.builder()
        .region(Region.of("us-east-1")) // Região usada pelo LocalStack
        .endpointOverride(URI.create("http://localhost:4566"))
        .credentialsProvider{ AwsBasicCredentials.create("localstack", "localstack") }
        .build()) {
    }

    override fun handleRequest(input:Map<String, Any>, context: Context):ApiGatewayResponse? {
        LOG.info("Iniciando processo de download de arquivo no S3...")
        try {
            LOG.info("Criando S3 configuration...")
            val s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true) // Necessário para LocalStack
                .build()
            LOG.info("Criando client do S3...")
            s3Client  = S3Client.builder()
                .region(Region.of("us-east-1")) // Região usada pelo LocalStack
                .endpointOverride(URI.create("http://localhost:4566"))
                .serviceConfiguration(s3Configuration)
                .credentialsProvider{ AwsBasicCredentials.create("localstack", "localstack") }
                .build()


            val bucketName = "s3-helloworld"
            val params = input["queryStringParameters"] as Map<*, *>
            val file = params["file"] as String
            val key = String.format("%s/%s", params["folder"], file)
            LOG.info("Key: $key")

            LOG.info("Criando solicitação de download...")
            // Criando a solicitação de download
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            LOG.info("Baixando arquivo da S3...")
            val objectResponse = s3Client.getObject(getObjectRequest)

            LOG.info("Transformando em ByteArray...")
            val content = objectResponse.readAllBytes()

            LOG.info("Retornando arquivo e finalizando o processo...")
            return ApiGatewayResponse.build {
                statusCode = 200
                objectBody = S3DownloadFileResponse(file, input)
                binaryBody = content
                headers = mapOf("content-type" to "application/json")
            }
        } catch (e: S3Exception) {
            e.printStackTrace()
            val message = "Erro ao baixar a imagem do S3: ${e.message}"
            LOG.error(message);
            return ApiGatewayResponse.build {
                statusCode = 500
                rawBody = message
                errorMessage = message
                headers = mapOf("content-type" to "application/json")
                base64Encoded = false
            }
        } catch (e: IOException) {
            e.printStackTrace();
            val message = "Erro ao baixar a imagem do S3: ${e.message}"
            LOG.error(message)
            return ApiGatewayResponse.build {
                statusCode = 500
                rawBody = message
                errorMessage = message
                headers = mapOf("content-type" to "application/json")
                base64Encoded = false
            }
        }catch (e: RuntimeException) {
            e.printStackTrace()
            val message = "Erro ao realizar download dos arquivos do S3: ${e.message}"
            LOG.error(message)
            return ApiGatewayResponse.build {
                statusCode = 500
                rawBody = message
                errorMessage = message
                headers = mapOf("content-type" to "application/json")
                base64Encoded = false
            }
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(HandlerDownload::class.java)
    }
}