package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.net.URI
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.S3Exception

class HandlerList(
    private var s3Client: S3Client
): RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    constructor() : this(S3Client.builder()
        .region(Region.of("us-east-1")) // Região usada pelo LocalStack
        .endpointOverride(URI.create("http://localhost:4566"))
        .credentialsProvider{ AwsBasicCredentials.create("localstack", "localstack") }
        .build()) {
    }

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        LOG.info("Iniciando a busca por uma lista de arquivos no bucket da S3.")
        try {
            LOG.info("Criando S3 configuration...")
            val s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true) // Necessário para LocalStack
                .build()
            LOG.info("Criando o client do S3")
            s3Client  = S3Client.builder()
                .region(Region.of("us-east-1")) // Região usada pelo LocalStack
                .endpointOverride(URI.create("http://localhost:4566"))
                .serviceConfiguration(s3Configuration)
                .credentialsProvider{ AwsBasicCredentials.create("localstack", "localstack") }
                .build()

            val params = input["queryStringParameters"] as Map<*, *>
            val bucketName = "s3-helloworld"
            val folder = params["folder"] as? String ?: throw IllegalArgumentException("O atributo folder é de preenchimento obrigatório.")
            LOG.info("Folder: $folder")
            LOG.info("BucketName: $bucketName")

            LOG.info("Criando solicitação de listagem...")
            val getObjectRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(String.format("%s/", folder))
                .build()
            LOG.info(getObjectRequest.toString())
            LOG.info("Obtendo lista de arquivos da S3...")
            val objectResponse = s3Client.listObjects(getObjectRequest)

            LOG.info("Listando arquivos da S3..")
            val content = objectResponse.contents().map { it.key() }.toList()
            LOG.info("Arquivos retornados na busca:")
            content.forEach {
                LOG.info(it)
            }
            val responseBody = S3ListFilesResponse(folder, content, input)
            return ApiGatewayResponse.build {
                statusCode = 200
                base64Encoded = false
                objectBody = responseBody
                headers = mapOf("content-type" to "application/json")
            }
        } catch (e: S3Exception) {
            e.printStackTrace()
            val message = "Erro ao listar os arquivos do S3 - S3Exception: ${e.message}"
            LOG.error(message)
            return ApiGatewayResponse.build {
                statusCode = 500
                rawBody = message
                errorMessage = message
                headers = mapOf("content-type" to "application/json")
                base64Encoded = false
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
            val message = "Erro ao listar os arquivos do S3 - RuntimeException: ${e.message}"
            LOG.error(message)
            return ApiGatewayResponse.build {
                statusCode = 500
                rawBody = message
                errorMessage = message
                headers = mapOf("content-type" to "application/json")
                base64Encoded = false
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            val message = "Erro ao listar os arquivos do S3 - IllegalArgumentException: ${e.message}"
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
        private val LOG = LogManager.getLogger(HandlerList::class.java)
    }
}