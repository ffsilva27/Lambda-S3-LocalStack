package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Base64
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.PutObjectRequest

class HandlerUpload(
    var s3Client: S3Client
): RequestHandler<Map<String, Any>, ApiGatewayResponse> {

    constructor() : this(S3Client.builder()
        .region(Region.of("us-east-1")) // Região usada pelo LocalStack
        .endpointOverride(URI.create("http://localhost:4566"))
        .credentialsProvider{ AwsBasicCredentials.create("localstack", "localstack") }
        .build()) {
    }

    override fun handleRequest(input: Map<String, Any>, context: Context?): ApiGatewayResponse {
        LOG.info("Iniciando upload do arquivo.")
        return try {
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

            // Nome do bucket e caminho do objeto no S3
            val bucketName = "s3-helloworld"
            val params = input["queryStringParameters"] as Map<*, *>
            val fileName: String = params["fileName"] as String
            val folder: String = params["folder"] as String

            LOG.info("Folder: $folder")
            LOG.info("BucketName: $bucketName")
            LOG.info("FileName: $fileName")

            val file: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(input["file"] as String))
            val key = java.lang.String.format("%s/%s", folder , fileName)

            LOG.info("Key: $key")

            LOG.info("Salvando arquivo temporário...")
            // Salvar o arquivo localmente
            val localFile = Files.createTempFile("temp", fileName)
            Files.copy(file, localFile, StandardCopyOption.REPLACE_EXISTING)

            LOG.info("Criando solicitação de upload...")
            // Criando a solicitação de upload
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            LOG.info("Realizando upload...")
            // Realizando o upload para o S3
            s3Client.putObject(putObjectRequest, localFile)

            LOG.info("Deletando arquivo temporário...")
            // Remover o arquivo temporário local
            Files.delete(localFile)

            LOG.info("Salvo com sucesso.")
            return ApiGatewayResponse.build {
                statusCode = 200
                objectBody = S3UploadFileResponse(
                    "Imagem enviada com sucesso para o S3!",
                    fileName,
                    input
                )
                headers = mapOf("content-type" to "application/json")
            }

        } catch (e: IOException) {
            e.printStackTrace()
            val message = e.message
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
            val message = "Erro ao realizar upload dos arquivos do S3: ${e.message}"
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
        private val LOG = LogManager.getLogger(HandlerUpload::class.java)
    }
}